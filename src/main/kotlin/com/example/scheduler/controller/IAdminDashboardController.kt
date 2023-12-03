package com.example.scheduler.controller

import javafx.scene.control.ToggleButton

interface IAdminDashboardController {
    fun createToggle(icon: String, text: String): ToggleButton
}