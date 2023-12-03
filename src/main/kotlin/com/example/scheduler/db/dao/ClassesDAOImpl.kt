package com.example.scheduler.db.dao

import com.example.scheduler.db.DBQueryExecutor
import com.example.scheduler.models.ClassesToRead
import com.example.scheduler.models.ClassesToWrite
import com.example.scheduler.utils.MessageBundle
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.SQLException
import java.time.LocalDate

/**
 * Implementacja interfejsu 'ClassesDAO' do obsługi operacji w bazie danych dla zajęć.
 */
class ClassesDAOImpl: ClassesDAO{

    /**
     * Pobiera listę nazw kierunków kształcenia, dla których plan nie jest jeszcze w pełni ułożony
     * (istnieją grupy na kierunku, dla których plan nie jest w pełni utworzony).
     *
     * @return Lista nazw kierunków studiów, dla których plan nie jest jeszcze w pełni ułożony.
     */
    override fun getFields(): ObservableList<String> {

        val query: String = "SELECT distinct fieldName FROM `fields` \n" +
                    "INNER JOIN field_subject ON field_subject.fieldID = fields.fieldID\n" +
                    "INNER JOIN group_field ON group_field.fieldID = fields.fieldID\n" +
                    "INNER JOIN group_subject_hours_left ON group_subject_hours_left.groupID = group_field.groupID\n" +
                    "WHERE weekHoursLeft > 0 ORDER BY fieldName"

        return DBQueryExecutor.executeQuery(query){ resultSet -> resultSet.getString(1) }
    }


    /**
     * Pobiera listę przedmiotów dla danej grupy i kierunku, dla których plan nie jest jeszcze w pełni ułożony.
     *  @param group Nazwa grupy
     *  @param field Nazwa kierunku, do którego przypisana jest grupa
     *
     * @return Lista przedmiotów dla danej grupy i kierunku, dla których plan nie jest jeszcze w pełni ułożony.
     */
    override fun getSubjects(group: String, field: String): ObservableList<String> {

        val query = "SELECT DISTINCT subjects.subjectName FROM group_subject_hours_left\n" +
                "INNER JOIN subjects ON subjects.subjectID = group_subject_hours_left.subjectID\n" +
                "WHERE group_subject_hours_left.groupID=(SELECT `groups`.groupID FROM `groups` INNER JOIN group_field ON group_field.groupID =\n" +
                "`groups`.groupID INNER JOIN fields ON fields.fieldID = group_field.fieldID\n" +
                "WHERE groupName=? AND fieldName=?) AND group_subject_hours_left.weekHoursLeft > 0 ORDER BY subjects.subjectName"


        return DBQueryExecutor.executeQuery(query, group, field){ resultSet -> resultSet.getString(1) }
    }

    /**
     * Pobiera liczbę godzin, która, została do ułożenia planu dla wybranej grupy i przedmiotu
     * (ile godzin zostało do zrealizowania szkolnego planu nauczania)
     * @param group Nazwa grupy
     * @param field Nazwa kierunku, do którego przypisana jest grupa
     * @param subject Nazwa kierunku, dla którego należy sprawdzić godziny
     * @return Liczba godzin, które pozostała do pełnego ułożenia planu dla grupy i przedmiotu
     */
    override fun getHowManyHoursLeft(group: String, field: String, subject: String): Int {
        val query = "SELECT DISTINCT group_subject_hours_left.weekHoursLeft FROM group_subject_hours_left\n" +
                "INNER JOIN subjects ON subjects.subjectID = group_subject_hours_left.subjectID\n" +
                "WHERE group_subject_hours_left.groupID=(SELECT `groups`.groupID FROM `groups` INNER JOIN group_field ON group_field.groupID =\n" +
                "`groups`.groupID INNER JOIN fields ON fields.fieldID = group_field.fieldID\n" +
                "WHERE groupName=? AND fieldName=?) AND subjects.subjectName = ?"

        return DBQueryExecutor.executeQuery(query, group, field, subject){ resultSet -> resultSet.getInt(1) }.first()
    }

