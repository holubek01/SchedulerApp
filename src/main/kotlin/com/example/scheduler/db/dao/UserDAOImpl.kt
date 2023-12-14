package com.example.scheduler.db.dao

import com.example.scheduler.db.DBQueryExecutor
import org.mindrot.jbcrypt.BCrypt
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Implementacja interfejsu `UserDAO` do obsługi operacji w bazie danych dla ekranu logowania.
 */
class UserDaoImpl : UserDAO {

    /**
     * Identyfikuje użytkownika na podstawie dostarczonych danych.
     *
     * @param username Nazwa użytkownika do uwierzytelnienia.
     * @param password Hasło użytkownika do uwierzytelnienia.
     * @return Identyfikator roli użytkownika po uwierzytelnieniu lub 0 jeśli uwierzytelnianie nie powiodło się.
     */
    override fun authenticateUser(username: String, password: String): Int {
        var query = "SELECT pass FROM user WHERE username=?"
        val hashedPassword = DBQueryExecutor.executeQuery(query, username) { resultSet ->resultSet.getString(1)}.firstOrNull()

        if (!hashedPassword.isNullOrEmpty())
        {
            //Porównaj zahashowane hasło z podanym
            return if (BCrypt.checkpw(password, hashedPassword)) {
                // Hasło jest poprawne, zezwól na uwierzytelnienie - pobierz rolę
                query = "SELECT roleId FROM user_role AS ur\n" +
                        "INNER JOIN user AS u ON u.userId = ur.userID\n" +
                        "WHERE u.username = ?"
                DBQueryExecutor.executeQuery(query, username) { resultSet ->
                    resultSet.getInt(1)
                }.first()


            } else {
                // Hasło jest niepoprawne, odrzuć uwierzytelnienie
                0
            }
        }
        return 0
    }

    /**
     * Zastępuje tymczasowe hasło nowym hasłem dla określonego użytkownika.
     *
     * @param newPassword Nowe hasło, które ma zostać ustawione.
     * @param username    Nazwa użytkownika, dla którego ma zostać zmienione hasło.
     */
    @Throws(SQLException::class)
    override fun changeTempPassword(newPassword: String, username: String) {
        val query = "{ CALL updatePassword(?,?) }"
        DBQueryExecutor.executePreparedStatement(query, newPassword, username)
    }

    /**
     * Sprawdza, czy hasło dla danego użytkownika jest tymczasowe.
     *
     * @param username Nazwa użytkownika, dla którego sprawdzane jest hasło.
     * @param password Hasło, które ma zostać sprawdzone.
     * return Wartość logiczna informująca, czy hasło jest tymczasowe (true) lub nie (false).
     */
    override fun isPasswordTemp(username: String, password: String): Boolean {
        val query = "SELECT isTempPassword FROM user WHERE username=?"
        return DBQueryExecutor.executeQuery(query, username) {resultSet -> resultSet.getBoolean(1) }.first()
    }

    /**
     * Testuje hasło do bazy danych podane przez użytkownika
     * @param db Baza danych, do której użytkownik ma się połączyć
     * @param username Nazwa użytkownika bazy danych
     * @param pass Hasło do bazy danych
     */
    override fun testDBPassword(db: String, username: String, pass: String): Boolean {
        return try {
            val conn = DriverManager.getConnection(db,username, pass)
            conn.close()
            true
        }catch (e: Exception) {
            false
        }

    }

    /*
    override fun checkIfPasswordIsOccupied(password: String): Boolean {
        val query = "SELECT pass FROM user"
        val hashedPasswordsFromDatabase = DBQueryExecutor.executeQuery(query) {resultSet -> resultSet.getString(1) }
        return hashedPasswordsFromDatabase.any { hashedPassword -> BCrypt.checkpw(password, hashedPassword) }
    }

     */
}