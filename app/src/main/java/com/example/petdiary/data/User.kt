package com.example.petdiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Пользователь приложения.
 *
 * Для локального однопользовательского дневника это необязательно, но
 * оставлено как задел, раз в макетах/задаче предполагалась регистрация -
 * если экран входа в итоге не понадобится, эти файлы (User/UserDao/AuthRepository)
 * можно просто не подключать.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String
)