    /**
     * Pobiera listę godzin, w których dana grupa nie jest zajęta w wybranym dniu.
     * @param group Nazwa grupy
     * @param field Nazwa kierunku, do którego przypisana jest grupa
     * @param day Dzień, w którym należy sprawdzić godziny
     *
     * @return Lista godzin, w których dana grupa nie jest zajęta.
     */
    override fun getHours(day: LocalDate, group: String, field: String): ObservableList<String> {
        val query = "SELECT DISTINCT hourRange FROM hours WHERE hourID NOT IN (SELECT hourID FROM plan WHERE date=? " +
            "AND groupId = (SELECT group_field.groupID FROM `groups` INNER JOIN group_field ON group_field.groupID = `groups`.groupID" +
            " INNER JOIN fields ON fields.fieldID = group_field.fieldID WHERE groupName = ? AND fieldName = ?))"

        return DBQueryExecutor.executeQuery(query, day, group, field){ resultSet -> resultSet.getString(1) }
    }

    /**
     * Pobiera listę sal w podanej lokalizacji, które w danym dniu o danej godzinie nie są zajęte przez inną grupę.
     * @param day Dzień, w którym należy sprawdzić zajętość sal
     * @param hour Godzina, w której należy sprawdzić zajętość sal
     * @param location Lokalizacja, w której należy sprawdzić sale
     *
     * @return Lista wolnych sal w danej lokalizacji w podanym terminie.
     */
    override fun getRooms(day: LocalDate, hour: String, location: String): ObservableList<String> {
        val query: String =
            "SELECT DISTINCT roomName FROM rooms INNER JOIN location_room AS lr ON lr.roomID = rooms.roomID WHERE lr.roomID not in (SELECT roomID FROM plan WHERE date=? " +
                    "AND hourId= (SELECT hourID from hours WHERE hourRange=?)) AND locationID = " +
                    "(SELECT locationId FROM location WHERE name=?)"


        return DBQueryExecutor.executeQuery(query, day, hour, location){ resultSet -> resultSet.getString(1) }
    }


    /**
     * Pobiera listę nauczycieli, którzy nie mają w podanym terminie innych zajęć oraz uczą wybranego przedmiotu
     * i zadeklarowali swoją dyspozycyjność w podanym terminie.
     *
     * @return Lista nauczycieli, których można przypisać do zajęć w tym terminie.
     */
    override fun getTeachers(day: LocalDate, hour: String, subject: String): ObservableList<String> {

        val query = "SELECT DISTINCT lastname, name FROM teachers INNER JOIN availability ON availability.teacherID = teachers.teacherID " +
                "WHERE teachers.teacherID IN (SELECT teacherID from availability WHERE dayID = (SELECT dayID from day_of_week WHERE" +
                " dayName = ?) AND hourID = (SELECT hourID FROM hours WHERE hourRange=?)) AND teachers.teacherID NOT IN" +
                "  (SELECT teacherId FROM plan WHERE date=? AND hourId = (SELECT hourID FROM hours WHERE hourRange=?)) AND " +
                "teachers.teacherID IN (SELECT teacherID from teacher_subject INNER JOIN subjects ON subjects.subjectID =" +
                " teacher_subject.subjectID WHERE subjectName = ?) ORDER BY lastname, name"

        return DBQueryExecutor.executeQuery(query,day.dayOfWeek.toString().uppercase(),hour,day, hour,subject){ resultSet -> "${resultSet.getString(1)} ${resultSet.getString(2)}"  }
    }

