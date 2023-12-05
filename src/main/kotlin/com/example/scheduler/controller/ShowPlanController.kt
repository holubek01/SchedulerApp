package com.example.scheduler.controller


import com.example.scheduler.controller.observers.FieldsObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.controller.observers.TeacherObserver
import com.example.scheduler.models.*
import com.example.scheduler.models.ClassesToRead
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.*
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.materialfx.filter.StringFilter
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.SQLException
import java.util.function.Consumer
import java.util.function.Function
import kotlin.reflect.KProperty1


/**
 * Klasa kontrolera do wyświetlania i eksportowania planów dla grup i nauczycieli oraz edycji zajęć
 */
class ShowPlanController:IShowPlanController, FieldsObserver, TeacherObserver, TabsObserver {

    /**
     * Tabela służąca do wyświetlania planów.
     */
    @FXML
    lateinit var planTableView: MFXTableView<ClassesToRead>

    /**
     * Przycisk służący do wyświetlania planu dla wybranej grupy.
     */
    @FXML
    lateinit var showPlanButtonGroup: MFXButton

    /**
     * Przycisk służący do eksportowania planu dla wybranego nauczyciela.
     */
    @FXML
    lateinit var exportTeacherPlanButton: MFXButton

    /**
     * Przycisk służący do eksportowania planu dla wybranej grupy.
     */
    @FXML
    lateinit var exportGroupPlanButton: MFXButton

    /**
     * Przycisk służący do wyświetlania planu dla wybranego nauczyciela.
     */
    @FXML
    lateinit var showPlanButtonTeacher: MFXButton

    /**
     * Przycisk służący do eksportowania planów dla wszystkich grup na wybranym kierunku.
     */
    @FXML
    lateinit var exportPlansForAllGroups: MFXButton

    /**
     * Przycisk służący do eksportowania planów dla wszystkich nauczycieli.
     */
    @FXML
    lateinit var exportPlansForAllTeachers: MFXButton

    /**
     * Kontrolka służąca do wyboru dnia do planu dla grupy
     */
    @FXML
    lateinit var dayChoiceBoxGroup: MFXComboBox<String>

    /**
     * Kontrolka służąca do wyboru dnia do planu dla nauczyciela
     */
    @FXML
    lateinit var dayChoiceBoxTeacher: MFXComboBox<String>

    /**
     * Kontrolka służąca do wyboru kierunku
     */
    @FXML
    lateinit var fieldOfStudyChoiceBox: MFXFilterComboBox<String>

    /**
     * Kontrolka służąca do wyboru grupy
     */
    @FXML
    lateinit var groupChoiceBox: MFXFilterComboBox<String>

    /**
     * Kontrolka służąca do wyboru nauczyciela
     */
    @FXML
    lateinit var teacherChoiceBox: MFXFilterComboBox<String>

    /**
     * Flaga informująca chęci eksportowania planu.
     */
    var wantToExport = false

    /**
     * Flaga informująca chęci usunięcia planu.
     */
    var wantToDelete = false

    /**
     * Pomocnicze okno dialogowe do menu kontekstowego.
     */
    lateinit var dialog: MFXStageDialog

    /**
     * Pomocnicze okno dialogowe do wiadomości.
     */
    lateinit var dialogMess: MFXStageDialog

    /**
     * Flaga informująca chęci edycji planu.
     */
    private var wantToEdit = false

    /**
     * Scena aplikacji
     */
    lateinit var stage: Stage

    /**
     * Menu kontekstowe wyświetlające się po kliknięciu na wiersz tabeli z planem (na pojedyncze zajęcia)
     */
    lateinit var menu: MFXContextMenu

    /**
     * Flaga informująca o wyświetleniu okna dialogowego z komunikatem.
     */
    private var messageShown = false

    /**
     * Flaga informująca o tym czy ostatnio wyświetlany plan dotyczył grupy.
     */
    var showGroupPlanPressed = false

    /**
     * Flaga informująca o tym czy ostatnio wyświetlany plan dotyczył nauczyciela.
     */
    var showTeacherPlanPressed = false

    val fieldsModel = FieldsModel()
    val teachersModel = TeachersModel()
    val plansModel = PlansModel()


    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize() {
        TabObserver.addObserver(this)
        FieldsModel.addObserver(this)
        TeachersModel.addObserver(this)

        setupTable()
        menu = MFXContextMenu(planTableView)
        setOnClassesSelected()
        setActions()
        setTexts()
        setListeners()
        setUpUI()
    }

    /**
     * Ustawia komponenty interfejsu graficznego
     */
    override fun setUpUI()
    {
        fieldOfStudyChoiceBox.items = fieldsModel.getFields().map { it.fieldName }.let { fieldList -> FXCollections.observableArrayList(fieldList) }
        teacherChoiceBox.items = teachersModel.getTeachers().map { "${it.lastname} ${it.firstname}" }.let { teacherList -> FXCollections.observableArrayList(teacherList) }
        dayChoiceBoxGroup.items = FXCollections.observableArrayList(MessageBundle.getMess("label.friday"), MessageBundle.getMess("label.saturday"), MessageBundle.getMess("label.sunday"), MessageBundle.getMess("label.wholePlan"))
        dayChoiceBoxTeacher.items = FXCollections.observableArrayList(MessageBundle.getMess("label.friday"), MessageBundle.getMess("label.saturday"), MessageBundle.getMess("label.sunday"), MessageBundle.getMess("label.wholePlan"))
        dayChoiceBoxGroup.isDisable = true
        dayChoiceBoxTeacher.isDisable = true
    }

