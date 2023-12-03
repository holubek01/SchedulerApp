package com.example.scheduler.controller

import com.example.scheduler.controller.observers.AdminTabsObserver
import com.example.scheduler.controller.observers.FieldsObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.models.FieldsModel
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Group
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.*
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.sql.SQLException


/**
 * Klasa kontrolera modułu GroupsModule do zarządzania grupami
 */
class GroupsModuleController: IGroupsModuleController, FieldsObserver, AdminTabsObserver, TabsObserver {

    /**
     * Lista służąca do wyświetlania grup na danym kierunku i semestrze
     */
    @FXML
    lateinit var groupsListView: MFXListView<String>

    /**
     * Pomocnicze okno dialogowe
     */
    lateinit var dialog: MFXStageDialog

    /**
     * Przycisk służący do wyświetlania listy grup na danym kierunku i semestrze.
     */
    @FXML
    lateinit var showGroupsButton: MFXButton

    /**
     * Przycisk dodawania grupy do wybranego semestru na danym kierunku.
     */
    @FXML
    lateinit var addGroupButton: MFXButton

    /**
     * Kontrolka przechowująca listę wszystkich kierunków
     */
    @FXML
    lateinit var fieldChoiceBox: MFXComboBox<String>

    /**
     * Kontrolka przechowująca listę semestrów na wybranym kierunku
     */
    @FXML
    lateinit var semesterChoiceBox: MFXComboBox<Int>


    /**
     * Menu kontekstowe wyświetlające się po kliknięciu na grupę na liście
     */
    lateinit var menu: MFXContextMenu

    /**
     * Flaga informująca o chęci usunięcia grupy
     */
    var wantToDelete = false

    val fieldsModel = FieldsModel()

    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize(){
        AdminTabObserver.addObserver(this)
        TabObserver.addObserver(this)
        FieldsModel.addObserver(this)
        getFields()
        setTexts()
        setListeners()
        menu = MFXContextMenu(groupsListView)
        showGroupsButton.setOnAction{ showGroups() }
        addGroupButton.setOnAction { addGroup() }
    }

    /**
     * Ustawia nasłuchiwanie dla kontrolek
     */
    override fun setListeners()
    {
        fieldChoiceBox.valueProperty().addListener { _, _, new ->
            semesterChoiceBox.items.clear()
            if (!fieldChoiceBox.value.isNullOrBlank())
            {
                //pobierz liczbę semestrów na kierunku
                val sems = fieldsModel.getSemesters(new)
                semesterChoiceBox.items = FXCollections.observableArrayList((1..sems).toList())
            }

        }

        setOnGroupSelected()
    }

    /**
     * Ustawia teksty na kontrolek
     */
    override fun setTexts()
    {
        fieldChoiceBox.floatingText = MessageBundle.getMess("label.field")
        semesterChoiceBox.floatingText = MessageBundle.getMess("label.sem")

        addGroupButton.text = MessageBundle.getMess("label.addGroup")
        showGroupsButton.text = MessageBundle.getMess("label.showGroups")
    }

    /**
     * Obsługuje akcje wywoływane po kliknięciu na grupę na liście.
     */
    override fun setOnGroupSelected() {
        groupsListView.setOnMouseClicked {
            val selectedItem = groupsListView.selectionModel.selectedValue
            if (selectedItem != null) {
                showContextMenu(groupsListView.items.indexOf(selectedItem), selectedItem)
            }
        }
    }

    /**
     * Pobiera listę nazw wszystkich kierunków
     */
    override fun getFields() {
        val fieldList = fieldsModel.getFields()
        fieldChoiceBox.items = FXCollections.observableArrayList(fieldList.map { field -> field.fieldName })
    }

    /**
     * Dodaje nową grupę do wybranego kierunku i semestru (pierwszą wolną w kolejności alfabetycznej)
     */
    override fun addGroup() {
        //Najpierw musisz wybrac kierunek i semestr
        if (fieldChoiceBox.value.isNullOrBlank() || semesterChoiceBox.value == null) MessageUtil.showWarningMessage(MessageBundle.getMess("warning.groupAddingError"), MessageBundle.getMess("warning.noFieldOrSem"))
        else
        {
            val romanNumber = CommonUtils.intToRoman(semesterChoiceBox.value)
            try {
                fieldsModel.addGroup(fieldChoiceBox.value, semesterChoiceBox.value, romanNumber)
                showGroups()
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.group.correctlyAdded"))
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.addGroupError"))
            }

        }
    }

