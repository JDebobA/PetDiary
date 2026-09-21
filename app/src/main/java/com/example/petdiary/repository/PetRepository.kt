package com.example.petdiary.repository

import androidx.lifecycle.LiveData
import com.example.petdiary.data.DiaryEntry
import com.example.petdiary.data.DiaryEntryDao
import com.example.petdiary.data.Pet
import com.example.petdiary.data.PetDao

class PetRepository(
    private val petDao: PetDao,
    private val diaryEntryDao: DiaryEntryDao
) {

    val allPets: LiveData<List<Pet>> = petDao.getAllPets()

    suspend fun insertPet(pet: Pet): Long = petDao.insert(pet)
    suspend fun updatePet(pet: Pet) = petDao.update(pet)
    suspend fun deletePet(pet: Pet) = petDao.delete(pet)
    suspend fun getPetById(id: Long): Pet? = petDao.getPetById(id)

    fun getEntriesForPet(petId: Long): LiveData<List<DiaryEntry>> =
        diaryEntryDao.getEntriesForPet(petId)

    suspend fun insertEntry(entry: DiaryEntry): Long = diaryEntryDao.insert(entry)
    suspend fun updateEntry(entry: DiaryEntry) = diaryEntryDao.update(entry)
    suspend fun deleteEntry(entry: DiaryEntry) = diaryEntryDao.delete(entry)
}
