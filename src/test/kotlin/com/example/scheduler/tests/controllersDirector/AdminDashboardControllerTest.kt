package com.example.scheduler.tests.controllersDirector

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.AdminDashboardController
import com.example.scheduler.utils.MessageBundle
import io.github.palexdev.materialfx.css.themes.MFXThemeManager
import io.github.palexdev.materialfx.css.themes.Themes
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ToggleButton
import javafx.stage.Stage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testfx.api.FxAssert
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.matcher.control.LabeledMatchers
import org.testfx.util.WaitForAsyncUtils
import java.util.*
import java.util.function.Predicate

class AdminDashboardControllerTest: ApplicationTest() {
    lateinit var controller: AdminDashboardController
    /*

override fun start(stage: Stage) {
    MessageBundle.loadBundle(Locale("PL", "pl"))
    val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/adminDashBoard.fxml"))
    loader.setController(AdminDashboardController())
    val root: Parent = loader.load()
    controller = loader.getController()
    controller.stage = stage
    stage.scene = Scene(root, 1280.0, 800.0)
    MFXThemeManager.addOn(stage.scene, Themes.DEFAULT, Themes.LEGACY)
    stage.show()
}


@Test
fun testCreateToggle() {
    runBlocking {
        val robot = FxRobot()
        val teachersButton = robot.lookup(".toggle-button").queryAll<ToggleButton>().find { it.text == "Nauczyciele" }!!
        Thread.sleep(1000)
        robot.clickOn(teachersButton)
        assertTrue(teachersButton.isSelected)

        val groupButton = robot.lookup(".toggle-button").queryAll<ToggleButton>().find { it.text == "Grupy" }!!
        robot.clickOn(groupButton)
        assertTrue(groupButton.isSelected)
        assertFalse(teachersButton.isSelected)

    }

     */

    }

