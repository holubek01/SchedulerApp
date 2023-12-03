package com.example.scheduler.db.dao

import com.example.scheduler.db.DBQueryExecutor
import com.example.scheduler.objects.Teacher
import javafx.collections.ObservableList
import java.sql.SQLException

/**
 * Implementacja interfejsu `TeacherDAO` do obsługi operacji w bazie danych dla nauczyciela
 */
class TeacherDAOImpl: TeacherDAO {

    /**
     * Pobiera przedmioty, których uczy nauczyciel.
     *
     * @param teacher Imię i nazwisko nauczyciela.
     * @return Lista przedmiotów.
     */
    override fun getTeacherSubjectsByName(teacher: String): ObservableList<String> {
        val query = "SELECT subjectName FROM subjects INNER JOIN teacher_subject ts on subjects.subjectID = ts.subjectID " +
                "INNER JOIN teachers t on ts.teacherID = t.teacherID WHERE name = ? AND lastname = ? ORDER BY subjectName"

        return DBQueryExecutor.executeQuery(query,
            teacher.split(" ")[1],
            teacher.split(" ")[0],
        ){resultSet -> resultSet.getString(1) }
    }


    /**
     * Metoda pobierająca id podanego nauczyciela.
     *
     * @param teacher Obiekt Teacher, którego id chcemy uzyskać
     * @return Id nauczyciela
     */
    override fun getTeacherID(teacher: Teacher): Int {
        val query = "SELECT teacherID FROM teachers " +
                "WHERE name = ? AND lastname = ? AND phone = ? AND mail = ?"

        return DBQueryExecutor.executeQuery(query,
            teacher.firstname,
            teacher.lastname,
            teacher.phone,
            teacher.email)
        {resultSet -> resultSet.getInt(1)}.first()
    }

