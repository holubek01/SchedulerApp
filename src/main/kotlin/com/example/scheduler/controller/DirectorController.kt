package com.example.scheduler.controller

import io.github.palexdev.materialfx.css.themes.MFXThemeManager
import io.github.palexdev.materialfx.css.themes.Themes
import com.example.scheduler.SchedulerApp
import com.example.scheduler.models.PlansModel
import com.example.scheduler.utils.DialogUtils
import com.example.scheduler.utils.MessageBundle
import com.example.scheduler.utils.MessageUtil
import com.example.scheduler.utils.TabObserver
import io.github.palexdev.materialfx.controls.MFXButton
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import javafx.animation.TranslateTransition
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.AnchorPane
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.WindowEvent
import javafx.util.Duration
import java.sql.SQLException
import kotlin.system.exitProcess


/**
 * Klasa obsługująca interakcje, takie jak zmianę zakładek,
 * wylogowywanie się, zamykanie programu z aktywnym planem.
 */
class DirectorController: IDirectorController{

    /**
     * Panel zakładek do przechodzenia między widokami w interfejsie.
     */
    @FXML
    lateinit var tabPane: TabPane

    /**
     * Przycisk do wylogowania się z aplikacji.
     */
    @FXML
    lateinit var logoutButton: MFXButton

    /**
     * Etykieta wyświetlająca aktywny plan.
     */
    @FXML
    lateinit var activePlanLabel: Label

    /**
     * Pomocnicze okno dialogowe.
     */
    lateinit var dialog: MFXStageDialog

    /**
     * Etykieta wyświetlająca nazwę zalogowanego użytkownika.
     */
    @FXML
    lateinit var userLabel: Label

    /**
     * Zakładka do tworzenia nowego planu.
     */
    @FXML
    private lateinit var createPlanTab: Tab

    /**
     * Zakładka do wyświetlania planu dla grup i nauczycieli.
     */
    @FXML
    private lateinit var showPlanTab: Tab

    /**
     * Zakładka do wyświetlania rozkładu dla sal
     */
    @FXML
    private lateinit var showPlanForRoomsTab: Tab

    /**
     * Zakładka wyświetlania panelu administracyjnego.
     */
    @FXML
    private lateinit var adminDashboard: Tab

    /**
     * Główne okno aplikacji.
     */
    lateinit var stage: Stage

    /**
     * Nazwa zalogowanego użytkownika.
     */
    lateinit var user:String

    val plansModel = PlansModel()

    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize() {
        setTexts()
        setTabs()
        logoutButton.setOnAction { handleBeforeLogout() }
        tabPane.selectionModel.selectedItemProperty().addListener { _, oldTab, newTab ->
            animateTabTransition(oldTab, newTab, stage)
            TabObserver.notifyObservers()
        }
        stage.setOnCloseRequest {event-> handleClosingProgram(event) }
    }

    /**
     * Ustawia teksty etykiet i przycisków.
     */
    override fun setTexts()
    {
        createPlanTab.text = MessageBundle.getMess("label.createPlan")
        showPlanTab.text = MessageBundle.getMess("label.showPlan")
        showPlanForRoomsTab.text = MessageBundle.getMess("label.planForRooms")
        adminDashboard.text = MessageBundle.getMess("label.adminPanel")
        logoutButton.text = MessageBundle.getMess("label.logout")
        activePlanLabel.text = MessageBundle.getMess("label.currentPlan")
        userLabel.text = "${MessageBundle.getMess("label.user")} $user"
    }

    /**
     * Ustawia zakładki interfejsu użytkownika i przypisuje do nich odpowiednie kontrolery.
     */
    override fun setTabs()
    {
        setTab(createPlanTab,"view/createPlan.fxml")
        setTab(showPlanTab, "view/showPlan.fxml")
        setTab(showPlanForRoomsTab, "view/showPlanForRooms.fxml")
        setTab(adminDashboard, "view/adminDashBoard.fxml")
    }

    /**
     * Obsługuje zdarzenie zamknięcia programu.
     *
     * Metoda jest wywoływana przy próbie zamknięcia programu i podejmuje odpowiednie działania,
     * takie jak zapisanie istniejącego planu przed zamknięciem, jeśli taki istnieje.
     *
     * @param event Zdarzenie zamknięcia okna.
     */
    override fun handleClosingProgram(event: WindowEvent)
    {
        event.consume()
        val shouldSavePlanBeforeClosing = plansModel.shouldSaveOldPlan()
        try {
            if (shouldSavePlanBeforeClosing)
            {
                var wantToSaveAndClose = false
                val message = MessageBundle.getMess("question.askBeforeCloseAppWhenPlanExists")

                val buttons = listOf(
                    MessageBundle.getMess("label.yes") to { wantToSaveAndClose = true },
                    MessageBundle.getMess("label.no") to { wantToSaveAndClose = false})

                dialog = DialogUtils.showMessageDialogWithButtons(message, stage, buttons)
                if (dialog.owner.isShowing) dialog.showAndWait()

                if (wantToSaveAndClose)
                {
                    plansModel.createNewPlan()
                    close()
                }
            }
            else
            {
                close()
            }
        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.savePlanError"))
        }
    }

