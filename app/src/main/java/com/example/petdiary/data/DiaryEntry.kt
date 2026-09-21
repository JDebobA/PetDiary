package com.example.petdiary.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Запись дневника, привязанная к конкретному питомцу (внешний ключ petId).
 * При удалении питомца все его записи удаляются каскадно.
 */
@Entity(
    tableName = "diary_entries",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("petId")]
)
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val date: String,
    val title: String,
    val description: String,
    val weight: Double? = null,
    // Хранит имя константы EventType (FEEDING, WALK, MEDICINE, CHECKUP, OTHER).
    // По умолчанию OTHER, чтобы старый код (без указания типа) не сломался.
    val eventType: String = EventType.OTHER.name
)
