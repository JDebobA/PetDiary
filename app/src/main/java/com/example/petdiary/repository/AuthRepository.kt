package com.example.petdiary.repository

import com.example.petdiary.data.User
import com.example.petdiary.data.UserDao
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Регистрация и вход.
 * Функции только определены - вызывать их (привязывать к экранам
 * Sign Up / Log In) будет тот, кто делает UI.
 */
class AuthRepository(private val userDao: UserDao) {

    data class AuthResult(
        val success: Boolean,
        val message: String,
        val userId: Long? = null
    )

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + password).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Регистрация нового пользователя.
     */
    suspend fun registration(username: String, password: String): AuthResult {
        if (username.isBlank() || password.isBlank()) {
            return AuthResult(false, "Логин и пароль обязательны")
        }
        if (userDao.getByUsername(username) != null) {
            return AuthResult(false, "Такой пользователь уже существует")
        }

        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        val userId = userDao.insert(User(username = username, passwordHash = "$salt:$hash"))

        return AuthResult(true, "Регистрация успешна", userId)
    }

    /**
     * Вход пользователя.
     */
    suspend fun login(username: String, password: String): AuthResult {
        val user = userDao.getByUsername(username)
            ?: return AuthResult(false, "Пользователь не найден")

        val parts = user.passwordHash.split(":")
        if (parts.size != 2) {
            return AuthResult(false, "Повреждённые данные пользователя")
        }
        val (salt, storedHash) = parts

        return if (hashPassword(password, salt) == storedHash) {
            AuthResult(true, "Вход выполнен", user.id)
        } else {
            AuthResult(false, "Неверный пароль")
        }
    }
}
