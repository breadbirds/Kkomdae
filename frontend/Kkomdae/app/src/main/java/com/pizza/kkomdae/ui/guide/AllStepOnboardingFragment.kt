package com.pizza.kkomdae.ui.guide

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentAllStepOnboardingBinding
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel
import com.pizza.kkomdae.ui.OathFragment
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AllStepOnboardingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "AllStepOnboardingFragme"
@AndroidEntryPoint
class AllStepOnboardingFragment : BaseFragment<FragmentAllStepOnboardingBinding>(
    FragmentAllStepOnboardingBinding::bind,
    R.layout.fragment_all_step_onboarding
) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val viewModel : MainViewModel by viewModels()

    data class OnboardingStep(
        val title: String,
        val stepNumber: Int,
        val mainText: String,
        val subTextLine1: String,
        val subTextLine2: String,
        val imageResId: Int
    )

    private val onboardingSteps = listOf(
        OnboardingStep(
            "STEP",
            1,
            "노트북 외관촬영",
            "노트북의 기기 상태를 기록하기 위해",
            "총 6방향에서 촬영해주세요",
            R.drawable.onboarding_step1
        ),
        OnboardingStep(
            "STEP",
            2,
            "노트북 자가진단",
            "노트북의 주요 기능을 테스트하여",
            "올바르게 작동하는지 확인해주세요",
            R.drawable.onboarding_step2
        ),
        OnboardingStep(
            "STEP",
            3,
            "노트북 정보 입력",
            "마지막 단계입니다!",
            "정확한 정보를 입력하여 등록을 완료해주세요",
            R.drawable.onboarding_step3
        )
    )

    private lateinit var indicatorDots: Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()

        setupViewPager()

        // 제목
        val topBarTitle = view.findViewById<View>(R.id.top_bar).findViewById<TextView>(R.id.tv_title)
        topBarTitle.text = "기기등록 가이드"

        // 뒤로가기 버튼
        binding.topBar.backButtonContainer.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // 등록 시작하기
        binding.btnFinish.setOnClickListener {
            Log.d(TAG, "onViewCreated: asdafdsf ")
            viewModel.postTest(null)
            
        }
        
        viewModel.testId.observe(viewLifecycleOwner){
            Log.d(TAG, "onViewCreated: $it")
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fl_main, Step1GuideFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun setupViews() {
        // 인디케이터 초기화
        indicatorDots = arrayOf(
            binding.indicator1,
            binding.indicator2,
            binding.indicator3
        )

        // 초기 상태 설정 (첫 번째 페이지)
        updateIndicators(0)
    }

    private fun setupViewPager() {
        // 커스텀 OnboardingPagerAdapter 생성
        val pagerAdapter = OnboardingPagerAdapter(requireContext(), onboardingSteps)
        binding.viewPager.adapter = pagerAdapter

        // ViewPager 페이지 변경 리스너 설정
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)

                // 마지막 페이지에서만 완료 버튼 표시
                binding.btnFinish.visibility = if (position == onboardingSteps.size - 1) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            }
        })
    }

    private fun updateIndicators(position: Int) {
        for (i in indicatorDots.indices) {
            if (i == position) {
                // 현재 페이지 인디케이터 활성화
                indicatorDots[i].setImageResource(R.drawable.indicator_active)
                val layoutParams = indicatorDots[i].layoutParams
                layoutParams.width = (22 * resources.displayMetrics.density).toInt()
                layoutParams.height = (10 * resources.displayMetrics.density).toInt()
                indicatorDots[i].layoutParams = layoutParams
            } else {
                // 다른 페이지 인디케이터 비활성화
                indicatorDots[i].setImageResource(R.drawable.indicator_inactive)
                val layoutParams = indicatorDots[i].layoutParams
                layoutParams.width = (10 * resources.displayMetrics.density).toInt()
                layoutParams.height = (10 * resources.displayMetrics.density).toInt()
                indicatorDots[i].layoutParams = layoutParams
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AllStepOnboardingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AllStepOnboardingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        @JvmStatic
        fun newInstance() = AllStepOnboardingFragment()
    }
}