package com.example.scheduler.controller

import com.example.scheduler.models.User
import io.github.palexdev.materialfx.controls.MFXPasswordField
import javafx.scene.layout.VBox

interface ILoginController {
    fun onLoginPressed()
    fun setActions()
    fun setTexts()
    fun setHandlers()
    fun loadNewScene(role: Int)
    fun setConstraints(newPassword: MFXPasswordField, newPasswordRepeat: MFXPasswordField)
    fun createNewPasswordDialog(user: User, role: Int): VBox
    fun checkDBpasswordExists()
}