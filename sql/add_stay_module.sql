-- =============================================================
-- 入住/床位管理模块（房间床位 + 入住退住 + 退住费用结算）
-- 用法：在 elder 库执行本文件（可重复执行，幂等）
-- =============================================================
USE `elder`;

-- 房间表
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '房间ID',
  `floor` int NOT NULL DEFAULT 1 COMMENT '楼层',
  `code` varchar(32) NOT NULL COMMENT '房间号（如 101）',
  `capacity` int NOT NULL DEFAULT 1 COMMENT '床位数',
  `price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '床位月费(元/月/床)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用 1正常',
  `remark` varchar(200) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_room_code` (`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '房间表' ROW_FORMAT = Dynamic;

-- 床位表
DROP TABLE IF EXISTS `bed`;
CREATE TABLE `bed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '床位ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `bed_no` varchar(16) NOT NULL COMMENT '床号（如 101-1）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0空闲 1占用 2维修',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_room_bed` (`room_id`, `bed_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '床位表' ROW_FORMAT = Dynamic;

-- 入住单（支持多条历史）
DROP TABLE IF EXISTS `elder_stay`;
CREATE TABLE `elder_stay` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入住单ID',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `bed_id` bigint NOT NULL COMMENT '床位ID',
  `care_level_id` bigint NULL DEFAULT NULL COMMENT '入住时护理等级ID（用于费用结算）',
  `check_in_date` date NOT NULL COMMENT '入住日期',
  `check_out_date` date NULL DEFAULT NULL COMMENT '退住日期（空=在住）',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作员工ID',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_stay_elder` (`elder_id`) USING BTREE,
  INDEX `idx_stay_bed` (`bed_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '老人入住单' ROW_FORMAT = Dynamic;

-- 退住结算单
DROP TABLE IF EXISTS `fee_settlement`;
CREATE TABLE `fee_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结算单ID',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `stay_id` bigint NOT NULL COMMENT '入住单ID',
  `days` int NOT NULL DEFAULT 0 COMMENT '计费天数',
  `care_level_name` varchar(64) NULL DEFAULT NULL COMMENT '护理等级名称(快照)',
  `care_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '护理费(等级月费/30*天数)',
  `bed_info` varchar(128) NULL DEFAULT NULL COMMENT '床位信息(快照 如 1楼101-101-1)',
  `bed_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '床位费(房间月费/30*天数)',
  `total_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '合计金额',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待缴费 1已结清',
  `settle_time` datetime NULL DEFAULT NULL COMMENT '结清时间',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_settle_stay` (`stay_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退住费用结算单' ROW_FORMAT = Dynamic;

-- 示例数据：3 个房间 6 张床；老人 1~5 已入住（与 elder.status=4 对应），201-2 空闲
INSERT INTO `room` (`floor`,`code`,`capacity`,`price`,`status`,`remark`) VALUES
(1,'101',2,900.00,1,'朝阳双人间'),
(1,'102',2,700.00,1,'标准双人间'),
(2,'201',2,1200.00,1,'南向双人间');

INSERT INTO `bed` (`room_id`,`bed_no`,`status`) VALUES
(1,'101-1',1),(1,'101-2',1),
(2,'102-1',1),(2,'102-2',1),
(3,'201-1',1),(3,'201-2',0);

INSERT INTO `elder_stay` (`elder_id`,`bed_id`,`care_level_id`,`check_in_date`,`check_out_date`,`operator_id`,`remark`) VALUES
(1,1,2,'2026-08-30',NULL,1,NULL),
(2,2,3,'2026-08-30',NULL,1,NULL),
(3,3,1,'2026-08-30',NULL,1,NULL),
(4,4,2,'2026-08-30',NULL,1,NULL),
(5,5,1,'2026-08-30',NULL,1,NULL);

-- 菜单授权：顶级目录“入住管理” + 两个子菜单，授管理员(role_id=1)
INSERT INTO `permission` (`parent_id`,`name`,`type`,`path`,`icon`,`sort`,`status`,`deleted`,`create_time`,`update_time`)
SELECT 0,'入住管理',0,NULL,'House',6,1,0,NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM `permission` WHERE `name`='入住管理' AND `parent_id`=0 AND `deleted`=0);

INSERT INTO `permission` (`parent_id`,`name`,`type`,`path`,`icon`,`sort`,`status`,`deleted`,`create_time`,`update_time`)
SELECT p.`id`,'房间床位',1,'/room','OfficeBuilding',1,1,0,NOW(),NOW()
FROM `permission` p WHERE p.`name`='入住管理' AND p.`parent_id`=0 AND p.`deleted`=0
  AND NOT EXISTS (SELECT 1 FROM `permission` x WHERE x.`path`='/room' AND x.`deleted`=0);

INSERT INTO `permission` (`parent_id`,`name`,`type`,`path`,`icon`,`sort`,`status`,`deleted`,`create_time`,`update_time`)
SELECT p.`id`,'入住退住',1,'/stay','Van',2,1,0,NOW(),NOW()
FROM `permission` p WHERE p.`name`='入住管理' AND p.`parent_id`=0 AND p.`deleted`=0
  AND NOT EXISTS (SELECT 1 FROM `permission` x WHERE x.`path`='/stay' AND x.`deleted`=0);

-- 管理员拥有这三条新权限
INSERT INTO `role_permission` (`role_id`,`permission_id`,`create_time`,`update_time`)
SELECT 1, p.`id`, NOW(), NOW()
FROM `permission` p
WHERE p.`deleted`=0 AND (p.`path`='/room' OR p.`path`='/stay' OR (p.`name`='入住管理' AND p.`parent_id`=0))
  AND NOT EXISTS (SELECT 1 FROM `role_permission` rp WHERE rp.`role_id`=1 AND rp.`permission_id`=p.`id`);
