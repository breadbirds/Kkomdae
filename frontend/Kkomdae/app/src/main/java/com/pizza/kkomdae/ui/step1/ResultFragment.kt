package com.pizza.kkomdae.ui.step1

import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.pizza.kkomdae.CameraActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentFontResultBinding
import com.pizza.kkomdae.presenter.viewmodel.CameraViewModel
import com.pizza.kkomdae.ui.guide.Step1GuideFragment

private const val TAG = "ResultFragment"
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var cameraActivity: CameraActivity

private var stopCameraDialog: Dialog? = null

class ResultFragment : BaseFragment<FragmentFontResultBinding>(
    FragmentFontResultBinding::bind,
    R.layout.fragment_font_result
){

    // 뷰모델
    private val viewModel: CameraViewModel by activityViewModels()

    private var param1: String? = null
    private var param2: String? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        var url :Uri? = null
        // uri 설정
        url = uri(url)

        // 이미지 설정
        initImage(url)

        // 버튼 설정
        settingButton()

        // 옵져버 설정
        observe()
    }

    private fun settingButton() {
        // 재촬영 버튼 눌렀을 때
        clickRePhotoBtn()

        // 체크 버튼 눌렀을 때
        clickCheckBtn()

        // X 버튼 눌렀을 때
        clickCloseBtn()
    }

    private fun observe() {
        // 통신 결과
        postResultObserve()

        //통신 실패시
        failResultObserve()

        // 재촬영 url
        rePhotoObserve()
    }

    private fun rePhotoObserve() {
        viewModel.reCameraUri.observe(viewLifecycleOwner) {
            it ?: return@observe
            cameraActivity.moveToBackReCamera(it)
        }
    }

    private fun failResultObserve() {
        viewModel.failResult.observe(viewLifecycleOwner) {
            it ?: return@observe
            Log.d(TAG, "onViewCreated: $it")
            showNetworkErrorDialog()
            viewModel.clearFail()
            Log.d(TAG, "onViewCreated: $it")
        }
    }

    private fun postResultObserve() {
        viewModel.postResult.observe(viewLifecycleOwner) {
            it ?: return@observe
            if (it?.success == true) {
                // 서버에 사진 전송 성공시에만 프론트에 단계 저장
                viewModel.confirmPhoto(viewModel.step.value ?: 0)
                // 다음 단계로 이동
                Log.d("imageBug", "postResultStep: ${viewModel.step.value}")
                cameraActivity.changeFragment((viewModel.step.value ?: -1) + 1)
                viewModel.clearResult()
            } else {
                showNetworkErrorDialog()
            }
        }
    }

    private fun clickCloseBtn() {
        binding.btnCancel?.setOnClickListener {
            showStopCameraDialog()
        }
    }

    private fun clickCheckBtn() {
        binding.btnCheck?.setOnClickListener {
            // ✅ step 2 하판인 경우 OCR 호출
            if (viewModel.step.value == 2) {
                viewModel.backUri.value?.let { uri ->
                    try {
                        val bitmap = uriToBitmap(requireContext(), uri)
                        viewModel.callOcrFromBitmap(requireContext(), bitmap)
                        Log.d(TAG, "📸 OCR called from ResultFragment - step 2")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ OCR 오류: ${e.message}")
                    }
                }
            }
            viewModel.postPhoto()
        }
    }

    private fun clickRePhotoBtn() {
        binding.btnBack?.setOnClickListener {
            cameraActivity.changeFragment((viewModel.step.value ?: -1))
        }
    }

    private fun initImage(url: Uri?) {
        // 로딩 이미지 설정
        loadingImage()
        binding.ivProduct?.let {
            Log.d(TAG, "CameraFragment uri: $it")
            Glide.with(it)
                .load(url)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?,
                    ) {
                        binding.ivProduct?.setImageDrawable(resource)
                        binding.ivProduct?.visibility = View.VISIBLE
                        binding.ivLoading?.visibility = View.GONE
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    private fun uri(url: Uri?): Uri? {
        var url1 = url
        if (viewModel.step.value == 1) {
            url1 = viewModel.frontUri.value
        } else if (viewModel.step.value == 2) {
            url1 = viewModel.backUri.value
        } else if (viewModel.step.value == 3) {
            url1 = viewModel.leftUri.value
        } else if (viewModel.step.value == 4) {
            url1 = viewModel.rightUri.value
        } else if (viewModel.step.value == 5) {
            url1 = viewModel.screenUri.value
        } else if (viewModel.step.value == 6) {
            url1 = viewModel.keypadUri.value
        }
        return url1
    }

    private fun loadingImage() {
        binding.ivLoading?.let {
            Glide.with(this)
                .asGif()
                .load(R.drawable.skeleton_ui) // 🔁 로딩용 GIF 리소스
                .into(it)
        }
    }

    //ocr
    private fun uriToBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }


    private fun showStopCameraDialog() {
        if (stopCameraDialog?.isShowing == true) return
        // 다이얼로그 생성
        stopCameraDialog = Dialog(requireContext())
        stopCameraDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        stopCameraDialog?.setContentView(R.layout.layout_stop_camera_dialog)

        stopCameraDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.5).toInt()
        stopCameraDialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 취소 버튼
        val btnCancel = stopCameraDialog?.findViewById<TextView>(R.id.btn_cancel)
        btnCancel?.setOnClickListener {
            stopCameraDialog?.dismiss()
        }

        // 그만하기 버튼
        val btnConfirm = stopCameraDialog?.findViewById<TextView>(R.id.btn_confirm)
        btnConfirm?.setOnClickListener {
            // 다이얼로그 닫기
            stopCameraDialog?.dismiss()
            cameraActivity.moveToBack()
        }

        stopCameraDialog?.show()
    }

    private fun showNetworkErrorDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_error_dialog)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.4).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 확인 버튼
        val confirmButton = dialog.findViewById<TextView>(R.id.tv_confirm)
        confirmButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Step1GuideFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        cameraActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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


}
