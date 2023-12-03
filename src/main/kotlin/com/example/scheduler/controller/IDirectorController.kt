package com.example.scheduler.controller

import javafx.scene.control.Tab
import javafx.stage.Stage
import javafx.stage.WindowEvent
import java.lang.ModuleLayer.Controller

interface IDirectorController {
    fun animateTabTransition(oldTab: Tab, newTab: Tab, stage: Stage)
    fun setTab(tab: Tab, url: String)
    fun logout()
    fun handleBeforeLogout()
    fun handleClosingProgram(event: WindowEvent)
    fun setTabs()
    fun setTexts()
}