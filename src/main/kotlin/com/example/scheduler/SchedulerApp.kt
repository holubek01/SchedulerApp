package com.example.scheduler

import com.example.scheduler.controller.LoginController
import io.github.palexdev.materialfx.css.themes.MFXThemeManager
import io.github.palexdev.materialfx.css.themes.Themes
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage


/**
 * Klasa będąca główną klasą dla aplikacji Scheduler. Inicjuje aplikację JavaFX .
 */
class SchedulerApp : Application() {
    lateinit var root:AnchorPane

    /**
     * Metoda wywoływana przy uruchamianiu aplikacji.
     */
    override fun start(stage: Stage) {

        /*
        UserAgentBuilder.builder()
            .themes(JavaFXThemes.MODENA)
            .themes(MaterialFXStylesheets.forAssemble(true))
            .setDeploy(true)
            .setResolveAssets(true)
            .build()
            .setGlobal()

         */

        val fxmlLoader = FXMLLoader(SchedulerApp::class.java.getResource("view/loginView.fxml"))
        val loginController = LoginController()
        loginController.stage = stage
        fxmlLoader.setController(loginController)
        root = fxmlLoader.load()
        val scene = Scene(root,1000.0,500.0)
        stage.scene = scene
        loginController.root = root
        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY)
        stage.sizeToScene()
        stage.isResizable = false
        stage.show()
    }

    companion object {
        /**
         * Metoda główna do uruchamiania aplikacji Scheduler.
         *
         * @param args Argumenty wiersza poleceń.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            launch(SchedulerApp::class.java)
        }
    }
}

