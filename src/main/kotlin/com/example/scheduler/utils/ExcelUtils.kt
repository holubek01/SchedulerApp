package com.example.scheduler.utils

import com.example.scheduler.controller.exceptions.IllegalValueException
import com.example.scheduler.objects.Subject
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode
import io.github.palexdev.materialfx.controls.MFXTableView
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.util.*

/**
 * Klasa narzędziowa zawierająca metody do operacji na arkuszach Excel, takie jak wczytywanie,
 * tworzenie i formatowanie arkuszy, a także przetwarzanie danych z plików Excel.
 */
object ExcelUtils {

    /**
     * Wczytuje właściwości z pliku konfiguracyjnego.
     *
     * @return Obiekt Properties zawierający wczytane właściwości.
     */
    fun loadConfigProps(): Properties {
        val prop = Properties()
        val configFile = Paths.get("").toAbsolutePath().resolve("config.properties")
        Files.newInputStream(configFile).use { input -> prop.load(input)}
        return prop
    }

    /**
     * Metoda tworząca nowy lub wczytująca istniejący arkusz Excel na podstawie ścieżki do pliku.
     *
     * @param filePath Ścieżka do pliku Excel.
     * @return Obiekt XSSFWorkbook reprezentujący arkusz Excel.
     */
    fun createWorkbook(filePath: String): XSSFWorkbook {
        val file = File(filePath)

        return if (file.exists()) {
            val fis = FileInputStream(file)
            XSSFWorkbook(fis)
        } else {
            XSSFWorkbook()
        }
    }


    /**
     * Metoda tworząca styl dla nagłówków w arkuszu Excel.
     *
     * @param workbook Obiekt XSSFWorkbook, do którego zostanie dodany styl.
     * @return Obiekt XSSFCellStyle reprezentujący styl nagłówków.
     */
    fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {

        val headerFont: Font = workbook.createFont()
        headerFont.bold = true
        headerFont.fontHeightInPoints = 12.toShort()
        headerFont.color = IndexedColors.BLACK.getIndex()

        val headerCellStyle = workbook.createCellStyle()
        headerCellStyle.fillForegroundColor = IndexedColors.AQUA.getIndex()
        headerCellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        headerCellStyle.fillBackgroundColor = IndexedColors.LIGHT_BLUE.index
        headerCellStyle.setFont(headerFont)
        headerCellStyle.borderBottom = BorderStyle.THIN
        headerCellStyle.borderTop = BorderStyle.THIN
        headerCellStyle.borderLeft = BorderStyle.THIN
        headerCellStyle.borderRight = BorderStyle.THIN
        headerCellStyle.verticalAlignment = VerticalAlignment.CENTER
        headerCellStyle.alignment = HorizontalAlignment.CENTER
        headerCellStyle.dataFormat = workbook.createDataFormat().getFormat("@")

        return headerCellStyle
    }

    /**
     * Metoda tworząca styl dla komórek w arkuszu Excel.
     *
     * @param workBook Obiekt XSSFWorkbook, do którego zostanie dodany styl.
     * @return Obiekt XSSFCellStyle reprezentujący styl komórek.
     */
    fun createCellStyle(workBook: XSSFWorkbook): XSSFCellStyle {
        val style = workBook.createCellStyle()
        style.borderTop = BorderStyle.THIN
        style.borderBottom = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN

        return style
    }


    /**
     * Metoda tworząca nazwę planu (nazwę zakładki) na podstawie podanej daty.
     *
     * @param day Data, na podstawie której ma być utworzona nazwa planu.
     * @return Nazwa planu w formacie plan_dd.MM-dd.MM"
     */
    fun createPlanName(day: LocalDate): String {

        val start = CommonUtils.getPlanStartDay(day)
        val end = start.plusDays(2)

        val startMonth = String.format("%02d", start.monthValue)
        val endMonth = String.format("%02d", end.monthValue)
        val startDay = String.format("%02d", start.dayOfMonth)
        val endDay = String.format("%02d", end.dayOfMonth)

        return "${startDay}.${startMonth}-${endDay}.${endMonth}"
    }


