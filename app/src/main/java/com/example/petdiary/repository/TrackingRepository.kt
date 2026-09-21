package com.example.petdiary.repository

import com.example.petdiary.data.DiaryEntry
import com.example.petdiary.data.DiaryEntryDao
import com.example.petdiary.data.EventType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Функции отслеживания/статистики по питомцу.
 * Соответствуют блоку "Статистика за неделю" на экране профиля питомца
 * (Кормлений: 21, Прогулок: 7, Лекарств: 3).
 */
class TrackingRepository(private val diaryEntryDao: DiaryEntryDao) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private fun parseDate(dateStr: String): Date? = try {
        dateFormat.parse(dateStr)
    } catch (e: Exception) {
        null
    }

    /**
     * Статистика за последние 7 дней: сколько событий каждого типа.
     */
    suspend fun getWeeklyStats(petId: Long): Map<EventType, Int> {
        val entries = diaryEntryDao.getEntriesForPetSync(petId)

        val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
        val today = Date()

        val stats = mutableMapOf(
            EventType.FEEDING to 0,
            EventType.WALK to 0,
            EventType.MEDICINE to 0,
            EventType.CHECKUP to 0
        )

        for (entry in entries) {
            val date = parseDate(entry.date) ?: continue
            if (!date.before(weekAgo) && !date.after(today)) {
                val type = runCatching { EventType.valueOf(entry.eventType) }.getOrNull()
                if (type != null && stats.containsKey(type)) {
                    stats[type] = stats.getValue(type) + 1
                }
            }
        }
        return stats
    }

    /**
     * Общее количество событий заданного типа за всё время (не только за неделю).
     */
    suspend fun getEventCountByType(petId: Long, type: EventType): Int {
        return diaryEntryDao.getEntriesForPetSync(petId).count { it.eventType == type.name }
    }

    /**
     * Последнее по дате событие заданного типа
     * (например, когда был последний осмотр у ветеринара).
     */
    suspend fun getLastEventOfType(petId: Long, type: EventType): DiaryEntry? {
        return diaryEntryDao.getEntriesForPetSync(petId)
            .filter { it.eventType == type.name }
            .mapNotNull { entry -> parseDate(entry.date)?.let { date -> date to entry } }
            .maxByOrNull { it.first }
            ?.second
    }

    /**
     * Сколько дней прошло с последнего события заданного типа.
     * null, если такого события ещё не было ни разу.
     */
    suspend fun daysSinceLastEvent(petId: Long, type: EventType): Int? {
        val lastEvent = getLastEventOfType(petId, type) ?: return null
        val lastDate = parseDate(lastEvent.date) ?: return null
        val diffMillis = Date().time - lastDate.time
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}