    /**
     * Pobiera listę nauczycieli i odpowiadających im godzin w wybranym dniu, w których mogą prowadzić zajęcia z wybranego przedmiotu
     * @param subject Przedmiot, którego musi uczyć nauczyciel
     * @param date Data, w której należy sprawdzić dyspozycyjność nauczycieli
     * @return lista nauczycieli i odpowiadających im godzin w wybranym dniu, w których mogą prowadzić zajęcia z wybranego przedmiotu
     */
    override fun getTeacherWithHoursHint(subject: String, date: LocalDate): ObservableList<Pair<String, String>> {
        val query = "SELECT DISTINCT CONCAT(lastname, ' ', name) as teacher, h.hourRange FROM teachers AS t \n" +
                "INNER JOIN teacher_subject AS ts ON ts.teacherID = t.teacherID\n" +
                "INNER JOIN subjects AS s ON s.subjectID = ts.subjectID\n" +
                "INNER JOIN availability AS a ON a.teacherID = t.teacherID\n" +
                "INNER JOIN hours AS h ON h.hourId = a.hourID\n" +
                "WHERE s.subjectName = ?\n" +
                "AND (t.teacherID,h.hourId) NOT IN (SELECT teacherId, hourId FROM plan AS p WHERE p.date = ?)\n" +
                "AND a.dayID = (SELECT dayID FROM day_of_week WHERE dayName = dayname(?))"

        return DBQueryExecutor.executeQuery(query,subject,date,date){ resultSet -> Pair(resultSet.getString(1), resultSet.getString(2))  }
    }


    /**
     * Metoda, która dodaje zajęcia do planu, wykorzystująca procedurę z bazy danych.
     *
     * @param classes Obiekt reprezentujący pojedyńcze zajęcia do dodania do planu.
     */
    @Throws(SQLException::class)
    override fun addToPlan(classes: ClassesToWrite) {
        val query = "{ CALL addToPlan(?,?,?,?,?,?,?,?,?) }"

        return  DBQueryExecutor.executePreparedStatement(
            query,
            classes.date!!,
            classes.hour!!,
            classes.room!!,
            classes.group!!,
            classes.teacher!!.split(" ")[1],
            classes.teacher!!.split(" ")[0],
            classes.subject!!,
            classes.fieldOfStudy!!,
            classes.location!!
        )
    }

    /**
     * Sprawdza, czy nauczyciel zdąży przenieść się pomiędzy lokalizacjami w danym dniu i godzinie
     * (Nauczyciel na zmianę lokalizacji powinien mieć odpowiednio długą przerwę odpowiadającą długości trwania co najmniej 1 zajęć).
     *
     * @param firstname Imię nauczyciela.
     * @param lastname Nazwisko nauczyciela.
     * @param location Lokalizacja, w której odbywają się następne zajęcia.
     * @param hour Godzina, w której odbywają się zajęcia.
     * @param date Data, w której odbywają się zajęcia.
     * @return true, jeśli nauczyciel zdąży przenieść się między zajęciami, false w przeciwnym razie.
     */
    override fun canTeacherMoveBetweenClasses(firstname: String, lastname: String, location: String, hour: String, date: String): Boolean {
        val query= "{ ? = call canTeacherMoveBetweenClasses(?, ?, ?, ?, ?) }"
        return DBQueryExecutor.executeFunction(query, firstname, lastname, location, hour, date)
    }


    /**
     * Sprawdza, czy grupa zdąży przenieść się pomiędzy lokalizacjami w danym dniu i godzinie
     * (Grupa na zmianę lokalizacji powinna mieć odpowiednio długą przerwę odpowiadającą długości trwania co najmniej 1 zajęć).
     *
     * @param group Nazwa grupy.
     * @param field Nazwa kierunku kształcenia.
     * @param location Lokalizacja, w której odbywają się zajęcia.
     * @param hour Godzina, w której odbywają się zajęcia.
     * @param date Data, w której odbywają się zajęcia.
     * @return true, jeśli grupa zdąży przenieść się między zajęciami, false w przeciwnym razie.
     */
    override fun canGroupMoveBetweenClasses(group: String, field: String, location: String, hour: String, date: String): Boolean {
        val query= "{ ? = call canGroupMoveBetweenClasses(?, ?, ?, ?, ?) }"
        return DBQueryExecutor.executeFunction(query, group, field, location, hour, date)
    }



