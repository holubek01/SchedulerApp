package com.example.scheduler.db.dao

import com.example.scheduler.db.DBConnection
import com.example.scheduler.db.DBQueryExecutor
import com.example.scheduler.models.ClassesToRead
import com.example.scheduler.objects.PlanForRooms
import com.example.scheduler.utils.CommonUtils
import com.example.scheduler.utils.EnglishDayConverter
import com.example.scheduler.utils.MessageBundle
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.SQLException
import java.time.LocalDate
import java.util.Locale

/**
 * Implementacja interfejsu `PlanDAO` do obsługi operacji w bazie danych dla planów
 */
class PlanDAOImpl:PlanDAO {

    /**
     * Usuwa plan o podanej nazwie (tabela w bazie danych) oraz tabelę z odpowiadającymi mu
     * godzinami jakie zostały do ułożenia pełnego planu.
     *
     * @param planName      Nazwa planu do usunięcia.
     * @param hoursLeftName Nazwa tabeli z godzinami, które pozostały.
     */
    @Throws(SQLException::class)
    override fun deletePlan(planName: String, hoursLeftName: String) {
        val query = "{ CALL deletePlan(?,?) }"
        DBQueryExecutor.executePreparedStatement(query,planName, hoursLeftName)
    }

    /**
     * Pobiera listę nazw wszystkich istniejących planów w bazie danych.
     *
     * @return Lista istniejących planów.
     */
    override fun getAllPlans(): ObservableList<String> {
        val query= "SELECT table_name FROM information_schema.tables WHERE table_name LIKE 'plan_%' AND table_schema = DATABASE();"
        return DBQueryExecutor.executeQuery(query){ resultSet -> resultSet.getString(1) }
    }

    /**
     * Pobiera maksymalną datę z tabeli 'plan'.
     *
     * @return Maksymalna data z tabeli 'plan' lub LocalDate.MIN, jeśli tabela jest pusta.
     */
    override fun getMaxDateFromTable(): LocalDate {
        val query = "SELECT DISTINCT MAX(date) FROM plan"
        val result = DBQueryExecutor.executeQuery(query) { resultSet ->
            resultSet.getString(1)?.let {
                LocalDate.parse(it)
            }
        }

        return result.first() ?: LocalDate.MIN
    }


    /**
     * Pobiera plan zajęć dla danej lokalizacji w formie rozkładu dla sal (W tabeli w ShowPlanForRooms).
     * @return Rozpiska zajęć dla wybranej lokalizacji.
     */
    override fun getPlan(location: String, rooms: ObservableList<String>): ObservableList<PlanForRooms> {

        val query = "SELECT plan.date,hourRange, GROUP_CONCAT(CONCAT(CONCAT(SUBSTRING(groupName, 1, LENGTH(groupName)-1), fieldShort, SUBSTRING(groupName, LENGTH(groupName), 1)),':',r.roomName) SEPARATOR '|') AS pairs from plan " +
                "INNER JOIN hours h on plan.hourId = h.hourId " +
                "INNER JOIN `groups` g on plan.groupId = g.groupID " +
                "INNER JOIN rooms r on plan.roomId = r.roomID " +
                "INNER JOIN location_room AS lr ON lr.roomID = r.roomID " +
                "INNER JOIN group_field gf on g.groupID = gf.groupID "+
                "INNER JOIN fields f on gf.fieldID = f.fieldID " +
                "WHERE locationID = (SELECT locationID FROM location WHERE location.name = ?) " +
                "GROUP BY date, hourRange ORDER BY date, hourRange"


        val connection = DBConnection.getConnection()
        val statement = connection.prepareStatement(query)
        statement.setString(1,location)

        val resultSet = statement.executeQuery()

        val resultList = FXCollections.observableArrayList<PlanForRooms>()

        while(resultSet.next()) {

            val list: ObservableList<String> = FXCollections.observableArrayList()
            repeat(rooms.size) {
                list.add("")
            }

            //IIA:Aula|IB:Aula
            val pairs = resultSet.getString("pairs").split("|")
            for (pair in pairs)
            {
                //mamy pare np IIA:Aula
                val shortcut = pair.split(":")[0].trim()
                val index = rooms.indexOf(pair.split(":")[1].trim())
                list[index] = shortcut
            }

            val loc = PlanForRooms(
                date = LocalDate.parse(resultSet.getString("date")),
                hour = resultSet.getString("hourRange"),
                rooms = list)

            resultList.add(loc)
        }

        statement.close()
        DBConnection.closeConnection()
        return resultList
    }