    /**
     * Wyświetla menu kontekstowe dla wybranej grupy z tabeli.
     *
     * @param selectedRowIndex Indeks zaznaczonego wiersza na liście.
     * @param selectedItem       Wybrana grupa.
     */
    override fun showContextMenu(selectedRowIndex: Int, selectedItem: String) {
        val deleteButton = MFXContextMenuItem(MessageBundle.getMess("label.deleteGroup"))

        deleteButton.graphic =  MFXFontIcon("fas-trash-can", 16.0, Color.BLACK)

        deleteButton.setOnAction {
            val groupToDelete = Group(selectedItem, fieldChoiceBox.value, semesterChoiceBox.value)
            showDialogYesNoMessage(MessageBundle.getMess("question.group.askBeforeDelete"))
            deleteGroup(groupToDelete)
        }

        menu.items.clear()
        menu.hide()
        menu.items.addAll(deleteButton)

        val selectedRow = groupsListView.getCell(selectedRowIndex)

        if (selectedRow!=null)
        {
            val bounds = selectedRow.boundsInParent
            val boundsInWindow = groupsListView.localToScene(bounds.maxX, bounds.maxY)
            val menuX = boundsInWindow.x + groupsListView.scene.window.x + 10
            val menuY = boundsInWindow.y + groupsListView.scene.window.y + 40

            menu.show(groupsListView, menuX, menuY)
        }

    }

    /**
     * Usuwa grupę z bazy danych.
     *
     * @param groupToDelete  grupa do usunięcia z bazy danych.
     */
    override fun deleteGroup(groupToDelete: Group) {
        groupsListView.selectionModel.clearSelection()
        menu.hide()

        if (wantToDelete)
        {
            try {
                fieldsModel.deleteGroup(groupToDelete)
                if (groupsListView.items.size>1) showGroups() else groupsListView.items.clear()
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.plan.correctlyDeletedGroup"))

            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.deleteGroupError"))
            }

        }
    }

    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content       Wiadomość do wyświetlenia.
     */
    fun showDialogYesNoMessage(content: String) {
        val buttons = listOf(
            MessageBundle.getMess("label.yes") to { wantToDelete = true },
            MessageBundle.getMess("label.no") to { wantToDelete = false}
        )

        val stage: Stage = showGroupsButton.scene.window as Stage

        dialog = DialogUtils.showMessageDialogWithButtons(content, stage, buttons)
        if (dialog.owner.isShowing) dialog.showAndWait()
    }


    /**
     * Wyświetla listę grup na wybranym kierunku i semestrze
     */
    override fun showGroups() {
        val field = fieldChoiceBox.value
        val semester = semesterChoiceBox.value

        if (field.isNullOrBlank()) MessageUtil.showWarningMessage(MessageBundle.getMess("warning.showGroupsError"), MessageBundle.getMess("warning.showGroupsError.noField"))
        else if (semester == null)  MessageUtil.showWarningMessage(MessageBundle.getMess("warning.showGroupsError"), MessageBundle.getMess("warning.showGroupsError.noSem"))
        else
        {
            val groups = fieldsModel.getGroupsForGivenSem(field, semester)

            if (groups.isEmpty()) {
                MessageUtil.showInfoMessage(MessageBundle.getMess("warning.noGroups"), MessageBundle.getMess("warning.noGroupsInGivenField"))
                groupsListView.items.clear()
            }
            else
            {
                groupsListView.items = groups
            }
        }
    }

    /**
     * Metoda wywoływana po dokonaniu zmiany na kierunkach w bazie danych
     */
    override fun onFieldsChanged() {
        getFields()
    }

    /**
     * Metoda wywoływana podczas zmiany zakładek przez użytkownika - czyści panel.
     */
    override fun onTabsChanged() {
        CommonUtils.clearBox(fieldChoiceBox)
        CommonUtils.clearBox(semesterChoiceBox)
        groupsListView.items.clear()
    }


}