    /**
     * Pobiera nauczycieli, którzy są wolni w określonym dniu i godzinie, zgłosili
     * dyspozycyjność oraz uczą określonego przedmiotu.
     *
     * @param subject Nazwa przedmiotu, którego nauczyciel musi nauczać.
     * @param date Data.
     * @param hour Godzina.
     * @return Lista nazw nauczycieli w formacie "Nazwisko Imię", którzy są wolni w określonym czasie.
     */
    override fun getFreeTeachers(subject: String, date: LocalDate, hour: String): ObservableList<String>{
        val query = "SELECT DISTINCT CONCAT(lastname, ' ', name) as teacher FROM teachers INNER JOIN availability ON availability.teacherID = teachers.teacherID " +
                "WHERE teachers.teacherID IN (SELECT teacherID from availability WHERE dayID = (SELECT dayID from day_of_week WHERE" +
                " dayName = ?) AND hourID = (SELECT hourID FROM hours WHERE hourRange=?)) AND teachers.teacherID NOT IN" +
                "  (SELECT teacherId FROM plan WHERE date=? AND hourId = (SELECT hourID FROM hours WHERE hourRange=?)) AND " +
                "teachers.teacherID IN (SELECT teacherID from teacher_subject INNER JOIN subjects ON subjects.subjectID =" +
                " teacher_subject.subjectID WHERE subjectName = ?) ORDER BY lastname"

        return DBQueryExecutor.executeQuery(query,date.dayOfWeek.toString().uppercase(),hour,date, hour,subject){ resultSet -> resultSet.getString(1)}}



    /**
     * Pobiera lokalizacje, dla których w wybranym dniu istnieją wolne sale
     * (może zdarzyć się, że w wybranym dniu we wszystkich godzinach wszystkie sale są zajęte)
     * @param day Dzień, w którym należy sprawdzić dyspozycyjność sal
     *
     * @return Lista nazw lokalizacji.
     */
    override fun getLocations(day: LocalDate): ObservableList<String> {
        val query = "SELECT name FROM (SELECT DISTINCT l.name FROM location_room AS lr \n" +
                "INNER JOIN location AS l ON l.locationId = lr.locationId\n" +
                "INNER JOIN rooms AS r ON r.roomID = lr.roomID \n" +
                "WHERE r.roomID IN ( \n" +
                "SELECT r.roomId FROM rooms AS r WHERE r.roomId NOT IN ( \n" +
                "SELECT p.roomID FROM plan AS p WHERE p.date = ? GROUP BY p.roomID \n" +
                "HAVING COUNT(p.roomID) = (SELECT COUNT(*) FROM hours)))\n" +
                "UNION (SELECT 'Platform' AS name)) AS result\n" +
                "ORDER BY name"
        return DBQueryExecutor.executeQuery(query,day){ resultSet -> resultSet.getString(1) }
    }

    /**
     * Pobiera nauczycieli, którzy są zajęci w podanym terminie, ale uczą wybranego przedmiotu
     * Wykorzystywane przy generowaniu podpowiedzi, gdy brakuje nauczycieli
     */
    override fun getBusyTeachersHint(subject: String, date:String, hour:String): ObservableList<String> {
        val query = "SELECT DISTINCT CONCAT(lastname, ' ', name) as teacher FROM teachers AS t \n" +
                "INNER JOIN teacher_subject AS ts ON ts.teacherID = t.teacherID\n" +
                "INNER JOIN subjects AS s ON s.subjectID = ts.subjectID\n" +
                "WHERE s.subjectName = ?\n" +
                "AND t.teacherID IN \n" +
                "(SELECT teacherId FROM plan AS p WHERE p.date=? AND p.hourId=(SELECT hourID from hours \n" +
                "WHERE hourRange=?))"

        return DBQueryExecutor.executeQuery(query,subject, date, hour){ resultSet -> resultSet.getString(1)}

    }

