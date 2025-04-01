package com.pizza.kkomdae.ui

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentNoteBinding
import com.pizza.kkomdae.ui.step3.FinalResultFragment


class NoteFragment : BaseFragment<FragmentNoteBinding>(
    FragmentNoteBinding::bind,
    R.layout.fragment_note
) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 건너뛰기 버튼 클릭 이벤트
        binding.btnSkip.setOnClickListener {
            navigateToFinalResult()
        }

        // 저장하기 버튼 클릭 이벤트
        binding.btnSave.setOnClickListener {
            navigateToFinalResult()
        }

        // 뒤로가기 버튼
        binding.topBar.backButtonContainer.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // X 버튼
        binding.topBar.cancelButtonContainer.setOnClickListener {
            showQuitBottomSheet()
        }
    }

    private fun navigateToFinalResult() {
        // 다음 화면으로 이동
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