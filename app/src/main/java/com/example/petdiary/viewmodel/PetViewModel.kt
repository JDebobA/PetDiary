package com.example.petdiary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petdiary.data.DiaryEntry
import com.example.petdiary.data.Pet
import com.example.petdiary.repository.PetRepository
import kotlinx.coroutines.launch

class PetViewModel(private val repository: PetRepository) : ViewModel() {

    val allPets: LiveData<List<Pet>> = repository.allPets

    fun insertPet(pet: Pet) = viewModelScope.launch { repository.insertPet(pet) }
    fun updatePet(pet: Pet) = viewModelScope.launch { repository.updatePet(pet) }
    fun deletePet(pet: Pet) = viewModelScope.launch { repository.deletePet(pet) }

    fun getEntriesForPet(petId: Long): LiveData<List<DiaryEntry>> =
        repository.getEntriesForPet(petId)

    fun insertEntry(entry: DiaryEntry) = viewModelScope.launch { repository.insertEntry(entry) }
    fun updateEntry(entry: DiaryEntry) = viewModelScope.launch { repository.updateEntry(entry) }
    fun deleteEntry(entry: DiaryEntry) = viewModelScope.launch { repository.deleteEntry(entry) }
}
