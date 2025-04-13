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

/**
 * A simple [Fragment] subclass.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "ResultFragment"
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var cameraActivity: CameraActivity

class ResultFragment : BaseFragment<FragmentFontResultBinding>(
    FragmentFontResultBinding::bind,
    R.layout.fragment_font_result
){
    private val viewModel: CameraViewModel by activityViewModels()


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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

        binding.ivLoading?.let {
            Glide.with(this)
                .asGif()
                .load(R.drawable.skeleton_ui) // 🔁 로딩용 GIF 리소스
                .into(it)
        }


        Log.d(TAG, "onViewCreated: ${viewModel.frontUri.value}")
        Log.d(TAG, "onViewCreated stage: ${viewModel.step.value}")
        var url :Uri? = null
        if(viewModel.step.value == 1){
            url = viewModel.frontUri.value
        }else if (viewModel.step.value == 2){
            url = viewModel.backUri.value
        }else if (viewModel.step.value == 3){
            url = viewModel.leftUri.value
        }else if (viewModel.step.value == 4){
            url = viewModel.rightUri.value
        }else if (viewModel.step.value == 5){
            url = viewModel.screenUri.value
        }else if (viewModel.step.value == 6){
            url = viewModel.keypadUri.value
        }
        binding.ivProduct?.let {
            Log.d(TAG, "CameraFragment uri: $it")
            Glide.with(it)
                .load(url)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        binding.ivProduct?.setImageDrawable(resource)
                        binding.ivProduct?.visibility = View.VISIBLE
                        binding.ivLoading?.visibility = View.GONE
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        binding.btnBack?.setOnClickListener {
            cameraActivity.changeFragment((viewModel.step.value?:-1))
        }

        // 체크 버튼
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

        // X 버튼 눌렀을 때
        binding.btnCancel?.setOnClickListener {
            showStopCameraDialog()
        }

        viewModel.postResult.observe(viewLifecycleOwner){
            it?: return@observe
            if(it?.success == true){
                // 서버에 사진 전송 성공시에만 프론트에 단계 저장
                viewModel.confirmPhoto(viewModel.step.value ?: 0)
                // 다음 단계로 이동
                cameraActivity.changeFragment((viewModel.step.value?:-1)+1)
                viewModel.clearResult()
            } else {
                showNetworkErrorDialog()
            }
        }

        //실패시
        viewModel.failResult.observe(viewLifecycleOwner){
            it?: return@observe
            Log.d(TAG, "onViewCreated: $it")
            showNetworkErrorDialog()
            viewModel.clearFail()
            Log.d(TAG, "onViewCreated: $it")
        }

        // 재촬영 url
        viewModel.reCameraUri.observe(viewLifecycleOwner){
            it?: return@observe
            cameraActivity.moveToBackReCamera(it)
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Step1GuideFragment.
         */
        // TODO: Rename and change types and number of parameters
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


}
