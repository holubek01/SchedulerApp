package com.example.scheduler.db.dao

import com.example.scheduler.db.DBQueryExecutor
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Group
import com.example.scheduler.objects.Subject
import com.example.scheduler.objects.TeachingPlan
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.SQLException

/**
 * Implementacja interfejsu `FieldDAO` do obsługi operacji w bazie danych dla kierunków
 */
class FieldDAOImpl:FieldDAO {

    /**
     * Dodaje przedmiot z podaną liczbą godzin do semestru dla podanego kierunku i semestru.
     *
     * @param subject     Nazwa przedmiotu.
     * @param field       Nazwa kierunku kształcenia.
     * @param sem         Numer semestru.
     * @param weeklyHours Liczba godzin w tygodniu.
     */
    @Throws(SQLException::class)
    override fun addSubjectToSem(subject: String, field:String, sem:Int, weeklyHours: Int)
    {
        val query = "{ CALL addSubjectToSemester(?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(query, subject, field, sem, weeklyHours)
    }


    /**
     * Sprawdza, czy nazwa kierunku kształcenia, istnieje w bazie danych.
     *
     * @param fieldName         Nazwa kierunku do sprawdzenia
     * @return                  `true`, jeśli nazwa kierunku istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfFieldNameInDb(fieldName: String): Boolean {
        val query = "SELECT COUNT(*) FROM fields " +
                "WHERE fieldName = ? "

        return DBQueryExecutor.executeQuery(query,
            fieldName
        )
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy skrót kierunku kształcenia, który nie jest skrótem podanego kierunku, istnieje w bazie danych.
     *
     * @param fieldShort         Skrót kierunku do sprawdzenia
     * @param selectedField      Kierunek kształcenia do pominięcia
     * @return                  `true`, jeśli skrót kierunku istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfFieldShortInDbEdit(fieldShort: String, selectedField: Field): Boolean {
        val query = "SELECT COUNT(*) FROM fields " +
                "WHERE fieldShort = ? AND fields.fieldID != (SELECT fieldID FROM fields WHERE fieldName = ? AND fieldShort = ? )"

        return DBQueryExecutor.executeQuery(query,
            fieldShort, selectedField.fieldName, selectedField.shortcut
        )
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy nazwa kierunku kształcenia, która nie jest nazwą podanego kierunku, istnieje w bazie danych.
     *
     * @param fieldName          Nazwa kierunku do sprawdzenia
     * @param selectedField      Kierunek kształcenia do pominięcia
     * @return                  `true`, jeśli nazwa kierunku istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfFieldNameInDbEdit(fieldName: String, selectedField: Field): Boolean {
        val query = "SELECT COUNT(*) FROM fields " +
                "WHERE fieldName = ? AND fields.fieldID != (SELECT fieldID FROM fields WHERE fieldName = ? AND fieldShort = ? )"

        return DBQueryExecutor.executeQuery(query,
            fieldName, selectedField.fieldName, selectedField.shortcut
        )
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy skrót nazwy kierunku kształcenia, istnieje w bazie danych.
     *
     * @param shortcut           Skrót nazwy kierunku do sprawdzenia
     * @return                  `true`, jeśli skrót istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfFieldShortcutInDb(shortcut: String): Boolean {
        val query = "SELECT COUNT(*) FROM fields " +
                "WHERE fieldShort = ? "

        return DBQueryExecutor.executeQuery(query,
            shortcut
        )
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy kierunek, istnieje w bazie danych.
     *
     * @param fieldName         Nazwa kierunku
     * @param shortcut          Skrót nazwy kierunku
     * @return                  `true`, jeśli skrót istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfFieldInDb(fieldName: String, shortcut: String): Boolean {
        val query = "SELECT COUNT(*) FROM fields " +
                "WHERE fieldName = ? AND fieldShort = ?"

        return DBQueryExecutor.executeQuery(query,
            fieldName, shortcut
        )
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy kierunek kształcenia, który nie jest kierunkiem z danymi 'Old', istnieje w bazie danych.
     *
     * @param fieldName         Nazwa kierunku do sprawdzenia
     * @param shortcut          Skrót do sprawdzenia
     * @param fieldNameOld      Nazwa kierunku do pominięcia
     * @param shortcutOld       Skrót do pominięcia
     * @return                  `true`, jeśli kierunek istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfFieldInDbEdit(
        fieldName: String,
        shortcut: String,
        fieldNameOld: String,
        shortcutOld: String
    ): Boolean {
        val query = "SELECT COUNT(*) FROM fields " +
                "WHERE fieldName = ? AND fieldShort = ? " +
                "AND fields.fieldID != (SELECT fieldID FROM fields WHERE fieldName = ? AND fieldShort = ? )"

        return DBQueryExecutor.executeQuery(query,
            fieldName, shortcut, fieldNameOld, shortcutOld
        )
        {resultSet -> resultSet.getBoolean(1)}.first()
    }


    /**
     * Metoda dodająca kierunek do bazy danych przy użyciu procedury z bazy danych.
     *
     * @param field     Kierunek do dodania do bazy danych
     */
    @Throws(SQLException::class)
    override fun addField(field: Field) {
        val query = "{ CALL addField(?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            field.fieldName,
            field.shortcut,
            field.semsNumber
        )
    }

    /**
     * Metoda dodająca grupę do bazy danych przy użyciu procedury z bazy danych.
     *
     * @param fieldName Nazwa kierunku, do którego ma zostać przypisana grupa
     * @param sem Numer semestru, do którego ma zostać przypisana grupa
     * @param romanNumber Liczba rzymska odpowiadająca numerowi semestru
     */
    @Throws(SQLException::class)
    override fun addGroup(fieldName: String, sem: Int, romanNumber: String) {

        val query = "{ CALL addGroup(?,?,?) }"
        DBQueryExecutor.executePreparedStatement(query, fieldName, sem, romanNumber)
    }


    /**
     * Metoda pobierająca id podanego kierunku.
     *
     * @param lastSelectedField Obiekt Field, którego id chcemy uzyskać
     * @return Id kierunku
     */
    override fun getFieldID(lastSelectedField: Field): Int {
        val query = "SELECT fieldID FROM fields AS f " +
                "WHERE f.fieldName = ? AND f.fieldShort = ? AND f.semNumber = ? "

        return DBQueryExecutor.executeQuery(query,
            lastSelectedField.fieldName,
            lastSelectedField.shortcut,
            lastSelectedField.semsNumber
        )
        {resultSet -> resultSet.getInt(1)}.first()
    }


    /**
     * Metoda pobierająca listę przedmiotów dla danego kierunku i semestru.
     *
     * @param field     Nazwa kierunku
     * @param semester Numer semestru
     * @return          Lista przedmiotów
     */
    override fun getSubjectsByFieldAndSem(field: String, semester: String): ObservableList<Subject> {
        val query = "SELECT subjectName FROM subjects INNER JOIN field_subject fs on subjects.subjectID = fs.subjectID " +
                "INNER JOIN fields f on fs.fieldID = f.fieldID WHERE fieldName = ? AND term = ? ORDER BY subjectName"

        return DBQueryExecutor.executeQuery(query, field, semester){
                resultSet ->
            Subject(subjectName = resultSet.getString(1))
        }

    }


    /**
     * Metoda pobierająca listę grup dla danego kierunku kształcenia i semestru.
     *
     * @param fieldName Nazwa kierunku
     * @param sem       Numer semestru
     * @return          Lista grup
     */
    override fun getGroups(fieldName: String, sem: Int): ObservableList<String> {
        val query = "SELECT groupName FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID\n" +
                "INNER JOIN `fields` AS f ON f.fieldID = gf.fieldID\n" +
                "WHERE f.fieldName = ? AND g.term = ? ORDER BY RIGHT(groupName,1)"

        return DBQueryExecutor.executeQuery(query, fieldName, sem){
                resultSet -> resultSet.getString(1)
        }
    }


    /**
     * Metoda dodająca grupy do podanego kierunku i semestru do bazy danych.
     *
     * @param field Kierunek, do którego należy przypisać grupy
     * @param groupsNum Liczba grup
     * @param sem Numer semestru, na jaki należy przypisać grupy
     * @param shortcutRoman Liczba rzymska odpowiadająca numerowi semestru
     */
    @Throws(SQLException::class)
    override fun addGroups(field: Field, groupsNum: Int, sem: Int, shortcutRoman: String) {
        val query = "{ CALL addGroups(?,?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(query, field.fieldName, field.shortcut, groupsNum, sem, shortcutRoman)
    }

    /**
     * Metoda aktualizująca kierunek kształcenia w bazie danych przy użyciu procedury z bazy danych.
     *
     * @param fieldID Id kierunku do aktualizacji
     * @param fieldName Nowa nazwa kierunku
     * @param shortcut Nowy skrót nazwy kierunku
     */
    @Throws(SQLException::class)
    override fun updateField(fieldID: Int, fieldName: String, shortcut: String) {
        val query = "{ CALL updateField(?,?,?) }"
        DBQueryExecutor.executePreparedStatement(query, fieldID, fieldName, shortcut)
    }


    /**
     * Metoda usuwająca grupę z bazy danych
     *
     * @param group Grupa do usunięcia
     */
    @Throws(SQLException::class)
    override fun deleteGroup(group: Group) {
        val query = "{ CALL deleteGroup(?,?,?) }"
        DBQueryExecutor.executePreparedStatement(query, group.groupName, group.sem, group.fieldName)
    }


    /**
     * Metoda usuwająca kierunek kształcenia z bazy danych.
     * Przy usuwaniu kierunku usuwane są także wszystkie grupy z nim związane
     * oraz wszystkie zajęcia związane z tymi grupami we wszystkich planach
     *
     * @param field Kierunek do usunięcia
     */
    @Throws(SQLException::class)
    override fun deleteField(field: Field) {
        val query = "{ CALL deleteField(?,?,?) }"
        DBQueryExecutor.executePreparedStatement(query, field.fieldName, field.semsNumber, field.shortcut)
    }


    /**
     * Metoda pobierająca liczbę semestrów na danym kierunku.
     *
     * @param field Nazwa kierunku
     * @return Liczba semestrów na danym kierunku
     */
    override fun getSemesters(field: String): Int {
        val query = "SELECT DISTINCT semNumber FROM fields WHERE fieldName = ?"

        return DBQueryExecutor.executeQuery(query, field){ resultSet -> resultSet.getInt(1) }.first()
    }

    /**
     * Pobiera szkolny plan nauczania dla określonego kierunku kształcenia.
     *
     * @param field kierunek, dla którego ma zostać pobrany szkolny plan nauczania.
     * @return Szkolny plan nauczania dla wybranego kierunku w formie listy obiektów TeachingPlan.
     */
    override fun showSPN(field: Field): ObservableList<TeachingPlan> {

        val columnSubquery = (1..field.semsNumber).joinToString(", ") { "MAX(CASE WHEN fs.term = $it THEN fs.weeklyHours END)" }

        val query = "SELECT s.subjectName, $columnSubquery\n" +
                "FROM field_subject AS fs\n" +
                "INNER JOIN `fields` AS f ON f.fieldID = fs.fieldID\n" +
                "INNER JOIN subjects AS s ON s.subjectID = fs.subjectID\n" +
                "WHERE f.fieldName = ?\n" +
                "GROUP BY s.subjectName\n" +
                "ORDER BY s.subjectID;"


        return DBQueryExecutor.executeQuery(query,field.fieldName){
                resultSet ->
            TeachingPlan(
                subjectName = resultSet.getString(1),
                semesters = FXCollections.observableArrayList(
                    (1..field.semsNumber).map { resultSet.getInt(it + 1)}
                ))
        }


    }

    /**
     * Pobiera listę wszystkich kierunków kształcenia.
     *
     * @return Lista wszystkich kierunków kształcenia.
     */
    override fun getFields(): ObservableList<Field> {
        val query = "SELECT fieldName, semNumber, fieldShort FROM fields ORDER BY fieldName"

        return DBQueryExecutor.executeQuery(query){
                resultSet ->
            Field(
                fieldName = resultSet.getString(1),
                semsNumber = resultSet.getInt(2),
                shortcut = resultSet.getString(3))
        }
    }

    /**
     * Pobiera wszystkie grupy dla określonego kierunku kształcenia.
     *
     * @param field Nazwa kierunku kształcenia.
     * @return Lista grup.
     */
    override fun getGroupsByField(field: String): ObservableList<String> {
        val query = "SELECT groupName FROM `groups` INNER JOIN group_field ON group_field.groupID = `groups`.groupID \n" +
                "INNER JOIN fields ON fields.fieldID = group_field.fieldID WHERE fieldName = ?\n" +
                "ORDER BY\n" +
                "  CASE\n" +
                "    WHEN groupName LIKE 'I%' THEN 1\n" +
                "    WHEN groupName LIKE 'II%' THEN 2\n" +
                "    WHEN groupName LIKE 'III%' THEN 3\n" +
                "    WHEN groupName LIKE 'IV%' THEN 4\n" +
                "    WHEN groupName LIKE 'V%' THEN 5\n" +
                "    WHEN groupName LIKE 'VI%' THEN 6\n" +
                "    WHEN groupName LIKE 'VII%' THEN 7\n" +
                "    WHEN groupName LIKE 'VIII%' THEN 8\n" +
                "    WHEN groupName LIKE 'IX%' THEN 9\n" +
                "    WHEN groupName LIKE 'X%' THEN 10\n" +
                "  END,\n" +
                "  groupName;"
        return DBQueryExecutor.executeQuery(query, field){
                resultSet -> resultSet.getString(1) }
    }

    /**
     * Usuwa szkolny plan nauczania związany z określonym kierunkiem kształcenia.
     *
     * @param fieldName Nazwa kierunku, dla którego należy usunąć szkolny plan nauczania.
     */
    @Throws(SQLException::class)
    override fun deleteSPN(fieldName: String) {
        val query = "{ CALL deleteFromSPNByField(?) }"
        DBQueryExecutor.executePreparedStatement(query, fieldName)
    }

    /**
     * Sprawdza, czy kierunek kształcenia istnieje w bazie danych.
     *
     * @param fieldName Nazwa kierunku studiów do sprawdzenia.
     * @return `true`, jeśli kierunek istnieje; w przeciwnym razie `false`.
     */
    override fun checkIfFieldExists(fieldName: String):Boolean {
        val query = "SELECT COUNT(*) FROM fields WHERE fieldName = ?"

        return DBQueryExecutor.executeQuery(query,
            fieldName)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

}