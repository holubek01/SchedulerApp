package com.example.scheduler.controller

import com.example.scheduler.SchedulerApp
import com.example.scheduler.models.User
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.MFXButton
import io.github.palexdev.materialfx.controls.MFXPasswordField
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode
import io.github.palexdev.materialfx.controls.MFXTextField
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.SQLException
import java.util.*
import kotlin.system.exitProcess


/**
 * Klasa kontrolera obsługująca logowanie użytkownika
 */
class LoginController: ILoginController {

    /**
     * Pole tekstowe do wprowadzania loginu.
     */
    @FXML
    lateinit var loginTextField: MFXTextField

    /**
     * Pole tekstowe do wprowadzania hasła.
     */
    @FXML
    lateinit var passwordTextField: MFXPasswordField

    /**
     * Pole przechowujące etykietę powitalną.
     */
    @FXML
    lateinit var welcomeLabel: Label

    /**
     * Pole przechowujące etykietę logowania.
     */
    @FXML
    lateinit var logLabel: Label

    /**
     * Przycisk typu toggle służący jako przełącznik języka.
     */
    @FXML
    lateinit var languageToggle: MFXRectangleToggleNode

    /**
     * Przycisk służący do logowania
     */
    @FXML
    lateinit var loginButton: MFXButton

    lateinit var stage: Stage
    lateinit var root: AnchorPane

    /**
     * Okno dialogowe pozwalające na zmianę hasła tymczasowego
     */
    private lateinit var dialog: MFXStageDialog

    /**
     * Okno dialogowe pozwalające na ustawienie hasła do bazy danych
     */
    private lateinit var dbDialog: MFXStageDialog

    /**
     * Pole przechowujące aktualny obiekt Locale, decydujący o języku aplikacji - Domyślnie polski
     */
    private var currentLocale: Locale = Locale("pl", "PL")

    /**
     * Pole tekstowe do wprowadzania nowego hasła.
     */
    val newPassword = MFXPasswordField()

    /**
     * Pole tekstowe do wprowadzania powtórzenia nowego hasła.
     */
    val newPasswordRepeat = MFXPasswordField()

    /**
     * Przycisk do ustawiania nowego hasła do bazy danych
     */
    private var setNewPasswordButton = MFXButton()

    /**
     * Maksymalne wymiary okna aplikacji
     */
    private val MAX_WINDOW_WIDTH = 1280.0
    private val MAX_WINDOW_HEIGHT = 860.0


    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize()
    {
        MessageBundle.loadBundle(currentLocale)
        setActions()
        setHandlers()
        setTexts()

        val img = Image(SchedulerApp::class.java.getResourceAsStream("photos/flagPL.png"))
        languageToggle.labelTrailingIcon = ImageView(img)
    }

    /**
     * Metoda, sprawdzająca, czy hasło do bazy danych istnieje. Jeśli nie istnieje tworzy okno dialogowe do jego podania
     */
    override fun checkDBpasswordExists(){
        val prop = Properties()

        val configFilePath = Paths.get("").toAbsolutePath().resolve("config.properties")

        //Files.newInputStream(Path.of("src/main/resources/config.properties")).use { input -> prop.load(input)}
        Files.newInputStream(configFilePath).use { input -> prop.load(input)}
        val pass = prop.getProperty("db.password")
        if(pass!! == "")
        {
            val vBox = createDbPasswordDialog(prop)
            dbDialog = DialogUtils.showCustomDialog(stage, vBox, true) {}
            dbDialog.content.stylesheets.add(SchedulerApp::class.java.getResource("css/customStyles.css")?.toExternalForm()!!)
            dbDialog.showAndWait()
        }
    }

    /**
     * Metoda ustawiająca teksty na kontrolkach
     */
    override fun setTexts()
    {
        welcomeLabel.text = MessageBundle.getMess("label.welcomeInApp")
        logLabel.text = MessageBundle.getMess("label.logging")
        loginTextField.floatingText = MessageBundle.getMess("label.login")
        passwordTextField.floatingText = MessageBundle.getMess("label.password")
        loginButton.text = MessageBundle.getMess("label.log")
    }

    /**
     * Metoda ustawiająca przycisk Enter jako wowołanie metody onLoginPressed()
     */
    override fun setHandlers()
    {
        loginTextField.addEventHandler(KeyEvent.KEY_RELEASED) { event ->
            if (event.code == KeyCode.ENTER) {
                onLoginPressed()
            }
        }

        passwordTextField.addEventHandler(KeyEvent.KEY_RELEASED) { event ->
            if (event.code == KeyCode.ENTER) {
                onLoginPressed()
            }
        }

        if (this::stage.isInitialized) stage.setOnCloseRequest {
            Platform.exit()
            exitProcess(0)
        }
    }

