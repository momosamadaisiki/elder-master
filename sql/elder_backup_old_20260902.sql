-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: elder
-- ------------------------------------------------------
-- Server version	8.0.40

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
-- Table structure for table `elder`
--

DROP TABLE IF EXISTS `elder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `elder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '老人ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `gender` tinyint NOT NULL DEFAULT '1' COMMENT '性别（1：男，2：女）',
  `age` tinyint DEFAULT NULL COMMENT '年龄',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '家庭住址',
  `emergency_contact` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '紧急联系电话',
  `health_status` tinyint NOT NULL DEFAULT '1' COMMENT '健康状况（1：健康，2：一般，3：较差）',
  `checkin_date` date DEFAULT NULL COMMENT '入住日期',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '照片URL',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除（0：未删除，1：已删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='老人信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `elder`
--

LOCK TABLES `elder` WRITE;
/*!40000 ALTER TABLE `elder` DISABLE KEYS */;
INSERT INTO `elder` VALUES (1,'张桂芳',2,78,'370102194803154625','13800000001','济南市历下区解放路10号','张建国','13900000001',1,'2025-03-12',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(2,'李长顺',1,82,'370103194410221317','13800000002','济南市市中区经四路25号','李红','13900000002',2,'2025-04-01',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(3,'王秀兰',2,75,'37010419510708462X','13800000003','济南市槐荫区纬十二路8号','王强','13900000003',1,'2025-05-20',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(4,'赵德福',1,79,'370105194706115538','13800000004','济南市天桥区堤口路66号','赵敏','13900000004',3,'2025-06-15',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(5,'陈玉梅',2,73,'370106195309274621','13800000005','济南市历城区花园路101号','陈刚','13900000005',2,'2025-07-08',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(6,'刘振华',1,85,'370102194112034416','13800000006','济南市历下区和平路3号','刘洋','13900000006',3,'2024-11-20',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(7,'孙桂英',2,77,'370103194904165827','13800000007','济南市市中区英雄山路45号','孙磊','13900000007',1,'2025-01-10',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(8,'周宝山',1,80,'370104194608095513','13800000008','济南市槐荫区经十路200号','周静','13900000008',2,'2025-02-14',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(9,'吴凤仙',2,71,'370105195502264628','13800000009','济南市天桥区北园大街12号','吴斌','13900000009',1,'2025-08-01',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(10,'郑国栋',1,76,'370106195005315534','13800000010','济南市历城区二环东路88号','郑爽','13900000010',2,'2025-08-10',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(11,'冯淑珍',2,84,'370102194209174624','13800000011','济南市历下区文化东路29号','冯军','13900000011',3,'2024-09-05',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(12,'蒋守义',1,74,'370103195203124419','13800000012','济南市市中区舜耕路56号','蒋丽','13900000012',1,'2025-06-28',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(13,'沈玉兰',2,79,'370104194711205826','13800000013','济南市槐荫区张庄路170号','沈鹏','13900000013',2,'2025-05-30',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(14,'韩志远',1,81,'370105194507065511','13800000014','济南市天桥区无影山路7号','韩雪','13900000014',1,'2025-04-22',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(15,'杨春花',2,72,'370106195408184623','13800000015','济南市历城区祝舜路35号','杨帆','13900000015',1,'2025-07-15',NULL,0,'2026-09-01 09:37:36','2026-09-01 09:37:36'),(16,'TestElder2',1,81,'370000000000000001','13800000000','addr','ec','13900000000',2,'2026-09-01',NULL,1,'2026-09-01 10:45:19','2026-09-01 10:45:57'),(17,'123',1,NULL,NULL,'123123','1231231',NULL,NULL,1,NULL,'https://momosama.oss-cn-beijing.aliyuncs.com/b3fb53b81f8245458bba7c0a28e8113a.png',0,'2026-09-01 10:49:48','2026-09-01 10:49:48');
/*!40000 ALTER TABLE `elder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `elder_tag`
--

DROP TABLE IF EXISTS `elder_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `elder_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_elder_tag` (`elder_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='老人-标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `elder_tag`
--

LOCK TABLES `elder_tag` WRITE;
/*!40000 ALTER TABLE `elder_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `elder_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `code` varchar(32) NOT NULL COMMENT '标签编码（LIVE_ALONE/EMPTY_NEST/...）',
  `name` varchar(32) NOT NULL COMMENT '标签名称（独居/空巢/...）',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除（0：未删除，1：已删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
INSERT INTO `tag` VALUES (1,'LIVE_ALONE','独居',0,'2026-08-27 15:58:48','2026-08-27 15:58:48'),(2,'EMPTY_NEST','空巢',0,'2026-08-27 15:58:48','2026-08-27 15:58:48'),(3,'AGE_80','高龄',0,'2026-08-27 15:58:48','2026-08-27 15:58:48'),(4,'DISABLED','失能',0,'2026-08-27 15:58:48','2026-08-27 15:58:48'),(5,'C_HBP','高血压',0,'2026-08-27 15:58:48','2026-08-27 15:58:48'),(6,'C_DM','糖尿病',0,'2026-08-27 15:58:48','2026-08-27 15:58:48'),(7,'C_CHD','冠心病',0,'2026-08-27 15:58:48','2026-08-27 15:58:48');
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0：停用，1：正常）',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除（0：未删除，1：已删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_name` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'liunian','123456','123123','123123','https://momosama.oss-cn-beijing.aliyuncs.com/4b7a72fe64e24972889ff5111d8228e3.png',1,0,'2026-08-25 22:02:29','2026-09-01 10:57:13');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-02 16:56:00
