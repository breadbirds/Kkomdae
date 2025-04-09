package com.pizza.kkomdae.ui.step1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pizza.kkomdae.R
import com.pizza.kkomdae.databinding.FragmentImageDetailBinding
import com.pizza.kkomdae.databinding.ItemImageDetailBinding
import com.pizza.kkomdae.databinding.ItemStep1ResultBinding
import com.pizza.kkomdae.ui.guide.AllStepOnboardingFragment
import com.pizza.kkomdae.ui.guide.OnboardingPagerAdapter

class ImageDetailAdapter(
    private val list: List<ImageDetailFragment.ImageDetailStep>
) : RecyclerView.Adapter<ImageDetailAdapter.ImageDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageDetailViewHolder {

        return ImageDetailViewHolder(ItemImageDetailBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    }

    override fun onBindViewHolder(holder: ImageDetailViewHolder, position: Int) {

        holder.bind(position)
    }

    override fun getItemCount(): Int = list.size

    inner class ImageDetailViewHolder(val binding: ItemImageDetailBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int ) {
            Glide.with(binding.pvImage)
                .load(list[position].url)
                .into(binding.pvImage)

            binding.tvName.text=list[position].title

        }
    }
}