package com.pizza.kkomdae.ui.guide

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.graphics.YuvImage
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.pizza.kkomdae.AppData
import com.pizza.kkomdae.CameraActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentFontShotGuideBinding
import com.pizza.kkomdae.presenter.viewmodel.CameraViewModel
import com.pizza.kkomdae.util.BBox
import com.pizza.kkomdae.util.PreprocessedResult
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.collections.ArrayDeque


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var imageCapture: ImageCapture? = null
private var cameraProvider: ProcessCameraProvider? = null
private var camera: Camera? = null
private var cameraExecutor: ExecutorService? = null
private lateinit var cameraActivity: CameraActivity

/**
 * A simple [Fragment] subclass.
 * Use the [FrontShotGuideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FrontShotGuideFragment : BaseFragment<FragmentFontShotGuideBinding>(
    FragmentFontShotGuideBinding::bind,
    R.layout.fragment_font_shot_guide
) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: CameraViewModel by activityViewModels()

    // AutoCapture 변수
    private lateinit var tflite: Interpreter // tflite 모델을 불러오기 위한 변수
    private var lastCapturedBox: BBox? = null // crop을 위해 best box를 저장하는 변수
    private var isCapturing = false // 중복 캡처 방지
    // 이전 프레임과 비교를 위한 상태
    private var lastBox: BBox? = null
    private var stableFrameCount = 0
    private val requiredStableFrames = 3
    // Bitmap Pool (비트맵 재사용) 기법
    private val MAX_CANDIDATE_FRAMES = 3   // 후보 프레임 최대 개수 제한
    private val candidateBitmaps = ArrayDeque<Bitmap>() // → 큐 자료구조 사용
    private var preview: Preview? = null

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FontShotGuideFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FrontShotGuideFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        cameraActivity = context as CameraActivity
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 모델 불러오기
        tflite = Interpreter(loadModelFile())
        
        // 카메라 초기화
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        // 가이드 닫기 버튼 눌렀을 때
        binding.btnCancel?.setOnClickListener {
            binding.clGuide?.isVisible = false
            binding.overlayView?.isVisible=true
            binding.btnBack?.isVisible = true
            binding.btnShot?.isVisible = true
            binding?.btnGuide?.isVisible = true
        }

        // 가이드 보기 버튼 눌렀을 떄
        binding.btnGuide?.setOnClickListener {
            binding.clGuide?.isVisible = true
            binding.overlayView?.isVisible=false
            binding.btnBack?.isVisible = false
            binding.btnShot?.isVisible = false
            binding?.btnGuide?.isVisible = false
        }

        // X 버튼 눌렀을 때
        binding.btnBack?.setOnClickListener {
            showStopCameraDialog()
        }

        binding.btnShot?.setOnClickListener {
            takePhoto()
        }
    }

    private fun showStopCameraDialog() {
        // 다이얼로그 생성
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_stop_camera_dialog)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.5).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 취소 버튼
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 그만하기 버튼
        val btnConfirm = dialog.findViewById<TextView>(R.id.btn_confirm)
        btnConfirm.setOnClickListener {
            // 다이얼로그 닫기
            dialog.dismiss()
            cameraActivity.moveToBack()
        }

        dialog.show()
    }

    private fun startCamera() {
        // CameraX를 사용하기 위해 카메라 제공자(CameraProvider)를 비동기로 가져오는 중.
        // getInstance()는 앱의 생명주기에 맞춰 카메라를 관리해주는 객체를 반환
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // 카메라 준비가 끝났을 때 실행
        cameraProviderFuture.addListener({
            // 카메라 관리 객체
             cameraProvider = cameraProviderFuture.get()
            val my_preview_resolution = Size(3840, 2160) // 원하는 해상도

            // 후면으로 촬영
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // ✅ 1. Preview <- 미리 보기 구성. (Preview 화면 연결하여 미리보기 영상 출력)
            preview = Preview.Builder()
                .setTargetResolution(my_preview_resolution) // 원하는 해상도 요청 <- 최대한 높은 걸로 달라고 요청
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3) // 📌 비율 설정
                .build().also {
                    it.setSurfaceProvider(binding.previewView?.surfaceProvider) // preview와 연결
                }

            // ✅ 2. ImageCapture
            // 사진을 캡처(저장)할 수 있도록 ImageCapture 객체 생성
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3) // 📌 비율 설정
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // 고화질 우선
//                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) // 빠른 캡처 모드
                .build()

            // ✅ 3. ImageAnalysis
            // 카메라에서 들어오는 실시간 프레임(영상)을 분석
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // 📌 비율 설정
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 분석이 끝날 때까지 기다리지 말고, 가장 최근 프레임만 분석
                .build().also {
                    // 프레임이 들어올 때마다 이미지 분석 함수 실행
                    it.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), { imageProxy ->
                        // YOLO 모델로 감지
                        analyzeImage(imageProxy)
                    })
                }

            try {
                // 혹시 이전에 카메라에 묶인 기능들이 있으면 전부 해제
                cameraProvider?.unbindAll()
                // 위에서 만든 preview, imageCapture, imageAnalyzer를
                // 생명주기(Lifecycle)에 맞게 카메라에 연결(bind)
                // 여기서 실제 카메라 실행!
                camera = cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("CameraFragment", "카메라 실행 오류: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // 촬영 버튼 눌러서 촬영 함수
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(requireContext().externalMediaDirs.firstOrNull(),
            "IMG_${System.currentTimeMillis()}.png")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    shutdownCamera()

                    // ✅ 2️⃣ 로티 띄우기
                    binding.loadingLottie?.visibility = View.VISIBLE
                    binding.loadingLottie?.playAnimation()

                    Thread {
                        val savedUri = Uri.fromFile(photoFile)
                        Log.d("CameraFragment", "사진 저장됨: $savedUri")

                        // ✅ 4️⃣ UI Thread 복귀
                        Handler(Looper.getMainLooper()).post {
                            Log.d("CameraFragment", "사진 저장됨: $savedUri")
                            viewModel.setFront(savedUri)
                            viewModel.setStep(1)

                            binding.loadingLottie?.cancelAnimation()
                            binding.loadingLottie?.visibility = View.GONE

                            cameraActivity.changeFragment(0)
                        }

                    }.start()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraFragment", "사진 촬영 실패: ${exception.message}")
                }
            })
    }

    // 자동 촬영 함수
    private fun autoTakePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(requireContext().externalMediaDirs.firstOrNull(),
            "IMG_${System.currentTimeMillis()}.png")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("autoTakePhoto", "👍사진 저장하는 곳에 들어왔어!")
                    val box = lastCapturedBox ?: return

                    shutdownCamera()

                    // ✅ 2️⃣ 로티 띄우기
                    binding.loadingLottie?.visibility = View.VISIBLE
                    binding.loadingLottie?.playAnimation()

                    // ✅ 3️⃣ Heavy 작업 백그라운드 처리
                    Thread {
                        val highResBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                        val analyzedWidth = 1280f
                        val analyzedHeight = 720f

                        val scaleX = highResBitmap.width / analyzedWidth
                        val scaleY = highResBitmap.height / analyzedHeight

                        val rectF = RectF(
                            box.rect.left * scaleX,
                            box.rect.top * scaleY,
                            box.rect.right * scaleX,
                            box.rect.bottom * scaleY
                        )

                        val paddingScale = 1.1f
                        val centerX = rectF.centerX()
                        val centerY = rectF.centerY()
                        val halfWidth = rectF.width() / 2 * paddingScale
                        val halfHeight = rectF.height() / 2 * paddingScale

                        val expandedRect = RectF(
                            centerX - halfWidth,
                            centerY - halfHeight,
                            centerX + halfWidth,
                            centerY + halfHeight
                        )

                        val cropRect = Rect(
                            expandedRect.left.toInt().coerceAtLeast(0),
                            expandedRect.top.toInt().coerceAtLeast(0),
                            expandedRect.right.toInt().coerceAtMost(highResBitmap.width),
                            expandedRect.bottom.toInt().coerceAtMost(highResBitmap.height)
                        )

                        val croppedBitmap = Bitmap.createBitmap(
                            highResBitmap,
                            cropRect.left,
                            cropRect.top,
                            cropRect.width(),
                            cropRect.height()
                        )

                        FileOutputStream(photoFile).use { out ->
                            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }

                        val savedUri = Uri.fromFile(photoFile)

                        // ✅ 4️⃣ UI Thread 복귀
                        Handler(Looper.getMainLooper()).post {
                            Log.d("CameraFragment", "사진 저장됨: $savedUri")
                            viewModel.setFront(savedUri)
                            viewModel.setStep(1)

                            binding.loadingLottie?.cancelAnimation()
                            binding.loadingLottie?.visibility = View.GONE

                            cameraActivity.changeFragment(0)
                        }

                    }.start()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraFragment", "사진 촬영 실패: ${exception.message}")
                }
            })
    }

    // 모델을 불러오는 함수
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = requireContext().assets.openFd("yolo_laptop.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    // 이미지를 분석하는 함수
    private fun analyzeImage(imageProxy: ImageProxy) {
        // ImageProxy에서 가져온 카메라 프레임을 Bitmap으로 변환 (YOLO 입력용)
        val bitmap = imageProxyToBitmap(imageProxy)
        // YOLOv8 TFLite 모델에 넣기 위한 전처리 작업 (640x640 크기, float 정규화 등)
        val preprocessed = preprocessBitmap(bitmap)
        val input = preprocessed.input
        val scale = preprocessed.scale
        val dx = preprocessed.dx
        val dy = preprocessed.dy

        // YOLO 모델 실행
        // 입력: input
        // 출력: [1, 5, 8400] 형식 (YOLOv11의 head 출력 → center x/y, width/height, confidence)
        val output = Array(1) { Array(5) { FloatArray(8400) } }
        tflite.run(input, output)

        // 추론 결과
        val predictions = output[0]
        // confidence 85%이상만 검출
        val threshold = 0.85f
        val modelInputSize = 640f

        // confidence 75%이상만 검출
        val detectedBoxes = mutableListOf<BBox>()

        // YOLOv11의 전체 anchor/grid 수 (8400개)
        for (i in 0 until 8400) {
            val score = predictions[4][i]

            // confidence score가 기준을 넘으면 유효한 객체로 간주
            if (score > threshold) {
                // YOLO 640 기준 bbox
                val cx = predictions[0][i] * modelInputSize
                val cy = predictions[1][i] * modelInputSize
                val w = predictions[2][i] * modelInputSize
                val h = predictions[3][i] * modelInputSize

                // YOLO 640 기준 bbox
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
                detectedBoxes.add(BBox(RectF(left, top, right, bottom), "ssafy_laptop", score))
//                Log.d("BBoxFinal", "BBox: RectF($left, $top, $right, $bottom)")
            }
        }

        // runOnUIThread에서 돌리던 걸 백그라운드로 옮김

        // 감지된 박스들 중에서 가장 넓은 박스 하나만 선택
        val bestBox = detectedBoxes.maxByOrNull { it.rect.width() * it.rect.height() }

        // 탐지된 객체가 있을 때만 아래 로직 실행
        if (bestBox != null) {
            val guideRect = binding.overlayView?.getGuideRect() ?: run {
                imageProxy.close()
                return
            }

            // 1️⃣ 비율 구하기 (bbox → overlay 변환)
            val scaleX = (binding.overlayView?.width?.div(bitmap.width.toFloat())) ?: 1f
            val scaleY = (binding.overlayView?.height?.div(bitmap.height.toFloat())) ?: 1f

            // 2️⃣ bbox를 overlay 좌표계로 변환
            val transformedRect = RectF(
                bestBox.rect.left * scaleX,
                bestBox.rect.top * scaleY,
                bestBox.rect.right * scaleX,
                bestBox.rect.bottom * scaleY
            )
            // bbox가 가이드 라인에 얼마나 유사한지 판단
            val iou = computeIoU(transformedRect, guideRect)

            // 이전 프레임의 bestBox와 비교해서
            // 중심좌표의 이동이 20px 이하이면 → "카메라 흔들림 없음"으로 간주
            // 이전 박스가 없으면 → 그냥 true (처음 프레임)
            val isPositionStable = lastBox?.let {
                val dx = abs(it.rect.centerX() - bestBox.rect.centerX())
                val dy = abs(it.rect.centerY() - bestBox.rect.centerY())
                dx < 20 && dy < 20
            } ?: true

            // 안정적인 위치에 있을 경우만
            if (isPositionStable) {
                // 안정된 프레임으로 인정되면 카운트 증가 (stableFrameCount)
                stableFrameCount++
                // 후보 프레임이 너무 많으면 앞에서부터 버림
                if (candidateBitmaps.size >= MAX_CANDIDATE_FRAMES) {
                    candidateBitmaps.removeFirst().recycle() // 비트맵 메모리 직접 해제
                }
                bitmap.config?.let { bitmap.copy(it, false) }?.let { candidateBitmaps.addLast(it) }
            } else {
                // 조건 불만족이면:
                // 안정 프레임 수 초기화
                // 후보 비트맵들 전부 삭제 (다시 모아야 함)
                stableFrameCount = 0
                candidateBitmaps.clear()
            }

            // 다음 프레임 비교를 위해 현재 박스를 저장
            lastBox = bestBox

            // 📌 IoU가 70% 이상일 때만 촬영 로직 실행
            // ✅ 10프레임 연속 안정된 상태
            // ✅ 현재 캡처하는 중이 아님
            if (iou > 0.7f && stableFrameCount >= requiredStableFrames && !isCapturing) {
                isCapturing = true
                // 지금까지 모은 비트맵 중에서 가장 좋은 걸 선택해서 저장!
                // 저장 후 초기화
                val bestBitmap = candidateBitmaps.maxByOrNull {
                    val lumaScore = calculateLuma(it).toDouble()
                    val sharpnessScore = calculateSharpness(it) * 5000
                    lumaScore + sharpnessScore
                }

                if (bestBitmap != null) {
                    lastCapturedBox = bestBox
//                    yolo가 낸 best가 bbox를 잘 탐지하고 있는지 갤러리에 사진 저장
//                    saveDebugBitmapWithBoxes(bestBitmap, detectedBoxes)
                    autoTakePhoto() // 사진 촬영
                }

                stableFrameCount = 0
                candidateBitmaps.forEach { it.recycle() }
                candidateBitmaps.clear()
            }

            // ✅ UI thread에는 오버레이만 넘김 (초경량)
            requireActivity().runOnUiThread {
//                화면에 탐지한 bbox를 그림
                binding.overlayView?.setBoxes(listOf(bestBox), bitmap.width, bitmap.height)
            }
        }

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

    // IoU 계산 함수 (IoU란, 가이드 라인과 탐지한 bbox가 얼마나 유사하나 판단하는 거임)
    private fun computeIoU(rect1: RectF, rect2: RectF): Float {
        val intersectionLeft = maxOf(rect1.left, rect2.left)
        val intersectionTop = maxOf(rect1.top, rect2.top)
        val intersectionRight = minOf(rect1.right, rect2.right)
        val intersectionBottom = minOf(rect1.bottom, rect2.bottom)

        // 면적이 얼마나 겹치는 걸로 판단!
        val intersectionArea = maxOf(0f, intersectionRight - intersectionLeft) *
                maxOf(0f, intersectionBottom - intersectionTop)
        val rect1Area = rect1.width() * rect1.height()
        val rect2Area = rect2.width() * rect2.height()

        val unionArea = rect1Area + rect2Area - intersectionArea

        return if (unionArea == 0f) 0f else intersectionArea / unionArea
    }

    private fun shutdownCamera() {
        try {
            // 카메라 사용 중지
            camera?.cameraControl?.enableTorch(false) // 플래시 사용 중이면 종료
            cameraProvider?.unbindAll() // 모든 카메라 바인딩 해제

            // 실행자 종료
            cameraExecutor?.shutdown()
            cameraExecutor = null
            camera = null
        } catch (e: Exception) {

        }
    }

    // yolo가 진짜로 잘 탐지했는지를 보기위해 laptop을 탐지하면 bbox를 그리고 갤러리에 저장. (삭제)
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
            requireContext().externalMediaDirs.first(),
            "yolo_debug_${System.currentTimeMillis()}.jpg"
        )
        FileOutputStream(debugFile).use { out ->
            debugBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        MediaScannerConnection.scanFile(
            requireContext(),
            arrayOf(debugFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )

        Log.d("MainActivity", "✅ YOLO 디버그 이미지 저장됨: ${debugFile.absolutePath}")
    }
}
