package com.example.petdiary.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PetDao {

    @Insert
    suspend fun insert(pet: Pet): Long

    @Update
    suspend fun update(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)

    @Query("SELECT * FROM pets ORDER BY name ASC")
    fun getAllPets(): LiveData<List<Pet>>

    // Одноразовая (не LiveData) выборка всех питомцев - нужна для
    // ReminderRepository, которому не нужна подписка на изменения.
    @Query("SELECT * FROM pets ORDER BY name ASC")
    suspend fun getAllPetsSync(): List<Pet>

    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetById(petId: Long): Pet?
}