    /**
     * Pobiera plan zajęć dla platformy wirtualnej.
     * @return Rozpiska zajęć dla sali wirtualnej.
     */
    override fun getPlanForPlatform(): ObservableList<PlanForRooms> {

        val query = "SELECT plan.date, hourRange, GROUP_CONCAT(CONCAT(SUBSTRING(groupName, 1, LENGTH(groupName)-1), fieldShort, SUBSTRING(groupName, LENGTH(groupName), 1)) SEPARATOR ', ') AS `groups` from plan " +
                "INNER JOIN hours h on plan.hourId = h.hourId \n" +
                "INNER JOIN `groups` g on plan.groupId = g.groupID \n" +
                "INNER JOIN rooms r on plan.roomId = r.roomID \n" +
                "INNER JOIN location_room AS lr ON lr.roomID = r.roomID \n" +
                "INNER JOIN group_field gf on g.groupID = gf.groupID \n" +
                "INNER JOIN fields f on gf.fieldID = f.fieldID\n" +
                "WHERE locationID = (SELECT locationID FROM location WHERE location.name = 'Platform')\n" +
                "GROUP BY date, hourRange ORDER BY date, hourRange"

        val connection = DBConnection.getConnection()
        val statement = connection.prepareStatement(query)

        val resultSet = statement.executeQuery()

        val resultList = FXCollections.observableArrayList<PlanForRooms>()

        while(resultSet.next()) {

            val list: ObservableList<String> = FXCollections.observableArrayList()
            val groups = resultSet.getString("groups")
            list.add(groups)

            val loc = PlanForRooms(
                date = LocalDate.parse(resultSet.getString("date")),
                hour = resultSet.getString("hourRange"),
                rooms = list)

            resultList.add(loc)
        }

        statement.close()
        DBConnection.closeConnection()
        return resultList
    }

    /**
     * Pobiera plan lekcji nauczyciela dla określonego dnia.
     *
     * @param teacher Imię i nazwisko nauczyciela.
     * @param day Dzień, dla którego pobierany jest plan lekcji.
     * @return Plan zajęć nauczyciela w formie listy obiektów ClassesRead.
     */
    override fun getPlanTeacher(teacher: String, day: String): ObservableList<ClassesToRead> {

        var query  = "SELECT date, hourRange, subjectName, CONCAT(roomName, ', ', location.name) AS roomName, CONCAT(teachers.lastname, ' ', teachers.name) AS teacher, CONCAT(groupName, ', ', fieldName) AS `group` FROM plan\n" +
                "INNER JOIN hours ON hours.hourID = plan.hourId\n" +
                "INNER JOIN subjects ON subjects.subjectID = plan.subjectId\n" +
                "INNER JOIN rooms ON rooms.roomID = plan.roomId\n" +
                "INNER JOIN location_room AS lr ON lr.roomID  = rooms.roomID\n" +
                "INNER JOIN location ON location.locationID = lr.locationID\n" +
                "INNER JOIN teachers ON teachers.teacherID = plan.teacherId\n" +
                "INNER JOIN `groups` g on plan.groupId = g.groupID\n" +
                "INNER JOIN group_field gf on g.groupID = gf.groupID\n" +
                "INNER JOIN fields f on gf.fieldID = f.fieldID\n" +
                "WHERE teachers.name = ? AND teachers.lastname = ? \n"

        val name = teacher.split(" ")[1]
        val lastname = teacher.split(" ")[0]

        if (day != MessageBundle.getMess("label.wholePlan"))
        {
            val englishDay = if(MessageBundle.bundle.locale.equals(Locale("pl", "PL"))) EnglishDayConverter.fromPolishName(day)!!.uppercase() else day.uppercase()
            val subquery = "AND DAYNAME(date) = ? ORDER BY plan.date, hours.hourID"
            query += subquery

            return DBQueryExecutor.executeQuery(query, name, lastname, englishDay){
                    resultSet ->
                ClassesToRead(
                    date = LocalDate.parse(resultSet.getString(1)),
                    hour = resultSet.getString(2),
                    subject = resultSet.getString(3),
                    room = resultSet.getString(4),
                    teacher = resultSet.getString(5),
                    group = resultSet.getString(6))
            }
        }
        else{
            val subquery ="ORDER BY plan.date, hours.hourID"
            query += subquery

            return DBQueryExecutor.executeQuery(query, name, lastname){
                    resultSet ->
                ClassesToRead(
                    date = LocalDate.parse(resultSet.getString(1)),
                    hour = resultSet.getString(2),
                    subject = resultSet.getString(3),
                    room = resultSet.getString(4),
                    teacher = resultSet.getString(5),
                    group = resultSet.getString(6))
            }
        }
    }


