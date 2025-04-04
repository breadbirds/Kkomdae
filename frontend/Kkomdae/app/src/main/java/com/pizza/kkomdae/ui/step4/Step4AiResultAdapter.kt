package com.pizza.kkomdae.ui.step4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.pizza.kkomdae.R
import com.pizza.kkomdae.databinding.ItemStep1ResultBinding
import com.pizza.kkomdae.presenter.model.Step4AiResult
import com.pizza.kkomdae.presenter.viewmodel.FinalViewModel

class Step4AiResultAdapter(
    val list: List<Step4AiResult>,
    val listen: (Int) -> Unit,
    private val viewModel: FinalViewModel
): RecyclerView.Adapter<Step4AiResultAdapter.Step1ResultViewHolder>() {
    private var selectedPosition: Int = 0

    private val showPositions = mutableSetOf<Int>() // ✅ 숨긴 아이템을 저장하는 Set

    inner class Step1ResultViewHolder(val binding: ItemStep1ResultBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int){
            Glide.with(binding.ivPosition)
                .load(list[position].image)
                .into(binding.ivPosition)


            val damageCount = when(position) {
                0 -> viewModel.frontDamage.value ?: 0
                1 -> viewModel.backDamage.value ?: 0
                2 -> viewModel.leftDamage.value ?: 0
                3 -> viewModel.rightDamage.value ?: 0
                4 -> viewModel.screenDamage.value ?: 0
                5 -> viewModel.keyboardDamage.value ?: 0
                else -> 0
            }


            // 선택된 아이템 처리
            if (position == selectedPosition) {
                // 선택된 아이템은 결함 유무에 따라 다른 색상 적용하고, 배지는 숨김
                binding.tvDamageBadge.visibility = View.GONE

                if (damageCount > 0) {
                    // 선택됨 + 결함 있음 = 빨간색
                    binding.root.strokeColor =
                        ContextCompat.getColor(binding.root.context, R.color.error)
                    binding.tvPosition.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.error)
                    )
                } else {
                    // 선택됨 + 결함 없음 = 파란색
                    binding.root.strokeColor =
                        ContextCompat.getColor(binding.root.context, R.color.blue500)
                    binding.tvPosition.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.blue500)
                    )
                }
            } else {
                // 선택되지 않은 아이템
                if (damageCount > 0) {
                    // 선택 안됨 + 결함 있음 = 회색 + 배지 표시
                    binding.tvDamageBadge.visibility = View.VISIBLE
                    binding.tvDamageBadge.text = damageCount.toString()
                    binding.root.strokeColor =
                        ContextCompat.getColor(binding.root.context, R.color.gray200)
                    binding.tvPosition.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.gray500)
                    )
                } else {
                    // 선택 안됨 + 결함 없음 = 회색
                    binding.tvDamageBadge.visibility = View.GONE
                    binding.root.strokeColor =
                        ContextCompat.getColor(binding.root.context, R.color.gray200)
                    binding.tvPosition.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.gray500)
                    )
                }
            }


            // ✅ 특정 인덱스의 `TextView` 숨기기 / 보이기
            if (showPositions.contains(position)) {
                binding.clLoading.visibility = View.VISIBLE  // 👀 텍스트 숨김
            } else {
                binding.clLoading.visibility = View.GONE  // 👀 텍스트 보이기
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

    // ✅ 특정 인덱스의 `TextView`를 숨기는 함수 (다시 보이게 하지 않음)
    fun showTextAt(index: Int) {
        if (!showPositions.contains(index)) { // 이미 숨긴 경우 다시 숨기지 않음
            showPositions.add(index)
            notifyItemChanged(index) // 해당 아이템만 업데이트
        }
    }

    // ✅ 특정 인덱스의 `TextView`를 다시 보이게 하는 함수 (VISIBLE)
    fun hideTextAt(index: Int) {
        if (showPositions.contains(index)) { // 숨겨진 경우에만 다시 보이게 함
            showPositions.remove(index)
            notifyItemChanged(index) // 해당 아이템만 업데이트
        }
    }
}