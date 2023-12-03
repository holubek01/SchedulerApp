package com.example.scheduler.controller

import com.example.scheduler.controller.observers.LocationObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.models.LocationsModel
import com.example.scheduler.models.PlansModel
import com.example.scheduler.models.RoomsModel
import com.example.scheduler.objects.PlanForRooms
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.MFXButton
import io.github.palexdev.materialfx.controls.MFXComboBox
import io.github.palexdev.materialfx.controls.MFXTableColumn
import io.github.palexdev.materialfx.controls.MFXTableView
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.materialfx.filter.StringFilter
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.function.Function


/**
 * Klasa kontrolera do wyświetlania i eksportowania planów dla sal
 */
class ShowPlanRoomsController: IShowPlanRoomsController, LocationObserver, TabsObserver{

    /**
     * Tabela służąca do wyświetlania planu dla sal.
     */
    @FXML
    lateinit var planTableView: MFXTableView<PlanForRooms>

    /**
     * Przycisk służący do wyświetlania planu dla sal.
     */
    @FXML
    lateinit var showPlanButton: MFXButton

    /**
     * Przycisk służący do wykonywania eksportu planu dla sal.
     */
    @FXML
    lateinit var exportPlanButton: MFXButton

    /**
     * Kontrolka zawierająca listę lokalizacji
     */
    @FXML
    lateinit var locationChoiceBox: MFXComboBox<String>

    /**
     * Flaga informująca chęci eksportowania planu dla sal.
     */
    var wantToExport = false

    /**
     * Pomocnicze okno dialogowe.
     */
    lateinit var dialog: MFXStageDialog

    /**
     * Scena aplikacji
     */
    lateinit var stage: Stage

    val locationsModel = LocationsModel()
    val roomsModel = RoomsModel()
    val plansModel = PlansModel()


    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize() {
        TabObserver.addObserver(this)
        LocationsModel.addObserver(this)
        setTexts()
        setActions()
        exportPlanButton.isDisable = true
        locationChoiceBox.items = locationsModel.getLocationsNames()
    }

    /**
     * Metoda ustawiająca teskty dla kontrolek
     */
    override fun setTexts()
    {
        locationChoiceBox.floatingText = MessageBundle.getMess("label.chooseLocation")
        showPlanButton.text = MessageBundle.getMess("label.showPlan")
        exportPlanButton.text = MessageBundle.getMess("label.exportPlan")
    }


    /**
     * Metoda tworząca i konfigurująca tabelę z zajęciami (plan).
     *
     * @param rooms     Lista sal w wybranej lokalizacji
     */
    override fun setupTable(rooms: ObservableList<String>) {
        val columns: ObservableList<MFXTableColumn<PlanForRooms>> = FXCollections.observableArrayList()
        val dateColumn: MFXTableColumn<PlanForRooms> = MFXTableColumn(MessageBundle.getMess("label.day"), false, Comparator.comparing(PlanForRooms::date))
        val hourColumn: MFXTableColumn<PlanForRooms> = MFXTableColumn(MessageBundle.getMess("label.hour"), false, Comparator.comparing(PlanForRooms::hour))

        //Pobierz liste sal i zrób z nich kolumny
        for(room in rooms)
        {
            columns.add(MFXTableColumn(room.toString(), true))
        }

        dateColumn.rowCellFactory = Function<PlanForRooms, MFXTableRowCell<PlanForRooms?, *>>
        {
            MFXTableRowCell<PlanForRooms?, Any?>(PlanForRooms::date)
        }

        hourColumn.rowCellFactory = Function<PlanForRooms, MFXTableRowCell<PlanForRooms?, *>>
        {
            MFXTableRowCell<PlanForRooms?, Any?>(PlanForRooms::hour)
        }


        for ((counter, column) in columns.withIndex()) {
            column.rowCellFactory = Function {
                val cell = MFXTableRowCell<PlanForRooms, Any?> { item ->
                    item?.rooms?.getOrNull(counter)
                }
                cell
            }
        }

        //Filtry
        if (planTableView.filters.size>0) planTableView.filters.clear()
        planTableView.filters.addAll(
            StringFilter(MessageBundle.getMess("label.hour"), PlanForRooms::hour),
            StringFilter(MessageBundle.getMess("label.day")) { planForRooms -> planForRooms.date.toString() }
        )

        //Filtry
        for ((index, room) in rooms.withIndex())
        {
            planTableView.filters.add(StringFilter(room) { plan:PlanForRooms -> plan.rooms[index] })
        }


        planTableView.tableColumns.addAll(dateColumn, hourColumn)
        planTableView.tableColumns.addAll(columns)

        for (i in 0 until planTableView.tableColumns.size) {
            planTableView.tableColumns[i].styleClass.add("table-header")
        }

        if (rooms.size==1) planTableView.tableColumns[2].minWidth = 1000.0
        TableRowStyler.setTableStyle(planTableView)
        planTableView.isFooterVisible = true
    }

    /**
     * Ustawia akcje dla kontrolek
     */
    fun setActions()
    {
        exportPlanButton.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.planForRooms.askBeforeExport"))
            exportPlan() }

