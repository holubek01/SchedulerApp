package com.example.scheduler.utils

import com.dustinredmond.fxtrayicon.FXTrayIcon
import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.ShowPlanController
import javafx.stage.Stage
import java.awt.SystemTray
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Klasa służąca do wyświetlania komunikatów różnego typu jako powiadomienia systemowe.
 */
object MessageUtil {
    var content = ""

    /**
     * Klasa enum reprezentująca typy komunikatów.
     */
    enum class MessageType{
        INFO, WARNING, ERROR
    }

    /**
     * Metoda służąca do wyświetlenia powiadomienia systemowego z określonym tytułem i treścią.
     *
     * @param title Tytuł komunikatu.
     * @param content Treść komunikatu.
     * @param type Typ powiadomienia (INFO, WARNING, lub ERROR).
     */
    private fun showMessage(title: String, content: String, type: MessageType) {
        if (FXTrayIcon.isSupported()){
            val mess = FXTrayIcon(Stage(), SchedulerApp::class.java.getResource("photos/schedulerImg.png"), 16,16)
            mess.show()

            when(type)
            {
                MessageType.ERROR -> mess.showErrorMessage(title, content)
                MessageType.INFO -> mess.showInfoMessage(title, content)
                MessageType.WARNING -> mess.showWarningMessage(title, content)
            }

            val executor = Executors.newSingleThreadScheduledExecutor()

            executor.schedule({
                mess.hide()
            }, 2, TimeUnit.SECONDS)
        }

    }

    /**
     * Metoda służąca do wyświetlenia komunikatu typu WARNING jako powiadomienia systemowego.
     *
     * @param title   Tytuł komunikatu.
     * @param content Treść komunikatu.
     */
    fun showWarningMessage(title: String, content: String) {
        this.content = content
        showMessage(title, content, MessageType.WARNING)
    }

    /**
     * Metoda służąca do wyświetlenia komunikatu typu INFO jako powiadomienia systemowego.
     *
     * @param title   Tytuł komunikatu.
     * @param content Treść komunikatu.
     */
    fun showInfoMessage(title: String, content: String) {
        this.content = content
        showMessage(title, content, MessageType.INFO)
    }

    /**
     * Metoda służąca do wyświetlenia komunikatu typu ERROR jako powiadomienia systemowego.
     *
     * @param title   Tytuł komunikatu.
     * @param content Treść komunikatu.
     */
    fun showErrorMessage(title: String, content: String) {
        this.content = content
        showMessage(title, content, MessageType.ERROR)
    }
}