    /**
     * Metoda ustawiająca akcje na kontrolkach
     */
    override fun setActions()
    {
        loginButton.setOnAction { onLoginPressed()}

        //zmiana języka
        languageToggle.setOnMouseClicked {
            if (currentLocale == Locale("pl", "PL")) {
                currentLocale = Locale("en", "EN")
                languageToggle.text = "English"
                val img = Image(SchedulerApp::class.java.getResourceAsStream("photos/flagGB.png"))
                languageToggle.labelTrailingIcon = ImageView(img)
            } else {
                currentLocale = Locale("pl", "PL")
                languageToggle.text = "Polski"
                val img = Image(SchedulerApp::class.java.getResourceAsStream("photos/flagPL.png"))
                languageToggle.labelTrailingIcon = ImageView(img)
            }

            MessageBundle.loadBundle(currentLocale)
            setTexts()
        }
    }

    /**
     * Metoda pozwalająca za załadowanie głównego okna aplikacji
     */
    override fun loadNewScene(role: Int) {

        GlobalScope.launch(Dispatchers.JavaFx)
        {
            //val loader = ProgressIndicator()
            //loader.setPrefSize(100.0, 100.0)
            //loader.layoutX = loginButton.scene.width  - 100
            //loader.layoutY = loginButton.scene.height - 100
            //root.children.add(loader)

            val screenBounds: javafx.geometry.Rectangle2D = Screen.getPrimary().bounds
            val fxmlLoader = FXMLLoader(SchedulerApp::class.java.getResource("view/directorMain.fxml"))
            val controller = DirectorController()
            controller.stage = stage
            controller.user = loginTextField.text
            fxmlLoader.setController(controller)
            val newRoot: AnchorPane = fxmlLoader.load()
            root.children.setAll(newRoot)
            //stage.width = screenBounds.width+20
            //stage.height = screenBounds.height

            stage.width = MAX_WINDOW_WIDTH + 20.0
            stage.height = MAX_WINDOW_HEIGHT

            stage.maxWidth = MAX_WINDOW_WIDTH + 20.0
            stage.maxHeight = MAX_WINDOW_HEIGHT

            if (screenBounds.width<=MAX_WINDOW_WIDTH && screenBounds.height<MAX_WINDOW_HEIGHT)
            {
                stage.isMaximized = true
            }

            stage.x = (screenBounds.width-MAX_WINDOW_WIDTH)/2
            stage.y = (screenBounds.height-MAX_WINDOW_HEIGHT)/2
            stage.isResizable = true

            val anch = root.children[0] as AnchorPane

            AnchorPane.setLeftAnchor(anch, 0.0)
            AnchorPane.setRightAnchor(anch, 0.0)
            AnchorPane.setTopAnchor(anch, 0.0)
            AnchorPane.setBottomAnchor(anch, 0.0)
        }
    }

    /**
     * Ustawia ograniczenia na pola tekstowe walidujące poprawność haseł.
     * @param newPassword Pole do wpisania nowego hasła
     * @param newPasswordRepeat Pole do wpisania powtórzenia noweg hasła
     */
    override fun setConstraints(newPassword: MFXPasswordField, newPasswordRepeat: MFXPasswordField)
    {
        newPassword.validator.constraint(ValidatorUtil.createNotEmptyConstraint(newPassword.textProperty(), MessageBundle.getMess("password.validation.notEmpty")))
        newPassword.validator.constraint(ValidatorUtil.createMoreThanFiveLetterConstraint(newPassword.textProperty(), MessageBundle.getMess("password.validation.moreThan6Letters")))
        newPassword.validator.constraint(ValidatorUtil.createContainsDigitConstraint(newPassword.textProperty(), MessageBundle.getMess("password.validation.containsDigit")))
        newPassword.validator.constraint(ValidatorUtil.createContainsBigLetterConstraint(newPassword.textProperty(), MessageBundle.getMess("password.validation.containsBigLetter")))
        //newPassword.validator.constraint(ValidatorUtil.createEqualPasswordConstraint(newPasswordRepeat.textProperty(),newPassword.textProperty(), MessageBundle.getMess("password.validation.notEqualPasswords")))

        newPasswordRepeat.validator.constraint(ValidatorUtil.createNotEmptyConstraint(newPasswordRepeat.textProperty(), MessageBundle.getMess("password.validation.notEmpty")))
        newPasswordRepeat.validator.constraint(ValidatorUtil.createEqualPasswordConstraint(newPassword.textProperty(),newPasswordRepeat.textProperty(), MessageBundle.getMess("password.validation.notEqualPasswords")))
    }


