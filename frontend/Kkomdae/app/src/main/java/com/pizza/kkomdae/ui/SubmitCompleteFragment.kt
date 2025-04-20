package com.pizza.kkomdae.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import com.pizza.kkomdae.MainActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentOathBinding
import com.pizza.kkomdae.databinding.FragmentSubmitCompleteBinding
import com.pizza.kkomdae.presenter.viewmodel.FinalViewModel
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel
import com.pizza.kkomdae.ui.step3.FinalResultFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

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

    // 시스템 백 버튼 콜백 선언
    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)


        }

        finalViewModel.pdfName.value?.let {
            lifecycleScope.launch {
                val result = finalViewModel.getPdfUrl(it)
                result.onSuccess {
                    viewModel.setPdfUrl(it.url)
                }.onFailure {
                    // todo 에러 다이얼 로그 띄우기
                }
            }
        }

        // 시스템 백 버튼 동작 설정
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 애니메이션
        setupAnimation()

        // 버튼 클릭 이벤트 설정
        setupButtonListeners()

        // pdf 다운로드 버튼
        binding.btnDownloadPdf.setOnClickListener {
            val url = viewModel.getPdfUrl()
            if(url!="") {
                Toast.makeText(requireContext(),"파일이 다운로드 중입니다",Toast.LENGTH_SHORT).show()
                        viewModel.downloadPdf(url)
            }else{
                Toast.makeText(requireContext(),"pdf 생성 중",Toast.LENGTH_SHORT).show()
            }

        }



        binding.btnShare.setOnClickListener {
            val url = viewModel.getPdfUrl()

            if (url != "") {
                lifecycleScope.launch {
                    try {
                        val pdfFile = downloadPdf(
                            requireContext(),
                            url.substringBefore("?")
                        )

                        val uri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.fileprovider",
                            pdfFile
                        )

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        startActivity(Intent.createChooser(shareIntent, "PDF 공유하기"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "PDF 공유 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "PDF 생성 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        
    }

    suspend fun downloadPdf(context: Context, url: String): File = withContext(Dispatchers.IO) {
        val fileName = "shared_pdf.pdf"
        val file = File(context.cacheDir, fileName)

        val urlConnection = URL(url).openConnection()
        urlConnection.getInputStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return@withContext file
    }


    private fun setupAnimation() {
        binding.animationView.setAnimation(R.raw.final_confetti)
        binding.animationView.playAnimation()
    }

    private fun setupButtonListeners() {
        // 닫기 버튼
        binding.btnClose.setOnClickListener {
            goHome()
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

    private fun goHome() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_main, MainFragment())
        transaction.disallowAddToBackStack()
        transaction.commit()
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