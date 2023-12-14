CREATE DATABASE  IF NOT EXISTS `scheduler_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `scheduler_db`;
-- MySQL dump 10.13  Distrib 8.0.30, for Win64 (x86_64)
--
-- Host: localhost    Database: scheduler_db
-- ------------------------------------------------------
-- Server version	8.0.30

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `availability`
--

DROP TABLE IF EXISTS `availability`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `availability` (
  `availabilityID` int NOT NULL AUTO_INCREMENT,
  `teacherID` int NOT NULL,
  `dayID` int NOT NULL,
  `hourID` int NOT NULL,
  PRIMARY KEY (`availabilityID`),
  KEY `fk_availability_teacher_idx` (`teacherID`),
  KEY `fk_availability_hour_idx` (`hourID`),
  KEY `fk_availability_day_idx` (`dayID`),
  CONSTRAINT `fk_availability_day` FOREIGN KEY (`dayID`) REFERENCES `day_of_week` (`dayID`),
  CONSTRAINT `fk_availability_hour` FOREIGN KEY (`hourID`) REFERENCES `hours` (`hourId`),
  CONSTRAINT `fk_availability_teacher` FOREIGN KEY (`teacherID`) REFERENCES `teachers` (`teacherID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `availability`
--

LOCK TABLES `availability` WRITE;
/*!40000 ALTER TABLE `availability` DISABLE KEYS */;
/*!40000 ALTER TABLE `availability` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `day_of_week`
--

DROP TABLE IF EXISTS `day_of_week`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `day_of_week` (
  `dayID` int NOT NULL AUTO_INCREMENT,
  `dayName` varchar(20) NOT NULL,
  PRIMARY KEY (`dayID`),
  UNIQUE KEY `dayName_UNIQUE` (`dayName`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `day_of_week`
--

LOCK TABLES `day_of_week` WRITE;
/*!40000 ALTER TABLE `day_of_week` DISABLE KEYS */;
INSERT INTO `day_of_week` VALUES (1,'FRIDAY'),(2,'SATURDAY'),(3,'SUNDAY');
/*!40000 ALTER TABLE `day_of_week` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `field_subject`
--

DROP TABLE IF EXISTS `field_subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `field_subject` (
  `fieldSubjectID` int NOT NULL AUTO_INCREMENT,
  `subjectID` int NOT NULL,
  `fieldID` int NOT NULL,
  `term` int NOT NULL,
  `weeklyHours` int NOT NULL,
  PRIMARY KEY (`fieldSubjectID`),
  KEY `fk_subject_idx` (`subjectID`),
  KEY `fk_field_idx` (`fieldID`),
  CONSTRAINT `fk_field` FOREIGN KEY (`fieldID`) REFERENCES `fields` (`fieldID`),
  CONSTRAINT `fk_subject` FOREIGN KEY (`subjectID`) REFERENCES `subjects` (`subjectID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `field_subject`
--

LOCK TABLES `field_subject` WRITE;
/*!40000 ALTER TABLE `field_subject` DISABLE KEYS */;
/*!40000 ALTER TABLE `field_subject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fields`
--

DROP TABLE IF EXISTS `fields`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fields` (
  `fieldID` int NOT NULL AUTO_INCREMENT,
  `fieldName` varchar(80) NOT NULL,
  `fieldShort` varchar(10) NOT NULL,
  `semNumber` int NOT NULL,
  PRIMARY KEY (`fieldID`),
  UNIQUE KEY `fieldShort_UNIQUE` (`fieldShort`),
  UNIQUE KEY `fieldName_UNIQUE` (`fieldName`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fields`
--

LOCK TABLES `fields` WRITE;
/*!40000 ALTER TABLE `fields` DISABLE KEYS */;
/*!40000 ALTER TABLE `fields` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_field`
--

DROP TABLE IF EXISTS `group_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_field` (
  `groupFieldID` int NOT NULL AUTO_INCREMENT,
  `groupID` int NOT NULL,
  `fieldID` int NOT NULL,
  PRIMARY KEY (`groupFieldID`),
  KEY `fk_group_idx` (`groupID`),
  KEY `fk_field_idx` (`fieldID`),
  CONSTRAINT `fk_ffield` FOREIGN KEY (`fieldID`) REFERENCES `fields` (`fieldID`),
  CONSTRAINT `fk_group` FOREIGN KEY (`groupID`) REFERENCES `groups` (`groupID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_field`
--

LOCK TABLES `group_field` WRITE;
/*!40000 ALTER TABLE `group_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_subject_hours_left`
--

DROP TABLE IF EXISTS `group_subject_hours_left`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_subject_hours_left` (
  `hoursLeftID` int NOT NULL AUTO_INCREMENT,
  `groupID` int NOT NULL,
  `subjectID` int NOT NULL,
  `weekHoursLeft` int NOT NULL,
  PRIMARY KEY (`hoursLeftID`),
  KEY `fk_hoursLeft_group_idx` (`groupID`),
  KEY `fk_hoursLeft_subject_idx` (`subjectID`),
  CONSTRAINT `fk_hoursLeft_group` FOREIGN KEY (`groupID`) REFERENCES `groups` (`groupID`),
  CONSTRAINT `fk_hoursLeft_subject` FOREIGN KEY (`subjectID`) REFERENCES `subjects` (`subjectID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_subject_hours_left`
--

LOCK TABLES `group_subject_hours_left` WRITE;
/*!40000 ALTER TABLE `group_subject_hours_left` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_subject_hours_left` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS `groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `groups` (
  `groupID` int NOT NULL AUTO_INCREMENT,
  `groupName` varchar(10) NOT NULL,
  `term` int NOT NULL,
  PRIMARY KEY (`groupID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groups`
--

LOCK TABLES `groups` WRITE;
/*!40000 ALTER TABLE `groups` DISABLE KEYS */;
/*!40000 ALTER TABLE `groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hours`
--

DROP TABLE IF EXISTS `hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hours` (
  `hourId` int NOT NULL AUTO_INCREMENT,
  `hourRange` varchar(45) NOT NULL,
  PRIMARY KEY (`hourId`),
  UNIQUE KEY `range_UNIQUE` (`hourRange`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hours`
--

LOCK TABLES `hours` WRITE;
/*!40000 ALTER TABLE `hours` DISABLE KEYS */;
INSERT INTO `hours` VALUES (1,'08.00-08.45'),(2,'08.45-09.30'),(3,'09.35-10.20'),(4,'10.20-11.05'),(5,'11.10-11.55'),(6,'11.55-12.40'),(7,'12.50-13.35'),(8,'13.35-14.20'),(9,'14.25-15.10'),(10,'15.10-15.55'),(11,'16.00-16.45'),(12,'16.45-17.30'),(13,'17.30-18.15'),(14,'18.20-19.05'),(15,'19.10-19.55'),(16,'20.00-20.45'),(17,'20.45-21.30');
/*!40000 ALTER TABLE `hours` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `location` (
  `locationId` int NOT NULL AUTO_INCREMENT,
  `name` varchar(60) NOT NULL,
  `city` varchar(45) NOT NULL,
  `street` varchar(60) NOT NULL,
  `postcode` varchar(6) NOT NULL,
  PRIMARY KEY (`locationId`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` VALUES (1,'Platform','-','-','-');
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_room`
--

DROP TABLE IF EXISTS `location_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `location_room` (
  `location_room_id` int NOT NULL AUTO_INCREMENT,
  `locationID` int NOT NULL,
  `roomID` int NOT NULL,
  PRIMARY KEY (`location_room_id`),
  KEY `fk_location_id_idx` (`locationID`),
  KEY `fk_room_id_idx` (`roomID`),
  CONSTRAINT `fk_location_id` FOREIGN KEY (`locationID`) REFERENCES `location` (`locationId`),
  CONSTRAINT `fk_room_id` FOREIGN KEY (`roomID`) REFERENCES `rooms` (`roomID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_room`
--

LOCK TABLES `location_room` WRITE;
/*!40000 ALTER TABLE `location_room` DISABLE KEYS */;
INSERT INTO `location_room` VALUES (1,1,1);
/*!40000 ALTER TABLE `location_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plan`
--

DROP TABLE IF EXISTS `plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plan` (
  `classesId` int NOT NULL AUTO_INCREMENT,
  `date` varchar(45) NOT NULL,
  `hourId` int NOT NULL,
  `roomId` int NOT NULL,
  `groupId` int NOT NULL,
  `teacherId` int NOT NULL,
  `subjectId` int NOT NULL,
  PRIMARY KEY (`classesId`),
  KEY `fk_plan_hour_id_idx` (`hourId`),
  KEY `fk_plan_room_id_idx` (`roomId`),
  KEY `fk_plan_group_id_idx` (`groupId`),
  KEY `fk_plan_teacher_id_idx` (`teacherId`),
  KEY `fk_plan_subject_id_idx` (`subjectId`),
  CONSTRAINT `fk_plan_group_id` FOREIGN KEY (`groupId`) REFERENCES `groups` (`groupID`),
  CONSTRAINT `fk_plan_hour_id` FOREIGN KEY (`hourId`) REFERENCES `hours` (`hourId`),
  CONSTRAINT `fk_plan_room_id` FOREIGN KEY (`roomId`) REFERENCES `rooms` (`roomID`),
  CONSTRAINT `fk_plan_subject_id` FOREIGN KEY (`subjectId`) REFERENCES `subjects` (`subjectID`),
  CONSTRAINT `fk_plan_teacher_id` FOREIGN KEY (`teacherId`) REFERENCES `teachers` (`teacherID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plan`
--

LOCK TABLES `plan` WRITE;
/*!40000 ALTER TABLE `plan` DISABLE KEYS */;
/*!40000 ALTER TABLE `plan` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `decreaseHours` AFTER INSERT ON `plan` FOR EACH ROW BEGIN
	UPDATE group_subject_hours_left AS g SET g.weekHoursLeft = g.weekHoursLeft - 1
	WHERE g.groupID = NEW.groupID AND g.subjectId = NEW.subjectId;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `increaseHours` AFTER DELETE ON `plan` FOR EACH ROW BEGIN
	UPDATE group_subject_hours_left AS g SET g.weekHoursLeft = g.weekHoursLeft + 1
	WHERE g.groupID = OLD.groupID AND g.subjectId = OLD.subjectId;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `roleId` int NOT NULL AUTO_INCREMENT,
  `roleName` varchar(40) NOT NULL,
  PRIMARY KEY (`roleId`),
  UNIQUE KEY `roleName_UNIQUE` (`roleName`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'Dyrektor');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `roomID` int NOT NULL AUTO_INCREMENT,
  `roomName` varchar(45) NOT NULL,
  `volume` int NOT NULL,
  `floor` int NOT NULL,
  PRIMARY KEY (`roomID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES (1,'Virtual',100,0);
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subjects`
--

DROP TABLE IF EXISTS `subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subjects` (
  `subjectID` int NOT NULL AUTO_INCREMENT,
  `subjectName` varchar(120) NOT NULL,
  PRIMARY KEY (`subjectID`),
  UNIQUE KEY `subjectName_UNIQUE` (`subjectName`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subjects`
--

LOCK TABLES `subjects` WRITE;
/*!40000 ALTER TABLE `subjects` DISABLE KEYS */;
/*!40000 ALTER TABLE `subjects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_subject`
--

DROP TABLE IF EXISTS `teacher_subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_subject` (
  `teacherSubjectID` int NOT NULL AUTO_INCREMENT,
  `teacherID` int NOT NULL,
  `subjectID` int NOT NULL,
  PRIMARY KEY (`teacherSubjectID`),
  KEY `fk_teacher_subject_idx` (`subjectID`),
  KEY `fk_subject_teacher_idx` (`teacherID`),
  CONSTRAINT `fk_subject_teacher` FOREIGN KEY (`teacherID`) REFERENCES `teachers` (`teacherID`),
  CONSTRAINT `fk_teacher_subject` FOREIGN KEY (`subjectID`) REFERENCES `subjects` (`subjectID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_subject`
--

LOCK TABLES `teacher_subject` WRITE;
/*!40000 ALTER TABLE `teacher_subject` DISABLE KEYS */;
/*!40000 ALTER TABLE `teacher_subject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teachers`
--

DROP TABLE IF EXISTS `teachers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teachers` (
  `teacherID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `lastname` varchar(45) NOT NULL,
  `phone` varchar(45) NOT NULL,
  `mail` varchar(45) NOT NULL,
  PRIMARY KEY (`teacherID`),
  UNIQUE KEY `mail_UNIQUE` (`mail`),
  UNIQUE KEY `phone_UNIQUE` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teachers`
--

LOCK TABLES `teachers` WRITE;
/*!40000 ALTER TABLE `teachers` DISABLE KEYS */;
/*!40000 ALTER TABLE `teachers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `userId` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `pass` char(60) NOT NULL,
  `isTempPassword` tinyint NOT NULL,
  PRIMARY KEY (`userId`),
  UNIQUE KEY `password_UNIQUE` (`pass`),
  UNIQUE KEY `name_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'User','$2a$10$oXm70.vicEeUzd2lCWVya.0guh4VQd0O4Bf0V95UCq1umWlnQpGzO',1);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `iduser_role` int NOT NULL AUTO_INCREMENT,
  `userID` int NOT NULL,
  `roleID` int NOT NULL,
  PRIMARY KEY (`iduser_role`),
  KEY `fk_roleID_idx` (`roleID`),
  KEY `fk_userID_idx` (`userID`),
  CONSTRAINT `fk_roleID` FOREIGN KEY (`roleID`) REFERENCES `roles` (`roleId`),
  CONSTRAINT `fk_userID` FOREIGN KEY (`userID`) REFERENCES `user` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,1,1);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'scheduler_db'
--

--
-- Dumping routines for database 'scheduler_db'
--
/*!50003 DROP FUNCTION IF EXISTS `canGroupMoveBetweenClasses` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `canGroupMoveBetweenClasses`(
	groupName varchar(10),
	fieldName varchar(60),
	locationName varchar(50),
	hourRange varchar(30),
    actualDate varchar(30)
) RETURNS tinyint(1)
    READS SQL DATA
    DETERMINISTIC
BEGIN
	DECLARE actualHourID INT;
    DECLARE field INT;
    DECLARE groupp INT;
    DECLARE location INT;
    DECLARE result BOOLEAN;
    
    SET location = (SELECT DISTINCT locationID FROM location AS l WHERE l.name = locationName);
    SET field = (SELECT DISTINCT fieldID FROM `fields` AS f WHERE f.fieldName = fieldName);
    SET groupp = (SELECT DISTINCT g.groupID FROM group_field AS gf INNER JOIN `groups` AS g ON g.groupID = gf.groupID
					WHERE g.groupName = groupName AND gf.fieldID = field);
	SET actualHourID = (SELECT DISTINCT hourID FROM hours AS h WHERE h.hourRange = hourRange);
    
    set result = 1;
    
    #Nie wykonuj gdy platforma
    IF locationName!='Platform' THEN
		IF actualHourID = 0 THEN
		SET result = (SELECT
		CASE
			WHEN COUNT(*) > 0 THEN false
			ELSE true
		END 
		FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
		WHERE p.groupId = groupp AND rl.locationID != location AND lr.locationID != (SELECT locationID FROM location WHERE name='Platform')
		AND p.hourId IN (SELECT hourID FROM hours AS h WHERE h.hourID = actualHourID + 1)
		AND p.date = actualDate);
    
    #Jeśli ostatnia godzina w dniu to nie sprawdzaj później
    ELSEIF actualHourID = (SELECT MAX(hourID) FROM hours) THEN
		SET result = (SELECT
		CASE
			WHEN COUNT(*) > 0 THEN false
			ELSE true
		END 
		FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
		WHERE  p.groupId = groupp AND lr.locationID != location AND lr.locationID != (SELECT locationID FROM location WHERE name='Platform')
		AND p.hourId IN (SELECT hourID FROM hours AS h WHERE h.hourID = actualHourID - 1)
		AND p.date = actualDate);
    
    ELSE 
    SET result = (SELECT
		CASE
			WHEN COUNT(*) > 0 THEN false
			ELSE true
		END 
		FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
		WHERE p.groupId = groupp AND lr.locationID != location AND lr.locationID != (SELECT locationID FROM location WHERE name='Platform')
		AND p.hourId IN (SELECT hourID FROM hours AS h WHERE h.hourID = actualHourID - 1 OR h.hourID = actualHourID + 1 )
		AND p.date = actualDate);
    END IF;
    end if;
    
    
    IF result = 1 THEN
        SET @locationOfClassesBefore = (SELECT l.name FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
        INNER JOIN location AS l ON l.locationID = lr.locationID
        WHERE p.hourID = (actualHourID-1) AND p.groupId = groupp AND p.date = actualDate);
        
        SET @hourCounter = actualHourID-1;
        IF @locationOfClassesBefore is not null then
			WHILE (@locationOfClassesBefore = 'Platform') DO
				set @hourCounter = @hourCounter-1;
				set @locationOfClassesBefore = (SELECT l.name FROM plan AS p
				INNER JOIN rooms AS r ON p.roomId = r.roomID
				INNER JOIN location_room AS lr ON r.roomID = lr.roomID
				INNER JOIN location AS l ON l.locationID = lr.locationID
				WHERE p.hourID = (@hourCounter) AND p.groupId = groupp AND p.date = actualDate);
			END WHILE;
        END IF;
        
        SET @locationOfClassesAfter = (SELECT l.name FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
        INNER JOIN location AS l ON l.locationID = lr.locationID
        WHERE p.hourID = (actualHourID+1) AND p.groupId = groupp AND p.date = actualDate);
        
        SET @hourCounter = actualHourID+1;
        
        IF @locationOfClassesAfter is not null THEN
			WHILE (@locationOfClassesAfter = 'Platform') DO
				set @hourCounter = @hourCounter+1;
				set @locationOfClassesAfter = (SELECT l.name FROM plan AS p
				INNER JOIN rooms AS r ON p.roomId = r.roomID
				INNER JOIN location_room AS lr ON r.roomID = lr.roomID
				INNER JOIN location AS l ON l.locationID = lr.locationID
				WHERE p.hourID = (@hourCounter) AND p.groupId = groupp AND p.date = actualDate);
			END WHILE;
        END IF;
        
        IF (@locationOfClassesAfter = @locationOfClassesBefore 
			or (@locationOfClassesAfter is null and @locationOfClassesBefore is null)
            or (@locationOfClassesAfter = locationName and @locationOfClassesBefore is null) 
			or (@locationOfClassesBefore = locationName and @locationOfClassesAfter is null)
			or (@locationOfClassesAfter is not null and (@locationOfClassesBefore is null) and (locationName='Platform' or locationName = @locationOfClassesAfter)) 
            or (@locationOfClassesAfter is null and @locationOfClassesBefore is not null and (locationName='Platform' or locationName = @locationOfClassesBefore))) 
            then set result = 1;
        else set result = 0;
        end if;
    end if;
	RETURN result;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `canTeacherMoveBetweenClasses` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `canTeacherMoveBetweenClasses`(
	firstname varchar(50),
	lastname varchar(60),
	locationName varchar(50),
	hourRange varchar(30),
    actualDate varchar(30)
) RETURNS tinyint(1)
    READS SQL DATA
    DETERMINISTIC
BEGIN
	DECLARE actualHourID INT;
    DECLARE teacher INT;
    DECLARE location INT;
    DECLARE result BOOLEAN;
    
    SET location = (SELECT DISTINCT locationID FROM location AS l WHERE l.name = locationName);
	SET teacher = (SELECT DISTINCT teacherID FROM teachers AS t WHERE t.name = firstname AND t.lastname=lastname);
	SET actualHourID = (SELECT DISTINCT hourID FROM hours AS h WHERE h.hourRange = hourRange);
    
    #0 - nie zdąży zmienić lokalizacji
    SET result = 1;
    
    IF locationName!='Platform' THEN
		IF actualHourID = 0 THEN
		SET result = (SELECT
		CASE
			WHEN COUNT(*) > 0 THEN false
			ELSE true
		END 
		FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
		WHERE p.teacherId = teacher AND rl.locationID != location AND lr.locationID != (SELECT locationID FROM location WHERE name='Platform')
		AND p.hourId IN (SELECT hourID FROM hours AS h WHERE h.hourID = actualHourID + 1)
		AND p.date = actualDate);
    
    #Jeśli ostatnia godzina w dniu to nie sprawdzaj później
    ELSEIF actualHourID = (SELECT MAX(hourID) FROM hours) THEN
		SET result = (SELECT
		CASE
			WHEN COUNT(*) > 0 THEN false
			ELSE true
		END 
		FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
		WHERE p.teacherId = teacher AND lr.locationID != location AND lr.locationID != (SELECT locationID FROM location WHERE name='Platform')
		AND p.hourId IN (SELECT hourID FROM hours AS h WHERE h.hourID = actualHourID - 1)
		AND p.date = actualDate);
    
    ELSE 
    SET result = (SELECT
		CASE
			WHEN COUNT(*) > 0 THEN false
			ELSE true
		END 
		FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
		WHERE p.teacherId = teacher AND lr.locationID != location AND lr.locationID != (SELECT locationID FROM location WHERE name='Platform')
		AND p.hourId IN (SELECT hourID FROM hours AS h WHERE h.hourID = actualHourID - 1 OR h.hourID = actualHourID + 1 )
		AND p.date = actualDate);
    END IF;
    end if;
    
    
    IF result = 1 THEN
        SET @locationOfClassesBefore = (SELECT l.name FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
        INNER JOIN location AS l ON l.locationID = lr.locationID
        WHERE p.hourID = (actualHourID-1) AND p.teacherId = teacher AND p.date = actualDate);
	
        SET @hourCounter = actualHourID-1;
        IF @locationOfClassesBefore is not null then
			WHILE (@locationOfClassesBefore = 'Platform') DO
				SET @hourCounter = @hourCounter-1;
				SET @locationOfClassesBefore = (SELECT l.name FROM plan AS p
				INNER JOIN rooms AS r ON p.roomId = r.roomID
				INNER JOIN location_room AS lr ON r.roomID = lr.roomID
				INNER JOIN location AS l ON l.locationID = lr.locationID
				WHERE p.hourID = (@hourCounter) AND p.teacherId = teacher AND p.date = actualDate);
			END WHILE;
        END IF;
        
        SET @locationOfClassesAfter = (SELECT l.name FROM plan AS p
		INNER JOIN rooms AS r ON p.roomId = r.roomID
        INNER JOIN location_room AS lr ON r.roomID = lr.roomID
        INNER JOIN location AS l ON l.locationID = lr.locationID
        WHERE p.hourID = (actualHourID+1) AND p.teacherId = teacher AND p.date = actualDate);
        
        SET @hourCounter = actualHourID+1;
        
        IF @locationOfClassesAfter is not null THEN
			WHILE (@locationOfClassesAfter = 'Platform') DO
				set @hourCounter = @hourCounter+1;
				set @locationOfClassesAfter = (SELECT l.name FROM plan AS p
				INNER JOIN rooms AS r ON p.roomId = r.roomID
				INNER JOIN location_room AS lr ON r.roomID = lr.roomID
				INNER JOIN location AS l ON l.locationID = lr.locationID
				WHERE p.hourID = (@hourCounter) AND p.teacherId = teacher AND p.date = actualDate);
			END WHILE;
        END IF;
        
        IF (@locationOfClassesAfter = @locationOfClassesBefore 
			or (@locationOfClassesAfter is null and @locationOfClassesBefore is null)
            or (@locationOfClassesAfter = locationName and @locationOfClassesBefore is null) 
			or (@locationOfClassesBefore = locationName and @locationOfClassesAfter is null)
			or (@locationOfClassesAfter is not null and (@locationOfClassesBefore is null) and (locationName='Platform' or locationName = @locationOfClassesAfter)) 
            or (@locationOfClassesAfter is null and @locationOfClassesBefore is not null and (locationName='Platform' or locationName = @locationOfClassesBefore))) 
            then set result = 1;
        else set result = 0;
        end if;
    end if;
	RETURN result;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addAvailabilityToTeacher` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `addAvailabilityToTeacher`(
	dayName varchar(50),
    hoursJSON json)
BEGIN 
    SET @teacherID := (SELECT MAX(teacherID) FROM teachers);
    SET @dayID = (SELECT dayID FROM day_of_week WHERE CAST(day_of_week.dayName AS BINARY) = CAST(dayName AS BINARY));
    
	#JSON_TABLE, parsuje dane JSON i zwraca je jako zestaw wierszy, który jest łączony z tabelą hours 
	INSERT INTO availability (teacherID, dayID, hourID)
	SELECT @teacherID, @dayID, hours.hourID
	FROM JSON_TABLE(hoursJSON, '$[*]' COLUMNS (hourRange VARCHAR(50) PATH '$')) AS jt
	INNER JOIN hours ON hours.hourRange = jt.hourRange;
    
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addField` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `addField`(
	fieldName varchar(50),
    fieldShort varchar(10),
    semNumber int
    )
BEGIN 
	INSERT INTO `fields` (fieldName, fieldShort, semNumber) VALUES (fieldName, fieldShort, semNumber);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addGroup` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `addGroup`(
	fieldName varchar(50),
    sem int,
    romanNumber varchar(5)
    )
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;

	START TRANSACTION;
	SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
    
    #Oznaczenia grup na danym kierunku np [A,B,D]
    SET @signs = (SELECT GROUP_CONCAT(sorted_names ORDER BY sorted_names) 
					FROM (
						SELECT RIGHT(groupName,1) as sorted_names
						FROM `groups` AS g 
						INNER JOIN group_field AS gf ON gf.groupID = g.groupID
						WHERE gf.fieldID = @fieldID AND g.term = sem
						GROUP BY RIGHT(groupName, 1)) t);
    
    
	SET @i = 0;
    SET @groupSign = "A";
	SET @signCounter = @signs;
    
    #Pętla szuka oznaczenia dla wstawianej grupy (szuka pierwszej wolnej luki np dla A,B,D przypisze C)
    myLoop: WHILE LENGTH(@signCounter) > 0 DO
			SET @sign = SUBSTRING_INDEX(@signCounter, ',', 1);
	
            IF @sign != CHAR(65 + @i) THEN
				SET @groupSign = CAST(CHAR(65 + @i) AS CHAR);
				LEAVE myLoop;
			END IF;
            
            SET @i = @i+1;
            SET @signCounter = TRIM(BOTH ',' FROM SUBSTRING(@signCounter, LENGTH(@sign) + 2));
            IF LENGTH(@signCounter) = 0 THEN SET @groupSign = CAST(CHAR(65 + @i) AS CHAR); END IF;
            
	END WHILE ;
                    
	#Wstaw grupę
	SET @groupName = CONCAT(romanNumber, @groupSign);
	INSERT INTO `groups` (groupName, term) VALUES (@groupName, sem);
        
	#powiązanie grupy z kierunkiem
    SET @id = (SELECT max(groupID) FROM `groups`);
	INSERT INTO group_field (groupID, fieldID) VALUES (@id, @fieldID);
    
    #Trzeba do wszystkich tabel group_subject_hours_left dodać wartości z field_subject
    #(przypisać szkolny plan nauczania do tabeli z pozotsałymi godzinami)
	SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('group_subject_hours_left%' AS BINARY) AND table_schema = DATABASE());
    SET @counter = @tablesToUpdate;
    
    #Pętla po wszystkich tabelach
    WHILE LENGTH(@counter) > 0 DO
		SET @plan = SUBSTRING_INDEX(@counter, ',', 1);
		SET @subjects = (SELECT GROUP_CONCAT(subjectID) FROM field_subject AS fs WHERE fs.fieldID = @fieldID AND fs.term = sem);
        
		SET @counter2 = @subjects;
        select @subjects;
		
	    #Pętla po wszystkich przedmiotach ze szkolnego planu nauczania do dodania
		WHILE LENGTH(@counter2) > 0 DO
			SET @subjectID = SUBSTRING_INDEX(@counter2, ',', 1);
			SET @weeklyHours = (SELECT weeklyHours FROM field_subject WHERE subjectID = @subjectID AND fieldID = @fieldID AND term = sem);
			
			#Wstawianie odpowiednich wartości ze szkolnego planu nauczania
			SET @insertQuery = CONCAT('INSERT INTO `', @plan, '` (groupID, subjectID, weekHoursLeft) VALUES (',@id,',',@subjectID, ',',@weeklyHours,');');
			PREPARE stmt FROM @insertQuery;
			EXECUTE stmt;
			DEALLOCATE PREPARE stmt;
			SET @counter2 = TRIM(BOTH ',' FROM SUBSTRING(@counter2, LENGTH(@subjectID) + 2));
		END WHILE;
		
		SET @counter = TRIM(BOTH ',' FROM SUBSTRING(@counter, LENGTH(@plan) + 2));
	END WHILE;
	COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addGroups` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `addGroups`(
	fieldName varchar(50),
    fieldShort varchar(10),
    groupsInSem int,
    sem int,
    romanNumber varchar(5)
    )
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
    
	SET @i = 1;
    SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY) AND f.fieldShort = fieldShort);
    
    WHILE @i <= groupsInSem DO
        SET @groupName = CONCAT(romanNumber, CHAR(65 + @i - 1));
        INSERT INTO `groups` (groupName, term) VALUES (@groupName, sem);
        
        #powiązanie grupy z kierunkiem
        SET @id = (SELECT MAX(groupID) FROM `groups`);
        INSERT INTO group_field (groupID, fieldID) VALUES (@id, @fieldID);
        SET @i = @i + 1;
    END WHILE;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addLocation` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `addLocation`(
	locationName varchar(50),
	city varchar(50),
	street varchar(50),
	postcode varchar(6)
    )
BEGIN 
	INSERT INTO location (`name`, `city`, `street`, `postcode`) 
    VALUES (locationName, city, street, postcode);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addRoom` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `addRoom`(
	roomname varchar(50),
	volume int,
    floor int,
    locationName varchar(50))
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	SET @locID = (SELECT locationId FROM location WHERE CAST(location.name AS BINARY)= CAST(locationName AS BINARY));  
	INSERT INTO rooms (roomName, volume, floor) VALUES (roomname, volume, floor);
    
    SET @addedRoomID = (SELECT MAX(roomID) FROM rooms);
    INSERT INTO location_room (locationID, roomID) VALUES (@locID, @addedRoomID);
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addSubjectToSemester` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `addSubjectToSemester`(
	subName varchar(150),
    fName varchar(60),
    sem int,
    weeklyHours int)
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
    
    SET @subjectExists := (SELECT subjectID FROM subjects WHERE CAST(subjectName AS BINARY) = CAST(subName AS BINARY));
    SET @fieldID := (SELECT fieldID FROM `fields` WHERE CAST(fieldName AS BINARY) = CAST(fName AS BINARY));
    
    #Jeśli przedmiot nie istnieje to najpierw go dodaj
    IF @subjectExists IS NULL THEN
		INSERT INTO subjects (subjectName) VALUES (subName);
        SET @subjectId := LAST_INSERT_ID();
	ELSE
		SET @subjectId := @subjectExists;
    END IF;
    
    #Przypisz przedmiot wraz z godzinami do kierunku i semestru
    INSERT INTO field_subject (subjectID, fieldID, term, weeklyHours) VALUES (@subjectID, @fieldID, sem, weeklyHours);
    
    #wybierz wszystkie grupy tego kierunku na danym semestrze i dodaj dla nich przedmiot we wszystkich tabelach group_subject_hours_left
    SET @groupsToUpdate = (SELECT group_concat(g.groupID) FROM group_field AS gf 
							INNER JOIN `groups` AS g ON g.groupID = gf.groupID
							WHERE gf.fieldID = @fieldID AND g.term = sem);
                            
	SET @counter = @groupsToUpdate;
    
    WHILE LENGTH(@counter) > 0 DO
        SET @groupID = SUBSTRING_INDEX(@counter, ',', 1);
        
		SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('group_subject_hours_left%' AS BINARY) AND table_schema = DATABASE());
		SET @counter2 = @tablesToUpdate;
        
        WHILE LENGTH(@counter2) > 0 DO
			SET @plan = SUBSTRING_INDEX(@counter2, ',', 1);
            SET @insertQuery = CONCAT('INSERT INTO `', @plan, '` (groupID, subjectID, weekHoursLeft) VALUES (',@groupID,',',@subjectID, ',',weeklyHours,');');
			PREPARE stmt FROM @insertQuery;
			EXECUTE stmt;
			DEALLOCATE PREPARE stmt;
			SET @counter2 = TRIM(BOTH ',' FROM SUBSTRING(@counter2, LENGTH(@plan) + 2));
        END WHILE;
	
        SET @counter = TRIM(BOTH ',' FROM SUBSTRING(@counter, LENGTH(@groupID) + 2));
    END WHILE;
    
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addTeacherWithSubject` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `addTeacherWithSubject`(
	firstname varchar(50),
	lastname varchar(50),
	phone varchar(50),
	mail varchar(50),
    subjectsJSON json)
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
		BEGIN
			ROLLBACK;
		END;
    
    START TRANSACTION;
	INSERT INTO teachers (name, lastname, phone, mail) VALUES (firstname, lastname, phone,mail);
    
    SET @teacherID := last_insert_id();
    
    #Przypisanie przedmiotów nauczycielowi
    INSERT INTO teacher_subject (teacherID, subjectID)
    SELECT @teacherID, subjects.subjectID
    FROM JSON_TABLE(subjectsJSON, '$[*]' COLUMNS (subject VARCHAR(50) PATH '$')) AS jt
    INNER JOIN subjects ON subjects.subjectName = jt.subject;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addToPlan` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `addToPlan`(`date` varchar(50), `hourr` varchar(50), `room` varchar(50), `group` varchar(50), `teacherName` varchar(50), `teacherLastname` varchar(100), `subject` varchar(150), `field` varchar(100), `location` varchar(100))
BEGIN
	DECLARE room_index INT;
    DECLARE group_index INT;
	DECLARE teacher_index INT;
	DECLARE subject_index INT;
    DECLARE hour_index INT;
    
    SET subject_index = (SELECT subjectID FROM subjects WHERE CAST(subjectName AS BINARY) = CAST(`subject` AS BINARY));
    SET room_index = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID INNER JOIN location AS l ON l.locationID = lr.locationID WHERE CAST(r.roomName AS BINARY) = CAST(`room` AS BINARY) AND CAST(l.name AS BINARY) = CAST(`location` AS BINARY));
    SET group_index = (SELECT `groups`.groupID FROM `groups`
		INNER JOIN group_field ON group_field.groupID = `groups`.groupID
		INNER JOIN fields ON fields.fieldID = group_field.fieldID WHERE CAST(groupName AS BINARY)= CAST(`group` AS BINARY) AND CAST(fieldName AS BINARY) = CAST(field AS BINARY));

    SET teacher_index = (SELECT teacherID FROM teachers WHERE CAST(name AS BINARY) = CAST(teacherName AS BINARY) AND CAST(lastname AS BINARY)=CAST(teacherLastname AS BINARY));
    SET hour_index = (SELECT hourID from hours WHERE hourRange = hourr);
    
	INSERT INTO plan (date, hourId, roomId, groupId, teacherId, subjectId) VALUES(date, hour_index, room_index, group_index, teacher_index, subject_index);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `changeHourPlatform` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `changeHourPlatform`(
	firstname varchar(50),
	lastname varchar(50),
    dayName varchar(30),
	oldHourRange varchar(30),
	newHourRange varchar(30),
	oldRoomName varchar(30),
	groupName varchar(30),
	subjectName varchar(100),
	fieldName varchar(60),
    oldLocationName varchar(50)
)
BEGIN 

	SET @firstLocationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(oldLocationName AS BINARY));
    SET @teacherID = (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(firstname AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(lastname AS BINARY));
	SET @oldRoomID = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE CAST(r.roomName AS BINARY) = CAST(oldRoomName AS BINARY) AND lr.locationID = @firstLocationID);
    
    SET @oldHourID = (SELECT hourID FROM hours WHERE hours.hourRange = oldHourRange);
    SET @newHourID = (SELECT hourID FROM hours WHERE hours.hourRange = newHourRange);
    
    SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
    SET @groupID = (SELECT g.groupID FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID
    WHERE g.groupName = groupName AND gf.fieldID = @fieldID);
    
    SET @subjectID = (SELECT subjectID from subjects AS s WHERE CAST(s.subjectName AS BINARY) = CAST(subjectName AS BINARY));
    
	UPDATE plan AS p SET p.hourId = @newHourID WHERE
    p.date = dayName AND p.hourId = @oldHourID AND p.roomId = @oldRoomID AND p.groupId = @groupID AND p.subjectID = @subjectID
    AND p.teacherId = @teacherID;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `changeHours` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `changeHours`(
	firstname varchar(50),
	lastname varchar(50),
    dayName varchar(30),
	oldHourRange varchar(30),
	newHourRange varchar(30),
	oldRoomName varchar(30),
	newRoomName varchar(30),
	groupName varchar(30),
	subjectName varchar(100),
	fieldName varchar(60),
    oldLocationName varchar(50),
    newLocationName varchar(50)
    )
BEGIN 

	SET @firstLocationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(oldLocationName AS BINARY));
	SET @secondLocationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(newLocationName AS BINARY));
    SET @teacherID = (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(firstname AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(lastname AS BINARY));
    
	SET @oldRoomID = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE CAST(r.roomName AS BINARY) = CAST(oldRoomName AS BINARY) AND lr.locationID = @firstLocationID);
	SET @newRoomID = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE CAST(r.roomName AS BINARY) = CAST(newRoomName AS BINARY) AND lr.locationID = @secondLocationID);
    
    SET @oldHourID = (SELECT hourID FROM hours WHERE hours.hourRange = oldHourRange);
    SET @newHourID = (SELECT hourID FROM hours WHERE hours.hourRange = newHourRange);
    
    SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
    SET @groupID = (SELECT g.groupID FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID
    WHERE g.groupName = groupName AND gf.fieldID = @fieldID);
    
    SET @subjectID = (SELECT subjectID from subjects AS s WHERE CAST(s.subjectName AS BINARY) = CAST(subjectName AS BINARY));
    
	UPDATE plan AS p SET p.hourId = @newHourID, p.roomId = @newRoomID WHERE
    p.date = dayName AND p.hourId = @oldHourID AND p.roomId = @oldRoomID AND p.groupId = @groupID AND p.subjectID = @subjectID
    AND p.teacherId = @teacherID;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `changeRooms` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `changeRooms`(
	firstname varchar(50),
	lastname varchar(50),
    dayName varchar(30),
	hourRange varchar(30),
	firstRoomName varchar(30),
	secondRoomName varchar(30),
	groupName varchar(30),
	subjectName varchar(100),
	fieldName varchar(60),
    firstLocationName varchar(50),
    secondLocationName varchar(50)
    )
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	SET @firstLocationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(firstLocationName AS BINARY));
	SET @secondLocationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(secondLocationName AS BINARY));
    SET @teacherID = (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(firstname AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(lastname AS BINARY));
	SET @firstRoomID = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE CAST(r.roomName AS BINARY) = CAST(firstRoomName AS BINARY) AND lr.locationID = @firstLocationID);
	SET @secondRoomID = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE CAST(r.roomName AS BINARY) = CAST(secondRoomName AS BINARY) AND lr.locationID = @secondLocationID);
    SET @hourID = (SELECT hourID FROM hours WHERE hours.hourRange = hourRange);
    SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
    SET @groupID = (SELECT g.groupID FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID
    WHERE g.groupName = groupName AND gf.fieldID = @fieldID);
    
    SET @subjectID = (SELECT subjectID from subjects AS s WHERE CAST(s.subjectName AS BINARY) = CAST(subjectName AS BINARY));
    
    SET @firstPlanID = (SELECT classesId FROM plan AS p WHERE 
    p.date = dayName AND p.hourId = @hourID AND p.roomId = @secondRoomID);
    
    SET @secondPlanID = (SELECT classesId FROM plan AS p WHERE p.teacherId = @teacherID AND 
    p.date = dayName AND p.hourId = @hourID AND p.roomId = @firstRoomID AND p.groupId = @groupID AND p.subjectID = @subjectID);
    
	UPDATE plan AS p SET p.roomId = @firstRoomId WHERE p.classesId = @firstPlanId;
    UPDATE plan AS p SET p.roomId = @secondRoomID WHERE p.classesId = @secondPlanId;
	COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `changeTeachers` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `changeTeachers`(
	t1firstname varchar(50),
	t1lastname varchar(50),
	t2firstname varchar(50),
	t2lastname varchar(50),
    dayName varchar(30),
	hourRange varchar(30),
	roomName varchar(30),
	groupName varchar(30),
	subjectName varchar(100),
	fieldName varchar(60),
    locationName varchar(50)
    )
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	SET @firstTeacherID = (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(t1firstname AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(t1lastname AS BINARY));
	SET @secondTeacherID = (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(t2firstname AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(t2lastname AS BINARY));
    
    SET @hourID = (SELECT hourID FROM hours WHERE hours.hourRange = hourRange);
    SET @locationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(locationName AS BINARY));
    SET @roomID = (SELECT rooms.roomID FROM rooms INNER JOIN location_room AS lr ON lr.roomID = rooms.roomID WHERE CAST(rooms.roomName AS BINARY) = CAST(roomName AS BINARY) AND lr.locationID = @locationID);
    SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
    SET @groupID = (SELECT g.groupID FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID
    WHERE g.groupName = groupName AND gf.fieldID = @fieldID);
    
    SET @subjectID = (SELECT subjectID from subjects AS s WHERE CAST(s.subjectName AS BINARY) = CAST(subjectName AS BINARY));
    
    SET @firstPlanID = (SELECT classesId FROM plan AS p WHERE p.teacherId = @secondTeacherID AND 
    p.date = dayName AND p.hourId = @hourID);
    SELECT @firstPlanID;
    
    SET @secondPlanID = (SELECT classesId FROM plan as p WHERE p.teacherId = @firstTeacherID AND 
    p.date = dayName AND p.hourId = @hourID AND p.roomId = @roomID AND p.groupId = @groupID AND p.subjectID = @subjectID);
    
	UPDATE plan AS p SET p.teacherId = @firstTeacherID WHERE p.classesId = @firstPlanId;
    UPDATE plan AS p SET p.teacherId = @secondTeacherID WHERE p.classesId = @secondPlanId;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `copyTable` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `copyTable`(
	tableName varchar(80),
    tableFrom varchar(80),
    fridayToChange varchar(20),
    saturdayToChange varchar(20),
    sundayToChange varchar(20),
    newFriday varchar(20),
    newSaturday varchar(20),
    newSunday varchar(20))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
    
	SET @createTableQuery := CONCAT('CREATE TABLE `', tableName, '` AS SELECT * FROM `', tableFrom, '`');
	PREPARE createTableStmt FROM @createTableQuery;
	EXECUTE createTableStmt;
	DEALLOCATE PREPARE createTableStmt;
    
    SET @updateQuery := CONCAT('UPDATE `', tableName, '` SET date = "', newFriday, '" WHERE date = "', fridayToChange, '"');
    PREPARE updateStmt FROM @updateQuery;
    EXECUTE updateStmt;
    DEALLOCATE PREPARE updateStmt;
    
    SET @updateQuery := CONCAT('UPDATE `', tableName, '` SET date = "', newSaturday, '" WHERE date = "', saturdayToChange, '"');
    PREPARE updateStmt FROM @updateQuery;
    EXECUTE updateStmt;
    DEALLOCATE PREPARE updateStmt;
    
    SET @updateQuery := CONCAT('UPDATE `', tableName, '` SET date = "', newSunday, '" WHERE date = "', sundayToChange, '"');
    PREPARE updateStmt FROM @updateQuery;
    EXECUTE updateStmt;
    DEALLOCATE PREPARE updateStmt;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `createOrUpdateTable` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `createOrUpdateTable`(
	tableName varchar(80),
    tableFrom varchar(80))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	#Sprawdz czy tabela istnieje
	SET @tableExists := (SELECT COUNT(*) > 0 FROM information_schema.tables WHERE CAST(table_name AS BINARY) = CAST(tableName AS BINARY));

    
    IF @tableExists = 0 THEN
		SET @createTableQuery := CONCAT('CREATE TABLE `', tableName, '` AS SELECT * FROM ', tableFrom);
        PREPARE createTableStmt FROM @createTableQuery;
        EXECUTE createTableStmt;
        DEALLOCATE PREPARE createTableStmt;
    ELSE
		SET @deleteQuery := CONCAT('DELETE FROM `', tableName, '`;');
        SET @insertQuery := CONCAT('INSERT INTO `', tableName, '` SELECT * FROM ', tableFrom);
        
        PREPARE deleteStmt FROM @deleteQuery;
        PREPARE insertStmt FROM @insertQuery;
        
        EXECUTE deleteStmt;
        EXECUTE insertStmt;
        
        DEALLOCATE PREPARE deleteStmt;
        DEALLOCATE PREPARE insertStmt;
    END IF;
    COMMIT;
    
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteClassesAssociatedToDeletedTeacherAndAvailability` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deleteClassesAssociatedToDeletedTeacherAndAvailability`(
	teacherID INT, 
    dayName varchar(50),
    hourRange varchar(50))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    START TRANSACTION;
	
	SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('group_subject_hours_left_%' AS BINARY) AND table_schema = DATABASE());
	SET @tablesToUpdateLoop = @tablesToUpdate;
    
    SET @hourID = (SELECT hourID FROM hours WHERE hours.hourRange=hourRange);
	
	WHILE LENGTH(@tablesToUpdateLoop) > 0 DO
		SET @table = SUBSTRING_INDEX(@tablesToUpdateLoop, ',', 1);
		SET @fromPlan = CONCAT('plan_', SUBSTRING(@table, LENGTH('group_subject_hours_left_') + 1));
		SET @updateQuery = CONCAT('UPDATE `', @table, '` AS t SET t.weekHoursLeft = t.weekHoursLeft + 1 WHERE (groupID,subjectID) IN (SELECT groupId, subjectId FROM `',@fromPlan,'` WHERE teacherId =', teacherID, ' AND hourId = ', @hourID ,' AND dayname(date) = "', dayName ,'");');
        select @updateQuery;
		PREPARE stmt FROM @updateQuery;
		EXECUTE stmt;
        
		DEALLOCATE PREPARE stmt;
		SET @tablesToUpdateLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdateLoop, LENGTH(@table) + 2));
	END WHILE;
	
	SET @tablesToUpdatePlan = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('plan%' AS BINARY) AND table_schema = DATABASE());
	SET @tablesToUpdatePlanLoop = @tablesToUpdatePlan;
	
	 WHILE LENGTH(@tablesToUpdatePlanLoop) > 0 DO
		SET @table = SUBSTRING_INDEX(@tablesToUpdatePlanLoop, ',', 1);
		SET @updateQuery = CONCAT('DELETE FROM `', @table, '` WHERE teacherId = ', teacherID ,' AND dayname(date) = "', dayName ,'" AND hourId = ', @hourID ,';');
		PREPARE stmt FROM @updateQuery;
        
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		SET @tablesToUpdatePlanLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdatePlanLoop, LENGTH(@table) + 2));
	END WHILE;
COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteClassesAssociatedWithTeacherAndSubject` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deleteClassesAssociatedWithTeacherAndSubject`(
	teacherID INT, 
    subjectToDeleteName varchar(140))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	
	
	SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('group_subject_hours_left_%' AS BINARY) AND table_schema = DATABASE());
	SET @tablesToUpdateLoop = @tablesToUpdate;
    
    SET @subjectID = (SELECT subjectID FROM subjects WHERE subjectName = subjectToDeleteName);
	
	WHILE LENGTH(@tablesToUpdateLoop) > 0 DO
		SET @table = SUBSTRING_INDEX(@tablesToUpdateLoop, ',', 1);
		SET @fromPlan = CONCAT('plan_', SUBSTRING(@table, LENGTH('group_subject_hours_left_') + 1));
        select @fromPlan;
		SET @updateQuery = CONCAT('UPDATE `', @table, '` AS t SET t.weekHoursLeft = t.weekHoursLeft + 1 WHERE (groupID,subjectID) IN (SELECT groupId, subjectId FROM `',@fromPlan,'` WHERE teacherId =', teacherID, ' AND subjectId = ', @subjectID ,');');
        
		PREPARE stmt FROM @updateQuery;
		EXECUTE stmt;
        
		DEALLOCATE PREPARE stmt;
		SET @tablesToUpdateLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdateLoop, LENGTH(@table) + 2));
	END WHILE;
	
	SET @tablesToUpdatePlan = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('plan%' AS BINARY) AND table_schema = DATABASE());
	SET @tablesToUpdatePlanLoop = @tablesToUpdatePlan;
	
	 WHILE LENGTH(@tablesToUpdatePlanLoop) > 0 DO
		SET @table = SUBSTRING_INDEX(@tablesToUpdatePlanLoop, ',', 1);
		SET @updateQuery = CONCAT('DELETE FROM `', @table, '` WHERE teacherId = ', teacherID ,' AND subjectId = ', @subjectID ,';');
		PREPARE stmt FROM @updateQuery;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		SET @tablesToUpdatePlanLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdatePlanLoop, LENGTH(@table) + 2));
	END WHILE;
COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteField` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deleteField`(
	fieldname varchar(50),
	semNum int,
	shortcut varchar(10))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
    
	SET @fieldToDeleteID = (SELECT DISTINCT fieldID FROM `fields` AS f WHERE BINARY f.fieldName = fieldname AND f.semNumber=semNum AND BINARY f.fieldShort = shortcut);
    IF @fieldToDeleteID IS NOT NULL THEN
		SET @groupsToDelete = (SELECT group_concat(groupID) FROM group_field AS g WHERE g.fieldID = @fieldToDeleteID);
		
		IF @groupsToDelete IS NOT NULL AND @groupsToDelete != '' THEN
        
			#Usuwanie z group_subject_hours_left wszystkich wierszy grup związanych z kierunkiem
			SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE BINARY table_name LIKE 'group_subject_hours_left%' AND table_schema = DATABASE());
			SET @tablesToUpdatePlan = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE BINARY table_name LIKE 'plan%' AND table_schema = DATABASE());
			SET @tablesToUpdateLoop = @tablesToUpdate;
			SET @tablesToUpdatePlanLoop = @tablesToUpdatePlan;

			 WHILE LENGTH(@tablesToUpdateLoop) > 0 DO
				SET @table = SUBSTRING_INDEX(@tablesToUpdateLoop, ',', 1);
				SET @updateQuery = CONCAT('DELETE FROM `', @table, '` AS t WHERE t.groupID IN (',@groupsToDelete,');');
				
				PREPARE stmt FROM @updateQuery;
				EXECUTE stmt;
				DEALLOCATE PREPARE stmt;
				SET @tablesToUpdateLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdateLoop, LENGTH(@table) + 2));
			END WHILE;
			

			#Usuwanie ze wszystkich planów grup związanych z kierunkiem
			WHILE LENGTH(@tablesToUpdatePlanLoop) > 0 DO
				SET @table = SUBSTRING_INDEX(@tablesToUpdatePlanLoop, ',', 1);
				SET @updateQuery = CONCAT('DELETE FROM `', @table, '` WHERE groupId IN (',@groupsToDelete,');');
				PREPARE stmt FROM @updateQuery;
				EXECUTE stmt;
				DEALLOCATE PREPARE stmt;
				SET @tablesToUpdatePlanLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdatePlanLoop, LENGTH(@table) + 2));
			END WHILE;
			
			
			DELETE FROM field_subject WHERE fieldID = @fieldToDeleteID;
			DELETE FROM group_field WHERE fieldID = @fieldToDeleteID;
			
			SET @sql2 = CONCAT('DELETE FROM `groups` WHERE groupID IN (',@groupsToDelete,');');
			PREPARE stmt FROM @sql2;
			EXECUTE stmt;
			DEALLOCATE PREPARE stmt;
			
			DELETE FROM fields WHERE fields.fieldID = @fieldToDeleteID;
		ELSE 
			DELETE FROM field_subject WHERE fieldID = @fieldToDeleteID;
			DELETE FROM fields WHERE fields.fieldID = @fieldToDeleteID;
		END IF;
	END IF;
    COMMIT;
    
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteFromPlan` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deleteFromPlan`(
	dayName varchar(30),
	hourRange varchar(30),
	roomName varchar(30),
	locationName varchar(50),
	groupName varchar(30),
	teacherName varchar(30),
	teacherLastname varchar(50),
	subjectName varchar(100),
	fieldName varchar(60)
    )
BEGIN
	SET @hourID = (SELECT hourID FROM hours WHERE hours.hourRange = hourRange);
	SET @locationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(locationName AS BINARY));
    SET @roomID = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON r.roomID = lr.roomID WHERE CAST(r.roomName AS BINARY) = CAST(roomName AS BINARY) AND lr.locationID = @locationID);
    SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
    SET @groupID = (SELECT g.groupID FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID
    WHERE g.groupName = groupName AND gf.fieldID = @fieldID);
    
    SET @teacherID = (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(teacherName AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(teacherLastname AS BINARY));
    SET @subjectID = (SELECT subjectID from subjects AS s WHERE CAST(s.subjectName AS BINARY) = CAST(subjectName AS BINARY));
    
	#Usuwamy jedynie z aktualnego planu
	DELETE FROM plan AS p WHERE p.date = dayName AND p.hourID = @hourID AND roomID = @roomID AND
    p.groupID = @groupID AND p.teacherID = @teacherID AND p.subjectID = @subjectID;
    
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteFromSPNByField` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deleteFromSPNByField`(
	fieldName varchar(60))
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;

	SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE f.fieldName = fieldName);
    
    #Usuwanie starego SPN
	DELETE FROM field_subject WHERE fieldID = @fieldID;
	
	SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('group_subject_hours_left%' AS BINARY) AND table_schema = DATABASE());
	SET @tablesToUpdateLoop = @tablesToUpdate;
	
	#Musimy usunąć ze wszystkich tabel group_subject_hours_left
	WHILE LENGTH(@tablesToUpdateLoop) > 0 DO
		SET @table = SUBSTRING_INDEX(@tablesToUpdateLoop, ',', 1);
		SET @deleteQuery = CONCAT('DELETE FROM `', @table, '` AS gshl WHERE gshl.groupID IN (
								SELECT groupID FROM group_field AS gf WHERE gf.fieldID = ',@fieldID,')');
		
		PREPARE stmt FROM @deleteQuery;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		SET @tablesToUpdateLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdateLoop, LENGTH(@table) + 2));
	END WHILE;
	
	#Trzeba też usunąć ze wszystkich planów
	SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE
							CAST(table_name AS BINARY) LIKE CAST('plan%' AS BINARY) AND table_schema = DATABASE());
							
	SET @tablesToUpdateLoop = @tablesToUpdate;
	
	WHILE LENGTH(@tablesToUpdateLoop) > 0 DO
		SET @table = SUBSTRING_INDEX(@tablesToUpdateLoop, ',', 1);
		SET @deleteQuery = CONCAT('DELETE FROM `', @table, '` AS p WHERE p.groupID IN (
								SELECT groupID FROM group_field AS gf WHERE gf.fieldID = ',@fieldID,')');
		
		PREPARE stmt FROM @deleteQuery;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;
		SET @tablesToUpdateLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdateLoop, LENGTH(@table) + 2));
	END WHILE;
	COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteGroup` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deleteGroup`(
	groupName varchar(50),
	sem int,
	fieldName varchar(50))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	SET @fieldID := (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
	SET @groupID := (SELECT g.groupId FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID
    INNER JOIN `fields` AS f ON f.fieldID = gf.fieldID WHERE BINARY g.groupName = groupName AND g.term = sem AND
    CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
    
    SET @tablesToUpdatePlan = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('plan%' AS BINARY) AND table_schema = DATABASE());
    SET @tablesToUpdatePlanLoop = @tablesToUpdatePlan;
    
	#Usuń grupę ze wszsytkich planów
     WHILE LENGTH(@tablesToUpdatePlanLoop) > 0 DO
        SET @table = SUBSTRING_INDEX(@tablesToUpdatePlanLoop, ',', 1);
        SET @updateQuery = CONCAT('DELETE FROM `', @table, '` WHERE groupId = ',@groupID, ';');
        PREPARE stmt FROM @updateQuery;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SET @tablesToUpdatePlanLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdatePlanLoop, LENGTH(@table) + 2));
    END WHILE;
    
    SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('group_subject_hours_left%' AS BINARY) AND table_schema = DATABASE());
    SET @tablesToUpdateLoop = @tablesToUpdate;
    
    WHILE LENGTH(@tablesToUpdateLoop) > 0 DO
        SET @table = SUBSTRING_INDEX(@tablesToUpdateLoop, ',', 1);
        SET @updateQuery = CONCAT('DELETE FROM `', @table, '` WHERE groupID = ', @groupID, ';');
        PREPARE stmt FROM @updateQuery;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SET @tablesToUpdateLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdateLoop, LENGTH(@table) + 2));
    END WHILE;
    
	DELETE FROM group_field WHERE groupID = @groupID;
    DELETE FROM `groups` WHERE groupID = @groupID;
    
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteLocation` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deleteLocation`(
	locationName varchar(50),
	city varchar(50),
	street varchar(50),
	postcode varchar(6))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
    SET @locationID = (SELECT locationId FROM location as l WHERE CAST(l.name AS BINARY)=CAST(locationName AS BINARY) AND
					l.city = city AND l.street = street AND l.postcode = postcode);
    
    IF @locationID IS NOT NULL THEN
		#ID sal do usunięcia
		SET @roomsToDelete = (SELECT GROUP_CONCAT(r.roomID) FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE lr.locationID = @locationID);
        IF @roomsToDelete IS NOT NULL THEN
			SET @counter = @roomsToDelete;
			
			#dla każdej sali z lokalizacji wywołaj procedurę deleteRoom
			WHILE LENGTH(@counter) > 0 DO
				SET @id = SUBSTRING_INDEX(@counter, ',', 1);
				SELECT roomName, volume, floor INTO @roomName, @volume, @floor 
				FROM rooms WHERE rooms.roomID = @id;
		  
				CALL deleteRoom(@roomName, @volume, @floor, locationName);
				SET @counter = TRIM(BOTH ',' FROM SUBSTRING(@counter, LENGTH(@id) + 2));
			END WHILE;
		END IF;
		DELETE FROM location WHERE locationID = @locationID;
    END IF;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deletePlan` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deletePlan`(
	planTable varchar(80),
    hoursLeftTable varchar(80))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	#Sprawdz czy tabela istnieje
	SET @tableExists := (SELECT COUNT(*) > 0 FROM information_schema.tables WHERE CAST(table_name AS BINARY) = CAST(planTable AS BINARY));

	#Jeśli tabela istnieje to trzeba ją usunąć
    IF @tableExists = 1 THEN
		SET @deleteTableQuery := CONCAT('DROP TABLE `', planTable, '`;');
        PREPARE deleteTableStmt FROM @deleteTableQuery;
        EXECUTE deleteTableStmt;
        DEALLOCATE PREPARE deleteTableStmt;
        
        SET @deleteTableQuery := CONCAT('DROP TABLE `', hoursLeftTable, '`;');
        PREPARE deleteTableStmt FROM @deleteTableQuery;
        EXECUTE deleteTableStmt;
        DEALLOCATE PREPARE deleteTableStmt;
    END IF;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteRoom` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deleteRoom`(
	roomName varchar(30),
	volume int,
	floor int,
	locationName varchar(50))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	SET @virtualRoom := (SELECT r.roomID FROM rooms AS r WHERE CAST(r.roomName AS BINARY) = CAST('Virtual' AS BINARY));
    SET @locationOfDeletedRoom := (SELECT locationId FROM location as l WHERE CAST(l.name AS BINARY)=CAST(locationName AS BINARY));
    SET @roomToDelete := (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE CAST(r.roomName AS BINARY) = CAST(roomName AS BINARY) AND r.volume = volume AND r.floor = floor AND lr.locationID = @locationOfDeletedRoom);
    
	SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('plan%' AS BINARY) AND table_schema = DATABASE());
    SET @tablesToUpdateLoop = @tablesToUpdate;
    
    #Dla wszystkich planów zamień usuwaną salę na wirtualną
    WHILE LENGTH(@tablesToUpdateLoop) > 0 DO
        SET @table = SUBSTRING_INDEX(@tablesToUpdateLoop, ',', 1);
        SET @updateQuery = CONCAT('UPDATE `', @table, '` AS t SET t.roomId = ', @virtualRoom, ' WHERE t.roomId = ', @roomToDelete, ';');
        PREPARE stmt FROM @updateQuery;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SET @tablesToUpdateLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdateLoop, LENGTH(@table) + 2));
    END WHILE;
    
    #Usuń salę
    DELETE FROM location_room WHERE roomID = @roomToDelete;
    DELETE FROM rooms WHERE rooms.roomID = @roomToDelete;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteTeacher` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `deleteTeacher`(
	firstname varchar(50),
	lastname varchar(50),
	phone varchar(50),
	mail varchar(50))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	SET @teacherID := (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(firstname AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(lastname AS BINARY) AND t.phone = phone AND t.mail = mail);
    IF @teacherID IS NOT NULL THEN
		
		DELETE FROM availability WHERE teacherID = @teacherID;
		DELETE FROM teacher_subject WHERE teacherID = @teacherID;
        
        SET @tablesToUpdate = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('group_subject_hours_left_%' AS BINARY) AND table_schema = DATABASE());
		SET @tablesToUpdateLoop = @tablesToUpdate;
		
		WHILE LENGTH(@tablesToUpdateLoop) > 0 DO
			SET @table = SUBSTRING_INDEX(@tablesToUpdateLoop, ',', 1);
            SET @fromPlan = CONCAT('plan_', SUBSTRING(@table, LENGTH('group_subject_hours_left_') + 1));
			SET @updateQuery = CONCAT('UPDATE `', @table, '` AS t SET t.weekHoursLeft = t.weekHoursLeft + 1 WHERE (groupID,subjectID) IN (SELECT groupId, subjectId FROM `',@fromPlan,'` WHERE teacherID = ', @teacherID, ');');
			PREPARE stmt FROM @updateQuery;
			EXECUTE stmt;
			DEALLOCATE PREPARE stmt;
			SET @tablesToUpdateLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdateLoop, LENGTH(@table) + 2));
		END WHILE;
		
		SET @tablesToUpdatePlan = (SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE CAST(table_name AS BINARY) LIKE CAST('plan%' AS BINARY) AND table_schema = DATABASE());
		SET @tablesToUpdatePlanLoop = @tablesToUpdatePlan;
		
		 WHILE LENGTH(@tablesToUpdatePlanLoop) > 0 DO
			SET @table = SUBSTRING_INDEX(@tablesToUpdatePlanLoop, ',', 1);
			SET @updateQuery = CONCAT('DELETE FROM `', @table, '` WHERE teacherID = ', @teacherID ,';');
			PREPARE stmt FROM @updateQuery;
			EXECUTE stmt;
			DEALLOCATE PREPARE stmt;
			SET @tablesToUpdatePlanLoop = TRIM(BOTH ',' FROM SUBSTRING(@tablesToUpdatePlanLoop, LENGTH(@table) + 2));
		END WHILE;
		

		DELETE FROM teachers WHERE teacherID = @teacherID;
    END IF;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `refillFromOldPlan` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `refillFromOldPlan`(
	oldPlanTable varchar(50),
    oldHoursLeftTable varchar(50))
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
    
	DELETE FROM plan;
    DELETE FROM group_subject_hours_left;
    
	SET @insertQuery := CONCAT('INSERT INTO plan SELECT * FROM `', oldPlanTable, '`;');
	PREPARE insertStmt FROM @insertQuery;
	EXECUTE insertStmt;
	DEALLOCATE PREPARE insertStmt;
    
    SET @insertQuery := CONCAT('INSERT INTO group_subject_hours_left SELECT * FROM `', oldHoursLeftTable, '`;');
	PREPARE insertStmt FROM @insertQuery;
	EXECUTE insertStmt;
	DEALLOCATE PREPARE insertStmt;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `refillHours` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `refillHours`()
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
    
	DELETE FROM plan;
    DELETE FROM group_subject_hours_left;
    
    SET @allFields = (SELECT group_concat(DISTINCT fieldID) FROM field_subject);
    SET @counter = @allFields;
    
    #pętla po kierunkach
    WHILE LENGTH(@counter) > 0 DO
        SET @fieldID = SUBSTRING_INDEX(@counter, ',', 1);
        
        #Pobierz liczbe semestrów na kierunku
        SET @semCount = (SELECT semNumber FROM `fields` WHERE fieldID = @fieldID);
        
        #Pętla po wszystkich semestrach kierunku
        SET @i = 1;
        WHILE @i <= @semCount DO
        
			#Pobierz liste przedmitów na i-tym semestrze
            SET @subjects = (SELECT group_concat(subjectID) FROM field_subject WHERE fieldID = @fieldID AND term = @i);
            SET @counter2 = @subjects;
            
            #Pętla po wszystkich przedmiotach
            WHILE LENGTH(@counter2) > 0 DO
				SET @subjectID = substring_index(@counter2, ',', 1);
                
                #pobierz liczbe godzin dla danego przedmiotu na danym semestrze danego kierunku
                SET @weekHours = (SELECT weeklyHours FROM field_subject WHERE subjectID = @subjectID AND fieldID = @fieldID AND term = @i);
                
                #wszystkie grupy z danego kierunku i danego  semestru
                SET @groupsToUpdate = (SELECT group_concat(distinct g.groupID) FROM group_field AS gf
					INNER JOIN `groups` AS g ON g.groupID = gf.groupID WHERE fieldID = @fieldID AND g.term = @i);
                    
				SET @counter3 = @groupsToUpdate;
                    
				#pętla po wszystkich grupach
                WHILE LENGTH(@counter3) > 0 DO
					SET @groupID = SUBSTRING_INDEX(@counter3, ',', 1);
                    
                    INSERT INTO group_subject_hours_left (groupID, subjectID, weekHoursLeft)
                    VALUES (@groupID, @subjectID, @weekHours );
                    
					SET @counter3 = TRIM(BOTH ',' FROM SUBSTRING(@counter3, LENGTH(@groupID) + 2));
				END WHILE;
			
                SET @counter2 = TRIM(BOTH ',' FROM SUBSTRING(@counter2, LENGTH(@subjectID) + 2));
			END WHILE;
		
            SET @i = @i+1;
            
        END WHILE;
        
        SET @counter = TRIM(BOTH ',' FROM SUBSTRING(@counter, LENGTH(@fieldID) + 2));
    END WHILE;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `resetPlan` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `resetPlan`()
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	DELETE FROM plan;
    DELETE FROM group_subject_hours_left;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `setAnotherRoom` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `setAnotherRoom`(
	firstname varchar(50),
	lastname varchar(50),
    dayName varchar(30),
	hourRange varchar(30),
	firstRoomName varchar(30),
	secondRoomName varchar(30),
	groupName varchar(30),
	subjectName varchar(100),
	fieldName varchar(60),
    firstLocationName varchar(50),
    secondLocationName varchar(50)
    )
BEGIN 

	SET @firstLocationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(firstLocationName AS BINARY));
	SET @secondLocationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(secondLocationName AS BINARY));
    
    SET @teacherID = (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(firstname AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(lastname AS BINARY));
    
	SET @firstRoomID = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE CAST(r.roomName AS BINARY) = CAST(firstRoomName AS BINARY) AND lr.locationID = @firstLocationID);
	SET @secondRoomID = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE CAST(r.roomName AS BINARY) = CAST(secondRoomName AS BINARY) AND lr.locationID = @secondLocationID);
    
    SET @hourID = (SELECT hourID FROM hours WHERE hours.hourRange = hourRange);
    SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
    SET @groupID = (SELECT g.groupID FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID
    WHERE CAST(g.groupName AS BINARY) = CAST(groupName AS BINARY) AND gf.fieldID = @fieldID);
    
    SET @subjectID = (SELECT subjectID from subjects AS s WHERE CAST(s.subjectName AS BINARY) = CAST(subjectName AS BINARY));
    
	UPDATE plan AS p SET p.roomId = @secondRoomID WHERE p.roomId = @firstRoomID AND 
    p.date = dayName AND p.hourId = @hourID AND p.teacherID = @teacherID AND p.groupId = @groupID AND p.subjectID = @subjectID;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `setAnotherTeacher` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `setAnotherTeacher`(
	t1firstname varchar(50),
	t1lastname varchar(50),
	t2firstname varchar(50),
	t2lastname varchar(50),
    dayName varchar(30),
	hourRange varchar(30),
	roomName varchar(30),
	groupName varchar(30),
	subjectName varchar(100),
	fieldName varchar(60),
    locationName varchar(50)
    )
BEGIN 
	SET @firstTeacherID = (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(t1firstname AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(t1lastname AS BINARY));
	SET @secondTeacherID = (SELECT teacherID FROM teachers AS t WHERE CAST(t.name AS BINARY) = CAST(t2firstname AS BINARY) AND CAST(t.lastname AS BINARY) = CAST(t2lastname AS BINARY));
    SET @locationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(locationName AS BINARY));
    
    SET @hourID = (SELECT hourID FROM hours WHERE hours.hourRange = hourRange);
    SET @roomID = (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE CAST(r.roomName AS BINARY) = CAST(roomName AS BINARY) AND lr.locationID = @locationID);
    SET @fieldID = (SELECT fieldID FROM `fields` AS f WHERE CAST(f.fieldName AS BINARY) = CAST(fieldName AS BINARY));
    SET @groupID = (SELECT g.groupID FROM `groups` AS g INNER JOIN group_field AS gf ON gf.groupID = g.groupID
    WHERE CAST(g.groupName AS BINARY) = CAST(groupName AS BINARY) AND gf.fieldID = @fieldID);
    
    SET @subjectID = (SELECT subjectID from subjects AS s WHERE CAST(s.subjectName AS BINARY) = CAST(subjectName AS BINARY));
    
	UPDATE plan AS p SET p.teacherId = @secondTeacherID WHERE p.teacherId = @firstTeacherID AND 
    p.date = dayName AND p.hourId = @hourID AND p.roomId = @roomID AND p.groupId = @groupID AND p.subjectID = @subjectID;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateField` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `updateField`(
	fieldID int,
	fieldName varchar(50),
	fieldShort varchar(6))
BEGIN 
	UPDATE `fields` AS f SET f.fieldName = fieldName, f.fieldShort=fieldShort WHERE f.fieldID = fieldID;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateLocation` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `updateLocation`(
	locationID int,
	locationName varchar(50),
	city varchar(50),
	street varchar(50),
	postcode varchar(6))
BEGIN 
	UPDATE location AS l SET l.name = locationName, l.city = city, l.street = street,
    l.postcode = postcode WHERE l.locationID = locationID;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updatePassword` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `updatePassword`(
	newPassword varchar(60),
    username varchar(45)
    )
BEGIN 
	UPDATE user AS u SET u.pass = newPassword, isTempPassword = 0 WHERE u.username = username;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateRoom` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `updateRoom`(
	roomID int,
	roomName varchar(50),
	locationName varchar(50),
    volume int,
    floor int
    )
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
    SET @locationID = (SELECT locationID FROM location AS l WHERE CAST(l.name AS BINARY) = CAST(locationName AS BINARY));
    
    UPDATE location_room AS lr SET lr.locationID = @locationID WHERE lr.roomID = roomID;
	UPDATE rooms AS r SET r.roomName = roomName, r.floor = floor, r.volume= volume WHERE r.roomID = roomID;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateTeacher` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `updateTeacher`(
	teacherID int,
	firstname varchar(50),
	lastname varchar(50),
	phone varchar(50),
	mail varchar(50),
    subjectsJSON json)
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
	#Aktualizowanie danych nauczyciela
	UPDATE teachers AS t SET t.name=firstname, t.lastname=lastname, t.phone=phone, t.mail=mail WHERE t.teacherID=teacherID;
    
    DELETE FROM teacher_subject AS ts WHERE ts.teacherID = teacherID; 
    
    INSERT INTO teacher_subject (teacherID, subjectID)
    SELECT teacherID, subjects.subjectID
    FROM JSON_TABLE(subjectsJSON, '$[*]' COLUMNS (subject VARCHAR(50) PATH '$')) AS jt
    INNER JOIN subjects ON subjects.subjectName = jt.subject;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateTeacherAvailability` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `updateTeacherAvailability`(
	teacherID int,
	dayName varchar(50),
    hoursJSON json)
BEGIN 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
	END;
    
    START TRANSACTION;
    
    SET @dayID = (SELECT dayID FROM day_of_week WHERE CAST(day_of_week.dayName AS BINARY) = CAST(dayName AS BINARY));
    
    DELETE FROM availability WHERE availability.teacherID = teacherID AND availability.dayID = @dayID;
    
    INSERT INTO availability (teacherID, dayID, hourID)
    SELECT teacherID, @dayID, hours.hourID
    FROM JSON_TABLE(hoursJSON, '$[*]' COLUMNS (hourRange VARCHAR(50) PATH '$')) AS jt
    INNER JOIN hours ON hours.hourRange = jt.hourRange;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-11-22 16:34:03