    /**
     * Funkcja zamykająca program
     */
    fun close()
    {
        plansModel.refillHours()
        Platform.exit()
        exitProcess(0)
    }


    /**
     * Sprawdza czy przed wylogowaniem nie trzeba zapisać planu.
     */
    override fun handleBeforeLogout() {

        val shouldSavePlanBeforeLogout = plansModel.shouldSaveOldPlan()
        try {
            if (shouldSavePlanBeforeLogout)
            {
                var wantToSaveAndClose = false
                val message = MessageBundle.getMess("question.askBeforeLogoutWhenPlanExists")

                val buttons = listOf(
                    MessageBundle.getMess("label.yes") to { wantToSaveAndClose = true },
                    MessageBundle.getMess("label.no") to { wantToSaveAndClose = false})

                dialog = DialogUtils.showMessageDialogWithButtons(message, stage, buttons)
                if (dialog.owner.isShowing) dialog.showAndWait()

                if (wantToSaveAndClose)
                {
                    plansModel.createNewPlan()
                    plansModel.refillHours()
                    logout()
                }
            }
            else
            {
                logout()
            }
        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.savePlanError"))
        }
    }


    /**
     * Obsługuje wylogowanie użytkownika i przenosi go do widoku logowania.
     */
    override fun logout()
    {
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
        val controller = LoginController()
        controller.stage = stage
        fxmlLoader.setController(controller)
        val newRoot: AnchorPane = fxmlLoader.load()
        val screenBounds: javafx.geometry.Rectangle2D = Screen.getPrimary().bounds
        val scene = Scene(newRoot)
        controller.root = newRoot
        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY)
        stage.isResizable = false
        stage.scene = scene
        stage.sizeToScene()
        stage.x = (screenBounds.width-1000.0)/2
        stage.y = (screenBounds.height-500.0)/2
    }

    /**
     * Ustawia animacje przejścia między zakładkami.
     *
     * @param oldTab Zakładka, która jest zamykana.
     * @param newTab Nowa zakładka, która jest otwierana.
     * @param stage Główne okno aplikacji.
     */
    override fun animateTabTransition(oldTab: Tab, newTab: Tab, stage: Stage) {
        val oldContent = oldTab.content as AnchorPane
        val newContent = newTab.content as AnchorPane

        //Jeśli zakładka jest na prawo od obecnej to wysuń zakładkę z prawej
        val oldTabX = if (tabPane.tabs.indexOf(newTab) > tabPane.tabs.indexOf(oldTab)) {
            -tabPane.layoutBounds.width
        } else {
            tabPane.layoutBounds.width
        }
        val newTabX = -oldTabX

        val transitionOut = TranslateTransition(Duration.seconds(0.1), oldContent)
        transitionOut.fromX = 0.0
        transitionOut.toX = oldTabX

        val transitionIn = TranslateTransition(Duration.seconds(0.1), newContent)
        transitionIn.fromX = newTabX
        transitionIn.toX = 0.0

        transitionOut.setOnFinished {
            oldContent.isVisible = false
            newContent.isVisible = true
            transitionIn.play()
        }

        transitionOut.play()
    }

    /**
     * Ustawia zawartość zakładki na podstawie podanego widoku.
     *
     * @param tab Zakładka, dla której ustawiana jest zawartość.
     * @param url Ścieżka do widoku FXML.
     */
    override fun setTab(tab: Tab, url: String) {
        val fxmlLoader = FXMLLoader(SchedulerApp::class.java.getResource(url))

        val createPlanController = CreatePlanController()
        val showPlanController = ShowPlanController()
        val showPlanRoomsController = ShowPlanRoomsController()
        val adminDashboardController = AdminDashboardController()
        //stage.minWidth = 500.0

        createPlanController.stage = stage
        showPlanController.stage = stage
        showPlanRoomsController.stage = stage
        adminDashboardController.stage = stage

        when(tab)
        {
            createPlanTab -> {fxmlLoader.setController(createPlanController); createPlanController.activePlanLabel = activePlanLabel}
            showPlanTab -> fxmlLoader.setController(showPlanController)
            showPlanForRoomsTab -> fxmlLoader.setController(showPlanRoomsController)
            adminDashboard -> fxmlLoader.setController(adminDashboardController)
        }

        val anch: AnchorPane = fxmlLoader.load()
        tab.content = anch
    }
}