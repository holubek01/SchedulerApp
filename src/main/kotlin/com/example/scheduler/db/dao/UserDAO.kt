package com.example.scheduler.db.dao

interface UserDAO {
    fun authenticateUser(username: String, password: String): Int
    fun isPasswordTemp(username: String, password: String): Boolean
    fun changeTempPassword(newPassword: String, username: String)
    fun testDBPassword(db: String, username: String, pass: String): Boolean
}