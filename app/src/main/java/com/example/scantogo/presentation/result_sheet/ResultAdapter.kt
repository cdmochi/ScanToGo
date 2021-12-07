package com.example.scantogo.presentation.result_sheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scantogo.databinding.ViewResultItemBinding

class ResultAdapter(private val contents: List<String>,
                    private val onCopyListener: (String) -> Unit): RecyclerView.Adapter<ResultAdapter.ResultItemViewHolder>() {

    class ResultItemViewHolder(private val binding: ViewResultItemBinding,
                               private val onCopyListener: (String) -> Unit): RecyclerView.ViewHolder(binding.root) {
        fun bind(content: String) {
            binding.tvContent.text = content
            binding.root.setOnLongClickListener { view ->
                if (view.isPressed) {
                    onCopyListener(content)
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultItemViewHolder = ResultItemViewHolder(
        ViewResultItemBinding.inflate(LayoutInflater.from(parent.context)),
        onCopyListener
    )

    override fun onBindViewHolder(holder: ResultItemViewHolder, position: Int) {
        holder.bind(contents[position])
    }

    override fun getItemCount(): Int = contents.size
}