package com.pizza.kkomdae.ui.guide

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pizza.kkomdae.MainActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentStep2GuideBinding
import com.pizza.kkomdae.ui.step2.QrScanFragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.pizza.kkomdae.presenter.viewmodel.Step2ViewModel
import com.pizza.kkomdae.ui.step2.Step2ResultFragment
import com.pizza.kkomdae.ui.step3.LaptopInfoInputFragment
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Step2GuideFragment : BaseFragment<FragmentStep2GuideBinding>(
    FragmentStep2GuideBinding::bind,
    R.layout.fragment_step2_guide
) {

    private var param1: String? = null
    private var param2: String? = null

    // 시스템 백 버튼 콜백 선언
    private lateinit var backPressedCallback: OnBackPressedCallback
    private val viewModel: Step2ViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 랜덤키 생성
        crateRandom()

        binding.topBar.tvTitle.text = "Step 2"
        binding.topBar.pbStep.progress=200/3

        // dot 로딩
        binding.animationDot.apply {
            setAnimation(R.raw.dot_loading)
            repeatCount = LottieDrawable.INFINITE
            speed = 0.5f
            playAnimation()
        }

        // 배경 원형 애니메이션
        binding.animationBg.apply {
            setAnimation(R.raw.laptop_connection)
            repeatCount = LottieDrawable.INFINITE
            speed = 0.5f  // 속도 조절 (1.0f가 기본 속도, 0.5f는 절반 속도, 2.0f는 두 배 속도)
            playAnimation()
        }


        // X 클릭 이벤트 설정
        binding.topBar.backButtonContainer.setOnClickListener {
            showQuitBottomSheet()
        }

        // todo 임시 건너뛰기 나중에 삭제 해야함
        binding.btnPass.setOnClickListener {
            lifecycleScope.launch {
                val result = viewModel.postSecondStage()
                result.onSuccess {
                    if (it.success){
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fl_main, LaptopInfoInputFragment())
                        transaction.commit()
                    }
                }
            }

        }


        // 자가진단 결과보기 버튼 클릭
        binding.btnNext.setOnClickListener {

            lifecycleScope.launch {
                val result = viewModel.getStep2Result()
                result.onSuccess {
                    if(it.data.success==true){
                        // 자가진단 완료
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fl_main, Step2ResultFragment())
                        transaction.commit()
                    }else{
                        var data = ""
                        it.data?.let {
                            if(it.keyboard_status==null){
                                data += "키보드"
                            }
                            if(it.camera_status == null){
                                if(data!=""){
                                    data += ", "
                                }
                                data += "카메라"
                            }
                            if(it.usb_status == null){
                                if(data!=""){
                                    data += ", "
                                }
                                data += "USB"
                            }
                            if(it.charging_status == null){
                                if(data!=""){
                                    data += ", "
                                }
                                data += "충전기"
                            }

                            if(it.battery_report == null){
                                if(data!=""){
                                    data += ", "
                                }
                                data += "배터리"
                            }
                        }

                        showDevelopingDialog(data)
                    }


                }.onFailure {

                }
            }
            // 2단계 결과 받아오기



        }

        // 자가진단 결과가 완벽하게
        viewModel.getStep2Result.observe(viewLifecycleOwner){
            Log.d("Post", "onViewCreated: $it")
        }


        showIntroDialog()
    }

    // 개발 중 다이얼로그
    private fun showDevelopingDialog(data : String) {

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_step2_fail)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val confirmButton = dialog.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_confirm)

        val descriptionTextView = dialog.findViewById<TextView>(R.id.tv_description2)

        descriptionTextView.text = data
        confirmButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // 랜덤키 생성
    private fun crateRandom() {
        viewModel.postRandomKey()
        viewModel.randomKey.observe(viewLifecycleOwner) {
            binding.tvRandom.text="코드 번호 : $it"
            Log.d("Post", "onViewCreated: $it")
        }
    }

    private fun showIntroDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_step2_intro)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val confirmButton = dialog.findViewById<Button>(R.id.btn_confirm)
        confirmButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showQuitBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_bottom_sheet, null)

        // 계속하기 버튼 클릭 시 바텀시트 닫기
        val btnContinue = bottomSheetView.findViewById<View>(R.id.btn_continue)
        btnContinue.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // 그만두기 버튼 클릭 시 메인 화면으로 이동
        val btnQuit = bottomSheetView.findViewById<View>(R.id.btn_quit)
        btnQuit.setOnClickListener {
            bottomSheetDialog.dismiss()
            // 메인 화면으로 이동
            val mainActivity = requireActivity() as MainActivity
            mainActivity.supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Step2GuideFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        // 시스템 백 버튼 동작 설정
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showQuitBottomSheet()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)

    }

    override fun onDestroy() {
        super.onDestroy()
        backPressedCallback.remove()
    }
}