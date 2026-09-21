package com.example.petdiary.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petdiary.data.DiaryEntry
import com.example.petdiary.databinding.ItemDiaryEntryBinding
import com.example.petdiary.data.EventType

class DiaryAdapter(
    private val onEditClick: (DiaryEntry) -> Unit,
    private val onDeleteClick: (DiaryEntry) -> Unit
) : ListAdapter<DiaryEntry, DiaryAdapter.EntryViewHolder>(DiaryDiffCallback()) {

    inner class EntryViewHolder(val binding: ItemDiaryEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemDiaryEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = getItem(position)
        holder.binding.textDate.text = entry.date
        holder.binding.textTitle.text = entry.title
        holder.binding.textDescription.text = entry.description
        holder.binding.textWeight.text = entry.weight?.let { "Вес: $it кг" } ?: ""
        holder.binding.buttonEdit.setOnClickListener { onEditClick(entry) }
        holder.binding.buttonDelete.setOnClickListener { onDeleteClick(entry) }
        val type = runCatching { EventType.valueOf(entry.eventType) }.getOrNull() ?: EventType.OTHER
        holder.binding.textEventType.text = type.displayName()

        holder.binding.buttonEdit.setOnClickListener { onEditClick(entry) }
        holder.binding.buttonDelete.setOnClickListener { onDeleteClick(entry) }

        val icon = when (type) {
            EventType.FEEDING -> "🍗"
            EventType.WALK -> "🦮"
            EventType.MEDICINE -> "💉"
            EventType.CHECKUP -> "🩺"
            else -> "📃"
        }
        holder.binding.textEventTypeIcon.text = icon
    }

    class DiaryDiffCallback : DiffUtil.ItemCallback<DiaryEntry>() {
        override fun areItemsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry) = oldItem == newItem
    }
}