    /**
     * Pobiera nauczycieli, którzy są zajęci w określonym terminie, ale uczą wybranego przedmiotu, nauczyciel
     * z zajęć do edycji uczy przedmiotu, którego w tym czasie uczy proponowany nauczyciel
     * oraz nauczyciele prowadzą zajęcia w tej samej lokalizacji.
     *
     * @param subject Nazwa przedmiotu.
     * @param date Data.
     * @param hour Godzina.
     * @param subjects Lista nazw przedmiotów.
     * @param location Lokalizacja, w której nauczyciele muszą prowadzić zajęcia
     * @return Lista nauczycieli w formacie "Nazwisko Imię", którzy są zajęci w określonym czasie ale uczą określonych przedmiotów.
     */
    override fun getBusyTeachers(
        subject: String,
        date: LocalDate,
        hour: String,
        subjects: ObservableList<String>,
        location: String
    ): ObservableList<String> {

        val formattedSubjects = subjects.joinToString(", ", "(", ")") { "'$it'" }
        val loc = if (location == MessageBundle.getMess("label.platform")) "Platform" else location

        val query =
            "SELECT DISTINCT CONCAT(lastname, ' ', name) as teacher FROM teachers INNER JOIN availability ON availability.teacherID = teachers.teacherID " +
                    "INNER JOIN teacher_subject AS ts ON ts.teacherID = teachers.teacherID INNER JOIN subjects AS s ON s.subjectID = ts.subjectID WHERE teachers.teacherID IN (SELECT teacherID from availability WHERE dayID = (SELECT dayID from day_of_week WHERE" +
                    " dayName = ?) AND hourID = (SELECT hourID FROM hours WHERE hourRange=?)) AND teachers.teacherID IN" +
                    "  (SELECT teacherId FROM plan WHERE plan.roomId IN (SELECT roomId FROM location_room AS lr \n" +
                    "INNER JOIN location AS l\n" +
                    "ON l.locationID = lr.locationID WHERE l.name = ?) AND date=? AND hourId = (SELECT hourID FROM hours WHERE hourRange=?)) AND " +
                    "teachers.teacherID IN (SELECT teacherID from teacher_subject INNER JOIN subjects ON subjects.subjectID =" +
                    " teacher_subject.subjectID WHERE subjectName = ?) AND teachers.teacherID IN " +
                    "(SELECT teacherID FROM plan INNER JOIN subjects ON subjects.subjectID = plan.subjectId WHERE plan.date=?\n" +
                    "AND plan.hourID = (SELECT hourID FROM hours WHERE hourRange=?) AND subjects.subjectName IN $formattedSubjects) " +
                    " ORDER BY lastname"

        return DBQueryExecutor.executeQuery(query, date.dayOfWeek.toString().uppercase(), hour, loc, date, hour, subject, date, hour) { resultSet -> resultSet.getString(1) }

    }


    /**
     * Zamienia nauczyciela na danych zajęciach na innego wolnego nauczyciela.
     *
     * @param firstTeacher Imię i nazwisko pierwszego nauczyciela w formacie "Nazwisko Imię".
     * @param secondTeacher Imię i nazwisko drugiego nauczyciela w formacie "Nazwisko Imię".
     * @param classes Edytowane zajęcia.
     */
    @Throws(SQLException::class)
    override fun setAnotherTeacher(firstTeacher: String, secondTeacher: String, classes: ClassesToRead) {
        val query = "{ CALL setAnotherTeacher(?,?,?,?,?,?,?,?,?,?,?) }"
        val location = if (classes.room.split(", ")[1] == MessageBundle.getMess("label.platform")) "Platform" else classes.room.split(", ")[1]
        val room = if (classes.room.split(", ")[0] == MessageBundle.getMess("label.virtual")) "Virtual" else classes.room.split(", ")[0]

        return  DBQueryExecutor.executePreparedStatement(
            query,
            classes.teacher.split(" ")[1],
            classes.teacher.split(" ")[0],
            secondTeacher.split(" ")[1],
            secondTeacher.split(" ")[0],
            classes.date,
            classes.hour,
            room,
            classes.group.split(", ")[0],
            classes.subject,
            classes.group.split(", ")[1],
            location
        )
    }