    /**
     * Metoda tworząca nowy lub usuwająca i tworząca nowy arkusz o określonej nazwie w pliku Excel.
     *
     * @param workbook Obiekt XSSFWorkbook, w którym ma być utworzony arkusz.
     * @param planName Nazwa arkusza.
     * @return Obiekt XSSFSheet reprezentujący arkusz.
     */
    fun createSheet(workbook: XSSFWorkbook, planName: String): XSSFSheet {
        val sheet: XSSFSheet?
        val sheetIndex = workbook.getSheetIndex(planName)

        //zakładka nie istnieje
        sheet = if (sheetIndex == -1) {
            workbook.createSheet(planName)
        } else {
            //usuwa i tworzy
            workbook.removeSheetAt(sheetIndex)
            workbook.createSheet(planName)
        }

        return sheet
    }


    /**
     * Metoda tworząca arkusz o określonej nazwie w pliku Excel lub pobierająca istniejący arkusz o tej nazwie.
     *
     * @param workbook Obiekt XSSFWorkbook, w którym ma być utworzony arkusz.
     * @param planName Nazwa arkusza.
     * @return Obiekt XSSFSheet reprezentujący arkusz.
     */
    fun createOrGetSheet(workbook: XSSFWorkbook, planName: String): XSSFSheet {
        val sheet: XSSFSheet?
        val sheetIndex = workbook.getSheetIndex(planName)

        sheet = if (sheetIndex == -1) {
            workbook.createSheet(planName)
        } else {
            workbook.getSheet(planName)
        }

        return sheet
    }

    /**
     * Metoda wyszukująca odpowiednie miejsce dla grupy w arkuszu Excel.
     * Plany dla grup są posortowane rosnąco semestrami a następnie alfabetycznie po oznaczeniach grup (IA,IB,IIA itp)
     *
     * @param sheet Arkusz Excel, w którym jest szukane odpowiednie miejsce.
     * @param group Nazwa grupy, dla której szukane jest odpowiednie miejsce.
     * @return Para, która zawiera indeks wiersza, gdzie należy umieścić plan dla grupy oraz flagę informującą, czy grupa została znaleziona w arkuszu.
     */
    fun findCorrectPlaceForGroup(sheet: XSSFSheet, group: String): Pair<Int, Boolean>
    {
        val startRow = 2
        val startCell = 1

        for (i in startRow..sheet.lastRowNum)
        {
            if (sheet.getRow(i) == null) continue
            val value = sheet.getRow(i).getCell(startCell).stringCellValue

            //Pominięcie nie nagłówków
            if (!value.startsWith("I") && !value.startsWith("V") && !value.startsWith("X")) continue

            val insertedGroupSem = CommonUtils.romanToInt(group.dropLast(1))
            val currentGroupSem = CommonUtils.romanToInt(value.split(" ")[0].dropLast(1))

            val insertedGroupSign = group.last()
            val currentGroupSign = value.split(" ")[0].last()

            //Nie znaleziono grupy w pliku ale znaleziono odpowiednie miejsce do wstawienia planu
            if ((insertedGroupSign < currentGroupSign && insertedGroupSem<=currentGroupSem)|| insertedGroupSem < currentGroupSem)
            {
                return Pair(sheet.getRow(i).getCell(startCell).rowIndex-1, false)
            }

            //Znaleziono plan grupy w pliku (nadpisz go)
            else if (insertedGroupSign == currentGroupSign && insertedGroupSem == currentGroupSem)
            {
                return Pair(sheet.getRow(i).getCell(startCell).rowIndex, true)
            }
        }

        return if (sheet.lastRowNum == -1) Pair(2, false)       //gdy pusty plik to lastRowNum=-1
        else Pair(sheet.lastRowNum+3, false)
    }


