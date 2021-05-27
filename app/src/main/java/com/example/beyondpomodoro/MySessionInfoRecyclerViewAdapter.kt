package com.example.beyondpomodoro

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beyondpomodoro.databinding.FragmentSessionInfoBinding
import com.example.beyondpomodoro.placeholder.SessionType.SessionType

/**
 * [RecyclerView.Adapter] that can display a [SessionType].
 * TODO: Replace the implementation with code for your data type.
 */
class MySessionInfoRecyclerViewAdapter(
    private val values: List<SessionType>
) : RecyclerView.Adapter<MySessionInfoRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentSessionInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.sessionInfoTitleEditText.setText(item.title)
        holder.sessionInfoSessionTimeView.text = item.onTime.toString()
        holder.sessionInfoBreakTimeView.text = item.offTime.toString()
        holder.tagsTextView.text = item.tags
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentSessionInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tagsTextView: TextView = binding.tagsTextView
        val sessionInfoBreakTimeView: TextView = binding.textViewSessionInfoBreakTime
        val sessionInfoSessionTimeView: TextView = binding.textViewSessionInfoPomodoroTime
        val sessionInfoTitleEditText: EditText = binding.editTextSessionInfoTitle

        override fun toString(): String {
            return super.toString() + " ' ${sessionInfoTitleEditText.toString()}' ${sessionInfoSessionTimeView.toString()} ' ${sessionInfoBreakTimeView.toString()} ' ${tagsTextView.toString()}"
        }
    }
}