    /**
     * Pobiera listę grup na podanym kierunku, dla których plan nie jest jeszcze w pełni ułożony.
     *
     * @return Lista grup na podanym kierunku, dla których plan nie jest jeszcze w pełni ułożony.
     */
    override fun getGroups(field: String): ObservableList<String> {

        val query: String =
            "SELECT DISTINCT groupName FROM `groups`\n" +
                    "INNER JOIN group_field ON group_field.groupID = `groups`.groupID\n" +
                    "INNER JOIN group_subject_hours_left ON group_subject_hours_left.groupID = group_field.groupID\n" +
                    "WHERE fieldID = (SELECT fieldID FROM fields WHERE fieldName=?) \n" +
                    "AND group_subject_hours_left.weekHoursLeft>0\n" +
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


        return DBQueryExecutor.executeQuery(query, field){ resultSet -> resultSet.getString(1) }
    }


    /**
     * Pobiera sale, które w określonym czasie są wolne.
     *
     * @param classes Obiekt reprezentujący edytowane zajęcia.
     * @param locations Lista lokalizacji, z których można sprawdzić (nauczyciel lub grupa zdąży się do tych sal przemieścić)
     * @return Lista wolnych sal w określonym czasie.
     */
    override fun getFreeRooms(classes: ClassesToRead, locations: ObservableList<String>): ObservableList<String> {

        val formattedLocations = locations.joinToString(", ", "(", ")") { "'$it'" }

        val query = "SELECT CONCAT(r.roomName, ', ', l.name) AS rooms FROM rooms AS r\n" +
                "INNER JOIN location_room AS lr ON lr.roomID = r.roomID\n"+
                "INNER JOIN location AS l ON l.locationID = lr.locationID\n" +
                "WHERE r.roomID NOT IN \n" +
                "(SELECT roomId FROM plan AS p INNER JOIN hours AS h ON h.hourID = p.hourId WHERE p.date = ? AND h.hourRange = ?) AND l.name IN $formattedLocations"

        return DBQueryExecutor.executeQuery(query,
            classes.date,
            classes.hour
        ){resultSet -> resultSet.getString(1) }
    }

    /**
     * Pobiera sale, które są zajęte w danym terminie i znajdują się w tej samej lokalizacji co sala z edytowanych zajęć.
     *
     * @param classes Edytowane zajęcia.
     * @return Lista zajętych sal
     */
    override fun getBusyRooms(classes: ClassesToRead): ObservableList<String> {
        //W przypadku platformy nie mogą istnieć zajęte sale w
        if (classes.room.split(", ")[1] == MessageBundle.getMess("label.platform"))
        {
            return FXCollections.observableArrayList()
        }
        else
        {
            val query = "SELECT CONCAT(r.roomName, ', ', l.name) AS rooms FROM rooms AS r\n" +
            "        INNER JOIN location_room AS lr ON lr.roomID = r.roomID\n" +
                    "        INNER JOIN location AS l ON l.locationID = lr.locationID\n" +
                    "        INNER JOIN plan AS p ON p.roomId = r.roomID\n" +
                    "        WHERE p.date = ? AND p.hourId = (SELECT hourID FROM hours WHERE hourRange = ?) AND l.name=?"

            return DBQueryExecutor.executeQuery(query,
                classes.date,
                classes.hour,
                classes.room.split(", ")[1]
            ){resultSet -> resultSet.getString(1) }
        }
    }

    /**
     * Pobiera listę wolnych sal w wybranej lokalizacji w danym terminie.
     *
     * @param date  Data zajęć
     * @param hour  Godzina zajęć
     * @param location Lokalizacja
     * @return Lista wolnych sal w wybranej lokalizacji w danym terminie.
     */
    override fun getFreeRoomsByHour(date: LocalDate, hour: String, location: String): ObservableList<String> {
        val query = "SELECT CONCAT(roomName,', ', l.name) AS rooms FROM rooms AS r\n" +
                "INNER JOIN location_room AS lr ON lr.roomID = r.roomID\n" +
                "INNER JOIN location AS l ON l.locationID = lr.locationID\n" +
                " WHERE r.roomID NOT IN \n" +
                "(SELECT DISTINCT roomID FROM plan AS p WHERE p.date = ? AND p.hourId = (SELECT hourID FROM hours WHERE hourRange = ?)) AND l.name = ?"

        return DBQueryExecutor.executeQuery(query, date, hour, location){resultSet -> resultSet.getString(1) }
    }


