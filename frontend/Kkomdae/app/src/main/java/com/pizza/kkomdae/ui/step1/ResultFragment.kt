package com.pizza.kkomdae.ui.step1

import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
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

        Log.d(TAG, "onViewCreated: ${viewModel.frontUri.value}")
        Log.d(TAG, "onViewCreated stage: ${viewModel.step.value}")
        var url :Uri? = null
        if(viewModel.step.value ==1){
            url = viewModel.frontUri.value
        }else if (viewModel.step.value ==2){
            url = viewModel.backUri.value
        }else if (viewModel.step.value ==3){
            url = viewModel.leftUri.value
        }else if (viewModel.step.value ==4){
            url = viewModel.rightUri.value
        }else if (viewModel.step.value ==5){
            url = viewModel.screenUri.value
        }else if (viewModel.step.value ==6){
            url = viewModel.keypadUri.value
        }
        binding.ivProduct?.let {
            Log.d(TAG, "CameraFragment uri: $it")
            Glide.with(it)
                .load(url)
                .into(it)
        }

        binding.btnBack?.setOnClickListener {
            cameraActivity.changeFragment(viewModel.step.value?:0)
        }

        // 체크 버튼
        binding.btnCheck?.setOnClickListener {
            viewModel.postPhoto()
        }

        // X 버튼 눌렀을 때
        binding.btnCancel?.setOnClickListener {
            showStopCameraDialog()
        }

        viewModel.postResult.observe(viewLifecycleOwner){
            if(it?.success == true){
                cameraActivity.changeFragment((viewModel.step.value?:-1)+1)
                viewModel.clearResult()
            }
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
