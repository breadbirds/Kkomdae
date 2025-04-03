package com.pizza.kkomdae.ui.step1

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.pizza.kkomdae.AppData
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentFontResultBinding
import com.pizza.kkomdae.databinding.FragmentImageDetailBinding
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel
import com.pizza.kkomdae.ui.guide.AllStepOnboardingFragment.OnboardingStep
import com.pizza.kkomdae.ui.guide.Step2GuideFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ImageDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ImageDetailFragment :  BaseFragment<FragmentImageDetailBinding>(
    FragmentImageDetailBinding::bind,
    R.layout.fragment_image_detail
) {
    // TODO: Rename and change types of parameters
    private var param1: Int? = null
    private var param2: String? = null
    private val viewModel : MainViewModel by activityViewModels()

    data class ImageDetailStep(
        val title: String,
        val stepNumber: Int,
        val url: String?=null,
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageList = viewModel.resultImage.value

       val list = listOf(
            ImageDetailStep(
                "전면부",
                1,
                imageList?.get(0),
            ),
            ImageDetailStep(
                "후면부",
                2,
                imageList?.get(1),
            ),
            ImageDetailStep(
                "좌측면",
                3,
                imageList?.get(2),
            ),ImageDetailStep(
                "우측면",
                4,
                imageList?.get(3),
            ),ImageDetailStep(
                "모니터",
                5,
                imageList?.get(4),
            ),ImageDetailStep(
                "키보드",
                6,
                imageList?.get(5),
            ),
        )

        binding.viewPager.adapter=ImageDetailAdapter(list)
        binding.viewPager.setCurrentItem((param1?:1)-1,false)

        binding.btnCancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()

        }

        // 테이블 레이아웃 설정
        TabLayoutMediator(
            binding.tlStep,
            binding.viewPager,
        ) { tab, position ->
            binding.viewPager.currentItem = tab.position
        }.attach()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ImageDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int, param2: String) =
            ImageDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}