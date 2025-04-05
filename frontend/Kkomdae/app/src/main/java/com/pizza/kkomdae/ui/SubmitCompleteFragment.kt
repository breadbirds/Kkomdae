package com.pizza.kkomdae.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentOathBinding
import com.pizza.kkomdae.databinding.FragmentSubmitCompleteBinding
import com.pizza.kkomdae.presenter.viewmodel.FinalViewModel
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SubmitCompleteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubmitCompleteFragment : BaseFragment<FragmentSubmitCompleteBinding>(
    FragmentSubmitCompleteBinding::bind,
    R.layout.fragment_submit_complete
) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val viewModel : MainViewModel by activityViewModels()
    private val finalViewModel : FinalViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 애니메이션
        setupAnimation()

        // 버튼 클릭 이벤트 설정
        setupButtonListeners()

        // pdf 다운로드 버튼
        binding.btnDownloadPdf.setOnClickListener {
            Log.d("TAG", "onViewCreated: ${finalViewModel.pdfName}")
            finalViewModel.pdfName.value?.let {
                Toast.makeText(requireContext(),"파일이 다운로드 중입니다",Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    val result = finalViewModel.getPdfUrl(it)
                    result.onSuccess {
                        viewModel.downloadPdf(it.url)
                    }.onFailure {
                        // todo 에러 다이얼 로그 띄우기
                    }
                }

            }

        }

        finalViewModel.pdfUrl.observe(viewLifecycleOwner){
            viewModel.downloadPdf(it)
        }
    }

    private fun setupAnimation() {
        binding.animationView.setAnimation(R.raw.final_confetti)
        binding.animationView.playAnimation()
    }

    private fun setupButtonListeners() {
        // 닫기 버튼
        binding.btnClose.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        // PDF 다운로드 버튼
        binding.btnDownloadPdf.setOnClickListener {
            // PDF 다운로드 로직 구현
        }

        // 공유하기 버튼
        binding.btnShare.setOnClickListener {
            // 공유 로직 구현
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearBinding()
        finalViewModel.clearPostPdfName()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SubmitCompleteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SubmitCompleteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}