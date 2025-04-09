package com.pizza.kkomdae.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pizza.kkomdae.MainActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.presenter.model.Submission
import com.pizza.kkomdae.databinding.FragmentMainBinding
import com.pizza.kkomdae.presenter.viewmodel.FinalViewModel
import com.pizza.kkomdae.presenter.viewmodel.LoginViewModel
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel
import com.pizza.kkomdae.ui.guide.Step1GuideFragment
import com.pizza.kkomdae.ui.guide.Step2GuideFragment
import com.pizza.kkomdae.ui.step3.FinalResultFragment
import com.pizza.kkomdae.ui.step3.LaptopInfoInputFragment
import com.pizza.kkomdae.ui.step4.Step4AiResultFragment
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment :  BaseFragment<FragmentMainBinding>(
    FragmentMainBinding::bind,
    R.layout.fragment_main
)
{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var step =0 // 기기등록 단계

    private lateinit var mainActivity: MainActivity
    private val viewModel : MainViewModel by activityViewModels()
    private val finalViewModel : FinalViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserInfo()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SubmissionAdapter(clickPdf = {
            Toast.makeText(requireContext(),"pdf 파일이 다운로드 중입니다.",Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                val result = finalViewModel.getPdfUrl(it)
                result.onSuccess {
                    viewModel.downloadPdf(it.url)
                }.onFailure {
                    // todo 에러 다이얼 로그 띄우기
                }
            }

        }, clickRelease = {

            if(it.stage!=0 || it.picStage !=0){
                showContinueDialog()
                viewModel.saveTestId(it.onGoingTestId.toLong()) // 테스트 아이디
                viewModel.setRelease(true) // 반납인지 체크
                viewModel.setReleasePicStage(it.picStage) // 1단계 스테이지 저장

                step=it.stage
                Log.d("Post", "onViewCreated: $step")
            }else{
                newRelease(it.rentId)
                viewModel.savePhotoStage(0)
            }


        })

        binding.rvSubmission.adapter =adapter
        binding.rvSubmission.layoutManager = LinearLayoutManager(mainActivity)


        binding.btnLogout.setOnClickListener {
            mainActivity.logout()
        }
        viewModel.Fail.observe(viewLifecycleOwner){
            mainActivity.performLogout()

        }

        // 서버에서 받아온 유저 정보
        viewModel.userInfoResult.observe(viewLifecycleOwner) { userInfo ->
            Log.d("Post", "onViewCreated: ${userInfo.userRentTestRes}")
            step = userInfo.stage
            binding.tvWelcomeMessage.text = "${userInfo.name}님 안녕하세요!"

            // 필터링된 제출 파일 목록
            val filteredList = userInfo.userRentTestRes.filter { item ->
                item.rentPdfName != null
            }.reversed()

            // 목록이 비어있으면 "파일 없음" 메시지와 이미지 표시
            if (filteredList.isEmpty()) {
                binding.layoutNoFile.visibility = View.VISIBLE
                binding.rvSubmission.visibility = View.GONE
            } else {
                binding.layoutNoFile.visibility = View.GONE
                binding.rvSubmission.visibility = View.VISIBLE
                adapter.submitList(filteredList)
            }

            // 진행중인 과정이 있는지 확인하고 표시
            updateInProgressIndicator()
        }

        // 노트북 카드뷰 클릭 이벤트
        binding.cvLaptop.setOnClickListener {

            step= viewModel.stage.value?:0
            viewModel.setRelease(false)
            if (step == 0) {
                navigateToFragment(OathFragment())
            } else {
                showContinueDialog()
            }
        }

        // 모바일 카드뷰 클릭 이벤트
        binding.cvMobile.setOnClickListener {
            showDevelopingDialog()
        }

        // IoT 카드뷰 클릭 이벤트
        binding.cvIot.setOnClickListener {
            showDevelopingDialog()
        }
    }

    // 반납 처음 시작할때 testId 생성
    private fun newRelease(it: Int) {
        lifecycleScope.launch {
            val response = viewModel.postReleaseTest(it)
            response.onSuccess {
                viewModel.setRelease(true)
                viewModel.saveTestId(it)
                navigateToFragment(Step1GuideFragment())
            }.onFailure {

            }
        }
    }

    private fun updateInProgressIndicator() {
        val inProgressView = binding.cvLaptop.findViewById<TextView>(R.id.tv_in_progress)

        // step이 0이 아니면 "진행중" 표시
        if (step > 0) {
            inProgressView.visibility = View.VISIBLE
        } else {
            inProgressView.visibility = View.GONE
        }
    }

    // 임시저장 다이얼로그 표시
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showContinueDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_temp)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 닫기 버튼
        val cancelButton = dialog.findViewById<TextView>(R.id.btn_cancel)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // 이어하기 버튼
        val confirmButton = dialog.findViewById<TextView>(R.id.btn_confirm)
        confirmButton.setOnClickListener {

            dialog.dismiss()
            Log.d("Post", "showContinueDialog: $step")

            // 현재 step에 맞는 화면으로 이동
            when(step) {
                1 -> navigateToFragment(Step1GuideFragment())
                2 -> navigateToFragment(Step2GuideFragment())
                3 -> navigateToFragment(LaptopInfoInputFragment())
                4 -> navigateToFragment(LoadingFragment())
                5 -> navigateToFragment(FinalResultFragment())
            }
        }

        dialog.show()
    }

    // 프래그먼트 전환을 위한 헬퍼 함수
    private fun navigateToFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_main, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    // 개발 중 다이얼로그
    private fun showDevelopingDialog() {

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_mobile_iot)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val confirmButton = dialog.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_confirm)
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
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}