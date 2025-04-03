package com.pizza.kkomdae.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pizza.kkomdae.presenter.model.Submission
import com.pizza.kkomdae.databinding.ItemSubmissionBinding
import com.pizza.kkomdae.presenter.model.UserRentTestResponse


class SubmissionAdapter(val clickPdf:(String)->Unit): ListAdapter<UserRentTestResponse, SubmissionAdapter.SubmissionViewHolder>(object : DiffUtil.ItemCallback<UserRentTestResponse>(){
    override fun areContentsTheSame(oldItem: UserRentTestResponse, newItem: UserRentTestResponse): Boolean {
        return oldItem==newItem
    }

    override fun areItemsTheSame(oldItem: UserRentTestResponse, newItem: UserRentTestResponse): Boolean {
        return oldItem === newItem
    }
}){


    inner class SubmissionViewHolder(val binding: ItemSubmissionBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int){
            binding.btnDown.setOnClickListener {
                binding.tvState.isVisible = false
                binding.clMenu.isVisible=true
                binding.btnUp.isVisible=true
                binding.btnDown.isVisible=false
            }

            binding.btnUp.setOnClickListener {
                binding.tvState.isVisible = true
                binding.clMenu.isVisible=false
                binding.btnUp.isVisible=false
                binding.btnDown.isVisible=true
            }

            binding.btFileDownload.setOnClickListener {
                getItem(position).rentPdfName?.let {
                    clickPdf(it)
                }

            }

            binding.tvModelNumber.text= getItem(position).modelCode
            binding.tvInputDate.text = getItem(position).dateTime

        }
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        return SubmissionViewHolder(ItemSubmissionBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
}
