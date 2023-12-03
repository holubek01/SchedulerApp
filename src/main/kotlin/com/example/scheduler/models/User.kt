package com.example.scheduler.models

import com.example.scheduler.db.dao.UserDaoImpl
import org.mindrot.jbcrypt.BCrypt

/**
 * Klasa reprezentująca model użytkownika aplikacji wraz z logiką biznesową
 *
 * @property username       Nazwa użytkownika
 * @property password       Hasło użytkownika
 */
data class User(
    val username: String,
    val password: String
)
{
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val userDao = UserDaoImpl()

    /**
     * Haszuje hasło i zastępuje tymczasowe hasło nowym zahaszowanym hasłem dla określonego użytkownika.
     *
     * @param newPassword Nowe hasło w formie tekstowej.
     */
    fun changeTempPassword(newPassword: String) {
        val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        userDao.changeTempPassword(hashedPassword, username)
    }

    /**
     * @see UserDaoImpl.authenticateUser
     */
    fun authenticateUser(): Int {
        return userDao.authenticateUser(username, password)
    }

    /**
     * @see UserDaoImpl.isPasswordTemp
     */
    fun isPasswordTemp(): Boolean {
        return userDao.isPasswordTemp(username, password)
    }

    /**
     * Testuje hasło do bazy danych
     */
    fun testDbPassword(db: String): Boolean {
        return userDao.testDBPassword(db,username,password)
    }
}