    /**
     * Zmienia godzinę i salę podanych zajęć na inną wolną.
     *
     * @param selectedItem Nowa sala zajęć.
     * @param hour Nowa godzina zajęć
     * @param classes Obiekt reprezentujący zajęcia, dla których chcemy zmienić godzinę.
     */
    @Throws(SQLException::class)
    override fun changeHours(selectedItem: String, hour: String, classes: ClassesToRead) {
        val query = "{ CALL changeHours(?,?,?,?,?,?,?,?,?,?,?,?) }"
        return  DBQueryExecutor.executePreparedStatement(query, classes.teacher.split(" ")[1], classes.teacher.split(" ")[0], classes.date, classes.hour, hour, classes.room.split(", ")[0], selectedItem.split(", ")[0], classes.group.split(", ")[0], classes.subject, classes.group.split(", ")[1], classes.room.split(", ")[1], selectedItem.split(", ")[1])
    }


    /**
     * Zmienia godzinę podanych zajęć na inną (dla zajęć na platformie).
     *
     * @param hour Nowa godzina zajęć.
     * @param classes Obiekt reprezentujący zajęcia, dla których chcemy zmienić godzinę.
     */
    @Throws(SQLException::class)
    override fun changeHoursPlatform(hour: String, classes: ClassesToRead) {
        val query = "{ CALL changeHourPlatform(?,?,?,?,?,?,?,?,?,?) }"
        return  DBQueryExecutor.executePreparedStatement(query, classes.teacher.split(" ")[1], classes.teacher.split(" ")[0], classes.date, classes.hour, hour, classes.room.split(", ")[0], classes.group.split(", ")[0], classes.subject, classes.group.split(", ")[1], classes.room.split(", ")[1])
    }


    /**
     * Zmienia salę zajęć na inną wolną salę.
     *
     * @param selectedItem Nowa sala zajęć.
     * @param classes Obiekt reprezentujący edytowane zajęcia.
     */
    @Throws(SQLException::class)
    override fun setAnotherRoom(selectedItem: String, classes: ClassesToRead) {
        val locationTo = if (selectedItem.split(", ")[1] == MessageBundle.getMess("label.platform")) "Platform" else selectedItem.split(", ")[1]
        val roomTo = if (selectedItem.split(", ")[0] == MessageBundle.getMess("label.virtual")) "Virtual" else selectedItem.split(", ")[0]
        val locationFrom = if (classes.room.split(", ")[1] == MessageBundle.getMess("label.platform")) "Platform" else classes.room.split(", ")[1]
        val roomFrom = if (classes.room.split(", ")[0] == MessageBundle.getMess("label.virtual")) "Virtual" else classes.room.split(", ")[0]

        val query = "{ CALL setAnotherRoom(?,?,?,?,?,?,?,?,?,?,?) }"
        return  DBQueryExecutor.executePreparedStatement(query, classes.teacher.split(" ")[1], classes.teacher.split(" ")[0], classes.date, classes.hour, roomFrom, roomTo, classes.group.split(", ")[0], classes.subject, classes.group.split(", ")[1], locationFrom, locationTo)
    }

    /**
     * Zamienia sale na zajęciach (robi 2 updaty bo grupa, która odbywa zajęcia w sali, którą użytkownik chce przypisać też zmienia salę).
     *
     * @param selectedItem Nowa sala zajęć.
     * @param classes Obiekt reprezentujący edytowane zajęcia.
     */
    @Throws(SQLException::class)
    override fun changeRooms(selectedItem: String, classes: ClassesToRead) {
        val query = "{ CALL changeRooms(?,?,?,?,?,?,?,?,?,?,?) }"
        return  DBQueryExecutor.executePreparedStatement(query, classes.teacher.split(" ")[1], classes.teacher.split(" ")[0], classes.date, classes.hour, classes.room.split(", ")[0], selectedItem.split(", ")[0], classes.group.split(", ")[0], classes.subject, classes.group.split(", ")[1], classes.room.split(", ")[1], selectedItem.split(", ")[1])
    }

