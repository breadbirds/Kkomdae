package com.pizza.kkomdae.ui.step2

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.pizza.kkomdae.MainActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.presenter.model.DeviceReport
import com.pizza.kkomdae.databinding.FragmentQrScanBinding
import com.pizza.kkomdae.presenter.viewmodel.Step2ViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [QrScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
private lateinit var mainActivity: MainActivity
private const val TAG = "QrScanFragment"
class QrScanFragment : BaseFragment<FragmentQrScanBinding>(
    FragmentQrScanBinding::bind,
    R.layout.fragment_qr_scan
)  {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var camera: Camera? = null
    private val viewModel: Step2ViewModel by activityViewModels()

    inner class QrScanner : ImageAnalysis.Analyzer {
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(image: ImageProxy) {
            val mediaImage = image.image
            if (mediaImage != null) {
                val inputImage =
                    InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_AZTEC
                        )
                        .build()
                )
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            // 바코드 스캔 성공 시 처리 로직
                            val rawValue = barcode.rawValue
                            if(isValidJson("$rawValue")){
                                Log.d(TAG, "Scanned QR Code: $rawValue")
                                val report = Gson().fromJson(rawValue, DeviceReport::class.java)
                                viewModel.apply {
                                    setUsbStatus(report.usb)
                                    setCameraStatus(report.camera)
                                    setBatteryStatus(report.battery_report)
                                    setChargerStatus(report.charger)
                                    setKeyboardStatus(report.keyboard)
                                }
                                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                transaction.replace(R.id.fl_main, Step2ResultFragment())
                                transaction.addToBackStack(null)
                                transaction.commit()


                            }else{
                                Toast.makeText(requireContext(),"잘못된 QR코드 정보입니다.",Toast.LENGTH_SHORT).show()
                            }


                            // 여기에 스캔된 바코드 처리 로직 추가
                            // 예: 특정 액티비티로 이동, 데이터 처리 등
                        }
                    }
                    .addOnFailureListener {
                        // 스캔 실패 시 처리 로직
                        Log.e(TAG, "Barcode scanning failed")
                    }
                    .addOnCompleteListener {
                        // 항상 이미지 리소스 해제
                        image.close()
                    }
            }
        }
    }
    fun isValidJson(jsonString: String): Boolean {
        return try {
            JsonParser.parseString(jsonString)
            true
        } catch (e: JsonSyntaxException) {
            false
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.previewView
        startCamera()
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 후면 카메라 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Preview 설정
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // ImageAnalysis 설정 추가
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(requireContext().mainExecutor, QrScanner())
                }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis  // ImageAnalysis 추가
                )
            } catch (e: Exception) {
                Log.e("QrScanFragment", "카메라 실행 오류: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()  // 📌 카메라 리소스 완전히 해제
            Log.d(TAG, "카메라 해제 완료")
        }, ContextCompat.getMainExecutor(requireContext()))
    }






    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment QrScanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QrScanFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}