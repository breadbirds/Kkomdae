package com.pizza.kkomdae.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentNoteBinding
import com.pizza.kkomdae.presenter.viewmodel.FinalViewModel
import com.pizza.kkomdae.ui.step3.FinalResultFragment


class NoteFragment : BaseFragment<FragmentNoteBinding>(
    FragmentNoteBinding::bind,
    R.layout.fragment_note
) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var isTextEntered = false   // 텍스트 입력 여부를 추적하는 변수
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

        // 텍스트 입력 감지를 위한 TextWatcher 설정
        binding.etNoteDetail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 입력 여부에 따라 버튼 상태 업데이트
                isTextEntered = !s.isNullOrEmpty()
                updateSaveButtonState()
            }
        })

        // 건너뛰기 버튼 클릭 이벤트
        binding.btnSkip.setOnClickListener {
            navigateToFinalResult()
        }

        // 저장하기 버튼 클릭 이벤트
        binding.btnSave.setOnClickListener {
            viewModel.postFourthStage(binding.etNoteDetail.text.toString())

        }

        // 뒤로가기 버튼
        binding.topBar.backButtonContainer.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // X 버튼
        binding.topBar.cancelButtonContainer.setOnClickListener {
            showQuitBottomSheet()
        }

        // 통신 결과 감시
        viewModel.postFourth.observe(viewLifecycleOwner){
            if(it.success && it.status=="OK"){
                navigateToFinalResult()
            }
        }


        // 초기 버튼 상태 설정
        updateSaveButtonState()

    }

    // 저장하기 버튼 상태 업데이트
    private fun updateSaveButtonState() {
        if (isTextEntered) {
            binding.btnSave.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue500))
        } else {
            binding.btnSave.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue200))
        }
    }

    // 다음 화면으로 이동
    private fun navigateToFinalResult() {

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_main, FinalResultFragment())
        transaction.addToBackStack(null)
        transaction.commit()
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
            val mainActivity = requireActivity()
            mainActivity.supportFragmentManager.popBackStack(null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            val transaction = mainActivity.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fl_main, com.pizza.kkomdae.ui.MainFragment())
            transaction.commit()
        }

        // 바텀시트 표시
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"


        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NoteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NoteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}