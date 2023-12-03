package com.example.scheduler.tests.controllersDirector

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.LoginController
import com.example.scheduler.models.User
import com.example.scheduler.utils.MessageBundle
import com.example.scheduler.utils.MessageUtil
import com.example.scheduler.utils.ValidationWrapper
import io.github.palexdev.materialfx.controls.MFXButton
import io.github.palexdev.materialfx.controls.MFXTextField
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.util.*

class LoginControllerTest: ApplicationTest() {

    lateinit var controller: LoginController

    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/loginView.fxml"))
        loader.setController(LoginController())
        val root: Parent = loader.load()
        controller = loader.getController()
        controller.stage = stage!!
        stage.scene = Scene(root)
    }

    //Test sprawdzający ogarniczenia nałożone na hasło (długość, zawieranie cyfr itp.)
    @Test
    fun setConstraintTest()
    {
        val user = User("Anna", "Anna123")
        controller.createNewPasswordDialog(user, 1)
        controller.newPassword.text = ""
        controller.newPasswordRepeat.text = ""

        val errorPassword = ValidationWrapper.createErrorLabel()
        val errorPasswordRepeat = ValidationWrapper.createErrorLabel()

        controller.setNewPassword(errorPassword, errorPasswordRepeat, user)

        assertEquals(MessageBundle.getMess("password.validation.notEmpty"), errorPassword.text)

        controller.newPassword.text = "Tom!"
        controller.setNewPassword(errorPassword, errorPasswordRepeat, user)
        assertEquals(MessageBundle.getMess("password.validation.moreThan6Letters"), errorPassword.text)

        controller.newPassword.text = "JanKowalski"
        controller.setNewPassword(errorPassword, errorPasswordRepeat, user)
        assertEquals(MessageBundle.getMess("password.validation.containsDigit"), errorPassword.text)

        controller.newPassword.text = "jakkowalski12"
        controller.setNewPassword(errorPassword, errorPasswordRepeat, user)
        assertEquals(MessageBundle.getMess("password.validation.containsBigLetter"), errorPassword.text)

        controller.newPassword.text = "JanKowal12"
        controller.newPasswordRepeat.text = "JanKowal123"
        controller.setNewPassword(errorPassword, errorPasswordRepeat, user)
        assertEquals(MessageBundle.getMess("password.validation.notEqualPasswords"), errorPasswordRepeat.text)
    }


    //Test sprawdzający poprawność danych logowania
    @Test
    fun onLoginPressedTest()
    {
        Platform.runLater {
            controller.loginTextField.text = ""
            controller.passwordTextField.text = ""
            controller.onLoginPressed()
            assertEquals(MessageBundle.getMess("warning.noPasswordOrUsername"), MessageUtil.content)


            controller.loginTextField.text = "Lorem"
            controller.passwordTextField.text = "Ipsum"
            controller.onLoginPressed()
            assertEquals(MessageBundle.getMess("warning.incorrectLoginOrPassword"), MessageUtil.content)
        }
    }


    //Test sprawdzjący czy po kliknięciu na toggle zmieni się zawartość
    @Test
    fun setActionsTest()
    {
        Platform.runLater{
            assertEquals("Polski", controller.languageToggle.text)
            controller.languageToggle.onMouseClicked.handle(null)
            assertEquals("English", controller.languageToggle.text)
        }
    }


    //Test sprawdzający czy etykiety przypisały się do komponentów zgodnie z oczekiwaniami
    @Test
    fun setTextsTest()
    {
        Platform.runLater {
            runBlocking {
                assertEquals(MessageBundle.getMess("label.welcomeInApp"), controller.welcomeLabel.text)
                assertEquals(MessageBundle.getMess("label.logging"), controller.logLabel.text)
                assertEquals(MessageBundle.getMess("label.login"), controller.loginTextField.floatingText)
                assertEquals(MessageBundle.getMess("label.password"), controller.passwordTextField.floatingText)
                assertEquals(MessageBundle.getMess("label.log"), controller.loginButton.text)
            }
        }
    }


    //Testowanie tworzenia okna przypisana hasła do bazy danych
    @Test
    fun createDbPasswordDialogTest()
    {
        Platform.runLater {
            runBlocking {
                val user = User("An", "An124")
                val vbox = controller.createDbPasswordDialog(Properties())

                Assertions.assertTrue(vbox.children[0] is Label)
                Assertions.assertTrue(vbox.children[0].styleClass.contains("header-label-white-big"))
                assertEquals((vbox.children[0] as Label).text, MessageBundle.getMess("label.firstAppUse"))

                Assertions.assertTrue(vbox.children[1] is Label)
                Assertions.assertTrue(vbox.children[1].styleClass.contains("header-label_white"))
                assertEquals((vbox.children[1] as Label).text, MessageBundle.getMess("label.shouldEnterPassword"))

                Assertions.assertTrue(vbox.children[2] is MFXTextField)
                Assertions.assertTrue(vbox.children[2].id == "comboWhite")
                assertEquals((vbox.children[2] as MFXTextField).floatingText, MessageBundle.getMess("label.password"))

                Assertions.assertTrue(vbox.children[3] is MFXButton)
                Assertions.assertTrue(vbox.children[3].id == "customButton")
                assertEquals((vbox.children[3] as MFXButton).text, MessageBundle.getMess("label.setPassword"))

            }
        }
    }


}