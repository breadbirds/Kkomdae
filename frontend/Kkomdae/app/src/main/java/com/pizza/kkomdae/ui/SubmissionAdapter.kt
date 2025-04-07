package com.pizza.kkomdae.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pizza.kkomdae.presenter.model.Submission
import com.pizza.kkomdae.databinding.ItemSubmissionBinding
import com.pizza.kkomdae.presenter.model.UserRentTestResponse


class SubmissionAdapter(val clickRelease:(UserRentTestResponse)->Unit, val clickPdf:(String)->Unit): ListAdapter<UserRentTestResponse, SubmissionAdapter.SubmissionViewHolder>(object : DiffUtil.ItemCallback<UserRentTestResponse>(){
    override fun areContentsTheSame(oldItem: UserRentTestResponse, newItem: UserRentTestResponse): Boolean {
        return oldItem==newItem
    }

    override fun areItemsTheSame(oldItem: UserRentTestResponse, newItem: UserRentTestResponse): Boolean {
        return oldItem === newItem
    }
}){



    inner class SubmissionViewHolder(val binding: ItemSubmissionBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int){
            Log.d("TAG", "bind: $position")
            binding.btReturnDownload.isVisible=false
            binding.btReturn.isVisible=false
            binding.clMenu.isVisible=false
            binding.btnUp.isVisible=false
            binding.btnDown.isVisible=true
            if(getItem(position).releasePdfName!=null){
                binding.tvStateRelease.isVisible=true
                binding.tvStateRant.isVisible=false
                binding.btReturnDownload.isVisible=true
                binding.btReturn.isVisible=false
            }else{
                binding.tvStateRelease.isVisible=false
                binding.tvStateRant.isVisible=true
                binding.btReturnDownload.isVisible=false
                binding.btReturn.isVisible=true
            }
            binding.btnDown.setOnClickListener {
                binding.tvStateRant.isVisible = false
                binding.tvStateRelease.isVisible=false
                binding.clMenu.isVisible=true
                binding.btnUp.isVisible=true
                binding.btnDown.isVisible=false

                if(getItem(position).releasePdfName!=null){
                    binding.btReturnDownload.isVisible=true
                    binding.btReturn.isVisible=false

                }else{
                    binding.btReturnDownload.isVisible=false
                    binding.btReturn.isVisible=true
                }
            }

            binding.btnUp.setOnClickListener {
                if(getItem(position).releasePdfName!=null){
                    binding.tvStateRelease.isVisible=true
                    binding.tvStateRant.isVisible=false
                }else{
                    binding.tvStateRelease.isVisible=false
                    binding.tvStateRant.isVisible=true
                }
                binding.btReturnDownload.isVisible=false
                binding.btReturn.isVisible=false
                binding.clMenu.isVisible=false
                binding.btnUp.isVisible=false
                binding.btnDown.isVisible=true
            }

            // 대여 pdf 다운로드
            binding.btFileDownload.setOnClickListener {
                getItem(position).rentPdfName?.let {
                    clickPdf(it)
                }

            }

            binding.btReturnDownload.setOnClickListener {
                getItem(position).releasePdfName?.let {
                    clickPdf(it)
                }
            }

            binding.btReturn.setOnClickListener {

                    clickRelease(getItem(position))


            }

            val regex = "\\(.*?\\)".toRegex()
            val match = regex.find(getItem(position).modelCode)
            val withParentheses = match?.value

            binding.tvModelNumber.text= getItem(position).serialNum + withParentheses
            binding.tvInputDate.text = getItem(position).dateTime?.substring(2)?.replace("-", "/")

        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        return SubmissionViewHolder(ItemSubmissionBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
}
