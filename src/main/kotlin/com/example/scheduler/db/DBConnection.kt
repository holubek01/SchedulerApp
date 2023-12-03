package com.example.scheduler.db

import com.example.scheduler.utils.MessageUtil
import javafx.stage.Stage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

/**
 * Obiekt służący do zarządzania połączeniem z bazą danych.
 */
object DBConnection {
    private var connection: Connection? = null

    /**
     * Metoda pobierająca połączenie z bazą danych.
     *
     * @return     Obiekt Connection reprezentujący połączenie z bazą danych.
     */
    fun getConnection(): Connection {
        if (connection == null) {
            val prop = Properties()
            //val configFile = Path.of("src/main/resources/config.properties")
            val configFile = Paths.get("").toAbsolutePath().resolve("config.properties")

            try
            {
                if(Files.exists(configFile))
                {
                    Files.newInputStream(configFile).use { input -> prop.load(input)}

                    val username = prop.getProperty("db.username")
                    val password = prop.getProperty("db.password")
                    val dbURL = prop.getProperty("db.name")

                    connection = DriverManager.getConnection(dbURL, username, password)
                }
                else
                {
                    println("file not found")
                }
            }
            catch (e: Exception) {
                println("Error while connecting to the database: ${e.message}")
            }
        }
        return connection!!
    }

    /**
     * Metoda zamykająca połączenie z bazą danych.
     */
    fun closeConnection() {
        connection?.close()
        connection = null
    }
}