    /**
     * Tworzy okno dialogowe do zmiany hasła tymczasowego.
     *
     * @param user Obiekt User użytkownika
     * @param role Rola użytkownika
     * @return VBox zawierający okno dialogowe do zmiany hasła
     */
    override fun createNewPasswordDialog(user: User, role: Int): VBox
    {
        val title = Label()
        title.text = MessageBundle.getMess("label.passwordChange")
        title.styleClass.add("header-label-white-big")

        val label = Label()
        label.text = MessageBundle.getMess("label.passwordTemporary")
        label.styleClass.add("header-label_white")

        newPassword.floatingText= MessageBundle.getMess("label.newPassword")
        newPassword.id = "comboWhite"
        newPassword.prefWidth = 300.0

        newPasswordRepeat.floatingText=MessageBundle.getMess("label.repeatNewPassword")
        newPasswordRepeat.id = "comboWhite"
        newPasswordRepeat.prefWidth = 300.0

        setNewPasswordButton.text = MessageBundle.getMess("label.setPassword")
        setNewPasswordButton.id = "customButton"

        val errorPassword = ValidationWrapper.createErrorLabel()
        val errorPasswordRepeat = ValidationWrapper.createErrorLabel()

        setConstraints(newPassword, newPasswordRepeat)

        setNewPasswordButton.setOnAction {
            setNewPassword(errorPassword, errorPasswordRepeat, user)
        }

        val vBox = VBox(30.0, title, label, ValidationWrapper.createWrapper(newPassword, errorPassword),ValidationWrapper.createWrapper(newPasswordRepeat, errorPasswordRepeat), setNewPasswordButton)
        vBox.alignment = Pos.CENTER

        return vBox
    }

    /**
     * Metoda, ustawiająca nowe hasło użytkownika i weryfikująca jego zgodność z normami
     */
    fun setNewPassword(errorPassword: Label, errorPasswordRepeat: Label, user: User)
    {
        ValidationWrapper.validationAction(newPassword, errorPassword)
        ValidationWrapper.validationAction(newPasswordRepeat, errorPasswordRepeat)

        if (errorPassword.text.isEmpty() && errorPasswordRepeat.text.isEmpty())
        {
            try {
                user.changeTempPassword(newPassword.text)
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.password.correctlyUpdated"))
                dialog.close()
                loginTextField.clear()
                passwordTextField.clear()
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.changeTempPassError"))
            }
        }
    }


    /**
     * Metoda wywoływana po wciśnięciu przycisku logowania, identyfikująca użytkownika.
     */
    override fun onLoginPressed() {
        checkDBpasswordExists()

        val login = loginTextField.text
        val password = passwordTextField.text

        val user = User(login,password)
        if (user.username.isBlank() || user.password.isBlank()){
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.loggingError"), MessageBundle.getMess("warning.noPasswordOrUsername"))
        }
        else
        {
            when(val role = user.authenticateUser())
            {
                1,2,3->
                {
                    val isPassTemp = user.isPasswordTemp()
                    if (!isPassTemp) loadNewScene(user.authenticateUser())
                    else
                    {
                        val vBox = createNewPasswordDialog(user, role)
                        dialog = DialogUtils.showCustomDialog(stage, vBox, true) {}
                        dialog.content.stylesheets.add(SchedulerApp::class.java.getResource("css/customStyles.css")?.toExternalForm()!!)
                        dialog.showAndWait()
                    }
                }
                else -> MessageUtil.showErrorMessage(MessageBundle.getMess("warning.loggingError"), MessageBundle.getMess("warning.incorrectLoginOrPassword"))
            }
        }
    }

    /**
     * Tworzy okno dialogowe do ustawienia hasła do serwera bazy danych.
     * @param prop Plik konfiguracyjny z danymi
     *
     * @return VBox zawierający okno dialogowe do ustawienia hasła do bazy
     */
    fun createDbPasswordDialog(prop: Properties): VBox
    {
        val dBPasswordTextField = MFXPasswordField()
        val title = Label()
        title.text = MessageBundle.getMess("label.firstAppUse")
        title.styleClass.add("header-label-white-big")

        val label = Label()
        label.text = MessageBundle.getMess("label.shouldEnterPassword")
        label.styleClass.add("header-label_white")

        dBPasswordTextField.floatingText= MessageBundle.getMess("label.password")
        dBPasswordTextField.id = "comboWhite"
        dBPasswordTextField.prefWidth = 300.0

        setNewPasswordButton.text = MessageBundle.getMess("label.setPassword")
        setNewPasswordButton.id = "customButton"

        setNewPasswordButton.setOnAction {
            //Sprawdź czy połączy się z bazą danych
            val user = User(prop.getProperty("db.username"), dBPasswordTextField.text)
            val isPassOk = user.testDbPassword(prop.getProperty("db.name"))
            if (isPassOk)
            {
                prop.setProperty("db.password", dBPasswordTextField.text)
                //val outputPath = "src/main/resources/config.properties"
                val outputPath = Paths.get("").toAbsolutePath().resolve("config.properties").toString()

                try {
                    FileOutputStream(outputPath).use { output ->
                        prop.store(output, null)
                        dbDialog.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            else{
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.dbConnectionError"), MessageBundle.getMess("warning.invalidDBPassword"))
            }
        }

        val vBox = VBox(30.0, title, label, dBPasswordTextField, setNewPasswordButton)
        vBox.alignment = Pos.CENTER

        return vBox
    }
}