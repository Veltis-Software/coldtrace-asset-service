/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `locations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `address` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `organization_id` bigint NOT NULL,
  `status` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_location_organization_id_name` (`organization_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gateways` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `location_id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `network` varchar(255) NOT NULL,
  `organization_id` bigint NOT NULL,
  `status` varchar(255) NOT NULL,
  `uuid` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gateway_organization_id_uuid` (`organization_id`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `capacity` double NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `location_id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `organization_id` bigint NOT NULL,
  `status` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `uuid` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_organization_id_uuid` (`organization_id`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `iot_devices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `asset_id` bigint DEFAULT NULL,
  `calibration_status` varchar(255) NOT NULL,
  `device_type` varchar(255) NOT NULL,
  `gateway_id` bigint NOT NULL,
  `last_calibration_date` date NOT NULL,
  `measurement_type` varchar(255) NOT NULL,
  `model` varchar(255) NOT NULL,
  `next_calibration_date` date NOT NULL,
  `organization_id` bigint NOT NULL,
  `reading_frequency_seconds` int NOT NULL,
  `status` varchar(255) NOT NULL,
  `uuid` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_organization_id_uuid` (`organization_id`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `iot_device_measurement_parameters` (
  `iot_device_id` bigint NOT NULL,
  `measurement_parameter` varchar(255) NOT NULL,
  KEY `FKavueayjfx6k934x3wh7m9rhyq` (`iot_device_id`),
  CONSTRAINT `FKavueayjfx6k934x3wh7m9rhyq` FOREIGN KEY (`iot_device_id`) REFERENCES `iot_devices` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asset_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `alert_threshold_minutes` int NOT NULL,
  `asset_id` bigint DEFAULT NULL,
  `calibration_frequency_days` int NOT NULL,
  `humidity_unit` varchar(255) NOT NULL,
  `maximum_humidity` double NOT NULL,
  `maximum_temperature` double NOT NULL,
  `minimum_humidity` double NOT NULL,
  `minimum_temperature` double NOT NULL,
  `organization_id` bigint NOT NULL,
  `reading_frequency_seconds` int NOT NULL,
  `temperature_unit` varchar(255) NOT NULL,
  `uuid` varchar(255) NOT NULL,
  `weight_unit` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asset_settings_asset_types` (
  `asset_settings_id` bigint NOT NULL,
  `asset_type` varchar(255) NOT NULL,
  KEY `FKh9nbwweqdqt470uhyo9rga9bb` (`asset_settings_id`),
  CONSTRAINT `FKh9nbwweqdqt470uhyo9rga9bb` FOREIGN KEY (`asset_settings_id`) REFERENCES `asset_settings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asset_settings_iot_device_types` (
  `asset_settings_id` bigint NOT NULL,
  `iot_device_type` varchar(255) NOT NULL,
  KEY `FKd07iyp43h2bw13xprwsbvcnvh` (`asset_settings_id`),
  CONSTRAINT `FKd07iyp43h2bw13xprwsbvcnvh` FOREIGN KEY (`asset_settings_id`) REFERENCES `asset_settings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asset_settings_versions` (
  `asset_id` bigint NOT NULL,
  `settings_version` bigint NOT NULL,
  PRIMARY KEY (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asset_integration_outboxes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attempts` int NOT NULL,
  `event_id` varchar(36) NOT NULL,
  `payload` json NOT NULL,
  `published_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKn2sv2kojpexjdovrqfqidhtfc` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `source_configurations` (
  `device_id` bigint NOT NULL,
  `configuration_version` bigint NOT NULL,
  `gateway_id` bigint NOT NULL,
  `organization_id` bigint NOT NULL,
  `snapshot` text NOT NULL,
  `source_id` varchar(64) NOT NULL,
  PRIMARY KEY (`device_id`),
  UNIQUE KEY `uk_source_org_identity` (`organization_id`,`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `source_configuration_histories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `configuration_version` bigint NOT NULL,
  `device_id` bigint NOT NULL,
  `organization_id` bigint NOT NULL,
  `profile_version` int NOT NULL,
  `snapshot` text NOT NULL,
  `source_id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_device_revision` (`device_id`,`configuration_version`),
  KEY `idx_source_profile_history` (`organization_id`,`source_id`,`profile_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