    /**
     * Metoda znajdująca istniejący merged region dla określonego wiersza w arkuszu Excel.
     *
     * @param sheet Arkusz Excel.
     * @param row Indeks wiersza.
     * @return Obiekt CellRangeAddress reprezentujący istniejący merged region lub null, jeśli nie istnieje.
     */
    fun findExistingMergedRegion(sheet: XSSFSheet, row: Int): CellRangeAddress? {
        val numMergedRegions = sheet.numMergedRegions
        for (i in 0 until numMergedRegions) {
            val mergedRegion = sheet.getMergedRegion(i)
            if (mergedRegion.isInRange(row, 1)) {
                return mergedRegion
            }
        }
        return null
    }


    /**
     * Metoda zliczająca liczbę semestrów w arkuszu Excel.
     *
     * @param sheet Arkusz Excel.
     * @return Liczba semestrów na kierunku.
     */
    private fun countSemestersNumber(sheet: Sheet): Int
    {
        val startRowNr = 3
        var startCellNr = 3
        var counter = 0
        while (sheet.getRow(startRowNr).getCell(startCellNr).stringCellValue.contains("sem"))
        {
            counter++
            startCellNr++
        }
        return counter
    }


    /**
     * Funkcja umożliwiająca załadowanie pliku Excel.
     *
     * @param uploadSPNToggle Przycisk służący do załadowania i wyświetlania ścieżki do wybranego pliku.
     * @param stage           Główna scena aplikacji.
     */
    fun searchFile(uploadSPNToggle: MFXRectangleToggleNode, stage: Stage)
    {
        uploadSPNToggle.text = ""
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter(MessageBundle.getMess("label.OnlyXLSX"), "*.xlsx"),
            //FileChooser.ExtensionFilter(MessageBundle.getMess("label.OnlyXLS"), "*.xls")
        )
        val selectedFile = fileChooser.showOpenDialog(stage)
        if (selectedFile!=null)
        {
            uploadSPNToggle.text = selectedFile.toString()
            uploadSPNToggle.isSelected = true
        }
    }

    /**
     * Metoda służąca do wczytywania szkolnego planu nauczania.
     * Wczytuje dane z pliku Excel i dodaje je do listy obiektów typu Subject.
     *
     * @param filePath       Ścieżka do pliku Excel, który ma zostać wczytany.
     * @param subjectsToAdd  Lista, do której zostaną dodane obiekty Subject.
     */
    fun uploadSingleSPN(filePath: String, subjectsToAdd: MutableList<Subject>)
    {
        val file = File(filePath)
        var fis:FileInputStream?=null
        var myWorkBook: XSSFWorkbook?=null

        try {
            fis = FileInputStream(file)
            myWorkBook = XSSFWorkbook(fis)
            val sheet = myWorkBook.getSheetAt(0)

            val semCount = countSemestersNumber(sheet)
            var startRowNr = 4
            var startCellNr = 2

            while (sheet.getRow(startRowNr) != null && sheet.getRow(startRowNr).getCell(startCellNr)!=null)
            {
                val subject = sheet.getRow(startRowNr).getCell(startCellNr).stringCellValue
                for (sem in 1..semCount)
                {
                    startCellNr++

                    val hoursWeekly = when (sheet.getRow(startRowNr).getCell(startCellNr).cellType) {
                        CellType.NUMERIC -> sheet.getRow(startRowNr).getCell(startCellNr).numericCellValue.toInt()
                        else -> throw IllegalStateException()   //Jeśli napis zamiast liczby godzin
                    }

                    //Ujemna liczba godzin
                    if (hoursWeekly < 0) throw IllegalValueException(MessageBundle.getMess("warning.shouldBeGreaterThanZero"))
                    if(hoursWeekly != 0) subjectsToAdd.add(Subject(subject,sem, hoursWeekly))
                }
                startCellNr = 2
                startRowNr++
            }
        }
        catch (e: IllegalValueException)
        {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.readError"), e.message!!)
            subjectsToAdd.clear()
        }
        catch (e: IllegalStateException)
        {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.readError"), MessageBundle.getMess("warning.incorrectExcelForm"))
            subjectsToAdd.clear()
        }
        catch (e: IOException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.readError"), MessageBundle.getMess("warning.fileAlreadyOpened"))
            subjectsToAdd.clear()
        }
        finally {
            myWorkBook!!.close()
            fis!!.close()
        }
    }


    /**
     * Metoda tworząca tytuł planu wraz z nadaniem stylu w arkuszu Excel.
     *
     * @param sheet           Arkusz Excel.
     * @param headerCellStyle Obiekt reprezentujący styl nagłówka.
     * @param title           Tytuł, który ma być wyświetlony jako nagłówek.
     * @param columnsNumber   Liczba kolumn, na których ma być wyświetlony tytuł.
     * @param startNr         Numer wiersza, w którym ma być wyświetlony tytuł.
     */
    fun createTitle(sheet: XSSFSheet, headerCellStyle: XSSFCellStyle, title: String, columnsNumber: Int, startNr: Int = 2)
    {
        val startRow = if (startNr == 2) startNr else startNr
        val mergedRow = sheet.createRow(startRow)
        val startCell = 1

        val existingMergedRegion = findExistingMergedRegion(sheet, startRow)
        if (existingMergedRegion != null) {
            val regionIndex = sheet.getMergedRegions().indexOf(existingMergedRegion)
            sheet.removeMergedRegion(regionIndex)
        }

        val mergedCell = mergedRow.createCell(startCell)

        val mergedRegion = CellRangeAddress(startRow,startRow , startCell, columnsNumber)
        mergedCell.cellStyle =  headerCellStyle

        RegionUtil.setBorderTop(BorderStyle.THIN, mergedRegion, sheet)
        RegionUtil.setBorderBottom(BorderStyle.THIN, mergedRegion, sheet)
        RegionUtil.setBorderLeft(BorderStyle.THIN, mergedRegion, sheet)
        RegionUtil.setBorderRight(BorderStyle.THIN, mergedRegion, sheet)

        sheet.addMergedRegion(CellRangeAddress(startRow,startRow , startCell, columnsNumber))
        mergedCell.setCellValue(title)
    }


    /**
     * Metoda wypełniająca nagłówki w arkuszu Excel odpowiednimi danymi.
     *
     * @param sheet       Arkusz Excel.
     * @param headerStyle Obiekt reprezentujący styl nagłówka.
     * @param tableView   Tabela w aplikacji, z której zczytywane są dane do arkusza Excel.
     * @param startNr     Numer wiersza, w którym mają zostać wypełnione nagłówki.
     */
    fun fillHeaders(sheet: XSSFSheet, headerStyle: XSSFCellStyle, tableView: MFXTableView<*>, startNr: Int=3) {
        val startRow = if(startNr==3) startNr else startNr
        val headerRow = sheet.createRow(startRow)

        for (j in tableView.tableColumns.indices) {
            headerRow.createCell(j + 1).setCellValue(tableView.tableColumns[j].text)
            headerRow.getCell(j+1).cellStyle = headerStyle
            sheet.addIgnoredErrors(CellRangeAddress(startRow,startRow , 0, tableView.tableColumns.size),IgnoredErrorType.NUMBER_STORED_AS_TEXT)
        }
    }

    /**
     * Metoda ustawiająca szerokość kolumn w arkuszu Excel.
     *
     * @param sheet       Arkusz Excel.
     * @param tableView   Obiekt klasy MFXTableView reprezentujący tabelę w aplikacji.
     */
    fun setColumnsSize(sheet: XSSFSheet, tableView: MFXTableView<*>) {
        for (j in tableView.tableColumns.indices) {
            val columnWidth = 256
            sheet.setColumnWidth(j + 1, columnWidth)
        }

        for (i in 0 until tableView.tableColumns.size + 2) {
            sheet.autoSizeColumn(i)
        }
    }
}