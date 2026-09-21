package com.example.petdiary.data

/**
 * Тип события в дневнике питомца.
 * Соответствует выбору типа на экране "Новое событие":
 * Кормление / Прогулка / Лекарство / Осмотр.
 */
enum class EventType {
    FEEDING,
    WALK,
    MEDICINE,
    CHECKUP,
    OTHER;

    /** Название для отображения в интерфейсе. */
    fun displayName(): String = when (this) {
        FEEDING -> "Кормление"
        WALK -> "Прогулка"
        MEDICINE -> "Лекарство"
        CHECKUP -> "Осмотр"
        OTHER -> "Другое"
    }
}
