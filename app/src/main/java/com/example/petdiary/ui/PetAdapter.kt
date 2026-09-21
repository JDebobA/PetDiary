package com.example.petdiary.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petdiary.data.Pet
import com.example.petdiary.databinding.ItemPetBinding

class PetAdapter(
    private val onItemClick: (Pet) -> Unit,
    private val onEditClick: (Pet) -> Unit,
    private val onDeleteClick: (Pet) -> Unit
) : ListAdapter<Pet, PetAdapter.PetViewHolder>(PetDiffCallback()) {

    inner class PetViewHolder(val binding: ItemPetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = getItem(position)
        holder.binding.textPetName.text = pet.name
        holder.binding.textPetSpecies.text = "${pet.species} • ${pet.breed}"
        holder.binding.root.setOnClickListener { onItemClick(pet) }
        holder.binding.buttonEdit.setOnClickListener { onEditClick(pet) }
        holder.binding.buttonDelete.setOnClickListener { onDeleteClick(pet) }
    }

    class PetDiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Pet, newItem: Pet) = oldItem == newItem
    }
}
