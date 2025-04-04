package com.pizza.kkomdae.ui.guide

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pizza.kkomdae.AppData
import com.pizza.kkomdae.MainActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentLaptopInfoInputBinding
import com.pizza.kkomdae.databinding.FragmentMainBinding
import com.pizza.kkomdae.databinding.FragmentStep1GuideBinding
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel
import kotlin.math.log

private lateinit var mainActivity: MainActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var step =0

/**
 * A simple [Fragment] subclass.
 * Use the [Step1GuideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Step1GuideFragment : BaseFragment<FragmentStep1GuideBinding>(
    FragmentStep1GuideBinding::bind,
    R.layout.fragment_step1_guide
){

    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val viewModel : MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        
        // 시스템 백 버튼 동작 설정
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 시스템 백 버튼 클릭 시 바텀시트 동작
                showQuitBottomSheet()
            }
        }
        // 콜백 등록
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)

    }

    override fun onDestroy() {
        super.onDestroy()
        // 콜백 해제
        backPressedCallback.remove()
    }


    override fun onResume() {
        super.onResume()

        val color = ContextCompat.getColorStateList(requireContext(), R.color.blue500)

        step=viewModel.getPhotoStage()
        when(step){
            1-> {
                binding.layoutStep.flStep1.backgroundTintList=color
            }
            2->{
                binding.layoutStep.flStep1.backgroundTintList=color
                binding.layoutStep.flStep2.backgroundTintList=color
            }
            3->{
                binding.layoutStep.flStep1.backgroundTintList=color
                binding.layoutStep.flStep2.backgroundTintList=color
                binding.layoutStep.flStep3.backgroundTintList=color
            }
            4->{
                binding.layoutStep.flStep1.backgroundTintList=color
                binding.layoutStep.flStep2.backgroundTintList=color
                binding.layoutStep.flStep3.backgroundTintList=color
                binding.layoutStep.flStep4.backgroundTintList=color
            }
            5->{
                binding.layoutStep.flStep1.backgroundTintList=color
                binding.layoutStep.flStep2.backgroundTintList=color
                binding.layoutStep.flStep3.backgroundTintList=color
                binding.layoutStep.flStep4.backgroundTintList=color
                binding.layoutStep.flStep5.backgroundTintList=color
            }
            6->{
                binding.layoutStep.flStep1.backgroundTintList=color
                binding.layoutStep.flStep2.backgroundTintList=color
                binding.layoutStep.flStep3.backgroundTintList=color
                binding.layoutStep.flStep4.backgroundTintList=color
                binding.layoutStep.flStep5.backgroundTintList=color
                binding.layoutStep.flStep6.backgroundTintList=color
            }
            else->{

            }
        }

        // 버튼 텍스트 업데이트
        updateButtonText()
    }

    private fun updateButtonText() {
        when {
            step == 6 -> {
                binding.btnNext.text = "완료"
                binding.btnNext.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue500)
            }
            step > 0 -> {
                binding.btnNext.text = "이어서 촬영하기"
            }
            else -> {
                binding.btnNext.text = "촬영하기"
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.topBar.tvTitle.text = "STEP 1"
        binding.topBar.pbStep.progress=100/3

        // 로그 추가
        step = viewModel.getPhotoStage()
        Log.d("Step1GuideFragment", "onViewCreated - Current step value: $step")

        binding.layoutStep.flStep1

        // X 버튼
        binding.topBar.backButtonContainer.setOnClickListener {
            showQuitBottomSheet()
        }

        // 촬영하기 버튼
        binding.btnNext.setOnClickListener {
            if (step == 6) {
                // 완료 버튼 클릭 시 Step2GuideFragment로 이동
                val transaction = mainActivity.supportFragmentManager.beginTransaction()
                mainActivity.supportFragmentManager.popBackStack()
                transaction.replace(R.id.fl_main, Step2GuideFragment())
                    .addToBackStack("")
                transaction.commit()
            } else {
                // 촬영 단계가 완료되지 않았으면 촬영 계속
                mainActivity.next()
            }
        }

        if (step == 0) {
            showIntroDialog()
        }
    }

    private var introDialog: Dialog? = null

    private fun showIntroDialog() {
        introDialog?.dismiss()

        introDialog = Dialog(requireContext())
        introDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        introDialog?.setContentView(R.layout.layout_dialog_step1_intro)
        
        // 배경 투명하게
        introDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // 너비 설정
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        introDialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 확인 버튼
        val confirmButton = introDialog?.findViewById<Button>(R.id.btn_confirm)
        confirmButton?.setOnClickListener {
            introDialog?.dismiss()
            introDialog = null
        }

        introDialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        introDialog?.dismiss()
        introDialog = null
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

            // UI 스레드에서 약간의 지연 후 화면 전환
            view?.post {
                try {
                    // 메인 화면으로 이동
                    val transaction = mainActivity.supportFragmentManager.beginTransaction()
                    transaction.setReorderingAllowed(true)
                    transaction.replace(R.id.fl_main, com.pizza.kkomdae.ui.MainFragment())
                    transaction.commit()

                    // 백스택 즉시 비우기
                    mainActivity.supportFragmentManager.popBackStackImmediate(null, 1)
                } catch (e: Exception) {
                    Log.e("Step1GuideFragment", "MainFragment로 이동 중 오류: ${e.message}", e)
                }
            }
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
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
}