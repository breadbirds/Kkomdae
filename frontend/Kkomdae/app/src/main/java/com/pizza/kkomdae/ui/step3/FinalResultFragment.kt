package com.pizza.kkomdae.ui.step3

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentFinalResultBinding
import com.pizza.kkomdae.databinding.FragmentOathBinding
import com.pizza.kkomdae.presenter.viewmodel.FinalViewModel
import com.pizza.kkomdae.ui.SubmitCompleteFragment
import com.pizza.kkomdae.ui.guide.AllStepOnboardingFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FinalResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FinalResultFragment : BaseFragment<FragmentFinalResultBinding>(
    FragmentFinalResultBinding::bind,
    R.layout.fragment_final_result
) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val viewModel : FinalViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topBar.tvTitle.text = "제출 내용 확인"

        showIntroDialog()

        val adapter = FinalResultAdapter(requireContext())
        binding.viewPager.adapter = adapter

        viewModel.getLaptopTotalResult()


        viewModel.getFinalResult.observe(viewLifecycleOwner){
            binding.apply {
                if(!it.keyboardStatus){ // 키보드
                    ivKeyboard.setImageResource(R.drawable.ic_fail)
                }

                if(!it.useStatus){ // usb
                    ivUsb.setImageResource(R.drawable.ic_fail)
                }

                if(!it.cameraStatus){ // 카메라
                    ivCamera.setImageResource(R.drawable.ic_fail)
                }
                if(!it.batteryStatus){ // 배터리 성능
                    ivBattery.setImageResource(R.drawable.ic_fail)
                }
                if(!it.chargerStatus){ // 충전기
                    ivCharger.setImageResource(R.drawable.ic_fail)
                }

                tvInputModelName.text=it.modelCode
                tvInputSerial.text=it.serialNum
                tvInputBarcode.text=it.barcodeNum
                tvInputDate.text=it.date

                tvInputLaptopCount.text = it.laptopCount.toString()
                tvInputMouseCount.text = it.mouseCount.toString()
                tvInputAdapterCount.text= it.adapterCount.toString()
                tvInputPowerCount.text=it.powerCableCount.toString()
                tvInputBagCount.text = it.bagCount.toString()
                tvInputPadCount.text = it.mousepadCount.toString()

                tvFrontTitle.text = it.description

                adapter.submitList(it.imageUrls)

                // 데이터가 로드되고 어댑터에 설정된 후 인디케이터를 초기화
                setupIndicators(0)
            }
        }

        // 페이지 변경 감지
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 인디케이터 업데이트
                setupIndicators(position)
            }
        })

        // 제출하기 클릭 이벤트
        binding.btnSubmit.setOnClickListener {
            showSubmitDialog()
        }

        viewModel.pdfName.observe(viewLifecycleOwner){

            // 제출완료 화면으로 전환
            val submitCompleteFragment = SubmitCompleteFragment.newInstance("", "")

            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_main, submitCompleteFragment)
                .addToBackStack(null)
                .commit()
        }

    }


    private fun setupIndicators(position: Int) {
        // 인디케이터 컨테이너 초기화
        binding.indicatorContainer.removeAllViews()

        // 이미지 개수만큼 인디케이터 추가
        val imageCount = (binding.viewPager.adapter as FinalResultAdapter).itemCount
        if (imageCount <= 1) return // 이미지가 1개 이하면 인디케이터 필요 없음

        // 인디케이터 점들 추가
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 0, 8, 0)

        for (i in 0 until imageCount) {
            val dot = ImageView(requireContext())
            dot.layoutParams = params

            // 현재 위치는 선택된 점, 나머지는 선택되지 않은 점으로 표시
            if (i == position) {
                dot.setImageResource(R.drawable.indicator_dot_selected)
            } else {
                dot.setImageResource(R.drawable.indicator_dot_unselected)
            }

            binding.indicatorContainer.addView(dot)
        }
    }

    // 제출하기 다이얼로그
    private fun showSubmitDialog() {
        // 다이얼로그 생성
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_submit_confirm)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 취소 버튼
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 제출하기 버튼
        val btnConfirm = dialog.findViewById<TextView>(R.id.btn_confirm)
        btnConfirm.setOnClickListener {
            // 다이얼로그 닫기
            dialog.dismiss()

            // api호출
            viewModel.postPdf()


        }

        dialog.show()
    }


    // 인트로 다이얼로그
    private fun showIntroDialog() {
        // 다이얼로그 생성
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_final_result)

        // 다이얼로그 배경 투명하게
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 너비 : 화면 너비의 90%
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 다이얼로그 확인 버튼 클릭 이벤트
        val confirmButton = dialog.findViewById<Button>(R.id.btn_confirm)
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
         * @return A new instance of fragment FinalResultFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FinalResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}