package com.pizza.kkomdae.ui.step4

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.pizza.kkomdae.R
import com.pizza.kkomdae.databinding.ItemStep1ResultBinding
import com.pizza.kkomdae.presenter.model.Step4AiResult

class Step4AiResultAdapter(val list: List<Step4AiResult>, val listen:(Int)->Unit): RecyclerView.Adapter<Step4AiResultAdapter.Step1ResultViewHolder>() {
    private var selectedPosition: Int = 0

    inner class Step1ResultViewHolder(val binding: ItemStep1ResultBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int){
            Glide.with(binding.ivPosition)
                .load(list[position].image)
                .into(binding.ivPosition)

            if (position == selectedPosition) {
                binding.root.scaleX = 1.2f  // 가로 크기 증가
                binding.root.scaleY = 1.2f  // 세로 크기 증가
                (binding.root as MaterialCardView).setStrokeColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.blue500
                    )
                )

            } else {
                binding.root.scaleX = 1.0f
                binding.root.scaleY = 1.0f
                (binding.root as MaterialCardView).setStrokeColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.gray200
                    )
                )
            }


            binding.tvPosition.text= list[position].name
            binding.root.setOnClickListener {
                listen(position)
                val previousPosition = selectedPosition
                selectedPosition = position  // 현재 선택한 아이템 저장

                notifyItemChanged(previousPosition) // 이전 선택 해제
                notifyItemChanged(selectedPosition) // 현재 선택 업데이트

                listen(position)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Step1ResultViewHolder {
        return Step1ResultViewHolder(
            ItemStep1ResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: Step1ResultViewHolder, position: Int) {

        holder.bind(position)
    }
}