    /**
     * Definiuje akcje dla kontrolek
     */
    override fun setActions()
    {
        showPlanButtonGroup.setOnAction { showGroupPlan() }
        showPlanButtonTeacher.setOnAction { showTeacherPlan() }
        exportGroupPlanButton.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.group.askBeforeExport"), ActionType.EXPORT)
            exportGroupPlan()
        }
        exportPlansForAllGroups.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.groups.askBeforeExport"), ActionType.EXPORT)
            exportAllGroupsFromField()
        }
        exportTeacherPlanButton.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.teacher.askBeforeExport"), ActionType.EXPORT)
            exportTeacherPlan()
        }
        exportPlansForAllTeachers.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.teacher.askBeforeExportAll"), ActionType.EXPORT)
            exportAllTeachers()
        }
    }

    /**
     * Ustawia nasłuchiwanie na kontrolkach (odpowiednie kontrolki odpowiednio się czyszczą lub dodają obiekty)
     */
    override fun setListeners()
    {
        fieldOfStudyChoiceBox.valueProperty().addListener { _, _, new ->
            dayChoiceBoxGroup.isDisable = false
            dayChoiceBoxGroup.items = FXCollections.observableArrayList(MessageBundle.getMess("label.friday"), MessageBundle.getMess("label.saturday"), MessageBundle.getMess("label.sunday"), MessageBundle.getMess("label.wholePlan"))

            if (new != null)
            {
                CommonUtils.clearBox(teacherChoiceBox)
                CommonUtils.clearBox(dayChoiceBoxTeacher)
                CommonUtils.clearBox(dayChoiceBoxGroup)
            }

        }

        groupChoiceBox.valueProperty().addListener { _, _, _ ->
            CommonUtils.clearBox(dayChoiceBoxGroup)
        }

        teacherChoiceBox.valueProperty().addListener { _, _, new ->
            dayChoiceBoxTeacher.isDisable = false
            dayChoiceBoxTeacher.items = FXCollections.observableArrayList(MessageBundle.getMess("label.friday"), MessageBundle.getMess("label.saturday"), MessageBundle.getMess("label.sunday"), MessageBundle.getMess("label.wholePlan"))

            if (new != null)
            {
                CommonUtils.clearBox(fieldOfStudyChoiceBox)
                CommonUtils.clearBox(groupChoiceBox)
                CommonUtils.clearBox(dayChoiceBoxGroup)
            }
        }


        fieldOfStudyChoiceBox.valueProperty().addListener { _, _, _ ->
            groupChoiceBox.items.clear()
            CommonUtils.clearBox(groupChoiceBox)

            if (!fieldOfStudyChoiceBox.value.isNullOrBlank()) groupChoiceBox.items = fieldsModel.getGroups(fieldOfStudyChoiceBox.value)
        }
    }

    /**
     * Ustawia teksty dla kontrolek
     */
    override fun setTexts()
    {
        fieldOfStudyChoiceBox.floatingText = MessageBundle.getMess("label.chooseField")
        groupChoiceBox.floatingText = MessageBundle.getMess("label.chooseGroup")
        dayChoiceBoxGroup.floatingText = MessageBundle.getMess("label.chooseDay")
        dayChoiceBoxTeacher.floatingText = MessageBundle.getMess("label.chooseDay")
        exportGroupPlanButton.text = MessageBundle.getMess("label.exportGroupPlan")
        exportPlansForAllGroups.text = MessageBundle.getMess("label.exportGroupsPlans")
        exportPlansForAllTeachers.text = MessageBundle.getMess("label.exportTeachersPlans")
        exportTeacherPlanButton.text = MessageBundle.getMess("label.exportTeacherPlan")
        teacherChoiceBox.floatingText = MessageBundle.getMess("label.chooseTeacher")
        showPlanButtonGroup.text = MessageBundle.getMess("label.showPlan")
        showPlanButtonTeacher.text = MessageBundle.getMess("label.showPlan")
    }

    /**
     * Obsługuje akcje wywoływane po kliknięciu na zajęcia w tabeli.
     */
    override fun setOnClassesSelected() {
        CommonUtils.setOnItemSelected(planTableView, menu) { selectedRowIndex ->
            showContextMenu(selectedRowIndex, planTableView.items[selectedRowIndex])
        }
    }

    /**
     * Metoda wywoływana podczas zmiany zakładek przez użytkownika - czyści panel.
     */
    override fun onTabsChanged() {
        planTableView.items.clear()
        clearAllBoxes()
    }

    /**
     * Metoda służąca do czyszczenia kontrolek
     */
    override fun clearAllBoxes()
    {
        CommonUtils.clearBox(teacherChoiceBox)
        CommonUtils.clearBox(groupChoiceBox)
        CommonUtils.clearBox(dayChoiceBoxGroup)
        CommonUtils.clearBox(dayChoiceBoxTeacher)
        CommonUtils.clearBox(fieldOfStudyChoiceBox)
    }


    /**
     * Wyświetla menu kontekstowe dla wybranych zajęć z tabeli.
     *
     * @param selectedRowIndex Indeks zaznaczonego wiersza w tabeli.
     * @param selectedItem       Wybrane zajęcia.
     */
    override fun showContextMenu(selectedRowIndex: Int, selectedItem: ClassesToRead) {

        val deleteButton = MFXContextMenuItem(MessageBundle.getMess("label.deleteFromPlan"))
        val changeTeacherButton = MFXContextMenuItem(MessageBundle.getMess("label.changeTeacher"))
        val changeRoomButton = MFXContextMenuItem(MessageBundle.getMess("label.changeRoom"))
        val changeHourButton = MFXContextMenuItem(MessageBundle.getMess("label.changeHour"))

        deleteButton.graphic =  MFXFontIcon("fas-trash-can", 16.0, Color.BLACK)
        changeTeacherButton.graphic =  MFXFontIcon("fas-arrows-rotate", 16.0, Color.BLACK)
        changeRoomButton.graphic =  MFXFontIcon("fas-arrows-rotate", 16.0, Color.BLACK)
        changeHourButton.graphic =  MFXFontIcon("fas-arrows-rotate", 16.0, Color.BLACK)

        deleteButton.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.classes.askBeforeDelete"), ActionType.DELETE)
            deleteFromPlan(selectedItem)
        }

        changeTeacherButton.setOnAction {
            changeTeacher(selectedItem)
        }

        changeRoomButton.setOnAction {
            changeRoom(selectedItem)
        }

        changeHourButton.setOnAction {
            changeHour(selectedItem)
        }

        CommonUtils.showContextMenu(selectedRowIndex, planTableView, menu, listOf(deleteButton, changeTeacherButton, changeRoomButton, changeHourButton))
    }

    /**
     * Metoda wyświetlająca okno dialogowe służące do zmiany godziny i sali (lub w przypadku platformy tylko godziny)
     * Pozwala na zmianę godziny zajęć wraz z możliwością wyboru nowej sali.
     * Po wyborze godziny należy wybrać nową salę.
     * Wyświetlane są tylko te godziny, w których grupa nie ma zajęć oraz nauczyciel nie jest zajęty oraz sale, które znajdują się w tej samej lokalizacji do zmieniana sala.
     *
     * @param classesToEdit Zajęcia, które mają zostać edytowane.
     */
    override fun changeHour(classesToEdit: ClassesToRead) {
        val hourChoicebox = MFXComboBox<String>()
        hourChoicebox.id = "comboWhite"
        val freeRooms = MFXListView<String>()
        val vbox:VBox?

        if (!classesToEdit.room.contains(MessageBundle.getMess("label.virtual")))
        {
            hourChoicebox.items = classesToEdit.getFreeHours()
            freeRooms.setOnMouseClicked {
                val selectedRoom = freeRooms.selectionModel.selectedValue
                if (selectedRoom != null) {
                    showDialogYesNoMessage(MessageBundle.getMess("question.askBeforeRoomAndHourChange"), ActionType.EDIT)
                    if (wantToEdit) {
                        try {
                            classesToEdit.changeHours(selectedRoom, hourChoicebox.value)
                            MessageUtil.showInfoMessage(MessageBundle.getMess("label.changeMade"), MessageBundle.getMess("success.roomHour.correctlyChanged"))
                            showPlanAfterChange(classesToEdit)
                        }catch (e: SQLException) {
                            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.changeHourAndRoomError"))
                        }
                    }
                }
            }

            vbox = createChangeHourVbox(freeRooms, hourChoicebox, classesToEdit)
        }
        else
        {
            hourChoicebox.items = classesToEdit.getFreeHours()
            vbox = createChangeHourPlatformVbox(hourChoicebox, classesToEdit)


        }

        createAndShowDialog(vbox)
    }

    /**
     * Metoda tworząca layout pozwalający na zmianę godziny zajęć na platformie
     * @param hourChoicebox kontrolka wyboru godziny
     * @param classesToEdit edytowane zajęcia
     */
    fun createChangeHourPlatformVbox(hourChoicebox: MFXComboBox<String>, classesToEdit: ClassesToRead):VBox
    {
        val changeButton = MFXButton()
        changeButton.text = MessageBundle.getMess("label.changeHour")
        changeButton.id = "customButton"

        changeButton.setOnAction {
            if (hourChoicebox.value!=null)
            {
                showDialogYesNoMessage(MessageBundle.getMess("question.askBeforeHourChange"), ActionType.EDIT)
                if (wantToEdit) {
                    try {
                        classesToEdit.changeHoursPlatform(hourChoicebox.value)
                        MessageUtil.showInfoMessage(MessageBundle.getMess("label.changeMade"), MessageBundle.getMess("success.hour.correctlyChanged"))
                        showPlanAfterChange(classesToEdit)
                    }catch (e: SQLException) {
                        MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.changeHourError"))
                    }
                }
            }
            else
            {
                MessageUtil.showWarningMessage(MessageBundle.getMess("warning.warning"), MessageBundle.getMess("warning.noHour"))
            }
        }

        hourChoicebox.promptText = MessageBundle.getMess("label.chooseHour")
        hourChoicebox.prefWidth = 300.0
        val vbox = VBox(15.0, hourChoicebox, changeButton)
        vbox.alignment = Pos.CENTER
        return vbox
    }


    /**
     * Tworzy VBox dla okna zmiany godziny i sali (dla lokalizcji różnych niż platforma).
     *
     * @param freeRooms     Lista zawierająca wolne sale.
     * @param hourChoicebox Kontrolka do wyboru nowej godziny zajęć.
     * @param classesToEdit Zajęcia, które mają zostać edytowane
     * @return Panel VBox dla zmiany godziny.
     */
    override fun createChangeHourVbox(freeRooms: MFXListView<String>, hourChoicebox: MFXComboBox<String>, classesToEdit: ClassesToRead): VBox
    {
        freeRooms.styleClass.add("customList2")
        hourChoicebox.promptText = MessageBundle.getMess("label.chooseHour")
        val freeRoomsLabel = Label(MessageBundle.getMess("label.freeRooms"))
        freeRoomsLabel.styleClass.add("header-label-big")

        hourChoicebox.valueProperty().addListener { _, _, new ->
            if (new!=null)
            {
                freeRooms.items = classesToEdit.getFreeRoomsByHour(new)
            }
        }

        val vbox = VBox(15.0, hourChoicebox, freeRoomsLabel,freeRooms)
        vbox.alignment = Pos.CENTER
        freeRooms.prefWidth = 350.0
        hourChoicebox.prefWidth = 300.0

        return vbox
    }


    /**
     * Tworzy VBox dla okna zmiany sali.
     *
     * @param freeRooms     Lista zawierająca wolne sale.
     * @param busyRooms     Lista zawierająca zajęte sale.
     * @return Panel VBox dla zmiany sali.
     */
    override fun createChangeRoomVbox(freeRooms: MFXListView<String>, busyRooms: MFXListView<String>): VBox
    {
        val freeTeachersLabel = Label(MessageBundle.getMess("label.freeRooms"))
        val busyTeachersLabel = Label(MessageBundle.getMess("label.busyRooms"))

        freeRooms.styleClass.add("customList2")
        busyRooms.styleClass.add("customList2")
        freeTeachersLabel.styleClass.add("header-label-big")
        busyTeachersLabel.styleClass.add("header-label-big")

        val vbox = if (freeRooms.items.isEmpty() && busyRooms.items.isEmpty())
        {
            val noRoomsLabel = Label(MessageBundle.getMess("label.noRooms"))
            noRoomsLabel.styleClass.add("header-label-big")
            VBox(noRoomsLabel)
        }
        else{
            val v1 = VBox(15.0, freeTeachersLabel, freeRooms)
            v1.alignment = Pos.CENTER

            val v2 = VBox(15.0, busyTeachersLabel, busyRooms)
            v2.alignment = Pos.CENTER

            val h1 = HBox(20.0, v1,v2)
            v1.prefWidth = 350.0
            v2.prefWidth = 350.0
            VBox(h1)
        }

        vbox.maxWidth = Double.MAX_VALUE
        vbox.alignment = Pos.CENTER

        return vbox
    }

    /**
     * Metoda obsługująca zmianę sal, wyświetla odpowiednie wiadomości w przypadku powodzenia lub błędu
     * @param selectedItem nowa sala, która ma zostać ustawiona
     * @param classesToEdit edytowane zajecia
     * @param func funkcja, która ma zostać wywołana
     */
    private fun handleRoomSelection(selectedItem: String, classesToEdit: ClassesToRead, func: (String) -> Unit) {
        showDialogYesNoMessage(MessageBundle.getMess("question.askBeforeRoomChange"), ActionType.EDIT)
        if (wantToEdit) {
            try {
                func(selectedItem)
                MessageUtil.showInfoMessage(
                    MessageBundle.getMess("label.changeMade"),
                    MessageBundle.getMess("success.room.correctlyChanged")
                )
                showPlanAfterChange(classesToEdit)
            } catch (e: SQLException) {
                MessageUtil.showErrorMessage(
                    MessageBundle.getMess("warning.operationFailed"),
                    MessageBundle.getMess("warning.changeRoomError")
                )
            }
        }
    }

    /**
     * Ustawia akcję po wybraniu sali do zmiany.
     *
     * @param freeRooms     Lista zawierająca wolne sale
     * @param busyRooms     Lista zawierająca zajęte sale
     * @param classesToEdit Zajęcia, które mają zostać edytowane
     */
    override fun setOnRoomToChangeSelected(
        freeRooms: MFXListView<String>,
        busyRooms: MFXListView<String>,
        classesToEdit: ClassesToRead
    )
    {
        freeRooms.setOnMouseClicked {
            val selectedItem = freeRooms.selectionModel.selectedValue
            handleRoomSelection(selectedItem,classesToEdit) { room -> classesToEdit.setAnotherRoom(room)}
        }

        busyRooms.setOnMouseClicked {
            val selectedItem = busyRooms.selectionModel.selectedValue
            handleRoomSelection(selectedItem,classesToEdit) { room -> classesToEdit.changeRooms(room)}
        }
    }


    /**
     * Metoda wyświetlająca okno dialogowe służące do zmiany sali na wybranych zajęciach.
     * Metoda wyświetla tylko te sale, które są wolne lub zajęte przez inną grupę.
     *
     * @param classesToEdit Zajęcia, które mają zostać edytowane.
     */
    override fun changeRoom(classesToEdit: ClassesToRead) {
        val freeRooms = MFXListView<String>()
        val busyRooms = MFXListView<String>()

        freeRooms.items = classesToEdit.getFreeRooms()
        busyRooms.items = classesToEdit.getBusyRooms()

        val vbox = createChangeRoomVbox(freeRooms, busyRooms)
        setOnRoomToChangeSelected(freeRooms, busyRooms, classesToEdit)
        createAndShowDialog(vbox)
    }

    /**
     * Metoda obsługująca zmianę nauczycieli, wyświetla odpowiednie wiadomości w przypadku powodzenia lub błędu
     * @param selectedItem nowy nauczyciel, który ma zostać ustawiony
     * @param classesToEdit edytowane zajecia
     * @param changeTeacher funkcja, która ma zostać wywołana
     */
    private fun handleTeacherSelection(selectedItem: String, classesToEdit: ClassesToRead, changeTeacher: (String, String) -> Unit) {
        showDialogYesNoMessage(MessageBundle.getMess("question.teacher.askBeforeChange"), ActionType.EDIT)
        if (wantToEdit) {
            try {
                changeTeacher(classesToEdit.teacher, selectedItem)
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.changeMade"), MessageBundle.getMess("success.teacher.correctlyChanged"))
                showPlanAfterChange(classesToEdit)
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.changeTeacherError"))
            }

        }
    }

    /**
     * Ustawia akcje po wybraniu nauczyciela do zmiany.
     *
     * @param freeTeachers  Lista zawierająca wolnych nauczycieli
     * @param busyTeachers  Lista zawierająca zajętych nauczycieli
     * @param classesToEdit Zajęcia, które mają zostać edytowane
     */
    override fun setOnTeacherToChangeSelected(
        freeTeachers: MFXListView<String>,
        busyTeachers: MFXListView<String>,
        classesToEdit: ClassesToRead
    )
    {
        freeTeachers.setOnMouseClicked {
            val selectedItem = freeTeachers.selectionModel.selectedValue
            handleTeacherSelection(selectedItem,classesToEdit) { teacher, selected-> classesToEdit.setAnotherTeacher(teacher,selected)}
        }

        busyTeachers.setOnMouseClicked {
            val selectedItem = busyTeachers.selectionModel.selectedValue
            handleTeacherSelection(selectedItem,classesToEdit) { teacher, selected -> classesToEdit.changeTeachers(teacher,selected)}
        }
    }


    /**
     * Wyswietla plan dla nauczyciela lub grupy w zależności, który plan był wyświetlany przed dokonaniem zmiany zajęć
     * @param classesToEdit edytowane zajęcia
     */
    fun showPlanAfterChange(classesToEdit: ClassesToRead)
    {
        dialog.close()
        if (showGroupPlanPressed) planTableView.items = plansModel.getPlanGroup(classesToEdit.group.split(", ")[1], classesToEdit.group.split(", ")[0], MessageBundle.getMess("label.wholePlan"))
        else if (showTeacherPlanPressed) planTableView.items = plansModel.getPlanTeacher(classesToEdit.teacher, MessageBundle.getMess("label.wholePlan"))
    }

    /**
     * Tworzy VBox dla okna zmiany nauczyciela.
     * @param freeTeachers  Lista zawierająca wolnych nauczycieli
     * @param busyTeachers  Lista zawierająca zajętych nauczycieli
     * @return Panel VBox dla zmiany nauczyciela.
     */
    override fun createChangeTeacherVbox(freeTeachers: MFXListView<String>, busyTeachers: MFXListView<String>): VBox
    {
        val freeTeachersLabel = Label(MessageBundle.getMess("label.freeTeachers"))
        val busyTeachersLabel = Label(MessageBundle.getMess("label.busyTeachers"))
        freeTeachersLabel.styleClass.add("header-label-big")
        busyTeachersLabel.styleClass.add("header-label-big")
        freeTeachers.styleClass.add("customList2")
        busyTeachers.styleClass.add("customList2")

        val vbox = if (freeTeachers.items.isEmpty() && busyTeachers.items.isEmpty())
        {
            val noAvailabilityLabel = Label(MessageBundle.getMess("warning.noTeachers"))
            noAvailabilityLabel.styleClass.add("header-label-big")
            VBox(noAvailabilityLabel)
        }
        else{
            val v1 = VBox(15.0, freeTeachersLabel, freeTeachers)
            v1.alignment = Pos.CENTER

            val v2 = VBox(15.0, busyTeachersLabel, busyTeachers)
            v2.alignment = Pos.CENTER

            val h1 = HBox(20.0, v1,v2)
            v1.prefWidth = 350.0
            v2.prefWidth = 350.0
            VBox(h1)
        }

        vbox.maxWidth = Double.MAX_VALUE
        vbox.alignment = Pos.CENTER

        return vbox
    }



    /**
     * Metoda wyświetlająca okno dialogowe służące do zmiany nauczyciela na wybranych zajęciach.
     * Metoda wyświetla tylko tych nauczycieli, którzy są wolni tym czasie i uczą wybranego przedmiotu
     * lub tych, którzy są zajęci i uczą wybranego przedmiotu oraz nauczyciel z zajęć do zmiany uczy przedmiotu drugiego nauczyciela.
     * Dodatkowo z list usuwani są nauczyciele, którzy nie zdążą zmienić lokalizacji pomiędzy zajęciami
     *
     * @param classesToEdit Zajęcia, które mają zostać edytowane.
     */
    override fun changeTeacher(classesToEdit: ClassesToRead) {
        val freeTeachers = MFXListView<String>()
        val busyTeachers = MFXListView<String>()

        //Przedmiot którego aktualnie uczy nauczyciel musi być nauczany przez nauczyciela do zmiany
        val subjects = teachersModel.getTeacherSubjectsByName(classesToEdit.teacher)
        freeTeachers.items = classesToEdit.getFreeTeachers()
        busyTeachers.items = classesToEdit.getBusyTeachers(subjects)

        createChangeTeacherVbox(freeTeachers, busyTeachers)
        setOnTeacherToChangeSelected(freeTeachers, busyTeachers, classesToEdit)
        val vbox = createChangeTeacherVbox(freeTeachers, busyTeachers)

        createAndShowDialog(vbox)

    }

    /**
     * Tworzy i wyświetla customowe okno dialogowe.
     *
     * @param vbox Kontener z zawartością okna dialogowego.
     */
    override fun createAndShowDialog(vbox: VBox) {
        vbox.alignment = Pos.CENTER
        dialog = DialogUtils.showCustomDialog(stage, vbox) {
            planTableView.selectionModel.clearSelection(); menu.hide(); messageShown=false
        }

        dialog.showAndWait()

    }

    /**
     * Metoda usuwająca wybrane zajęcia z planu
     */
    override fun deleteFromPlan(classesToDelete: ClassesToRead) {
        planTableView.selectionModel.clearSelection()
        menu.hide()

        if (wantToDelete)
        {
            try {
                classesToDelete.deleteClasses()
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.plan.correctlyDeletedClassesFromPlan"))
                if (showGroupPlanPressed) planTableView.items = plansModel.getPlanGroup(classesToDelete.group.split(", ")[1], classesToDelete.group.split(", ")[0], MessageBundle.getMess("label.wholePlan"))
                else if (showTeacherPlanPressed) planTableView.items = plansModel.getPlanTeacher(classesToDelete.teacher,  MessageBundle.getMess("label.wholePlan"))
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.deleteClassesFromPlanError"))
            }

        }
    }

    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content Wiadomość do wyświetlenia.
     * @param type flagi do ustawienia
     */
    fun showDialogYesNoMessage(content: String, type: ActionType) {

        val buttons = listOf(
            MessageBundle.getMess("label.yes") to {
                when (type) {
                    ActionType.EXPORT -> wantToExport = true
                    ActionType.EDIT -> wantToEdit = true
                    ActionType.DELETE -> wantToDelete = true
                }
            },
            MessageBundle.getMess("label.no") to {
                when (type) {
                    ActionType.EXPORT -> wantToExport = false
                    ActionType.EDIT -> wantToEdit = false
                    ActionType.DELETE -> wantToDelete = false
                }
            }
        )

        dialogMess = DialogUtils.showMessageDialogWithButtons(content, stage, buttons)
        if (dialogMess.owner.isShowing) dialogMess.showAndWait()
    }


    /**
     * Metoda wyświetlająca plan dla wybranej grupy
     */
    override fun showGroupPlan() {
        showGroupPlanPressed = true
        showTeacherPlanPressed = false
        if(!fieldOfStudyChoiceBox.value.isNullOrBlank() && !groupChoiceBox.value.isNullOrBlank() && !dayChoiceBoxGroup.value.isNullOrBlank())
        {
            planTableView.items = plansModel.getPlanGroup(fieldOfStudyChoiceBox.value, groupChoiceBox.value, dayChoiceBoxGroup.value)

            if (planTableView.items.size == 0) showEmptyPlanMessage()
            else{
                CommonUtils.clearBox(fieldOfStudyChoiceBox)
                CommonUtils.clearBox(groupChoiceBox)
                CommonUtils.clearBox(dayChoiceBoxGroup)
            }
        }
        else{
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.showPlanError"), MessageBundle.getMess("warning.valuesCanNotBeEmpty"))
        }
    }

    /**
     * Metoda wyświetlająca plan dla wybranego nauczyciela.
     */
    override fun showTeacherPlan() {
        showGroupPlanPressed = false
        showTeacherPlanPressed = true

        if(!teacherChoiceBox.value.isNullOrBlank() && !dayChoiceBoxTeacher.value.isNullOrBlank())
        {
            planTableView.items = plansModel.getPlanTeacher(teacherChoiceBox.value, dayChoiceBoxTeacher.value)

            if (planTableView.items.size == 0) showEmptyPlanMessage()
            else{
                CommonUtils.clearBox(teacherChoiceBox)
                CommonUtils.clearBox(dayChoiceBoxTeacher)
            }

        }
        else{
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.showPlanError"), MessageBundle.getMess("warning.valuesCanNotBeEmpty"))
        }
    }

    /**
     * Metoda wyświetlająca wiadomość informującą o pustym planie
     */
    override fun showEmptyPlanMessage() {
        MessageUtil.showWarningMessage(MessageBundle.getMess("warning.noPlan"), MessageBundle.getMess("warning.noPlanInGivenDay"))
        planTableView.items.clear()
    }

    /**
     * Metoda do eksportowania planów zajęć dla wszystkich nauczycieli.
     */
    override fun exportAllTeachers() {
        if (wantToExport)
        {
            val prop = ExcelUtils.loadConfigProps()
            val path = System.getProperty("user.home") + prop.getProperty("excel.plans.teachers.path")

            for(teacher in teacherChoiceBox.items)
            {
                val filePath = "$path/$teacher.xlsx"
                val folder = File(path)
                var myWorkBook:Workbook? = null
                if (!folder.exists()) folder.mkdirs()
                var fos: FileOutputStream? = null

                try {
                    //Dla każdego nauczyciela pobierz plan
                    val plan = plansModel.getPlanTeacher(teacher, MessageBundle.getMess("label.wholePlan"))

                    if (plan.size!=0)
                    {
                        myWorkBook = ExcelUtils.createWorkbook(filePath)
                        val headerCellStyle = ExcelUtils.createHeaderStyle(myWorkBook)
                        val cellStyle = ExcelUtils.createCellStyle(myWorkBook)
                        val planName = ExcelUtils.createPlanName(plan[0].date)
                        val sheet = ExcelUtils.createSheet(myWorkBook, planName)
                        ExcelUtils.createTitle(sheet, headerCellStyle!!, teacher, planTableView.tableColumns.size)
                        ExcelUtils.fillHeaders(sheet, headerCellStyle, planTableView)
                        fillTableAllGroups(sheet, cellStyle!!, plan)
                        ExcelUtils.setColumnsSize(sheet, planTableView)
                        fos = FileOutputStream(File(filePath))
                        myWorkBook.write(fos)
                    }
                }
                catch (e: IOException) {
                    MessageUtil.showErrorMessage(MessageBundle.getMess("warning.noSave"), MessageBundle.getMess("warning.fileAlreadyOpened"))
                }
                finally {
                    myWorkBook?.close()
                    fos?.close()
                }

            }

            MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), "${MessageBundle.getMess("success.teacher.correctlyExportedAll")}: $path")
        }
    }

    /**
     * Metoda do eksportowania planu zajęć dla wybranego nauczyciela.
     */
    override fun exportTeacherPlan() {
        if (wantToExport) {
            if (planTableView.items.isEmpty()) MessageUtil.showInfoMessage(MessageBundle.getMess("warning.noPlan"), MessageBundle.getMess("warning.teacher.noPlan"))
            else{
                GlobalScope.launch(Dispatchers.JavaFx)
                {
                    val prop = ExcelUtils.loadConfigProps()
                    val path = System.getProperty("user.home") + prop.getProperty("excel.plans.teachers.path")
                    val teacher = planTableView.items[0].teacher
                    val filePath = "$path/$teacher.xlsx"
                    val folder = File(path)
                    if (!folder.exists()) folder.mkdirs()
                    var myWorkBook:Workbook? = null
                    var fos: FileOutputStream? = null

                    try {
                        myWorkBook = ExcelUtils.createWorkbook(filePath)
                        val headerCellStyle = ExcelUtils.createHeaderStyle(myWorkBook) //Styl dla nagłówka
                        val cellStyle = ExcelUtils.createCellStyle(myWorkBook) //Styl dla zwykłej komórki
                        val planName = ExcelUtils.createPlanName(planTableView.items[0].date)
                        val sheet = ExcelUtils.createSheet(myWorkBook, planName)
                        ExcelUtils.createTitle(sheet, headerCellStyle!!, planTableView.items[0].teacher, planTableView.tableColumns.size)
                        ExcelUtils.fillHeaders(sheet, headerCellStyle, planTableView)
                        fillTable(sheet, cellStyle!!, sheet.lastRowNum)
                        ExcelUtils.setColumnsSize(sheet, planTableView)

                        fos = FileOutputStream(File(filePath))
                        myWorkBook.write(fos)

                        MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), "${MessageBundle.getMess("success.teacher.correctlyExported")}: $path")
                    } catch (e: IOException) {
                        MessageUtil.showErrorMessage(MessageBundle.getMess("warning.noSave"), MessageBundle.getMess("warning.fileAlreadyOpened"))
                    }
                    finally {
                        fos?.close()
                        myWorkBook?.close()
                    }
                }
            }

        }
    }

    /**
     * Wypełnia arkusz danymi planem przy eksportowaniu wszystkich grup (korzysta z planu z bazy danych a nie z tabeli w aplikacji).
     *
     * @param sheet       Obiekt XSSFSheet, na którym zostaną umieszczone dane.
     * @param cellStyle   Styl komórek, który ma zostać zastosowany do wypełniania danych.
     * @param plan        Plan zajęć w formie listy zajęć.
     */
    override fun fillTableAllGroups(sheet: XSSFSheet, cellStyle: XSSFCellStyle, plan:ObservableList<ClassesToRead>) {
        val startRow = sheet.lastRowNum

        for ((rowIndex, classes) in plan.withIndex()) {
            val row = sheet.createRow(rowIndex + startRow+1)
            createCells(row, classes, cellStyle)
        }
    }

    /**
     * Tworzy komórki dla danego wiersza na podstawie przesłanych danych.
     *
     * @param row        Wiersz, dla którego mają zostać utworzone komórki.
     * @param classes    Obiekt reprezentujący pojedyncze zajęcia.
     * @param cellStyle  Styl komórek, który ma zostać zastosowany do wypełniania danych.
     */
    override fun createCells(row: XSSFRow, classes: ClassesToRead, cellStyle: XSSFCellStyle)
    {
        row.createCell(1).setCellValue(classes.date.toString())
        row.createCell(2).setCellValue(classes.hour)
        row.createCell(3).setCellValue(classes.subject)
        row.createCell(4).setCellValue(classes.room)
        row.createCell(5).setCellValue(classes.teacher)
        row.createCell(6).setCellValue(classes.group)

        for (i in 1..6) row.getCell(i).cellStyle = cellStyle
    }

    /**
     * Wypełnia arkusz planem z tabeli w aplikacji
     *
     * @param sheet     Obiekt XSSFSheet, na którym zostaną umieszczone dane.
     * @param cellStyle Styl komórek, który ma zostać zastosowany do wypełniania danych.
     * @param startNr   Numer wiersza, od którego ma rozpocząć się wypełnianie danych.
     */
    override fun fillTable(sheet: XSSFSheet, cellStyle: XSSFCellStyle, startNr: Int) {

        for ((rowIndex, classes) in planTableView.items.withIndex()) {
            val row = sheet.createRow(rowIndex + startNr + 1)
            createCells(row, classes, cellStyle)
            if (rowIndex + startNr + 2 < sheet.lastRowNum) sheet.shiftRows(rowIndex + startNr + 2, sheet.lastRowNum, 1)
        }
    }

    /**
     * Metoda do eksportowania planów zajęć dla wszystkich grup z wybranego kierunku.
     */
    override fun exportAllGroupsFromField() {
        if (wantToExport) {
            if (fieldOfStudyChoiceBox.value != null)
            {
                val field = fieldOfStudyChoiceBox.value
                val prop = ExcelUtils.loadConfigProps()
                val path = System.getProperty("user.home") + prop.getProperty("excel.plans.groups.path")
                val filePath = "$path/$field.xlsx"
                val folder = File(path)

                //jeśli folder nie istnieje to go stwórz
                if (!folder.exists()) folder.mkdirs()

                val myWorkBook = ExcelUtils.createWorkbook(filePath)
                val headerCellStyle = ExcelUtils.createHeaderStyle(myWorkBook) //Styl dla nagłówka
                val cellStyle = ExcelUtils.createCellStyle(myWorkBook) //Styl dla zwykłej komórki

                val maxDay = plansModel.getMaxDateFromTable()
                val planStart = CommonUtils.getPlanStartDay(maxDay)
                val planName = ExcelUtils.createPlanName(planStart)
                val sheet = ExcelUtils.createSheet(myWorkBook, planName)

                for (group in groupChoiceBox.items)
                {
                    try {
                        val planForGroup = plansModel.getPlanGroup(fieldOfStudyChoiceBox.value, group, MessageBundle.getMess("label.wholePlan"))
                        if (!planForGroup.isEmpty())
                        {
                            val title = "$group $field"
                            ExcelUtils.createTitle(sheet, headerCellStyle!!, title, planTableView.tableColumns.size, sheet.lastRowNum+3)
                            ExcelUtils.fillHeaders(sheet, headerCellStyle, planTableView, sheet.lastRowNum+1)
                            fillTableAllGroups(sheet, cellStyle!!, planForGroup)
                            ExcelUtils.setColumnsSize(sheet, planTableView)

                            val fos = FileOutputStream(File(filePath))
                            myWorkBook.write(fos)
                            fos.close()
                        }

                    }
                    catch (e: IOException) {
                        MessageUtil.showErrorMessage(MessageBundle.getMess("warning.noSave"), MessageBundle.getMess("warning.fileAlreadyOpened"))
                    }
                }

                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), "${MessageBundle.getMess("success.plan.correctlyExportedAll")} $path")
            }

            else{
                MessageUtil.showInfoMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.plan.shouldChooseField"))
            }
        }
    }

    /**
     * Metoda do eksportowania aktualnie wyświetlanego planu zajęć dla grupy.
     */
    override fun exportGroupPlan() {
            if (wantToExport)
            {
                if (planTableView.items.isEmpty()) MessageUtil.showInfoMessage(MessageBundle.getMess("warning.noPlan"), MessageBundle.getMess("warning.group.noPlan"))
                else{
                    val prop = ExcelUtils.loadConfigProps()
                    val path = System.getProperty("user.home") + prop.getProperty("excel.plans.groups.path")
                    val group = planTableView.items[0].group.split(", ")[0]
                    val field = planTableView.items[0].group.split(", ")[1]

                    //nazwa pliku to nazwa kierunku
                    val filePath = "$path/$field.xlsx"
                    val folder = File(path)

                    if (!folder.exists()) folder.mkdirs()

                    GlobalScope.launch(Dispatchers.JavaFx)
                    {
                        var myWorkBook:Workbook?=null
                        var fos:FileOutputStream?=null
                        try {
                            myWorkBook = ExcelUtils.createWorkbook(filePath)
                            val headerCellStyle = ExcelUtils.createHeaderStyle(myWorkBook)
                            val cellStyle = ExcelUtils.createCellStyle(myWorkBook)


                            val planName = ExcelUtils.createPlanName(planTableView.items[0].date)

                            //szukaj czy nie ma już takiej zakładki (gdy zakładka istnieje to ją pobierz)
                            val sheet = ExcelUtils.createOrGetSheet(myWorkBook, planName)

                            val startRow = ExcelUtils.findCorrectPlaceForGroup(sheet, group).first
                            val groupExistsaInExcel =  ExcelUtils.findCorrectPlaceForGroup(sheet, group).second

                            //Jeśli grupa istnieje to najpierw usuń stary plan
                            //Należy przesunąć wiersze (chyba że to koniec pliku)
                            if (startRow != 2 && !groupExistsaInExcel && startRow!=sheet.lastRowNum+3) sheet.shiftRows(startRow+1,sheet.lastRowNum, planTableView.items.size+3)
                            else if (groupExistsaInExcel)
                            {
                                for(j in startRow+1..sheet.lastRowNum)
                                {
                                    val rowToDelete = sheet.getRow(j)
                                    if (rowToDelete != null) {
                                        sheet.removeRow(rowToDelete)
                                        if (j+1 < sheet.lastRowNum) sheet.shiftRows(j + 1, sheet.lastRowNum, -1)
                                    }
                                    else break
                                }
                            }

                            val title = "$group $field"
                            ExcelUtils.createTitle(sheet, headerCellStyle!!, title, planTableView.tableColumns.size, startRow)
                            ExcelUtils.fillHeaders(sheet, headerCellStyle, planTableView, startRow+1)
                            fillTable(sheet, cellStyle!!, startRow+1)
                            ExcelUtils.setColumnsSize(sheet, planTableView)

                            fos = FileOutputStream(File(filePath))
                            myWorkBook.write(fos)

                            MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), "${MessageBundle.getMess("success.group.correctlyExported")}: $path")

                        }
                        catch (e: IOException) {
                            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.noSave"), MessageBundle.getMess("warning.fileAlreadyOpened"))
                        }
                        finally {
                            fos?.close()
                            myWorkBook?.close()
                        }
                    }
                }
            }
    }


    /**
     * Tworzy i konfiguruje tabelę z planem.
     */
    override fun setupTable() {

        val columns = mapOf<MFXTableColumn<ClassesToRead>, KProperty1<ClassesToRead, *>>(
            MFXTableColumn(MessageBundle.getMess("label.day"), false, Comparator.comparing(ClassesToRead::date)) to ClassesToRead::date,
            MFXTableColumn(MessageBundle.getMess("label.hour"), false, Comparator.comparing(ClassesToRead::hour)) to ClassesToRead::hour,
            MFXTableColumn(MessageBundle.getMess("label.subject"), false, Comparator.comparing(ClassesToRead::subject)) to ClassesToRead::subject,
            MFXTableColumn(MessageBundle.getMess("label.room"), false, Comparator.comparing(ClassesToRead::room)) to ClassesToRead::room,
            MFXTableColumn(MessageBundle.getMess("label.teacher"), false, Comparator.comparing(ClassesToRead::teacher)) to ClassesToRead::teacher,
            MFXTableColumn(MessageBundle.getMess("label.group"), false, Comparator.comparing(ClassesToRead::group)) to ClassesToRead::group
        )

        columns.forEach{ column ->
            column.key.rowCellFactory = Function<ClassesToRead, MFXTableRowCell<ClassesToRead?, *>>
            {
                val cell = MFXTableRowCell<ClassesToRead?, Any?>(column.value)
                cell.styleClass.add("table-cell")
                cell
            }

            column.key.alignment = Pos.CENTER
        }

        planTableView.filters.clear()
        //filtry
        //Może powodować bug Invalid range for values: Min[11], Max[10] (błąd w bibliotece MaterialFX), który nie ma wpływu na działanie
        planTableView.filters.addAll(
            StringFilter(MessageBundle.getMess("label.hour"), ClassesToRead::hour),
            StringFilter(MessageBundle.getMess("label.subject"), ClassesToRead::subject),
            StringFilter(MessageBundle.getMess("label.room"), ClassesToRead::room),
            StringFilter(MessageBundle.getMess("label.teacher"), ClassesToRead::teacher),
            StringFilter(MessageBundle.getMess("label.group"), ClassesToRead::group)
        )

        planTableView.tableColumns.addAll(columns.keys)
        var screenWidth = 10.0

        val screenSizes: ObservableList<Screen> = Screen.getScreens()
        screenSizes.forEach(Consumer { screen: Screen -> screenWidth = screen.bounds.width })

        val widths = arrayOf(2,2,4,2,3,2)

        for (i in 0 until planTableView.tableColumns.size) {
            planTableView.tableColumns[i].prefWidth = (widths[i].toDouble()/15)*screenWidth
            planTableView.tableColumns[i].styleClass.add("table-header")
        }

        TableRowStyler.setTableStyle(planTableView)
        planTableView.isFooterVisible = true
    }

    /**
     * Metoda wywoływana podczas dokonania zmiany w tabeli kierunki w bazie danych
     */
    override fun onFieldsChanged() {
        fieldOfStudyChoiceBox.items = fieldsModel.getFields().map { it.fieldName }.let { fieldList -> FXCollections.observableArrayList(fieldList) }
    }

    /**
     * Metoda wywoływana podczas dokonania zmiany w tabeli nauczyciele w bazie danych
     */
    override fun onTeachersChanged() {
        teacherChoiceBox.items = teachersModel.getTeachers().map { "${it.lastname} ${it.firstname}" }.let { teacherList -> FXCollections.observableArrayList(teacherList) }
    }
}