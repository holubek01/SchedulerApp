package com.example.scheduler.tests.Utils

import com.example.scheduler.models.ClassesToRead
import com.example.scheduler.models.ClassesToWrite
import com.example.scheduler.objects.Subject
import com.example.scheduler.utils.ExcelUtils
import com.example.scheduler.utils.ExcelUtils.createOrGetSheet
import com.example.scheduler.utils.ExcelUtils.createTitle
import com.example.scheduler.utils.ExcelUtils.fillHeaders
import com.example.scheduler.utils.ExcelUtils.findCorrectPlaceForGroup
import com.example.scheduler.utils.ExcelUtils.findExistingMergedRegion
import com.example.scheduler.utils.ExcelUtils.setColumnsSize
import com.example.scheduler.utils.MessageBundle
import io.github.palexdev.materialfx.controls.MFXTableColumn
import io.github.palexdev.materialfx.controls.MFXTableView
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell
import javafx.application.Platform
import javafx.stage.Stage
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.*
import java.util.function.Function
import kotlin.Comparator
import kotlin.reflect.KProperty1

/**
 * Klasa testująca metody do obsługi plików Excel
 */
class ExcelUtilsTest:ApplicationTest() {

    private val testFilePath = "test.xlsx"

    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("pl", "PL"))
    }

    //Testowanie wczytywania pliku konfiguracyjnego
    @Test
    fun testLoadConfigProps() {
        val prop = ExcelUtils.loadConfigProps()
        val path = prop.getProperty("excel.plans.teachers.path")
        assertTrue(path.isNotEmpty())
    }

    //Testowanie tworzenia zakładki o odpowiedniej nazwie
    @Test
    fun testCreateSheet() {
        // Stwórz tymczasowy plik Excel w katalogu roboczym
        val createdWorkbook = ExcelUtils.createWorkbook(testFilePath)
        val sheet = ExcelUtils.createSheet(createdWorkbook, "2023-01-01")
        assertEquals(sheet.sheetName, "2023-01-01", "Names should be same")
    }

    //Testowanie metody tworzącej nazwę planu na podstawie podanego dnia
    //Zawsze powinno zwrócić przedział piątek-niedziela np 02.09-04.09 (od 2 do 4 września)
    //Dla piątek sobota niedziela zawsze powinno zwrócić ten sam plan
    @Test
    fun createPlanNameTest()
    {
        //Gdy piątek
        val friday = LocalDate.of(2023, 9, 8)
        val fridayPlanName = ExcelUtils.createPlanName(friday)

        //Gdy sobota
        val saturday = LocalDate.of(2023, 9, 9)
        val saturdayPlanName = ExcelUtils.createPlanName(saturday)

        //Gdy Niedziela
        val sunday = LocalDate.of(2023, 9, 10)
        val sundayPlanName = ExcelUtils.createPlanName(sunday)

        assertEquals("08.09-10.09", fridayPlanName)
        assertEquals("08.09-10.09", sundayPlanName)
        assertEquals("08.09-10.09", saturdayPlanName)

        //Gdy na przełomie miesięcy (piątek we wrześniu, niedziela w październiku)
        val day = LocalDate.of(2023, 9, 30)
        val planName = ExcelUtils.createPlanName(day)
        assertEquals("29.09-01.10", planName)
    }

    //Testowanie tworzenia stylu dla komórki
    @Test
    fun testCreateCellStyle() {
        val workbook = XSSFWorkbook()
        val cellStyle: XSSFCellStyle? = ExcelUtils.createCellStyle(workbook)
        assertNotNull(cellStyle)

        assertEquals(BorderStyle.THIN, cellStyle!!.borderBottom)
        assertEquals(BorderStyle.THIN, cellStyle.borderTop)
        assertEquals(BorderStyle.THIN, cellStyle.borderLeft)
        assertEquals(BorderStyle.THIN, cellStyle.borderRight)
    }


    //Testowanie tworzenia stylu dla nagłówka
    @Test
    fun testCreateHeaderStyle() {
        val workbook = XSSFWorkbook()
        val headerCellStyle: XSSFCellStyle? = ExcelUtils.createHeaderStyle(workbook)

        assertNotNull(headerCellStyle)
        val font: Font = headerCellStyle!!.font
        assertNotNull(font)
        assertEquals(12.toShort(), font.fontHeightInPoints)
        assertEquals(IndexedColors.BLACK.index, font.color)

        assertEquals(IndexedColors.AQUA.index, headerCellStyle.fillForegroundColor)
        assertEquals(FillPatternType.SOLID_FOREGROUND, headerCellStyle.fillPattern)
        assertEquals(IndexedColors.LIGHT_BLUE.index, headerCellStyle.fillBackgroundColor)
        assertEquals(BorderStyle.THIN, headerCellStyle.borderBottom)
        assertEquals(BorderStyle.THIN, headerCellStyle.borderTop)
        assertEquals(BorderStyle.THIN, headerCellStyle.borderLeft)
        assertEquals(BorderStyle.THIN, headerCellStyle.borderRight)
        assertEquals(VerticalAlignment.CENTER, headerCellStyle.verticalAlignment)
        assertEquals(HorizontalAlignment.CENTER, headerCellStyle.alignment)
    }


    //Testowanie metody znajdującej odpowiednie miejsce w arkuszu na wstawienie planu zajęć dla grupy
    @Test
    fun testGroupPlacement() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Test Sheet")
        val group = "IA"
        val result = findCorrectPlaceForGroup(sheet, group)

        //Arkusz jest pusty, zatem odpowiedni wiersz to wiersz nr 2
        assertEquals(2, result.first)
        assertEquals(false, result.second)
    }

    //Testowanie metody znajdującej odpowiednie miejsce w arkuszu na wstawienie planu zajęć dla grupy
    @Test
    fun testGroupPlacement2() {
        //Test umieszcza plan dla IA i sprawdza czy plan dla IB zostanie wpisany za planem IA
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Test Sheet")
        val group = "IB"
        insertGroupToCell(sheet, 2, 1, "IA")
        insertGroupToCell(sheet, 3, 1, "Tmp")
        val fileOut = FileOutputStream("workbook.xlsx")
        workbook.write(fileOut)
        fileOut.close()

        val result = findCorrectPlaceForGroup(sheet, group)
        assertEquals(6, result.first)
        assertEquals(false, result.second)

        val file = File("workbook.xlsx")
        file.delete()
    }

    //Testowanie metody znajdującej odpowiednie miejsce w arkuszu na wstawienie planu zajęć dla grupy
    @Test
    fun testGroupPlacement3() {
        //Test, który umieszcza plan dla IB i sprawdza czy plan dla IA zostanie wpisany przed planem IB
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Test Sheet")
        val group = "IA"
        insertGroupToCell(sheet, 2, 1, "IB")
        insertGroupToCell(sheet, 3, 1, "Tmp")
        val fileOut = FileOutputStream("workbook.xlsx")
        workbook.write(fileOut)
        fileOut.close()

        val result = findCorrectPlaceForGroup(sheet, group)
        assertEquals(1, result.first)
        assertEquals(false, result.second)

        val file = File("workbook.xlsx")
        file.delete()
    }

    //Test, który umieszcza plan dla IA, IB, IC i sprawdza czy plan dla IB zostanie dobrze wpisany
    @Test
    fun testGroupPlacement4() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Test Sheet")
        val group = "IB"
        insertGroupToCell(sheet, 2, 1, "IA")
        insertGroupToCell(sheet, 3, 1, "Tmp")

        insertGroupToCell(sheet, 6, 1, "IB")
        insertGroupToCell(sheet, 7, 1, "Tmp")

        insertGroupToCell(sheet, 10, 1, "IC")
        insertGroupToCell(sheet, 11, 1, "Tmp")

        val result = findCorrectPlaceForGroup(sheet, group)
        assertEquals(6, result.first)
        assertEquals(true, result.second)

        val file = File("workbook.xlsx")
        file.delete()
    }


    private fun insertGroupToCell(sheet: XSSFSheet, row: Int, cell: Int, group: String) {
        val rowObj = sheet.getRow(row) ?: sheet.createRow(row)
        val cellObj = rowObj.getCell(cell) ?: rowObj.createCell(cell)
        cellObj.setCellValue(group)
    }


    //Testowanie ładowania szkolnego planu nauczania z pliku
    @Test
    fun uploadSPNtest() {
        Platform.runLater {
            XSSFWorkbook()
            val subjectsToAdd: MutableList<Subject> = mutableListOf()
            val stage = Stage()

            assertTrue(subjectsToAdd.isEmpty())
            ExcelUtils.uploadSingleSPN(
                "src/test/kotlin/com/example/scheduler/tests/testResources/test.xlsx",
                subjectsToAdd
            )
            assertFalse(subjectsToAdd.isEmpty())
            assertEquals("Podstawy przedsiębiorczości", subjectsToAdd[0].subjectName)
            assertEquals(1, subjectsToAdd[0].semester)
            assertEquals(1, subjectsToAdd[0].hoursInSemester)
        }
    }

    //Testowanie tworzenia tytułu dla planu dla grupy
    @Test
    fun testCreateTitle() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Test Sheet")
        val title = "IA Kosmetyka"
        createTitle(sheet, ExcelUtils.createHeaderStyle(workbook)!!, title, 6)
        val createdTitle = findExistingMergedRegion(sheet, 2)
        assertTrue(createdTitle!=null)
        assertEquals(sheet.getRow(2).getCell(createdTitle!!.firstColumn).toString(), title)

        val file = File("workbook.xlsx")
        file.delete()
    }


    //Test sprawdza czy nagłówki tabeli w pliku excel utworzą się poprawnie
    @Test
    fun testFillHeaders() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Test Sheet")
        val headerStyle = workbook.createCellStyle()
        val tableView = MFXTableView<ClassesToWrite>()

        val columns = mapOf<MFXTableColumn<ClassesToRead>, KProperty1<ClassesToRead, *>>(
            MFXTableColumn("Day", false, Comparator.comparing(ClassesToRead::date)) to ClassesToRead::date,
            MFXTableColumn("Hour", false, Comparator.comparing(ClassesToRead::hour)) to ClassesToRead::hour,
            MFXTableColumn("Subject", false, Comparator.comparing(ClassesToRead::subject)) to ClassesToRead::subject,
            MFXTableColumn("Room", false, Comparator.comparing(ClassesToRead::room)) to ClassesToRead::room,
            MFXTableColumn("Teacher", false, Comparator.comparing(ClassesToRead::teacher)) to ClassesToRead::teacher,
            MFXTableColumn("Group", false, Comparator.comparing(ClassesToRead::group)) to ClassesToRead::group
        )

        columns.forEach{ column ->
            column.key.rowCellFactory = Function<ClassesToRead, MFXTableRowCell<ClassesToRead?, *>>
            {
                val cell = MFXTableRowCell<ClassesToRead?, Any?>(column.value)
                cell
            }
        }

        val startNr = 3

        fillHeaders(sheet, headerStyle, tableView, startNr)

        val headerRow = sheet.getRow(startNr)
        for (j in tableView.tableColumns.indices) {
            val cell = headerRow.getCell(j + 1)
            assertEquals(tableView.tableColumns[j].text, cell.stringCellValue)
            assertEquals(headerStyle, cell.cellStyle)
        }
    }

    @Test
    fun testSetColumnsSize() {
        val sheet: XSSFSheet = XSSFWorkbook().createSheet("Test Sheet")
        val tableView = MFXTableView<Any>()

        setColumnsSize(sheet, tableView)

        for (j in tableView.tableColumns.indices) {
            val columnWidth = 256
            assert(sheet.getColumnWidth(j + 1) == columnWidth)
        }
    }

    @Test
    fun testCreateOrGetSheet() {
        val workbook = XSSFWorkbook()
        val planName = "Test Sheet"
        val sheet = createOrGetSheet(workbook, planName)
        assertEquals(sheet.sheetName, planName)
    }
}
