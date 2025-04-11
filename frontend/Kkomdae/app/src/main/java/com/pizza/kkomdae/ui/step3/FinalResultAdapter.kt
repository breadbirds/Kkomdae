package com.pizza.kkomdae.ui.step3

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.pizza.kkomdae.R
import com.pizza.kkomdae.databinding.ItemFinalAiImageBinding
import com.pizza.kkomdae.ui.guide.AllStepOnboardingFragment
import com.pizza.kkomdae.ui.guide.OnboardingPagerAdapter
import com.pizza.kkomdae.ui.step3.FinalResultAdapter.FinalResultViewHolder

class FinalResultAdapter(
    private val context: Context,

    ) : ListAdapter<String,FinalResultViewHolder>(object :
    DiffUtil.ItemCallback<String>(){
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinalResultViewHolder {

        return FinalResultViewHolder(ItemFinalAiImageBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: FinalResultViewHolder, position: Int) {

        holder.bind(position)
    }


    inner class FinalResultViewHolder(val binding: ItemFinalAiImageBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int) {

            Glide.with(binding.ivLoading)
                .asGif()
                .load(R.drawable.skeleton_ui) // 🔁 로딩용 GIF 리소스
                .into(binding.ivLoading)

            Glide.with(binding.ivImage)
                .load(getItem(position))
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        binding.ivImage.setImageDrawable(resource)
                        binding.ivImage.visibility = View.VISIBLE
                        binding.ivLoading.visibility = View.GONE
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })

            binding.tvName.text=when(position){
                1-> "후면부"
                2-> "우측면"
                3-> "좌측면"
                4-> "모니터"
                5-> "키보드"
                else->  "전면부"
            }
        }
    }
}