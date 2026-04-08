-- MySQL dump 10.13  Distrib 8.0.44, for Linux (x86_64)
--
-- Host: localhost    Database: schoolsafetrack
-- ------------------------------------------------------
-- Server version	8.0.44
--
-- CREDENCIALES DE PRUEBA (contraseña: password123)
--   padre1@example.com   (PARENT)
--   padre2@example.com   (PARENT)
--   conductor1@example.com (DRIVER)
--   conductor2@example.com (DRIVER)
--   admin@schoolsafetrack.com (ADMIN) - contraseña distinta, usa el panel web para cambiarla
--

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `buses`
--

DROP TABLE IF EXISTS `buses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `buses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `matricula` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `marca` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `modelo` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `anio` int DEFAULT NULL,
  `capacidad` int NOT NULL,
  `color` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `driver_id` bigint DEFAULT NULL,
  `estado` enum('ACTIVO','MANTENIMIENTO','INACTIVO') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVO',
  `ultima_revision` date DEFAULT NULL,
  `proxima_revision` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lat` double DEFAULT NULL,
  `lon` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `matricula` (`matricula`),
  KEY `idx_matricula` (`matricula`),
  KEY `idx_driver` (`driver_id`),
  KEY `idx_estado` (`estado`),
  CONSTRAINT `buses_ibfk_1` FOREIGN KEY (`driver_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `buses`
--

