package com.example.scheduler.controller

import com.example.scheduler.SchedulerApp
import com.example.scheduler.utils.AdminTabObserver
import com.example.scheduler.utils.MessageBundle
import io.github.palexdev.materialfx.controls.MFXIconWrapper
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode
import io.github.palexdev.materialfx.utils.others.loader.MFXLoader
import io.github.palexdev.materialfx.utils.others.loader.MFXLoaderBean
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

/**
 * Klasa kontrolera do obsługi panelu admina
 */
class AdminDashboardController: IAdminDashboardController{

    /**
     * Pasek nawigacyjny.
     */
    @FXML
    private lateinit var navBar: VBox

    /**
     * Etykieta informująca o nazwie panelu.
     */
    @FXML
    private lateinit var adminLabel: Label

    /**
     * Kontener z zawartością panelu (różna treść w zależności od zakładki).
     */
    @FXML
    private lateinit var contentPane: AnchorPane

    /**
     * Grupa przycisków typu ToggleButton do zarządzania zawartością panelu.
     */
    private val toggleGroup: ToggleGroup = ToggleGroup()

    /**
     * Główna scena aplikacji
     */
    internal lateinit var stage: Stage


    /**
     * Tworzenie przycisku ToggleButton z tekstem oraz ikoną.
     *
     * @param icon Ikona przycisku.
     * @param text Tekst przycisku.
     * @return Przycisk typu ToggleButton.
     */
    override fun createToggle(icon: String, text: String): ToggleButton {
        val wrapper = MFXIconWrapper(icon, 22.0, 30.0)
        val toggleNode = MFXRectangleToggleNode(text, wrapper)

        toggleNode.alignment = Pos.CENTER_RIGHT
        toggleNode.maxWidth = Double.MAX_VALUE
        toggleNode.toggleGroup = toggleGroup
        return toggleNode
    }

    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize() {
        val loader = MFXLoader()
        adminLabel.text = MessageBundle.getMess("label.adminLabel")

        loader.addView(createLoaderBean("TEACHERS", "view/teacherModule.fxml", "fas-user", "label.teachers").setDefaultRoot(true).get())
        loader.addView(createLoaderBean("ROOMS", "view/roomModule.fxml","fas-school", "label.rooms").get())
        loader.addView(createLoaderBean("SPN", "view/schoolCurriculumUploadModule.fxml","fas-graduation-cap", "label.teachingPlan").get())
        loader.addView(createLoaderBean("FIELDS", "view/fieldModule.fxml","fas-user-graduate", "label.fields").get())
        loader.addView(createLoaderBean("GROUPS", "view/groupModule.fxml","fas-people-group", "label.groups").get())
        loader.addView(createLoaderBean("PLANS", "view/copyPlanModule.fxml","fas-graduation-cap", "label.plans").get())
        loader.addView(createLoaderBean("LOCATIONS", "view/locationModule.fxml","fas-location-dot", "label.locations").get())

        loader.setOnLoadedAction {  beans->
                //przekształca każdy bean (typu MFXloaderBean) na ToggleButton
                val nodes = beans.stream().map {
                        bean: MFXLoaderBean ->
                        val toggle = bean.beanToNodeMapper.get() as ToggleButton
                        toggle.setOnAction {
                            setContent(bean.root)
                            if (!toggle.isSelected) toggle.isSelected = true
                        }

                        toggle.isSelected = bean.isDefaultView
                        if (toggle.isSelected) setContent(bean.root)

                        toggle
                    }.toList()

                navBar.children.addAll(nodes)
            }

        Platform.runLater {
        loader.start()}
    }

    /**
     * Tworzenie obiektu typu MFXLoaderBean.Builder dla widoku.
     *
     * @param viewName Nazwa widoku.
     * @param resourcePath Ścieżka do pliku FXML.
     * @param icon Ikona przycisku.
     * @param text Tekst przycisku.
     * @return Obiekt MFXLoaderBean.Builder
     */
    private fun createLoaderBean(viewName: String, resourcePath: String, icon: String, text: String): MFXLoaderBean.Builder {
        return MFXLoaderBean.of(viewName, SchedulerApp::class.java.getResource(resourcePath))
            .setBeanToNodeMapper { createToggle(icon, MessageBundle.getMess(text)) }
    }

    /**
     * Ustawienie treści panelu na podstawie wybranego widoku.
     *
     * @param root Wczytany widok.
     */
    private fun setContent(root: Parent)
    {
        AdminTabObserver.notifyObservers()
        AnchorPane.setTopAnchor(root, 0.0)
        AnchorPane.setBottomAnchor(root, 0.0)
        AnchorPane.setLeftAnchor(root, 0.0)
        AnchorPane.setRightAnchor(root, 0.0)
        contentPane.children.setAll(root)
    }
}