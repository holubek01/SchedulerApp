package com.example.scheduler.controller

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.observers.AdminTabsObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.models.FieldsModel
import com.example.scheduler.objects.Subject
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.MFXButton
import io.github.palexdev.materialfx.controls.MFXListView
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.sql.SQLException
import java.util.Locale

/**
 * Klasa kontrolera modułu szkolnych planów nauczania do zarządzania szkolnymi planami nauczania
 */
class SchoolCurriculumUploaderController: AdminTabsObserver, ISchoolCurriculumUploaderController, TabsObserver{

    /**
     * Przycisk do wybierania plików z systemu
     */
    @FXML
    lateinit var chooseFilesButton: MFXButton

    /**
     * Przycisk do załadowywania plików wybranych przez użytkownika
     */
    @FXML
    lateinit var uploadSPNsButton: MFXButton

    @FXML
    lateinit var label: Label

    /**
     * Pomocnicze okno dialogowe.
     */
    private lateinit var dialog: MFXStageDialog

    /**
     * Tabela, przechowująca ścieżki do plików ze szkolnymi planami nauczania, wybranymi przez użytkownika
     */
    @FXML
    lateinit var chosenFilesListView: MFXListView<String>

    /**
     * Przykładowy wzór ze szkolnym planem nauczania
     */
    @FXML
    lateinit var spnExampleImage: ImageView

    /**
     * Flaga informująca o chęci edycji szkolnego planu nauczania.
     */
    var wantToEdit = false

    /**
     * Lista obiektów Subject reprezentujących plan nauczania
     */
    private  var subjectsToAdd: MutableList<Subject> = mutableListOf()

    val fieldsModel = FieldsModel()

    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize() {
        AdminTabObserver.addObserver(this)
        TabObserver.addObserver(this)
        chooseFilesButton.setOnAction { chooseFiles() }
        uploadSPNsButton.setOnAction { uploadSPNs() }

        uploadSPNsButton.text = MessageBundle.getMess("label.loadPlans")
        chooseFilesButton.text = MessageBundle.getMess("label.chooseFiles")
        label.text = MessageBundle.getMess("label.spnExample")
        spnExampleImage.image =
            if(MessageBundle.bundle.locale == Locale("pl", "PL")) Image(SchedulerApp::class.java.getResource("photos/excelSPNexample.png")?.toExternalForm())
            else Image(SchedulerApp::class.java.getResource("photos/excelSPNexampleEnglish.png")?.toExternalForm())
    }

    /**
     * Metoda nadpisująca szkolny plan nauczania nowym planem dla kierunku związanego z nazwą pliku
     * Nadpisanie SPN powoduje usunięcie aktualnego SPN oraz usunięcie z planu
     * wszystkich zajęć związanych z tym kierunkiem, czyli zajęć dla wszystkich grup z kierunku
     */
    override fun uploadSPNs()
    {
        val message = MessageBundle.getMess("warning.showBeforeOverridingSPN")
        showDialogYesNoMessage(message)

        if (wantToEdit)
        {
            for (spn in chosenFilesListView.items) {

                val fieldName = spn.split("\\").last().split(".")[0]
                val fieldExists = fieldsModel.checkIfFieldExists(fieldName)
                if (fieldExists)
                {
                    try {
                        deleteExistingSPN(fieldName)
                        uploadSPNsButton.scene.window as Stage
                        ExcelUtils.uploadSingleSPN(spn, subjectsToAdd)

                        if (subjectsToAdd.isNotEmpty())
                        {
                            try {
                                for (subject in subjectsToAdd)
                                {
                                    fieldsModel.addSubjectToSem(subject.subjectName, fieldName, subject.semester, subject.hoursInSemester)
                                }

                                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), "${MessageBundle.getMess("success.spn.planOverwrittenCorrectly")}: $fieldName")
                            }catch (e: SQLException) {
                                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.addSPNError"))
                                subjectsToAdd.clear()
                            }
                        }

                    }catch (e: SQLException) {
                        MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.deleteSPNError"))
                    }


                }
                else
                {
                    //Kierunek nie istnieje (Nazwa kierunku pobierana jest z końca nazwy pliku - w bazie nie ma takiego kierunku)
                    MessageUtil.showWarningMessage(MessageBundle.getMess("warning.noFieldInDB"), "${MessageBundle.getMess("warning.badFieldNameInFile")}: $fieldName")
                }
            }
            chosenFilesListView.items.clear()
        }
    }

    /**
     * Metoda wywoływana podczas zmiany zakładek przez użytkownika
     */
    override fun onTabsChanged() {
        chosenFilesListView.items.clear()
    }

    /**
     * Metoda pozwalająca na wybór przez użytkownika plików excel z systemu
     */
    override fun chooseFiles()
    {
        chosenFilesListView.items.clear()
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter(MessageBundle.getMess("label.OnlyXLSX"), "*.xlsx"),
            //FileChooser.ExtensionFilter(MessageBundle.getMess("label.OnlyXLS"), "*.xls")
        )
        val selectedFiles = fileChooser.showOpenMultipleDialog(chooseFilesButton.scene.window)

        selectedFiles?.forEach { file -> chosenFilesListView.items.add(file.toString()) }
    }

    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content Wiadomość do wyświetlenia.
     */
    override fun showDialogYesNoMessage(content: String) {

        val stage = uploadSPNsButton.scene.window as Stage
        val buttons = listOf(
            MessageBundle.getMess("label.yes") to { wantToEdit = true },
            MessageBundle.getMess("label.no") to { wantToEdit = false}
        )

        dialog = DialogUtils.showMessageDialogWithButtons(content, stage, buttons)
        if(dialog.owner.isShowing) dialog.showAndWait()
    }


    /**
     * Metoda usuwająca istniejący szkolny plan nauczania związany z danym kierunkiem
     * @param fieldName Nazwa przedmiotu, dla którego usuwany jest szkolny plan nauczania
     */
    override fun deleteExistingSPN(fieldName: String) {
        fieldsModel.deleteSPN(fieldName)
    }
}