    /**
     * Metoda aktualizująca nauczyciela w bazie danych przy użyciu procedury z bazy danych.
     *
     * @param teacherID Id nauczyciela do aktualizacji
     * @param teacher Nauczyciel ze zaktualizowanymi danymi
     * @param subjectsJSON JSON z nową listą przedmiotów
     */
    @Throws(SQLException::class)
    override fun updateTeacher(teacherID: Int, teacher: Teacher, subjectsJSON: String) {
        val query = "{ CALL updateTeacher(?,?,?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            teacherID,
            teacher.firstname,
            teacher.lastname,
            teacher.phone,
            teacher.email,
            subjectsJSON
        )
    }

    /**
     * Metoda, która aktualizuje dyspozycyjność nauczyciela w danym dniu tygodnia.
     *
     * @param teacherID ID nauczyciela, którego dyspozycyjność jest aktualizowana.
     * @param day        Dzień tygodnia, dla którego aktualizowana jest dyspozycyjność.
     * @param hoursJSON JSON zawierający nowe godziny dyspozycyjności.
     */
    @Throws(SQLException::class)
    override fun updateTeacherAvailability(teacherID: Int, day: String, hoursJSON: String) {
        val query = "{ CALL updateTeacherAvailability(?,?,?) }"
        DBQueryExecutor.executePreparedStatement(query, teacherID, day, hoursJSON)
    }

    /**
     * Metoda, która dodaje dyspozycyjność nauczycielowi na określony dzień przy użyciu procedury z bazy danych.
     *
     * @param day        Dzień tygodnia, dla którego dodawana jest dostępność.
     * @param hoursJSON JSON zawierający godziny dyspozycyjności do dodania.
     */
    @Throws(SQLException::class)
    override fun addAvailabilityToTeacher(day: String, hoursJSON: String) {
        val query = "{ CALL addAvailabilityToTeacher(?,?) }"
        DBQueryExecutor.executePreparedStatement(query, day, hoursJSON)
    }

    /**
     * Metoda usuwająca nauczyciela z bazy danych.
     * Przy usuwaniu nauczyciela usuwane są także cała dyspozycyjność oraz wszystkie
     * zajęcia, które powinien on prowadzić (we wszystkich planach)
     *
     * @param teacher Nauczyciel do usunięcia
     */
    @Throws(SQLException::class)
    override fun deleteTeacher(teacher: Teacher) {
        val query = "{ CALL deleteTeacher(?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            teacher.firstname,
            teacher.lastname,
            teacher.phone,
            teacher.email
        )
    }

    /**
     * Sprawdza, czy nauczyciel, który nie jest nauczycielem 'selectedTeacher' istnieje w bazie danych.
     *
     * @param teacher           Nauczyciel do sprawdzenia w bazie.
     * @param selectedTeacher   Wybrany nauczyciel do edycji (pomijany).
     * @return                  `true`, jeśli nauczyciel istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfTeacherInDbEdit(teacher: Teacher, selectedTeacher:Teacher): Boolean {
        val query = "SELECT COUNT(*) FROM teachers\n" +
                "WHERE (name = ? AND lastname =\n" +
                " ? AND phone = ? AND mail = ?)\n" +
                "AND teachers.teacherID != (SELECT teacherID FROM teachers AS t WHERE t.name =? AND t.lastname =? \n" +
                "AND t.phone=? AND t.mail = ?)"

        return DBQueryExecutor.executeQuery(query,
            teacher.firstname,
            teacher.lastname,
            teacher.phone,
            teacher.email,
            selectedTeacher.firstname,
            selectedTeacher.lastname,
            selectedTeacher.phone,
            selectedTeacher.email)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy nauczyciel istnieje w bazie danych.
     *
     * @param teacher            Nauczyciel do sprawdzenia w bazie
     * @return                  `true`, jeśli nauczyciel istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfTeacherInDb(teacher: Teacher): Boolean {
        val query = "SELECT COUNT(*) FROM teachers " +
                "WHERE name = ? AND lastname = ? AND phone = ? AND mail = ?"

        return DBQueryExecutor.executeQuery(query,
            teacher.firstname,
            teacher.lastname,
            teacher.phone,
            teacher.email)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Pobiera listę wszystkich nauczycieli z bazy danych.
     *
     * @return Lista obiektów Teacher reprezentujących nauczycieli.
     */
    override fun getTeachers(): ObservableList<Teacher> {
        val query = "SELECT name, lastname, mail, phone from teachers ORDER BY lastname, name"

        return DBQueryExecutor.executeQuery(query){
                resultSet -> Teacher(
            firstname = resultSet.getString(1),
            lastname = resultSet.getString(2),
            email = resultSet.getString(3),
            phone = resultSet.getString(4))
        }
    }

    /**
     * Metoda dodająca nauczyciela do bazy danych przy użyciu procedury z bazy danych.
     *
     * @param teacher Nauczyciel do dodania do bazy danych
     * @param subjectsJSON JSON z nową listą przedmiotów
     */
    @Throws(SQLException::class)
    override fun addTeacher(teacher: Teacher, subjectsJSON: String) {
        val query = "{ CALL addTeacherWithSubject(?,?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            teacher.firstname,
            teacher.lastname,
            teacher.phone,
            teacher.email,
            subjectsJSON
        )
    }

    /**
     * Usuwa ze wszystkich planów wszystkie zajęcia, przypisane do nauczyciela o podanym ID, które
     * prowadzone są z wybranego przedmiotu (usuwanego z listy nauczyciela)
     * @param subject Przedmiot, z którego zajęcia należy usunąć
     * @param teacherID ID nauczyciela, do którego są przypisane zajęcia, które należy usunąć
     *
     */
    @Throws(SQLException::class)
    override fun deleteClassesAssociatedToDeletedSubjectAndTeacher(subject: String, teacherID: Int) {
        val query = "{ CALL deleteClassesAssociatedWithTeacherAndSubject(?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            teacherID,
            subject
        )
    }

    /**
     * Usuwa ze wszystkich planów wszystkie zajęcia, przypisane do nauczyciela o podanym ID, które
     * odbywają się w w wybranym dniu tygodnia o wybranej godzinie
     * @param day Dzień tygodnia, w którym należy usunąć zajęcia
     * @param hour Godzina, w której należy usunąć zajęcia
     * @param teacherID ID nauczyciela, do którego są przypisane zajęcia, które należy usunąć
     *
     */
    @Throws(SQLException::class)
    override fun deleteClassesAssociatedToDeletedTeacherAndAvailability(day: String, hour: String, teacherID: Int) {
        val query = "{ CALL deleteClassesAssociatedToDeletedTeacherAndAvailability(?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            teacherID,
            day,
            hour
        )
    }

    /**
     * Sprawdza, czy imię i nazwisko istnieją w bazie danych w tabeli nauczycieli.
     *
     * @param firstname         Imię do sprawdzenia w bazie.
     * @param lastname          Nazwisko do sprawdzenia w bazie.
     * @return                  `true`, jeśli imię i nazwisko istnieją; `false` w przeciwnym razie.
     */
    override fun checkIfNameAndLastnameInDb(firstname: String, lastname: String): Boolean {
        val query = "SELECT COUNT(*) FROM teachers AS t " +
                "WHERE t.name = ? AND t.lastname = ? "

        return DBQueryExecutor.executeQuery(query, firstname, lastname)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Metoda pobierająca dostępność nauczyciela w wybranym dniu.
     *
     * @param teacher Nauczyciel, którego dostępność należy pobrać
     * @param day Dzień tygodnia
     * @return Lista godzin dyspozycyjnych w danym dniu
     */
    override fun getAvailabilityByDay(teacher: Teacher, day: String): ObservableList<String> {
        val query = "SELECT hourRange FROM availability INNER JOIN day_of_week AS dow on availability.dayID = dow.dayID " +
                "INNER JOIN hours h on availability.hourID = h.hourId WHERE dayName = ? AND teacherID = " +
                "(SELECT teacherID FROM teachers AS t WHERE t.name = ? AND t.lastname = ? AND t.phone = ? AND t.mail = ?) "+
                " ORDER BY h.hourID"

        return DBQueryExecutor.executeQuery(query,
            day,
            teacher.firstname,
            teacher.lastname,
            teacher.phone,
            teacher.email
        ){resultSet -> resultSet.getString(1)}
    }

    /**
     * Metoda pobierająca całą dyspozycyjność nauczyciela.
     *
     * @param teacher Nauczyciel, którego dostępność należy pobrać
     * @return Lista dni i godzin dyspozycyjności
     */
    override fun getAvailability(teacher: Teacher): ObservableList<String> {
        val query = "SELECT dayName, hourRange FROM availability INNER JOIN day_of_week dow on availability.dayID = dow.dayID " +
                "INNER JOIN hours h on availability.hourID = h.hourId WHERE teacherID = " +
                "(SELECT teacherID FROM teachers WHERE name = ? AND lastname = ? AND phone = ? AND mail = ?) ORDER BY dow.dayID"

        return DBQueryExecutor.executeQuery(query,
            teacher.firstname,
            teacher.lastname,
            teacher.phone,
            teacher.email
        ){resultSet -> "${resultSet.getString(1)} ${resultSet.getString(2)}" }
    }

    /**
     * Metoda pobierająca listę przedmiotów nauczyciela.
     *
     * @param teacher Nauczyciel, którego przedmioty należy pobrać
     * @return Lista przedmiotów nauczyciela
     */
    override fun getTeacherSubjects(teacher: Teacher): ObservableList<String> {
        val query = "SELECT subjectName FROM subjects INNER JOIN teacher_subject ts on subjects.subjectID = ts.subjectID " +
                "INNER JOIN teachers t on ts.teacherID = t.teacherID WHERE name = ? AND lastname = ? " +
                " AND phone = ? AND mail = ? ORDER BY subjectName"

        return DBQueryExecutor.executeQuery(query,
            teacher.firstname,
            teacher.lastname,
            teacher.phone,
            teacher.email
        ){resultSet -> resultSet.getString(1) }
    }


    /**
     * Sprawdza, czy adres e-mail istnieje w bazie danych.
     *
     * @param email              Adres e-mail do sprawdzenia w bazie
     * @return                  `true`, jeśli adres email istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfEmailInDb(email: String): Boolean {
        val query = "SELECT COUNT(*) FROM teachers AS t " +
                "WHERE t.mail = ?"

        return DBQueryExecutor.executeQuery(query, email)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy adres e-mail, który nie jest adresem email podanego nauczyciela, istnieje w bazie danych.
     *
     * @param email             Adres e-mail do sprawdzenia w bazie.
     * @param selectedTeacher   Wybrany nauczyciel do edycji.
     * @return                  `true`, jeśli adres email istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfEmailInDbEdit(email: String, selectedTeacher: Teacher): Boolean {
        val query = "SELECT COUNT(*) FROM teachers AS t " +
                "WHERE t.mail = ? AND t.teacherID != (SELECT teacherID FROM teachers AS t WHERE t.name =? AND t.lastname =? " +
                "AND t.phone=? AND t.mail = ?)"

        return DBQueryExecutor.executeQuery(query, email, selectedTeacher.firstname, selectedTeacher.lastname, selectedTeacher.phone, selectedTeacher.email)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }


    /**
     * Sprawdza, czy imię i nazwisko, które nie są imieniem i nazwiskiem wybranego nauczyciela, istnieją w bazie danych w tabeli nauczycieli.
     *
     * @param firstname         Imię do sprawdzenia w bazie.
     * @param lastname          Nazwisko do sprawdzenia w bazie.
     * @param selectedTeacher   Wybrany nauczyciel do edycji (pomijany).
     * @return                  `true`, jeśli imię i nazwisko istnieją; `false` w przeciwnym razie.
     */
    override fun checkIfNameAndLastnameInDbEdit(
        firstname: String,
        lastname: String,
        selectedTeacher: Teacher
    ): Boolean {
        val query = "SELECT COUNT(*) FROM teachers AS t " +
                "WHERE t.name = ? AND t.lastname = ? AND t.teacherID != (SELECT teacherID FROM teachers AS t WHERE t.name =? AND t.lastname =? " +
                "AND t.phone=? AND t.mail = ?)"

        return DBQueryExecutor.executeQuery(query, firstname, lastname, selectedTeacher.firstname, selectedTeacher.lastname, selectedTeacher.phone, selectedTeacher.email)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy nr telefonu istnieje w bazie danych.
     *
     * @param phone              Nr telefonu do sprawdzenia
     * @return                  `true`, jeśli nr telefonu istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfPhoneInDb(phone: String): Boolean {
        val query = "SELECT COUNT(*) FROM teachers AS t " +
                "WHERE t.phone = ?"

        return DBQueryExecutor.executeQuery(query, phone)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy nr telefonu, który nie jest nr telefonu wybranego nauczyciela, istnieje w bazie danych.
     *
     * @param phone              Nr telefonu do sprawdzenia
     * @param selectedTeacher   Wybrany nauczyciel do edycji (pomijany).
     * @return                  `true`, jeśli nr telefonu istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfPhoneInDbEdit(phone: String, selectedTeacher: Teacher): Boolean {
        val query = "SELECT COUNT(*) FROM teachers AS t " +
                "WHERE t.phone = ? AND t.teacherID != (SELECT teacherID FROM teachers AS t WHERE t.name =? AND t.lastname =? " +
                "AND t.phone=? AND t.mail = ?)"

        return DBQueryExecutor.executeQuery(query, phone, selectedTeacher.firstname, selectedTeacher.lastname, selectedTeacher.phone, selectedTeacher.email)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }
}