LOCK TABLES `buses` WRITE;
/*!40000 ALTER TABLE `buses` DISABLE KEYS */;
INSERT INTO `buses` VALUES (1,'1234-ABC','Mercedes','Sprinter',2020,25,'Blanco',4,'ACTIVO',NULL,NULL,'2025-12-29 01:13:08','2025-12-29 21:39:05',40.418,-3.7044),(2,'5678-XYZ','Iveco','Daily',2019,30,'Amarillo',5,'ACTIVO',NULL,NULL,'2025-12-29 01:13:08','2025-12-29 21:39:05',40.4187,-3.7051),(3,'9012-DEF','Ford','Transit',2021,20,'Azul',NULL,'MANTENIMIENTO',NULL,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08',NULL,NULL);
/*!40000 ALTER TABLE `buses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `device_tokens`
--

DROP TABLE IF EXISTS `device_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `device_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dispositivo` enum('ANDROID','IOS','WEB') COLLATE utf8mb4_unicode_ci NOT NULL,
  `modelo_dispositivo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version_app` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `ultimo_uso` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_token` (`user_id`,`token`(255)),
  KEY `idx_user` (`user_id`),
  KEY `idx_token` (`token`(255)),
  KEY `idx_activo` (`activo`),
  CONSTRAINT `device_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device_tokens`
--

LOCK TABLES `device_tokens` WRITE;
/*!40000 ALTER TABLE `device_tokens` DISABLE KEYS */;
INSERT INTO `device_tokens` VALUES (1,2,'fcm_token_example_android_parent1','ANDROID',NULL,NULL,1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(2,3,'fcm_token_example_android_parent2','ANDROID',NULL,NULL,1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(3,4,'fcm_token_example_android_driver1','ANDROID',NULL,NULL,1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08');
/*!40000 ALTER TABLE `device_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `incidents`
--

DROP TABLE IF EXISTS `incidents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `incidents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `route_assignment_id` bigint NOT NULL,
  `driver_id` bigint NOT NULL,
  `tipo` enum('RETRASO','MECANICO','ACCIDENTE','CLIMA','OTRO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitud` decimal(10,8) DEFAULT NULL,
  `longitud` decimal(11,8) DEFAULT NULL,
  `resuelto` tinyint(1) DEFAULT '0',
  `fecha_resolucion` datetime DEFAULT NULL,
  `observaciones_resolucion` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_assignment` (`route_assignment_id`),
  KEY `idx_driver` (`driver_id`),
  KEY `idx_tipo` (`tipo`),
  KEY `idx_resuelto` (`resuelto`),
  CONSTRAINT `incidents_ibfk_1` FOREIGN KEY (`route_assignment_id`) REFERENCES `route_assignments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `incidents_ibfk_2` FOREIGN KEY (`driver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `incidents`
--

LOCK TABLES `incidents` WRITE;
/*!40000 ALTER TABLE `incidents` DISABLE KEYS */;
INSERT INTO `incidents` VALUES (1,1,4,'RETRASO','TrÃ¡fico intenso en Calle Mayor. Estimamos 10 minutos de retraso.',NULL,NULL,0,NULL,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08');
/*!40000 ALTER TABLE `incidents` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `tr_notificar_incidencia` AFTER INSERT ON `incidents` FOR EACH ROW BEGIN
    DECLARE titulo_notif VARCHAR(255);
    DECLARE mensaje_notif TEXT;
    
    SET titulo_notif = CONCAT('Incidencia: ', NEW.tipo);
    SET mensaje_notif = NEW.descripcion;
    
    -- Notificar a todos los padres de la ruta afectada
    INSERT INTO notifications (user_id, tipo, titulo, mensaje)
    SELECT DISTINCT s.parent_id, 'INCIDENCIA', titulo_notif, mensaje_notif
    FROM route_assignments ra
    JOIN routes r ON ra.route_id = r.id
    JOIN stops st ON r.id = st.route_id
    JOIN students s ON st.id = s.stop_id
    WHERE ra.id = NEW.route_assignment_id AND s.activo = TRUE;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `tipo` enum('PROXIMIDAD','LLEGADA','RETRASO','INCIDENCIA','INFORMACION') COLLATE utf8mb4_unicode_ci NOT NULL,
  `titulo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `mensaje` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `leida` tinyint(1) DEFAULT '0',
  `data_json` text COLLATE utf8mb4_unicode_ci COMMENT 'Datos adicionales en formato JSON',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_leida` (`leida`),
  KEY `idx_created` (`created_at`),
  KEY `idx_user_leida` (`user_id`,`leida`),
  KEY `idx_notifications_user_leida_created` (`user_id`,`leida`,`created_at`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,2,'PROXIMIDAD','El bus estÃ¡ cerca','El autobÃºs escolar estÃ¡ a 5 minutos de la parada Plaza Mayor',0,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(2,3,'LLEGADA','Bus en parada','El autobÃºs ha llegado a Mercado Central',1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `real_time_location`
--

DROP TABLE IF EXISTS `real_time_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `real_time_location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bus_id` bigint NOT NULL,
  `route_assignment_id` bigint DEFAULT NULL,
  `latitud` decimal(10,8) NOT NULL,
  `longitud` decimal(11,8) NOT NULL,
  `velocidad` decimal(5,2) DEFAULT NULL COMMENT 'Velocidad en km/h',
  `direccion` decimal(5,2) DEFAULT NULL COMMENT 'DirecciÃ³n en grados (0-360)',
  `precision_gps` decimal(5,2) DEFAULT NULL COMMENT 'PrecisiÃ³n en metros',
  `timestamp` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_bus_timestamp` (`bus_id`,`timestamp`),
  KEY `idx_assignment` (`route_assignment_id`),
  KEY `idx_timestamp` (`timestamp`),
  CONSTRAINT `real_time_location_ibfk_1` FOREIGN KEY (`bus_id`) REFERENCES `buses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `real_time_location_ibfk_2` FOREIGN KEY (`route_assignment_id`) REFERENCES `route_assignments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `real_time_location`
--

LOCK TABLES `real_time_location` WRITE;
/*!40000 ALTER TABLE `real_time_location` DISABLE KEYS */;
INSERT INTO `real_time_location` VALUES (1,1,1,40.41650000,-3.70600000,25.50,NULL,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(2,1,1,40.41780000,-3.70520000,28.30,NULL,NULL,'2025-12-29 01:12:38','2025-12-29 01:13:08'),(3,1,1,40.41920000,-3.70450000,22.10,NULL,NULL,'2025-12-29 01:12:08','2025-12-29 01:13:08');
/*!40000 ALTER TABLE `real_time_location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route_assignments`
--

DROP TABLE IF EXISTS `route_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `route_assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `route_id` bigint NOT NULL,
  `bus_id` bigint NOT NULL,
  `fecha` date NOT NULL,
  `estado` enum('PROGRAMADA','EN_CURSO','FINALIZADA','CANCELADA') COLLATE utf8mb4_unicode_ci DEFAULT 'PROGRAMADA',
  `hora_inicio_real` datetime DEFAULT NULL,
  `hora_fin_real` datetime DEFAULT NULL,
  `distancia_recorrida` decimal(8,2) DEFAULT NULL,
  `retraso_minutos` int DEFAULT '0',
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_route_bus_fecha` (`route_id`,`bus_id`,`fecha`),
  KEY `idx_route` (`route_id`),
  KEY `idx_bus` (`bus_id`),
  KEY `idx_fecha` (`fecha`),
  KEY `idx_estado` (`estado`),
  KEY `idx_route_assignments_fecha_estado` (`fecha`,`estado`),
  CONSTRAINT `route_assignments_ibfk_1` FOREIGN KEY (`route_id`) REFERENCES `routes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `route_assignments_ibfk_2` FOREIGN KEY (`bus_id`) REFERENCES `buses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `route_assignments`
--

LOCK TABLES `route_assignments` WRITE;
/*!40000 ALTER TABLE `route_assignments` DISABLE KEYS */;
INSERT INTO `route_assignments` VALUES (1,1,1,'2025-12-29','EN_CURSO','2024-12-23 07:32:00',NULL,NULL,0,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(2,2,2,'2025-12-29','PROGRAMADA',NULL,NULL,NULL,0,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08');
/*!40000 ALTER TABLE `route_assignments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `routes`
--

DROP TABLE IF EXISTS `routes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `routes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `horario_inicio` time NOT NULL,
  `horario_fin` time NOT NULL,
  `tipo` enum('IDA','VUELTA') COLLATE utf8mb4_unicode_ci DEFAULT 'IDA',
  `dias_semana` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'LMXJV' COMMENT 'L=Lunes, M=Martes, X=Miercoles, J=Jueves, V=Viernes',
  `activa` tinyint(1) DEFAULT '1',
  `distancia_total` decimal(8,2) DEFAULT NULL COMMENT 'Distancia en kilÃ³metros',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_nombre` (`nombre`),
  KEY `idx_activa` (`activa`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `routes`
--

LOCK TABLES `routes` WRITE;
/*!40000 ALTER TABLE `routes` DISABLE KEYS */;
INSERT INTO `routes` VALUES (1,'Ruta Centro MaÃ±ana','Recogida por el centro de la ciudad','07:30:00','08:30:00','IDA','LMXJV',1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(2,'Ruta Norte MaÃ±ana','Recogida zona norte','07:45:00','08:45:00','IDA','LMXJV',1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(3,'Ruta Centro Tarde','Vuelta por el centro de la ciudad','17:00:00','18:00:00','VUELTA','LMXJV',1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08');
/*!40000 ALTER TABLE `routes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stops`
--

DROP TABLE IF EXISTS `stops`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stops` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `route_id` bigint NOT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `direccion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitud` decimal(10,8) NOT NULL,
  `longitud` decimal(11,8) NOT NULL,
  `orden` int NOT NULL COMMENT 'Orden de la parada en la ruta',
  `hora_estimada` time DEFAULT NULL COMMENT 'Hora estimada de llegada',
  `radio_proximidad` int DEFAULT '500' COMMENT 'Radio en metros para notificaciÃ³n',
  `activa` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_route` (`route_id`),
  KEY `idx_orden` (`orden`),
  KEY `idx_location` (`latitud`,`longitud`),
  CONSTRAINT `stops_ibfk_1` FOREIGN KEY (`route_id`) REFERENCES `routes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stops`
--

LOCK TABLES `stops` WRITE;
/*!40000 ALTER TABLE `stops` DISABLE KEYS */;
INSERT INTO `stops` VALUES (1,1,'Plaza Mayor','Plaza Mayor, 1',40.41536300,-3.70739800,1,'07:35:00',500,1,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(2,1,'Mercado Central','Calle Mercado, 10',40.41998900,-3.70566300,2,'07:45:00',500,1,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(3,1,'Parque del Retiro','Puerta de AlcalÃ¡',40.42018100,-3.68876800,3,'07:55:00',500,1,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(4,1,'Colegio San JosÃ©','Avenida ConstituciÃ³n, 50',40.42325300,-3.69456800,4,'08:10:00',500,1,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(5,2,'EstaciÃ³n Norte','Calle EstaciÃ³n, 5',40.44185000,-3.70539400,1,'07:50:00',500,1,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(6,2,'Centro Comercial Norte','Avenida Norte, 100',40.44512300,-3.69845600,2,'08:00:00',500,1,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(7,2,'Colegio San JosÃ©','Avenida ConstituciÃ³n, 50',40.42325300,-3.69456800,3,'08:20:00',500,1,'2025-12-29 01:13:08','2025-12-29 01:13:08');
/*!40000 ALTER TABLE `stops` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `apellidos` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `curso` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `parent_id` bigint NOT NULL,
  `stop_id` bigint DEFAULT NULL,
  `foto` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  `activo` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_stop` (`stop_id`),
  KEY `idx_nombre` (`nombre`,`apellidos`),
  KEY `idx_students_parent_activo` (`parent_id`,`activo`),
  CONSTRAINT `students_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `students_ibfk_2` FOREIGN KEY (`stop_id`) REFERENCES `stops` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES (1,'Pedro','GarcÃ­a MartÃ­nez','2015-05-10','3Âº Primaria',2,1,NULL,NULL,1,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(2,'LucÃ­a','GarcÃ­a MartÃ­nez','2017-09-15','1Âº Primaria',2,1,NULL,NULL,1,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(3,'SofÃ­a','MartÃ­nez LÃ³pez','2014-03-20','4Âº Primaria',3,2,NULL,NULL,1,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(4,'Diego','MartÃ­nez LÃ³pez','2016-11-08','2Âº Primaria',3,2,NULL,NULL,1,'2025-12-29 01:13:08','2025-12-29 01:13:08');
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `apellidos` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telefono` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('PARENT','DRIVER','ADMIN','PROFESOR') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PARENT',
  `activo` tinyint(1) DEFAULT '1',
  `foto_perfil` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_email` (`email`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
-- Contraseña de usuarios de prueba (padre1, padre2, conductor1, conductor2): password123
INSERT INTO `users` VALUES (1,'admin@schoolsafetrack.com','$2b$10$6O.2JWO1rg1qZzMm6xE4ouKMqlwYPp7gvS8xFa.WOTBDNT0wIfTr6','Administrador','Sistema','600000000','ADMIN',1,NULL,'2025-12-29 01:13:08','2025-12-29 14:20:20'),(2,'padre1@example.com','$2b$10$ROiHCOSFfwnOFkYZFZJjF.hLGXu7VBF2X6ytb1BB.66oaynm.V4lu','Juan','García López','611111111','PARENT',1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(3,'padre2@example.com','$2b$10$ROiHCOSFfwnOFkYZFZJjF.hLGXu7VBF2X6ytb1BB.66oaynm.V4lu','María','Martínez Ruiz','622222222','PARENT',1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(4,'conductor1@example.com','$2b$10$ROiHCOSFfwnOFkYZFZJjF.hLGXu7VBF2X6ytb1BB.66oaynm.V4lu','Carlos','Rodríguez Sánchez','633333333','DRIVER',1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(5,'conductor2@example.com','$2b$10$ROiHCOSFfwnOFkYZFZJjF.hLGXu7VBF2X6ytb1BB.66oaynm.V4lu','Ana','Fernández Torres','644444444','DRIVER',1,NULL,'2025-12-29 01:13:08','2025-12-29 01:13:08'),(8,'profesorprueba@schoolsafetrack.com','$2b$10$7Wliafc5yJ8cOlHue4tiEecW2bt5Awok7QsYxJMkqurC7gXT89uwm','Profesor','Pruebas','600000002','PARENT',1,NULL,'2025-12-29 11:54:51','2025-12-29 23:07:39');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_notificaciones_pendientes`
--

DROP TABLE IF EXISTS `v_notificaciones_pendientes`;
/*!50001 DROP VIEW IF EXISTS `v_notificaciones_pendientes`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_notificaciones_pendientes` AS SELECT 
 1 AS `user_id`,
 1 AS `email`,
 1 AS `nombre`,
 1 AS `notificaciones_pendientes`,
 1 AS `ultima_notificacion`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_rutas_activas`
--

DROP TABLE IF EXISTS `v_rutas_activas`;
/*!50001 DROP VIEW IF EXISTS `v_rutas_activas`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_rutas_activas` AS SELECT 
 1 AS `ruta_id`,
 1 AS `ruta_nombre`,
 1 AS `horario_inicio`,
 1 AS `horario_fin`,
 1 AS `bus_matricula`,
 1 AS `conductor_nombre`,
 1 AS `conductor_telefono`,
 1 AS `numero_paradas`,
 1 AS `numero_alumnos`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_ultima_ubicacion_buses`
--

DROP TABLE IF EXISTS `v_ultima_ubicacion_buses`;
/*!50001 DROP VIEW IF EXISTS `v_ultima_ubicacion_buses`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_ultima_ubicacion_buses` AS SELECT 
 1 AS `bus_id`,
 1 AS `matricula`,
 1 AS `latitud`,
 1 AS `longitud`,
 1 AS `velocidad`,
 1 AS `timestamp`,
 1 AS `conductor`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_notificaciones_pendientes`
--

/*!50001 DROP VIEW IF EXISTS `v_notificaciones_pendientes`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_notificaciones_pendientes` AS select `u`.`id` AS `user_id`,`u`.`email` AS `email`,`u`.`nombre` AS `nombre`,count(`n`.`id`) AS `notificaciones_pendientes`,max(`n`.`created_at`) AS `ultima_notificacion` from (`users` `u` left join `notifications` `n` on(((`u`.`id` = `n`.`user_id`) and (`n`.`leida` = false)))) group by `u`.`id`,`u`.`email`,`u`.`nombre` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_rutas_activas`
--

/*!50001 DROP VIEW IF EXISTS `v_rutas_activas`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_rutas_activas` AS select `r`.`id` AS `ruta_id`,`r`.`nombre` AS `ruta_nombre`,`r`.`horario_inicio` AS `horario_inicio`,`r`.`horario_fin` AS `horario_fin`,`b`.`matricula` AS `bus_matricula`,concat(`u`.`nombre`,' ',`u`.`apellidos`) AS `conductor_nombre`,`u`.`telefono` AS `conductor_telefono`,count(distinct `s`.`id`) AS `numero_paradas`,count(distinct `st`.`id`) AS `numero_alumnos` from (((((`routes` `r` left join `route_assignments` `ra` on(((`r`.`id` = `ra`.`route_id`) and (`ra`.`fecha` = curdate())))) left join `buses` `b` on((`ra`.`bus_id` = `b`.`id`))) left join `users` `u` on((`b`.`driver_id` = `u`.`id`))) left join `stops` `s` on(((`r`.`id` = `s`.`route_id`) and (`s`.`activa` = true)))) left join `students` `st` on(((`s`.`id` = `st`.`stop_id`) and (`st`.`activo` = true)))) where (`r`.`activa` = true) group by `r`.`id`,`r`.`nombre`,`r`.`horario_inicio`,`r`.`horario_fin`,`b`.`matricula`,`u`.`nombre`,`u`.`apellidos`,`u`.`telefono` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_ultima_ubicacion_buses`
--

/*!50001 DROP VIEW IF EXISTS `v_ultima_ubicacion_buses`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
--
-- Table structure for table `admin_messages`
--

DROP TABLE IF EXISTS `admin_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'Sistema',
  `type` enum('error','advertencia','info','soporte') COLLATE utf8mb4_unicode_ci DEFAULT 'info',
  `status` enum('nuevo','abierto','en_progreso','resuelto','cerrado') COLLATE utf8mb4_unicode_ci DEFAULT 'nuevo',
  `priority` enum('baja','media','alta','crítica') COLLATE utf8mb4_unicode_ci DEFAULT 'media',
  `read` tinyint(1) DEFAULT '0',
  `error_details` json DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_read` (`read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `admin_message_notes`
--

DROP TABLE IF EXISTS `admin_message_notes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_message_notes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint NOT NULL,
  `text` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `author_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_message` (`message_id`),
  CONSTRAINT `admin_message_notes_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `admin_messages` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_ultima_ubicacion_buses` AS select `rtl`.`bus_id` AS `bus_id`,`b`.`matricula` AS `matricula`,`rtl`.`latitud` AS `latitud`,`rtl`.`longitud` AS `longitud`,`rtl`.`velocidad` AS `velocidad`,`rtl`.`timestamp` AS `timestamp`,concat(`u`.`nombre`,' ',`u`.`apellidos`) AS `conductor` from (((`real_time_location` `rtl` join (select `real_time_location`.`bus_id` AS `bus_id`,max(`real_time_location`.`timestamp`) AS `max_timestamp` from `real_time_location` group by `real_time_location`.`bus_id`) `latest` on(((`rtl`.`bus_id` = `latest`.`bus_id`) and (`rtl`.`timestamp` = `latest`.`max_timestamp`)))) join `buses` `b` on((`rtl`.`bus_id` = `b`.`id`))) left join `users` `u` on((`b`.`driver_id` = `u`.`id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_client */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-02 23:43:28
