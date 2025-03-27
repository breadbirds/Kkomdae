package com.example.autocamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.graphics.Rect
import java.io.ByteArrayOutputStream
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.media.MediaScannerConnection
import android.os.Handler
import android.util.Size
import androidx.camera.core.AspectRatio
import com.example.autocamera.BBox
import com.example.autocamera.databinding.ActivityMainBinding
import java.io.FileOutputStream
import kotlin.math.abs

data class PreprocessedResult(
    val input: Array<Array<Array<FloatArray>>>,
    val scale: Float,
    val dx: Float,
    val dy: Float
)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tflite: Interpreter
    private lateinit var imageCapture: ImageCapture
    private var count = 0

    // 이전 프레임과 비교를 위한 상태
    private var lastBox: BBox? = null
    private var stableFrameCount = 0
    private val requiredStableFrames = 10
    private val candidateBitmaps = mutableListOf<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 사용
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tflite = Interpreter(loadModelFile())

        // 카메라 권한 체크
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    // 카메라 권한 확인
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 필요한 권한이 모두 허용됐는지 확인
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // 카메라 실행
    private fun startCamera() {
        // CameraX를 사용하기 위해 카메라 제공자(CameraProvider)를 비동기로 가져오는 중.
        // getInstance()는 앱의 생명주기에 맞춰 카메라를 관리해주는 객체를 반환
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // 카메라 준비가 끝났을 때 실행
        cameraProviderFuture.addListener({
            // 카메라 관리 객체
            val cameraProvider = cameraProviderFuture.get()

            // ✅ 1. Preview <- 미리 보기 구성. (Preview 화면 연결하여 미리보기 영상 출력)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val resolution = Size(3840, 2160)

            // ✅ 2. ImageCapture (📸 이 줄이 바로 여기!)
            // 사진을 캡처(저장)할 수 있도록 ImageCapture 객체 생성
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_DEFAULT) // 📌 비율 설정
                // .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) // 빠른 캡처 모드
                //.setTargetResolution(resolution)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // 고화질 우선
                .build()

            // ✅ 3. ImageAnalysis
            // 카메라에서 들어오는 실시간 프레임(영상)을 분석
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // 📌 비율 설정
                // .setTargetResolution(resolution)
                 .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 분석이 끝날 때까지 기다리지 말고, 가장 최근 프레임만 분석
                .build()
                .also {
                    // 프레임이 들어올 때마다 이미지 분석 함수 실행
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), { imageProxy ->
                        // Log.d("startCamera", "imageProxy 해상도: ${imageProxy.width}X${imageProxy.height}")
                        // YOLO 모델로 감지하고 bbox도 그려주는 기능이 작동
                        analyzeImage(imageProxy)
                    })
                }

            // 후면 카메라 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 혹시 이전에 카메라에 묶인 기능들이 있으면 전부 해제
                cameraProvider.unbindAll()
                // 위에서 만든 preview, imageCapture, imageAnalyzer를
                // 생명주기(Lifecycle)에 맞게 카메라에 연결(bind)
                // 여기서 실제 카메라 실행!
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("CameraX", "카메라 실행 오류", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("yolo_laptop.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    private fun analyzeImage(imageProxy: ImageProxy) {
        // ImageProxy에서 가져온 카메라 프레임을 Bitmap으로 변환 (YOLO 입력용)
        val bitmap = imageProxyToBitmap(imageProxy)
        val width = bitmap.width
        val height = bitmap.height
        // YOLOv8 TFLite 모델에 넣기 위한 전처리 작업 (640x640 크기, float 정규화 등)
        val preprocessed = preprocessBitmap(bitmap)
        val input = preprocessed.input
        val scale = preprocessed.scale
        val dx = preprocessed.dx
        val dy = preprocessed.dy

        // YOLO 모델 실행
        // 입력: input
        // 출력: [1, 5, 8400] 형식 (YOLOv8의 head 출력 → center x/y, width/height, confidence)
        val output = Array(1) { Array(5) { FloatArray(8400) } }
        tflite.run(input, output)

        // 추론 결과
        val predictions = output[0]
        // confidence 90%이상만 검출
        val threshold = 0.9f
        // YOLO 입력은 항상 640x640
        val modelInputSize = 640f

        // 감지된 박스를 저장할 리스트
        val detectedBoxes = mutableListOf<BBox>()

        // YOLOv8의 전체 anchor/grid 수 (8400개)
        for (i in 0 until 8400) {
            val score = predictions[4][i]
            // confidence score가 기준을 넘으면 유효한 객체로 간주
            if (score > threshold) {

                // YOLO 640 기준 bbox
                val cx = predictions[0][i] * modelInputSize
                val cy = predictions[1][i] * modelInputSize
                val w = predictions[2][i] * modelInputSize
                val h = predictions[3][i] * modelInputSize

                // padding 고려해서 원본 비트맵 좌표로 역변환
                val x = (cx - dx) / scale
                val y = (cy - dy) / scale
                val width = w / scale
                val height = h / scale

                val left = x - width / 2
                val top = y - height / 2
                val right = x + width / 2
                val bottom = y + height / 2

                // 최종적으로 계산한 박스를 BBox 객체로 만들어 리스트에 추가
                // 나중에 오버레이에 그리거나 베스트 박스를 고르기 위해 사용
                detectedBoxes.add(BBox(RectF(left, top, right, bottom), "laptop", score))
                Log.d("BBoxFinal", "BBox: RectF($left, $top, $right, $bottom)")

                if (detectedBoxes.isNotEmpty()) {
                    saveDebugBitmapWithBoxes(bitmap, detectedBoxes)
                }
            }
        }

        // 카메라 분석은 백그라운드 쓰레드에서 실행되기 때문에
        // UI 요소(Toast, TextView 등)를 업데이트하려면 메인(UI) 쓰레드로 이동
        runOnUiThread {
            // 감지된 박스들 중에서 가장 넓은 박스 하나만 선택
            val bestBox = detectedBoxes.maxByOrNull { it.rect.width() * it.rect.height() }

            // 탐지된 객체가 있을 때만 아래 로직 실행
            if (bestBox != null) {
                // bestBox가 화면 전체에서 차지하는 비율(면적 비율) 계산
                //  → 나중에 노트북이 충분히 클 때만 촬영하기 위해
                val viewWidth = binding.previewView.width.toFloat()
                val viewHeight = binding.previewView.height.toFloat()
                val bitmapWidth = bitmap.width.toFloat()
                val bitmapHeight = bitmap.height.toFloat()
                Log.d("화면", "bestBox.rect.width(): ${bestBox.rect.width()} bestBox.rect.height():${bestBox.rect.height()}")
                val areaRatio = (bestBox.rect.width() * bestBox.rect.height()) / (bitmapWidth * bitmapHeight)

                // 노트북이 화면의 20% 이상 차지할 때만 유효하다고 간주
                //→ 너무 멀리 있거나 작게 보이는 건 촬영하지 않음
                val minAreaRatio = 0.5f  // 화면의 20% 이상일 때만 인정
                val isBigEnough = areaRatio > minAreaRatio

                // 이전 프레임의 bestBox와 비교해서
                // 중심좌표의 이동이 100px 이하이면 → "카메라 흔들림 없음"으로 간주
                // 이전 박스가 없으면 → 그냥 true (처음 프레임)
                val isPositionStable = lastBox?.let {
                    val dx = Math.abs(it.rect.centerX() - bestBox.rect.centerX())
                    val dy = Math.abs(it.rect.centerY() - bestBox.rect.centerY())
                    dx < 10 && dy < 10  // 100px 이내 움직임이면 "안정"
                } ?: true
                Log.d("StableFrames", "count = $stableFrameCount, 크기 통과: $isBigEnough, 위치 통과: $isPositionStable")


                // 충분히 크고, 안정적인 위치에 있을 경우만
                if (isBigEnough && isPositionStable) {
                    // 안정된 프레임으로 인정되면 카운트 증가 (stableFrameCount)
                    stableFrameCount++
                    // 현재 비트맵을 복사해서 candidateBitmaps에 저장
                    // → 나중에 "베스트 컷" 고르기 위해
                    bitmap.config?.let { bitmap.copy(it, false) }?.let { candidateBitmaps.add(it) }
                } else {
                    // 조건 불만족이면:
                    // 안정 프레임 수 초기화
                    // 후보 비트맵들 전부 삭제 (다시 모아야 함)
                    stableFrameCount = 0
                    candidateBitmaps.clear()
                }

                // 다음 프레임 비교를 위해 현재 박스를 저장
                lastBox = bestBox

                // ✅ 10프레임 연속 안정된 상태
                if (stableFrameCount >= requiredStableFrames) {
                    // 지금까지 모은 비트맵 중에서 가장 좋은 걸 선택해서 저장!
                    // 저장 후 초기화
                    captureHighResImage()
                    stableFrameCount = 0
                    candidateBitmaps.clear()
                }

                // 오버레이에 감지된 박스 하나만 그리기
                // → 화면에 사각형이 표시됨
                binding.overlay.setBoxes(listOf(bestBox), bitmap.width, bitmap.height)
            }
        }

        // 현재 분석 중인 프레임 리소스 정리
        imageProxy.close()
    }

    //🔸 CameraX의 ImageAnalysis.Analyzer가 주는 ImageProxy를
    //🔸 Android에서 다룰 수 있는 Bitmap 형식으로 바꾸는 함수
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        // ImageProxy는 YUV_420_888 형식으로 되어 있어서
        // planes[0]: Y (밝기 정보)
        // planes[1]: U (색상 - 파란톤)
        // planes[2]: V (색상 - 빨간톤)
        // → 이 3개의 buffer를 가져와.
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        // 각 채널의 남은 바이트 수를 계산
        // → 이걸로 전체 크기를 정함
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // Android에서 JPEG 변환할 수 있는 NV21 포맷 배열 생성
        // → NV21은 YUV 데이터를 JPEG로 바꾸기 쉬운 포맷이야
        val nv21 = ByteArray(ySize + uSize + vSize)

        // 순서대로 nv21 배열에 데이터를 채워 넣음
        //📌 순서 주의: Y → V → U
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        // YUV 데이터를 JPEG로 압축 → Bitmap 만들기
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        // YUV 데이터를 JPEG 포맷으로 압축해서 **메모리 버퍼(out)**에 저장
        // 100: 최고 품질
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        // 메모리 스트림을 실제 바이트 배열로 변환
        val jpegBytes = out.toByteArray()

        // JPEG 바이트를 Bitmap 객체로 디코딩해서 반환
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }

    // bitmap을 yolo에 input 사이즈에 맞추어 변환
    private fun preprocessBitmap(bitmap: Bitmap): PreprocessedResult {
        // YOLOv8의 입력 크기 (항상 640x640으로 고정)
        val modelSize = 640
        //모델에 넣을 입력 배열 생성
        //[1, 640, 640, 3]
        //마지막 3은 RGB 채널 (0~1 사이 정규화된 값)
        val input = Array(1) { Array(modelSize) { Array(modelSize) { FloatArray(3) } } }

        //원본 Bitmap의 가로 세로 크기 저장
        //→ 나중에 비율 계산에 사용
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // 원본 이미지를 비율을 유지하면서 축소하기 위한 비율 계산
        // → 가로/세로 중 더 많이 축소되는 쪽에 맞춤
        val scale = minOf(modelSize / originalWidth.toFloat(), modelSize / originalHeight.toFloat())
        // 계산된 스케일을 바탕으로 축소된 이미지의 새 크기 계산
        val resizedWidth = (originalWidth * scale).toInt()
        val resizedHeight = (originalHeight * scale).toInt()

        val dx = (modelSize - resizedWidth) / 2f
        val dy = (modelSize - resizedHeight) / 2f

        // 비율 유지한 채 축소된 이미지 만들기
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true)

        // 640x640 검은 배경 만들기
        //  비율 유지로 인해 남는 공간을 검은색으로 채울 예정
        val paddedBitmap = Bitmap.createBitmap(modelSize, modelSize, Bitmap.Config.ARGB_8888)
        // Canvas를 이용해 배경을 검은색으로 칠함 (letterbox)
        val canvas = Canvas(paddedBitmap)
        canvas.drawColor(Color.BLACK)

        // (0, 0)에 축소된 이미지 그리기
        // → YOLO가 선호하는 letterbox 스타일
        canvas.drawBitmap(resizedBitmap, dx, dy, null)

        // paddedBitmap에서 픽셀 값을 하나씩 읽어서
        // RGB 각각을 0~1로 정규화해서 input 배열에 저장
        //shr은 비트를 밀어 색을 추출하는 방식
        for (y in 0 until modelSize) {
            for (x in 0 until modelSize) {
                val pixel = paddedBitmap.getPixel(x, y)
                input[0][y][x][0] = ((pixel shr 16 and 0xFF) / 255.0f)  // R
                input[0][y][x][1] = ((pixel shr 8 and 0xFF) / 255.0f)   // G
                input[0][y][x][2] = ((pixel and 0xFF) / 255.0f)         // B
            }
        }

        // 최종적으로 YOLO 모델에 넣을 [1, 640, 640, 3] 형식의 float 배열 반환
        return PreprocessedResult(input, scale, dx, dy)
    }

    // 가장 좋은 한 장을 골라 파일로 저장하는 함수
    private fun selectBestAndSave(bitmaps: List<Bitmap>) {
        // 후보 이미지가 하나도 없으면?
        // → 그냥 아무 것도 하지 않고 함수 종료
        if (bitmaps.isEmpty()) return

        //  밝기 점수 (Luma)와 선명도 점수 (Sharpness) 기준으로 베스트 선택 (여기선 단순히 가장 밝은 이미지로)
        // (이미 analyzeImage의 minAreaRatio로 노트북 면적이 50%이상일 때만 넘어옴)
        //  각 비트맵을 평가해서
        //  → 가장 점수가 높은 하나(maxByOrNull)를 best로 선택
        val best = bitmaps.maxByOrNull { bitmap ->
            val lumaScore = calculateLuma(bitmap)
            val sharpnessScore = calculateSharpness(bitmap)
            lumaScore * 1.0 + sharpnessScore * 1000  // 🔥 가중치 튜닝 가능
            // 만약 best가 null이라면 (bitmaps가 empty일 때)
            // → 저장 없이 종료
        } ?: return

        // 앱의 외부저장소 폴더에 "best_laptop_현재시간.jpg" 이름으로 파일 생성
        val photoFile = File(
            externalMediaDirs.first(),
            "best_laptop_${System.currentTimeMillis()}.jpg"
        )

        // 선택된 best 이미지를 JPEG 포맷으로 압축해서 파일로 저장
        // 100은 압축률 100% (최고 품질)
        FileOutputStream(photoFile).use { out ->
            best.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        // 안드로이드 갤러리에서 바로 인식되도록 미디어 스캔 요청
        //→ 저장한 사진이 갤러리 앱에 바로 표시됨! 🎉
        MediaScannerConnection.scanFile(
            applicationContext,
            arrayOf(photoFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )
    }

    // 실제 사람의 시각에 더 가까운 가중 평균 밝기 계산 (Luma 방식 (가중 평균))
    // Luma 방식은 디지털 이미지나 영상에서 **"사람이 느끼는 밝기(luminance)"**를 계산하기 위한 대표적인 방식
    // 사람의 눈은 색마다 밝기를 느끼는 민감도가 다르기 때문에, 각 색(R, G, B)에 가중치를 다르게
    fun calculateLuma(bitmap: Bitmap): Long {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return pixels.sumOf {
            val r = (it shr 16) and 0xFF
            val g = (it shr 8) and 0xFF
            val b = it and 0xFF
            (0.299 * r + 0.587 * g + 0.114 * b).toLong()
        }
    }

    // 명도는 보통 이미지의 경계(Edge)가 얼마나 뚜렷한지로 판단해.
    // ➡️ 가장 쉬운 방법은 Sobel 필터나 Laplacian(라플라시안) 연산을 사용
    fun calculateSharpness(bitmap: Bitmap): Double {
        val gray = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
        val pixels = IntArray(gray.width * gray.height)
        gray.getPixels(pixels, 0, gray.width, 0, 0, gray.width, gray.height)

        var sumDiff = 0L
        for (y in 1 until gray.height) {
            for (x in 1 until gray.width) {
                val current = pixels[y * gray.width + x] and 0xFF
                val left = pixels[y * gray.width + x - 1] and 0xFF
                val top = pixels[(y - 1) * gray.width + x] and 0xFF
                val diff = abs(current - left) + abs(current - top)
                sumDiff += diff
            }
        }

        return sumDiff.toDouble() / (gray.width * gray.height)
    }

    // yolo가 진짜로 잘 탐지했는지를 보기위해 laptop을 탐지하면 bbox를 그리고 갤러리에 저장.
    private fun saveDebugBitmapWithBoxes(bitmap: Bitmap, boxes: List<BBox>) {
        val debugBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(debugBitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        // 감지된 박스들을 이미지 위에 그림
        for (box in boxes) {
            Log.d("BBoxDebug", "📦 rect = ${box.rect}")
            canvas.drawRect(box.rect, paint)
        }

        // 저장
        val debugFile = File(
            externalMediaDirs.first(),
            "yolo_debug_${System.currentTimeMillis()}.jpg"
        )
        FileOutputStream(debugFile).use { out ->
            debugBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        MediaScannerConnection.scanFile(
            applicationContext,
            arrayOf(debugFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )

        Log.d("MainActivity", "✅ YOLO 디버그 이미지 저장됨: ${debugFile.absolutePath}")
    }

    private fun captureHighResImage() {
        val photoFile = File(
            externalMediaDirs.first(),
            "best_laptop_capture_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    MediaScannerConnection.scanFile(
                        applicationContext,
                        arrayOf(photoFile.absolutePath),
                        arrayOf("image/jpeg"),
                        null
                    )
                    Log.d("ImageCapture", "📸 고해상도 이미지 저장됨: ${photoFile.absolutePath}")
                    runOnUiThread {
                        binding.text1.text = "📸 고해상도 촬영 완료! ${++count}"
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("ImageCapture", "촬영 실패", exception)
                }
            }
        )
    }


    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}