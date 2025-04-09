package com.pizza.kkomdae.ui.step1

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentCameraBigFrameBinding

class CameraBigFrameFragment : BaseFragment<FragmentCameraBigFrameBinding>(
    FragmentCameraBigFrameBinding::bind,
    R.layout.fragment_camera_big_frame
) {
    private var camera: Camera? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCamera()

//        binding.btnCapture.setOnClickListener {
//            takePhoto()
//        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (e: Exception) {
                Log.e("CameraBigFrameFragment", "카메라 실행 오류: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }




    private fun takePhoto() {
        Log.d("CameraBigFrameFragment", "촬영 버튼이 클릭됨!")
        // 촬영 기능 추가
    }
}
