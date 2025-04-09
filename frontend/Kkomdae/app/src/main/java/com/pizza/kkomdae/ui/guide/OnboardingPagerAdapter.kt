package com.pizza.kkomdae.ui.guide

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pizza.kkomdae.R

/**
 * ViewPager2에서 온보딩 단계를 표시하기 위한 어댑터
 */
class OnboardingPagerAdapter(
    private val context: Context,
    private val onboardingSteps: List<AllStepOnboardingFragment.OnboardingStep>
) : RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.item_onboarding_page,
            parent,
            false
        )
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val step = onboardingSteps[position]
        holder.bind(step)
    }

    override fun getItemCount(): Int = onboardingSteps.size

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStepPrefix: TextView = itemView.findViewById(R.id.tv_step_prefix)
        private val tvStepNumber: TextView = itemView.findViewById(R.id.tv_step_number)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvSubtitleLine1: TextView = itemView.findViewById(R.id.tv_subtitle_line1)
        private val tvSubtitleLine2: TextView = itemView.findViewById(R.id.tv_subtitle_line2)
        private val ivImage: ImageView = itemView.findViewById(R.id.iv_step_image)

        fun bind(step: AllStepOnboardingFragment.OnboardingStep) {
            tvStepPrefix.text = step.title
            tvStepNumber.text = step.stepNumber.toString()
            tvTitle.text = step.mainText
            tvSubtitleLine1.text = step.subTextLine1
            tvSubtitleLine2.text = step.subTextLine2
            ivImage.setImageResource(step.imageResId)
        }
    }
}