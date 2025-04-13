package com.pizza.kkomdae.ui.step2

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pizza.kkomdae.MainActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentStep2ResultBinding
import com.pizza.kkomdae.presenter.viewmodel.Step2ViewModel
import com.pizza.kkomdae.ui.step3.LaptopInfoInputFragment
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Step2ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Step2ResultFragment : BaseFragment<FragmentStep2ResultBinding>(
    FragmentStep2ResultBinding::bind,
    R.layout.fragment_step2_result
) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val viewModel: Step2ViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.topBar.tvTitle.text = "Step 2"
        binding.topBar.pbStep.progress=200/3

        // 자가진단 결과 받아오기
        lifecycleScope.launch {
            val result = viewModel.getStep2Result()
            result.onSuccess {
                it.data.apply {
                    // 키보드
                    if(it.data.keyboard_status == true){
                        binding.tvKeyboard.text="통과"
                        binding.tvKeyboard.setBackgroundResource(R.drawable.bg_rounded_blue_light)
                        binding.tvKeyboard.setTextColor(Color.parseColor("#485B78"))
                    }else{
                        binding.tvKeyboard.text="실패"
                        binding.tvKeyboard.setBackgroundResource(R.drawable.bg_rounded_red_light)
                        binding.tvKeyboard.setTextColor(Color.parseColor("#E4614F"))
                    }

                    // 카메라
                    if(it.data.camera_status == true){
                        binding.tvCamera.text="통과"
                        binding.tvCamera.setBackgroundResource(R.drawable.bg_rounded_blue_light)
                        binding.tvCamera.setTextColor(Color.parseColor("#485B78"))
                    }else{
                        binding.tvCamera.text="실패"
                        binding.tvCamera.setBackgroundResource(R.drawable.bg_rounded_red_light)
                        binding.tvCamera.setTextColor(Color.parseColor("#E4614F"))
                    }

                    // USB
                    if(it.data.usb_status == true){
                        binding.tvUsb.text="통과"
                        binding.tvUsb.setBackgroundResource(R.drawable.bg_rounded_blue_light)
                        binding.tvUsb.setTextColor(Color.parseColor("#485B78"))
                    }else{
                        binding.tvUsb.text="실패"
                        binding.tvUsb.setBackgroundResource(R.drawable.bg_rounded_red_light)
                        binding.tvUsb.setTextColor(Color.parseColor("#E4614F"))
                    }

                    // 충전기
                    if(it.data.charging_status == true){
                        binding.tvCharger.text="통과"
                        binding.tvCharger.setBackgroundResource(R.drawable.bg_rounded_blue_light)
                        binding.tvCharger.setTextColor(Color.parseColor("#485B78"))
                    }else{
                        binding.tvCharger.text="실패"
                        binding.tvCharger.setBackgroundResource(R.drawable.bg_rounded_red_light)
                        binding.tvCharger.setTextColor(Color.parseColor("#E4614F"))
                    }

                    // 배터리
                    if(it.data.battery_report == true){
                        binding.tvBattery.text="통과"
                        binding.tvBattery.setBackgroundResource(R.drawable.bg_rounded_blue_light)
                        binding.tvBattery.setTextColor(Color.parseColor("#485B78"))
                    }else{
                        binding.tvBattery.text="실패"
                        binding.tvBattery.setBackgroundResource(R.drawable.bg_rounded_red_light)
                        binding.tvBattery.setTextColor(Color.parseColor("#E4614F"))
                    }
                }
            }
        }




        binding.btnNext.setOnClickListener {

            lifecycleScope.launch {
                val result = viewModel.postSecondToThird()
                result.onSuccess {
                    if (it.success){
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fl_main, LaptopInfoInputFragment())
                        transaction.commit()
                    }else{
                        // todo 에러 다이얼로그 추가
                    }
                }
            }

        }



        // X 클릭 이벤트 설정
        binding.topBar.backButtonContainer.setOnClickListener {
            showQuitBottomSheet()
        }
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Step2ResultFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Step2ResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}