    /**
     * Zamienia nauczycieli na określonych zajęciach.
     *
     * @param firstTeacher Imię i nazwisko pierwszego nauczyciela w formacie "Nazwisko Imię".
     * @param secondTeacher Imię i nazwisko drugiego nauczyciela w formacie "Nazwisko Imię".
     * @param classes Obiekt reprezentujący edytowane zajęcia.
     */
    @Throws(SQLException::class)
    override fun changeTeachers(firstTeacher: String, secondTeacher: String, classes: ClassesToRead) {
        val query = "{ CALL changeTeachers(?,?,?,?,?,?,?,?,?,?,?) }"
        val location = if (classes.room.split(", ")[1] == MessageBundle.getMess("label.platform")) "Platform" else classes.room.split(", ")[1]
        val room = if (classes.room.split(", ")[0] == MessageBundle.getMess("label.virtual")) "Virtual" else classes.room.split(", ")[0]
        return  DBQueryExecutor.executePreparedStatement(query, firstTeacher.split(" ")[1], firstTeacher.split(" ")[0], secondTeacher.split(" ")[1], secondTeacher.split(" ")[0], classes.date, classes.hour, room, classes.group.split(", ")[0], classes.subject, classes.group.split(", ")[1], location)
    }

    fun getLocationsNames(): ObservableList<String> {
        val query = "SELECT name FROM location WHERE name!='Platform' ORDER BY name"
        return DBQueryExecutor.executeQuery(query){ resultSet -> resultSet.getString(1) }
    }

    /**
     * Usuwa zajęcia z planu.
     *
     * @param classes Obiekt reprezentujący zajęcia do usunięcia.
     */
    @Throws(SQLException::class)
    override fun deleteClasses(classes: ClassesToRead) {
        val location = if (classes.room.split(", ")[1] == MessageBundle.getMess("label.platform")) "Platform" else classes.room.split(", ")[1]
        val room = if (classes.room.split(", ")[0] == MessageBundle.getMess("label.virtual")) "Virtual" else classes.room.split(", ")[0]

        val query = "{ CALL deleteFromPlan(?,?,?,?,?,?,?,?,?) }"
        return  DBQueryExecutor.executePreparedStatement(query, classes.date, classes.hour, room, location, classes.group.split(", ")[0], classes.teacher.split(" ")[1], classes.teacher.split(" ")[0], classes.subject, classes.group.split(", ")[1])
    }

    /**
     * Metoda, pobierająca tylko te godziny, w których grupa nie ma innych zajęć w danym dniu
     * oraz nauczyciel zgłosił na ten termin dyspozycyjność oraz nie ma innych zajęć
     *
     * @param classesToEdit Obiekt reprezentujący zajęcia do edycji.
     * @return Lista godzin spełniających warunki
     */
    override fun getFreeHours(classesToEdit: ClassesToRead): ObservableList<String> {
        val query = "SELECT hourRange FROM hours AS h WHERE h.hourID NOT IN\n" +
                "(SELECT hourId FROM plan AS p WHERE p.date=? AND p.groupId = \n" +
                "(SELECT g.groupID FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID INNER JOIN fields AS f \n" +
                "ON f.fieldID = gf.fieldID WHERE g.groupName=? AND f.fieldName = ?))\n" +
                "AND h.hourID IN \n" +
                "(SELECT hourID FROM availability AS a WHERE a.hourID NOT IN (SELECT hourId FROM plan AS p WHERE p.teacherId = (SELECT teacherID FROM teachers AS t WHERE t.name = ? AND t.lastname = ?)\n" +
                "AND p.date = ?)\n" +
                "AND a.teacherID = (SELECT teacherID FROM teachers AS t WHERE t.name = ? AND t.lastname = ?))"

        return DBQueryExecutor.executeQuery(query, classesToEdit.date, classesToEdit.group.split(", ")[0], classesToEdit.group.split(", ")[1], classesToEdit.teacher.split(" ")[1], classesToEdit.teacher.split(" ")[0], classesToEdit.date, classesToEdit.teacher.split(" ")[1], classesToEdit.teacher.split(" ")[0])
        { resultSet -> resultSet.getString(1)}}
}