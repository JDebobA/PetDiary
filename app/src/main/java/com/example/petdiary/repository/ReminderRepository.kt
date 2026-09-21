package com.example.petdiary.repository

import com.example.petdiary.data.EventType
import com.example.petdiary.data.PetDao

/**
 * Простые уведомления/напоминания - иконка колокольчика в шапке главного экрана.
 * Использует TrackingRepository, чтобы понять, что "давно не делалось".
 */
class ReminderRepository(
    private val petDao: PetDao,
    private val trackingRepository: TrackingRepository
) {

    /**
     * Формирует список текстовых уведомлений:
     *  - если у питомца сегодня ещё не было прогулки;
     *  - если давно (>180 дней) не было осмотра у ветеринара.
     */
    suspend fun getNotifications(): List<String> {
        val notifications = mutableListOf<String>()
        val pets = petDao.getAllPetsSync()

        for (pet in pets) {
            val daysWalk = trackingRepository.daysSinceLastEvent(pet.id, EventType.WALK)
            if (daysWalk == null || daysWalk >= 1) {
                notifications.add("${pet.name}: сегодня ещё не было прогулки")
            }

            val daysCheckup = trackingRepository.daysSinceLastEvent(pet.id, EventType.CHECKUP)
            if (daysCheckup != null && daysCheckup > 180) {
                notifications.add("${pet.name}: давно не было осмотра у ветеринара")
            }
        }

        return notifications
    }
}
