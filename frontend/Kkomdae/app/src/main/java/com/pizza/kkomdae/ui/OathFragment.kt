package com.pizza.kkomdae.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentOathBinding
import androidx.core.widget.ImageViewCompat
import android.content.res.ColorStateList
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.pizza.kkomdae.presenter.viewmodel.OathViewModel
import com.pizza.kkomdae.ui.guide.AllStepGuideFragment
import com.pizza.kkomdae.ui.guide.AllStepOnboardingFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class OathFragment : BaseFragment<FragmentOathBinding>(
    FragmentOathBinding::bind,
    R.layout.fragment_oath
) {

    private val viewModel: OathViewModel by viewModels()

    private var isOath1Checked = false
    private var isOath2Checked = false
    private var isOath3Checked = false
    private var isOath4Checked = false

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        viewModel.setIsOath1Checked(false)
        viewModel.setIsOath2Checked(false)
        viewModel.setIsOath3Checked(false)
        viewModel.setIsOath4Checked(false)

    }

//    override fun onResume() {
//        super.onResume()
//        isOath1Checked = false
//        isOath2Checked = false
//        isOath3Checked = false
//        isOath4Checked = false
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 다이얼로그 표시
        showIntroDialog()

        // 제목 설정
        val topBarTitle = view.findViewById<View>(R.id.top_bar).findViewById<TextView>(R.id.tv_title)
        topBarTitle.text = "수령확인서"

        val title1 = view.findViewById<View>(R.id.oath_title1).findViewById<TextView>(R.id.tv_model_number)
        title1.text = "01 손실 및 파손 책임"

        val title2 = view.findViewById<View>(R.id.oath_title2).findViewById<TextView>(R.id.tv_model_number)
        title2.text = "02 사용 규정 및 의무"

        val title3 = view.findViewById<View>(R.id.oath_title3).findViewById<TextView>(R.id.tv_model_number)
        title3.text = "03 반납 및 보관 의무"

        val title4 = view.findViewById<View>(R.id.oath_title4).findViewById<TextView>(R.id.tv_model_number)
        title4.text = "04 인수 확인"

        // 뒤로가기 버튼
        binding.topBar.backButtonContainer.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // 서약서 항목 클릭 이벤트
        binding.cardAgreement1.setOnClickListener {
            toggleOathSelection(it, R.id.oath_title1)
            isOath1Checked = !(viewModel.isOath1Checked.value?:false)
            Log.d("TAG", "onViewCreated: $isOath1Checked")
            viewModel.setIsOath1Checked(isOath1Checked)
            updateCompleteButtonState()
            binding.oathContent1.isVisible=!isOath1Checked
        }

        binding.cardAgreement2.setOnClickListener {
            toggleOathSelection(it, R.id.oath_title2)
            isOath2Checked = !(viewModel.isOath2Checked.value?:false)
            Log.d("TAG", "onViewCreated: $isOath2Checked")
            viewModel.setIsOath2Checked(isOath2Checked)
            updateCompleteButtonState()
            binding.oathContent2.isVisible=!isOath2Checked
        }
        binding.cardAgreement3.setOnClickListener {
            toggleOathSelection(it, R.id.oath_title3)
            isOath3Checked = !(viewModel.isOath3Checked.value?:false)
            Log.d("TAG", "onViewCreated: $isOath3Checked")
            viewModel.setIsOath3Checked(isOath3Checked)
            updateCompleteButtonState()
            binding.oathContent3.isVisible=!isOath3Checked
        }
        binding.cardAgreement4.setOnClickListener {
            toggleOathSelection(it, R.id.oath_title4)
            isOath4Checked = !(viewModel.isOath4Checked.value?:false)
            Log.d("TAG", "onViewCreated: $isOath4Checked")
            viewModel.setIsOath4Checked(isOath4Checked)
            updateCompleteButtonState()
            binding.oathContent4.isVisible=!isOath4Checked
        }


        // 다음 기기등록 화면으로 넘어가기
        binding.btnNext.setOnClickListener {
            if (isOath1Checked && isOath2Checked && isOath3Checked && isOath4Checked) {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fl_main, AllStepOnboardingFragment())
                transaction.commit()
            }
        }

    }

    private fun showIntroDialog() {
        // 다이얼로그 생성
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_oath_intro)

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

    private fun toggleOathSelection(cardView: View, titleId: Int) {
        val oathTitleView = cardView.findViewById<View>(titleId)
        oathTitleView.isSelected = !oathTitleView.isSelected

        val checkImageView = oathTitleView.findViewById<ImageView>(R.id.iv_check)
        if (oathTitleView.isSelected) {
            ImageViewCompat.setImageTintList(
                checkImageView,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue500))
            )
        } else {
            ImageViewCompat.setImageTintList(
                checkImageView,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray300))
            )
        }
    }

    // 버튼 상태 업데이트 함수
    private fun updateCompleteButtonState() {
        val allChecked = isOath1Checked && isOath2Checked && isOath3Checked && isOath4Checked
        Log.d("TAG", "updateCompleteButtonState: $allChecked")
        if (allChecked) {
            binding.btnNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue500))
        } else {
            binding.btnNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue200))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OathFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}