    /**
     * Pobiera plan lekcji grupy dla określonego dnia.
     *
     * @param field Nazwa kierunku kształcenia grupy.
     * @param group Nazwa grupy.
     * @param day Dzień, dla którego pobierany jest plan lekcji.
     * @return Plan zajęć grupy w formie listy obiektów ClassesRead.
     */
    override fun getPlanGroup(field: String, group: String,day: String): ObservableList<ClassesToRead> {

        var query  = "SELECT date, hourRange, subjectName, CONCAT(roomName, ', ', location.name) AS roomName, CONCAT(teachers.lastname, ' ', teachers.name) AS teacher, CONCAT(g.groupName, ', ', fieldName) AS `group` FROM plan\n" +
                "INNER JOIN hours ON hours.hourID = plan.hourId\n" +
                "INNER JOIN subjects ON subjects.subjectID = plan.subjectId\n" +
                "INNER JOIN rooms ON rooms.roomID = plan.roomId\n" +
                "INNER JOIN location_room AS lr ON lr.roomID = rooms.roomID\n" +
                "INNER JOIN location ON location.locationID = lr.locationID\n" +
                "INNER JOIN teachers ON teachers.teacherID = plan.teacherId\n" +
                "INNER JOIN `groups` g on plan.groupId = g.groupID\n" +
                "INNER JOIN group_field gf on g.groupID = gf.groupID\n" +
                "INNER JOIN fields f on gf.fieldID = f.fieldID\n" +
                "WHERE plan.groupId = (SELECT `groups`.groupID FROM `groups`" +
                "INNER JOIN group_field gf on `groups`.groupID = gf.groupID\n " +
                "INNER JOIN fields f on gf.fieldID = f.fieldID WHERE fieldName = ? AND groupName = ?)\n"


        if (day!= MessageBundle.getMess("label.wholePlan")) {
            val englishDay = if (MessageBundle.bundle.locale.equals(Locale("pl", "PL"))) EnglishDayConverter.fromPolishName(day)!!.uppercase() else day.uppercase()
            val subquery = " AND DAYNAME(date) = ? ORDER BY plan.date, hours.hourID"
            query += subquery

            return DBQueryExecutor.executeQuery(query, field, group, englishDay){
                    resultSet -> ClassesToRead(
                date = LocalDate.parse(resultSet.getString(1)),
                hour = resultSet.getString(2),
                subject = resultSet.getString(3),
                room = resultSet.getString(4),
                teacher = resultSet.getString(5),
                group = resultSet.getString(6))
            }
        }

        val subquery = " ORDER BY plan.date, hours.hourID"
        query += subquery
        return DBQueryExecutor.executeQuery(query, field, group){
                resultSet -> ClassesToRead(
            date = LocalDate.parse(resultSet.getString(1)),
            hour = resultSet.getString(2),
            subject = resultSet.getString(3),
            room = resultSet.getString(4),
            teacher = resultSet.getString(5),
            group = resultSet.getString(6))
        }
    }

    /**
     * Usuwa wszystkie zajęcia z aktualnego planu oraz ustawia tabelę z godzinami, które
     * pozostały do ułożenia pełnego planu na wartości domyślne (takie jak w szkolnym planie nauczania).
     */
    @Throws(SQLException::class)
    override fun refillHours() {
        val query = "{ CALL refillHours() }"
        DBQueryExecutor.executePreparedStatement(query)
    }