        showPlanButton.setOnAction { showPlan() }
    }


    /**
     * Metoda do wyświetlania planu zajęć dla sal
     */
    override fun showPlan() {
        exportPlanButton.isDisable = true

        planTableView.items.clear()
        planTableView.tableColumns.clear()

        if (locationChoiceBox.value != null)
        {
            val roomNames = roomsModel.getRooms(locationChoiceBox.value).map { it.roomName }.let { roomNamesList ->
                FXCollections.observableArrayList(roomNamesList)
            }
            setupTable(roomNames)

            //pobieranie planu dla lokalizacji
            val plan = if (locationChoiceBox.value!=MessageBundle.getMess("label.platform")) plansModel.getPlan(locationChoiceBox.value, roomNames)
            else plansModel.getPlanForPlatform()

            planTableView.items = plan

            if (plan.isEmpty())
            {
                MessageUtil.showInfoMessage(MessageBundle.getMess("warning.noPlan"), MessageBundle.getMess("warning.noPlanForLocation"))
                planTableView.items.clear()
            }
            else
            {
                exportPlanButton.isDisable = false
            }
        }
        else
        {
            exportPlanButton.isDisable = true
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.warning"), MessageBundle.getMess("warning.noLocation"))
        }
    }


    /**
     * Metoda do wypełniania tabeli w pliku Excel
     * @param sheet Arkusz, w którym ma zostać wypełniona tabela.
     * @param cellStyle Styl komórki w pliku Excel.
     */
    override fun fillTable(sheet: XSSFSheet, cellStyle: XSSFCellStyle) {
        val startRow = 3
        for ((rowIndex, locationModel) in planTableView.items.withIndex()) {
            val row = sheet.createRow(rowIndex + startRow + 1)
            row.createCell(1).setCellValue(locationModel.date.toString())
            row.createCell(2).setCellValue(locationModel.hour)

            row.getCell(1).cellStyle = cellStyle
            row.getCell(2).cellStyle = cellStyle

            for ((columnIndex, room) in locationModel.rooms.withIndex()) {
                row.createCell(columnIndex + 3).setCellValue(room)
                row.getCell(columnIndex+3).cellStyle = cellStyle
            }
        }
    }

    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content Wiadomość do wyświetlenia.
     */
    fun showDialogYesNoMessage(content: String) {

        val buttons = listOf(
            MessageBundle.getMess("label.yes") to {
               wantToExport = true
            },
            MessageBundle.getMess("label.no") to {
                wantToExport = false
            }
        )

        dialog = DialogUtils.showMessageDialogWithButtons(content, stage, buttons)
        if (dialog.owner.isShowing) dialog.showAndWait()
    }

    /**
     * Metoda do eksportowania planu dla sal.
     */
    override fun exportPlan() {
        GlobalScope.launch(Dispatchers.JavaFx) {
            if (planTableView.items.isEmpty()) MessageUtil.showInfoMessage(MessageBundle.getMess("warning.noPlan"), MessageBundle.getMess("warning.noPlanForLocation"))
            else
            {
                if (wantToExport)
                {
                    var myWorkBook: XSSFWorkbook? = null
                    var fos: FileOutputStream? = null
                    val prop = ExcelUtils.loadConfigProps()
                    val path = System.getProperty("user.home") + prop.getProperty("excel.plans.locations.path")
                    println(path)

                    val filePath = "$path/sale${locationChoiceBox.value}.xlsx"
                    val folder = File(path)

                    if (!folder.exists()) folder.mkdirs()

                    try {
                        myWorkBook = ExcelUtils.createWorkbook(filePath)
                        val headerCellStyle = ExcelUtils.createHeaderStyle(myWorkBook)
                        val cellStyle = ExcelUtils.createCellStyle(myWorkBook)
                        val planName = ExcelUtils.createPlanName(planTableView.items[0].date)

                        val sheet = ExcelUtils.createSheet(myWorkBook, planName)

                        ExcelUtils.createTitle(sheet, headerCellStyle!!, locationChoiceBox.value, planTableView.tableColumns.size)
                        ExcelUtils.fillHeaders(sheet, headerCellStyle, planTableView)
                        fillTable(sheet, cellStyle!!)
                        ExcelUtils.setColumnsSize(sheet, planTableView)

                        fos = FileOutputStream(File(filePath))
                        MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.planForRooms.correctlyExported"))
                        myWorkBook.write(fos)
                    }
                    catch (e: IOException) {
                        MessageUtil.showErrorMessage(MessageBundle.getMess("warning.noSave"), MessageBundle.getMess("warning.fileAlreadyOpened"))
                    }
                    finally {
                        myWorkBook?.close()
                        fos?.close()
                    }

                }
            }
        }
    }

    /**
     * Metoda wywoływana po zmianie lokalizacji w bazie danych (dodanie, usunięcie lub edycja lokalizacji).
     */
    override fun onLocationsChanged() {
        locationChoiceBox.items = locationsModel.getLocationsNames()
    }

    /**
     * Metoda wywoływana po zmianie zakładek - czyści kontrolki
     */
    override fun onTabsChanged() {
        exportPlanButton.isDisable = true
        planTableView.items.clear()
        planTableView.tableColumns.clear()
        CommonUtils.clearBox(locationChoiceBox)
    }
}