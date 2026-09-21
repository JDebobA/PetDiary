package com.example.petdiary.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DiaryEntryDao {

    @Insert
    suspend fun insert(entry: DiaryEntry): Long

    @Update
    suspend fun update(entry: DiaryEntry)

    @Delete
    suspend fun delete(entry: DiaryEntry)

    @Query("SELECT * FROM diary_entries WHERE petId = :petId ORDER BY date DESC")
    fun getEntriesForPet(petId: Long): LiveData<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE petId = :petId AND date = :date ORDER BY date DESC")
    fun getEntriesForPetAndDate(petId: Long, date: String): LiveData<List<DiaryEntry>>

    // Одноразовая (не LiveData) выборка - нужна для подсчёта статистики
    // и напоминаний в TrackingRepository / ReminderRepository.
    @Query("SELECT * FROM diary_entries WHERE petId = :petId")
    suspend fun getEntriesForPetSync(petId: Long): List<DiaryEntry>
}