    /**
     * Tworzy (kopiuje) lub aktualizuje tabelę w bazie danych na podstawie podanej nazwy tabeli.
     *
     * @param tableName   Nazwa tworzonej lub aktualizowanej tabeli.
     * @param fromTable   Nazwa źródłowej tabeli, na podstawie której ma być utworzona lub zaktualizowana tabela.
     */
    @Throws(SQLException::class)
    override fun createTable(tableName: String, fromTable: String) {
        val query = "{ CALL createOrUpdateTable(?,?) }"

        DBQueryExecutor.executePreparedStatement(
            query,
            tableName,
            fromTable
        )
    }

    /**
     * Sprawdza, czy należy zapisać aktualny plan (Czy istnieją zajęcia w planie).
     *
     * @return true, jeśli należy zapisać aktualny plan, false w przeciwnym razie.
     */
    override fun shouldSaveOldPlan(): Boolean {
        val query = "SELECT COUNT(*)>0 FROM plan"
        return DBQueryExecutor.executeQuery(query){ resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy istnieje już plan w podanym terminie, czyli czy istnieje odpowiadająca mu tabela w bazie danych.
     *
     * @param day Data, dla której ma być sprawdzone czy plan istnieje.
     * @return true, jeśli istnieje plan w podanym terminie, false w przeciwnym razie.
     */
    override fun checkIfPlanExists(day: LocalDate): Boolean{

        val planStart = CommonUtils.getPlanStartDay(day)
        val planName = "plan_$planStart"

        val query = "SELECT SUM(table_exists) FROM (\n" +
                "SELECT COUNT(*) > 0 AS table_exists FROM information_schema.tables WHERE table_name = ?\n" +
                "UNION \n" +
                "SELECT COUNT(*) FROM plan WHERE plan.date IN (?,?,?)) AS unitedTable"

        return DBQueryExecutor.executeQuery(query,planName, planStart, planStart.plusDays(1), planStart.plusDays(2)){ resultSet -> resultSet.getBoolean(1) }.first()
    }

    /**
     * Metoda, sprawdzająca czy plan jest pełny (wszystkie godziny ze szkolnego planu nauczania są realizowane)
     */
    override fun isPlanFull(): Boolean {
        val query = "SELECT sum(weekHoursLeft)=0 FROM group_subject_hours_left"
        return DBQueryExecutor.executeQuery(query){ resultSet -> resultSet.getBoolean(1) }.first()
    }


    /**
     * Uzupełnia plan (przywraca) zajęć na podstawie istniejącego planu
     * o podanej nazwie z bazy danych i odpowiadającej mu tabeli z godzinami.
     *
     * @param tablePlan Nazwa istniejącego planu, który chcemy przywrócić.
     * @param tableHoursLeft Nazwa odpowiadającej tabeli z godzinami.
     */
    @Throws(SQLException::class)
    override fun refillFromOldPlan(tablePlan: String, tableHoursLeft: String) {
        val query = "{ CALL refillFromOldPlan(?,?) }"

        DBQueryExecutor.executePreparedStatement(
            query,
            tablePlan,
            tableHoursLeft
        )
    }

    /**
     * Kopiuje zawartość jednej tabeli do innej w bazie danych (kopiuje plany).
     *
     * @param tableTo Nazwa tabeli docelowej, do której zostanie skopiowana zawartość.
     * @param tableFrom Nazwa tabeli źródłowej, z której zostanie skopiowana zawartość.
     * @param oldFriday Piątkowa data z tabeli, z której plan jest kopiowany.
     * @param oldSaturday Sobotnia data z tabeli, z której plan jest kopiowany.
     * @param oldSunday Niedzielna data z tabeli, z której plan jest kopiowany.
     * @param newFriday Piątkowa data z tabeli, do której plan jest kopiowany.
     * @param newSaturday Sobotnia data z tabeli, do której plan jest kopiowany.
     * @param newSunday Niedzielna data z tabeli, do której plan jest kopiowany.
     */
    @Throws(SQLException::class)
    override fun copyTable(tableTo: String, tableFrom: String, oldFriday: String, oldSaturday: String, oldSunday: String, newFriday: String, newSaturday: String, newSunday: String) {
        val query = "{ CALL copyTable(?,?,?,?,?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(query, tableTo, tableFrom, oldFriday, oldSaturday, oldSunday, newFriday, newSaturday, newSunday)
    }
}