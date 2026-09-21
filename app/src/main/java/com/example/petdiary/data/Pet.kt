package com.example.petdiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Питомец. Хранится в таблице "pets".
 */
@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val notes: String
)
