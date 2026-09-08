-- =============================================================
-- 养老院管理系统（elder）完整建库脚本
-- 根据 src/main/java/com/situ/elder/pojo/entity 下的实体类
-- 及 ui/ui-admin、ui/ui-app 前端页面生成
-- MySQL 8.0+ / utf8mb4
-- =============================================================

DROP DATABASE IF EXISTS `elder`;
CREATE DATABASE `elder` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `elder`;

-- =============================================================
-- 一、RBAC 权限模块
-- =============================================================

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(32) NOT NULL COMMENT '角色名称',
  `code` varchar(32) NOT NULL COMMENT '角色编码(admin/hugong)',
  `description` varchar(255) NULL DEFAULT NULL COMMENT '描述',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code` (`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

INSERT INTO `role` VALUES (1, '管理员', 'admin', '系统管理员，拥有全部权限', 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role` VALUES (2, '护工', 'hugong', '护理人员，仅可查看护理任务', 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- ----------------------------
-- 用户信息表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(32) NOT NULL COMMENT '登录用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `phone` varchar(20) NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用 1正常',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_name` (`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

INSERT INTO `user` VALUES (1, 'liunian', '123456', '123123', '123123', NULL, 1, 0, '2026-08-27 15:58:48', '2026-08-27 15:58:48');
INSERT INTO `user` VALUES (2, 'admin', '123456', '13800000001', 'admin@elder.com', NULL, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `user` VALUES (3, '张桂芳', '123456', '13800000002', NULL, NULL, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `user` VALUES (4, '王建国', '123456', '13800000003', NULL, NULL, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- ----------------------------
-- 员工-角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工-角色关联表' ROW_FORMAT = Dynamic;

INSERT INTO `user_role` VALUES (1, 1, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `user_role` VALUES (2, 1, 2, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `user_role` VALUES (3, 2, 3, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `user_role` VALUES (4, 2, 4, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- ----------------------------
-- 权限表（菜单与 ui-admin/src/router/index.js 路由一一对应）
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '所属上级，0为顶级',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `type` tinyint NOT NULL COMMENT '类型：0目录 1菜单 2按钮',
  `path` varchar(255) NULL DEFAULT NULL COMMENT '路由地址',
  `permission_value` varchar(255) NULL DEFAULT NULL COMMENT '权限值(按钮权限)',
  `icon` varchar(64) NULL DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁止 1正常',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

INSERT INTO `permission` VALUES (1, 0, '老人管理', 1, '/elder', NULL, 'Promotion', 1, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (2, 0, '标签管理', 1, '/tag', NULL, 'PriceTag', 2, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (3, 0, '护理管理', 0, NULL, NULL, 'Suitcase', 3, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (4, 3, '护理项目', 1, '/care-item', NULL, 'SetUp', 1, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (5, 3, '护理等级', 1, '/care-level', NULL, 'Histogram', 2, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (6, 3, '护理计划', 1, '/care-plan', NULL, 'Notebook', 3, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (7, 3, '护理任务', 1, '/care-task', NULL, 'AlarmClock', 4, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (8, 0, '体检管理', 0, NULL, NULL, 'FirstAidKit', 4, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (9, 8, '体检项目', 1, '/exam-item', NULL, 'List', 1, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (10, 8, '体检套餐', 1, '/exam-package', NULL, 'Box', 2, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (11, 0, '权限管理', 0, NULL, NULL, 'UserFilled', 5, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (12, 11, '用户管理', 1, '/user', NULL, 'User', 1, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (13, 11, '角色管理', 1, '/role', NULL, 'Crop', 2, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `permission` VALUES (14, 11, '权限管理', 1, '/permission', NULL, 'EditPen', 3, 1, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- ----------------------------
-- 角色-权限关联表（管理员拥有全部权限，护工仅护理任务）
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-权限关联表' ROW_FORMAT = Dynamic;

INSERT INTO `role_permission` VALUES (1, 1, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (2, 1, 2, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (3, 1, 3, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (4, 1, 4, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (5, 1, 5, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (6, 1, 6, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (7, 1, 7, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (8, 1, 8, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (9, 1, 9, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (10, 1, 10, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (11, 1, 11, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (12, 1, 12, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (13, 1, 13, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (14, 1, 14, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `role_permission` VALUES (15, 2, 7, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- =============================================================
-- 二、老人模块
-- =============================================================

-- ----------------------------
-- 老人表
-- ----------------------------
DROP TABLE IF EXISTS `elder`;
CREATE TABLE `elder`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '老人ID',
  `name` varchar(32) NOT NULL COMMENT '老人姓名',
  `password` varchar(100) NOT NULL DEFAULT '123456' COMMENT '密码',
  `avatar` varchar(255) NULL DEFAULT NULL COMMENT '头像URL',
  `id_card_no` varchar(18) NULL DEFAULT NULL COMMENT '身份证号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用 2请假 3退住中 4入住中 5已退住',
  `phone` varchar(20) NULL DEFAULT NULL COMMENT '手机号',
  `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
  `address` varchar(255) NULL DEFAULT NULL COMMENT '家庭住址',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '老人表' ROW_FORMAT = Dynamic;

INSERT INTO `elder` VALUES (1, '张秀兰', '123456', NULL, '110101194805152234', 4, '13900000001', '1948-05-15', '北京市东城区幸福路10号', '高血压，每日需服药', 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder` VALUES (2, '李德福', '123456', NULL, '110101194509203356', 4, '13900000002', '1945-09-20', '北京市西城区平安里5号', '行动不便，需要轮椅', 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder` VALUES (3, '王桂英', '123456', NULL, '110101195203104412', 4, '13900000003', '1952-03-10', '北京市朝阳区团结湖路8号', NULL, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder` VALUES (4, '赵铁柱', '123456', NULL, '110101194612255578', 4, '13900000004', '1946-12-25', '北京市海淀区学院路12号', '糖尿病，控制饮食', 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder` VALUES (5, '陈玉兰', '123456', NULL, '110101195107186690', 4, '13900000005', '1951-07-18', '北京市丰台区丰台路20号', NULL, 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder` VALUES (6, '刘长生', '123456', NULL, '110101194403027712', 2, '13900000006', '1944-03-02', '北京市石景山区八角路3号', '请假回家探亲', 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- ----------------------------
-- 标签表
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `code` varchar(32) NOT NULL COMMENT '标签编码',
  `name` varchar(32) NOT NULL COMMENT '标签名称',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tag_code` (`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

INSERT INTO `tag` VALUES (1, 'LIVE_ALONE', '独居', 0, '2026-08-27 15:58:48', '2026-08-27 15:58:48');
INSERT INTO `tag` VALUES (2, 'EMPTY_NEST', '空巢', 0, '2026-08-27 15:58:48', '2026-08-27 15:58:48');
INSERT INTO `tag` VALUES (3, 'AGE_80', '高龄', 0, '2026-08-27 15:58:48', '2026-08-27 15:58:48');
INSERT INTO `tag` VALUES (4, 'DISABLED', '失能', 0, '2026-08-27 15:58:48', '2026-08-27 15:58:48');
INSERT INTO `tag` VALUES (5, 'C_HBP', '高血压', 0, '2026-08-27 15:58:48', '2026-08-27 15:58:48');
INSERT INTO `tag` VALUES (6, 'C_DM', '糖尿病', 0, '2026-08-27 15:58:48', '2026-08-27 15:58:48');
INSERT INTO `tag` VALUES (7, 'C_CHD', '冠心病', 0, '2026-08-27 15:58:48', '2026-08-27 15:58:48');

-- ----------------------------
-- 老人-标签关联表
-- ----------------------------
DROP TABLE IF EXISTS `elder_tag`;
CREATE TABLE `elder_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '老人-标签关联表' ROW_FORMAT = Dynamic;

INSERT INTO `elder_tag` VALUES (1, 1, 5, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder_tag` VALUES (2, 1, 7, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder_tag` VALUES (3, 2, 4, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder_tag` VALUES (4, 2, 3, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder_tag` VALUES (5, 3, 2, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder_tag` VALUES (6, 4, 6, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder_tag` VALUES (7, 4, 3, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder_tag` VALUES (8, 5, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder_tag` VALUES (9, 6, 2, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `elder_tag` VALUES (10, 6, 3, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- =============================================================
-- 三、护理模块
-- =============================================================

-- ----------------------------
-- 护理等级表
-- ----------------------------
DROP TABLE IF EXISTS `care_level`;
CREATE TABLE `care_level`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '等级名称',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '护理费用',
  `description` varchar(500) NULL DEFAULT NULL COMMENT '等级说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '护理等级表' ROW_FORMAT = Dynamic;

INSERT INTO `care_level` VALUES (1, '自理级', 500.00, '生活能够自理，仅需日常巡视观察', 1, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_level` VALUES (2, '半护理级', 1200.00, '部分生活需要协助，如洗澡、洗衣等', 1, 2, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_level` VALUES (3, '全护理级', 2000.00, '生活完全不能自理，需要24小时专人护理', 1, 3, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- ----------------------------
-- 护理项目表
-- ----------------------------
DROP TABLE IF EXISTS `care_item`;
CREATE TABLE `care_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单次服务价格',
  `image` varchar(255) NULL DEFAULT NULL COMMENT '图片',
  `requirement` varchar(500) NULL DEFAULT NULL COMMENT '护理要求',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '护理项目表' ROW_FORMAT = Dynamic;

INSERT INTO `care_item` VALUES (1, '测量血压', 10.00, NULL, '每日早晚各一次，做好记录', 1, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_item` VALUES (2, '测量血糖', 15.00, NULL, '空腹测量，注意消毒', 2, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_item` VALUES (3, '协助进食', 20.00, NULL, '耐心喂食，注意呛咳', 3, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_item` VALUES (4, '翻身叩背', 20.00, NULL, '每2小时一次，预防褥疮', 4, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_item` VALUES (5, '口腔护理', 15.00, NULL, '每日两次，保持口腔清洁', 5, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_item` VALUES (6, '康复训练', 50.00, NULL, '在护理人员指导下进行适度训练', 6, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_item` VALUES (7, '协助洗澡', 30.00, NULL, '注意水温与防滑，防止跌倒', 7, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_item` VALUES (8, '陪同散步', 20.00, NULL, '每日一次，每次30分钟', 8, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- ----------------------------
-- 护理计划表
-- ----------------------------
DROP TABLE IF EXISTS `care_plan`;
CREATE TABLE `care_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '护理人员ID',
  `care_level_id` bigint NULL DEFAULT NULL COMMENT '护理等级ID',
  `name` varchar(100) NOT NULL COMMENT '计划名称',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0结束 1开始',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '护理计划表' ROW_FORMAT = Dynamic;

INSERT INTO `care_plan` VALUES (1, 1, 3, 2, '张秀兰日常护理计划', '2026-09-01', '2026-12-31', 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan` VALUES (2, 2, 3, 3, '李德福全护理计划', '2026-09-01', '2026-12-31', 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan` VALUES (3, 4, 4, 2, '赵铁柱血糖管理计划', '2026-09-01', '2026-12-31', 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan` VALUES (4, 3, 4, 1, '王桂英自理级观察计划', '2026-08-01', '2026-08-31', 0, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- ----------------------------
-- 护理计划-项目关联表
-- ----------------------------
DROP TABLE IF EXISTS `care_plan_item`;
CREATE TABLE `care_plan_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `care_plan_id` bigint NOT NULL COMMENT '护理计划ID',
  `care_item_id` bigint NOT NULL COMMENT '护理项目ID',
  `execute_time` time NULL DEFAULT NULL COMMENT '计划执行时间',
  `execute_cycle` tinyint NOT NULL DEFAULT 0 COMMENT '执行周期：0天 1周 2月',
  `execute_frequency` int NOT NULL DEFAULT 1 COMMENT '执行频次',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '护理计划-项目关联表' ROW_FORMAT = Dynamic;

INSERT INTO `care_plan_item` VALUES (1, 1, 1, '08:00:00', 0, 1, '早晨服药后测量', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan_item` VALUES (2, 1, 8, '09:30:00', 0, 1, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan_item` VALUES (3, 2, 3, '07:30:00', 0, 3, '早中晚各一次', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan_item` VALUES (4, 2, 4, '02:00:00', 0, 1, '夜间每2小时翻身', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan_item` VALUES (5, 2, 5, '09:00:00', 0, 2, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan_item` VALUES (6, 3, 2, '07:00:00', 0, 1, '空腹测量', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan_item` VALUES (7, 3, 6, '15:00:00', 1, 3, '每周三次', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `care_plan_item` VALUES (8, 4, 1, '08:00:00', 0, 1, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');

-- ----------------------------
-- 护理任务与打卡记录表
-- ----------------------------
DROP TABLE IF EXISTS `care_task`;
CREATE TABLE `care_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `care_plan_id` bigint NULL DEFAULT NULL COMMENT '来源护理计划ID',
  `care_item_id` bigint NOT NULL COMMENT '护理项目ID',
  `care_item_name` varchar(64) NULL DEFAULT NULL COMMENT '护理项目名称(冗余)',
  `user_id` bigint NULL DEFAULT NULL COMMENT '指定/实际执行护理员ID',
  `plan_execute_date` date NOT NULL COMMENT '计划执行日期',
  `plan_execute_time` time NULL DEFAULT NULL COMMENT '计划执行时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待执行 1已完成 2已跳过/取消',
  `actual_execute_time` datetime NULL DEFAULT NULL COMMENT '实际完成时间',
  `execute_result` varchar(500) NULL DEFAULT NULL COMMENT '执行结果描述/健康数值',
  `execute_img` varchar(1000) NULL DEFAULT NULL COMMENT '现场打卡照片URL(多张逗号隔开)',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '护理员执行备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务生成时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '护理任务与打卡记录表' ROW_FORMAT = Dynamic;

INSERT INTO `care_task` VALUES (1, 1, 1, 1, '测量血压', 3, '2026-09-01', '08:00:00', 1, '2026-09-01 08:05:00', '血压 130/85 mmHg', NULL, '老人状态良好', '2026-09-01 00:00:00', '2026-09-01 08:05:00');
INSERT INTO `care_task` VALUES (2, 1, 1, 8, '陪同散步', 3, '2026-09-01', '09:30:00', 1, '2026-09-01 09:40:00', '散步30分钟', NULL, NULL, '2026-09-01 00:00:00', '2026-09-01 09:40:00');
INSERT INTO `care_task` VALUES (3, 2, 2, 3, '协助进食', 3, '2026-09-01', '07:30:00', 1, '2026-09-01 07:35:00', '进食正常', NULL, '食欲较好', '2026-09-01 00:00:00', '2026-09-01 07:35:00');
INSERT INTO `care_task` VALUES (4, 2, 2, 4, '翻身叩背', 3, '2026-09-01', '02:00:00', 1, '2026-09-01 02:00:00', '已翻身', NULL, NULL, '2026-09-01 00:00:00', '2026-09-01 02:00:00');
INSERT INTO `care_task` VALUES (5, 4, 3, 2, '测量血糖', 4, '2026-09-01', '07:00:00', 1, '2026-09-01 07:03:00', '空腹血糖 6.8 mmol/L', NULL, '血糖偏高，已告知医生', '2026-09-01 00:00:00', '2026-09-01 07:03:00');
INSERT INTO `care_task` VALUES (6, 1, 1, 1, '测量血压', 3, '2026-09-02', '08:00:00', 0, NULL, NULL, NULL, NULL, '2026-09-02 00:00:00', '2026-09-02 00:00:00');
INSERT INTO `care_task` VALUES (7, 2, 2, 3, '协助进食', 3, '2026-09-02', '07:30:00', 0, NULL, NULL, NULL, NULL, '2026-09-02 00:00:00', '2026-09-02 00:00:00');
INSERT INTO `care_task` VALUES (8, 4, 3, 2, '测量血糖', 4, '2026-09-02', '07:00:00', 0, NULL, NULL, NULL, NULL, '2026-09-02 00:00:00', '2026-09-02 00:00:00');

-- =============================================================
-- 四、体检模块
-- =============================================================

-- ----------------------------
-- 体检项目表
-- ----------------------------
DROP TABLE IF EXISTS `exam_item`;
CREATE TABLE `exam_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '体检项目ID',
  `name` varchar(100) NOT NULL COMMENT '项目名称',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单项价格',
  `unit` varchar(50) NULL DEFAULT NULL COMMENT '单位',
  `result_type` tinyint NOT NULL DEFAULT 0 COMMENT '结果类型：0文本 1数值',
  `reference_min` decimal(10, 2) NULL DEFAULT NULL COMMENT '参考范围下限',
  `reference_max` decimal(10, 2) NULL DEFAULT NULL COMMENT '参考范围上限',
  `reference_unit` varchar(50) NULL DEFAULT NULL COMMENT '参考范围单位',
  `description` varchar(500) NULL DEFAULT NULL COMMENT '项目说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '体检项目表' ROW_FORMAT = Dynamic;

INSERT INTO `exam_item` VALUES (1, '血常规', 35.00, '次', 0, NULL, NULL, NULL, '检测红细胞、白细胞、血小板等指标', 1, 1, '2026-08-30 16:17:15', '2026-08-30 16:17:15');
INSERT INTO `exam_item` VALUES (2, '尿常规', 25.00, '次', 0, NULL, NULL, NULL, '检查尿液相关指标', 1, 2, '2026-08-30 16:17:15', '2026-08-30 16:17:15');
INSERT INTO `exam_item` VALUES (3, '肝功能', 80.00, '次', 1, 0.00, 40.00, 'U/L', '检测谷丙转氨酶等肝功能指标', 1, 3, '2026-08-30 16:17:15', '2026-08-30 16:17:15');
INSERT INTO `exam_item` VALUES (4, '肾功能', 70.00, '次', 1, 40.00, 100.00, 'μmol/L', '检测肌酐等肾功能指标', 1, 4, '2026-08-30 16:17:15', '2026-08-30 16:17:15');
INSERT INTO `exam_item` VALUES (5, '空腹血糖', 20.00, '次', 1, 3.90, 6.10, 'mmol/L', '检测空腹血糖水平', 1, 5, '2026-08-30 16:17:15', '2026-08-30 16:17:15');
INSERT INTO `exam_item` VALUES (6, '血脂', 60.00, '次', 1, 0.00, 5.20, 'mmol/L', '检测总胆固醇等血脂指标', 1, 6, '2026-08-30 16:17:15', '2026-08-30 16:17:15');
INSERT INTO `exam_item` VALUES (7, '心电图', 50.00, '次', 0, NULL, NULL, NULL, '检查心脏电生理活动情况', 1, 7, '2026-08-30 16:17:15', '2026-08-30 16:17:15');
INSERT INTO `exam_item` VALUES (8, '腹部彩超', 120.00, '次', 0, NULL, NULL, NULL, '检查肝脏、胆囊、胰腺、脾脏等', 1, 8, '2026-08-30 16:17:15', '2026-08-30 16:17:15');
INSERT INTO `exam_item` VALUES (9, '骨密度检测', 100.00, '次', 1, -1.00, 10.00, 'T值', '检测骨骼密度情况', 1, 9, '2026-08-30 16:17:15', '2026-08-30 16:17:15');
INSERT INTO `exam_item` VALUES (10, '胸部CT', 200.00, '次', 0, NULL, NULL, NULL, '检查肺部及胸部相关情况', 1, 10, '2026-08-30 16:17:15', '2026-08-30 16:17:15');

-- ----------------------------
-- 体检套餐表
-- ----------------------------
DROP TABLE IF EXISTS `exam_package`;
CREATE TABLE `exam_package`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '体检套餐ID',
  `name` varchar(100) NOT NULL COMMENT '套餐名称',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '套餐价格',
  `image` varchar(255) NULL DEFAULT NULL COMMENT '套餐图片',
  `description` varchar(500) NULL DEFAULT NULL COMMENT '套餐说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '体检套餐表' ROW_FORMAT = Dynamic;

INSERT INTO `exam_package` VALUES (1, '基础体检套餐', 199.00, NULL, '适合身体状况较好的老年人进行基础健康检查', 1, 1, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package` VALUES (2, '老年健康套餐', 399.00, NULL, '针对老年人常见健康问题设计的综合体检套餐', 1, 2, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package` VALUES (3, '心脑血管专项套餐', 499.00, NULL, '针对心脑血管健康进行专项检查', 1, 3, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package` VALUES (4, '骨健康套餐', 299.00, NULL, '针对老年人骨骼健康进行专项检查', 1, 4, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package` VALUES (5, '全面体检套餐', 699.00, NULL, '包含多个身体系统的综合健康检查', 1, 5, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package` VALUES (11, '11', 3.00, 'https://ai2607.oss-cn-beijing.aliyuncs.com/0f19a4d1b756435190c39446fddb8c21.png', '12', 1, 7, '2026-09-01 22:47:10', '2026-09-01 22:47:10');

-- ----------------------------
-- 体检套餐-项目关联表
-- ----------------------------
DROP TABLE IF EXISTS `exam_package_item`;
CREATE TABLE `exam_package_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `package_id` bigint NOT NULL COMMENT '体检套餐ID',
  `exam_item_id` bigint NOT NULL COMMENT '体检项目ID',
  `sort` int NOT NULL DEFAULT 0 COMMENT '项目排序',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '体检套餐-项目关联表' ROW_FORMAT = Dynamic;

INSERT INTO `exam_package_item` VALUES (5, 2, 1, 1, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (6, 2, 2, 2, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (7, 2, 3, 3, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (8, 2, 4, 4, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (9, 2, 5, 5, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (10, 2, 6, 6, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (11, 2, 7, 7, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (12, 3, 5, 1, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (13, 3, 6, 2, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (14, 3, 7, 3, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (15, 3, 10, 4, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (16, 4, 1, 1, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (17, 4, 4, 2, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (18, 4, 9, 3, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (19, 5, 1, 1, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (20, 5, 2, 2, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (21, 5, 3, 3, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (22, 5, 4, 4, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (23, 5, 5, 5, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (24, 5, 6, 6, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (25, 5, 7, 7, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (26, 5, 8, 8, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (27, 5, 9, 9, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (28, 5, 10, 10, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_package_item` VALUES (45, 1, 1, 0, NULL, '2026-09-01 22:46:48', '2026-09-01 22:46:48');
INSERT INTO `exam_package_item` VALUES (46, 1, 2, 1, NULL, '2026-09-01 22:46:48', '2026-09-01 22:46:48');
INSERT INTO `exam_package_item` VALUES (47, 1, 3, 2, NULL, '2026-09-01 22:46:48', '2026-09-01 22:46:48');
INSERT INTO `exam_package_item` VALUES (48, 1, 4, 3, NULL, '2026-09-01 22:46:48', '2026-09-01 22:46:48');
INSERT INTO `exam_package_item` VALUES (49, 11, 1, 0, NULL, '2026-09-01 22:47:36', '2026-09-01 22:47:36');

-- ----------------------------
-- 老人预约/体检记录表
-- ----------------------------
DROP TABLE IF EXISTS `exam_appointment`;
CREATE TABLE `exam_appointment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '体检记录ID',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `package_id` bigint NOT NULL COMMENT '体检套餐ID',
  `appointment_date` date NOT NULL COMMENT '预约/体检日期',
  `appointment_time` time NOT NULL COMMENT '预约/体检时间',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '体检套餐价格',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待体检 1体检中 2已完成 3已取消 4已过期',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '老人预约/体检记录表' ROW_FORMAT = Dynamic;

INSERT INTO `exam_appointment` VALUES (1, 1, 2, '2026-09-05', '08:30:00', 399.00, 0, '需要工作人员陪同', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment` VALUES (2, 2, 1, '2026-09-06', '09:00:00', 199.00, 0, NULL, '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment` VALUES (3, 3, 3, '2026-08-25', '08:00:00', 499.00, 2, '已完成体检', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment` VALUES (4, 4, 4, '2026-09-08', '10:00:00', 299.00, 0, '需要协助行动', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment` VALUES (5, 5, 5, '2026-08-20', '08:30:00', 699.00, 2, '体检已完成', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment` VALUES (6, 1, 1, '2026-09-01', '00:00:03', 199.00, 0, NULL, '2026-08-31 18:55:36', '2026-09-01 22:52:36');
INSERT INTO `exam_appointment` VALUES (7, 1, 11, '2026-09-02', '02:00:00', 3.00, 0, NULL, '2026-09-01 22:48:01', '2026-09-01 22:52:30');
INSERT INTO `exam_appointment` VALUES (8, 1, 4, '2026-09-03', '08:00:00', 299.00, 0, NULL, '2026-09-02 00:03:10', '2026-09-02 00:03:10');

-- ----------------------------
-- 体检记录明细表
-- ----------------------------
DROP TABLE IF EXISTS `exam_appointment_item`;
CREATE TABLE `exam_appointment_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '体检记录明细ID',
  `appointment_id` bigint NOT NULL COMMENT '体检记录ID',
  `exam_item_id` bigint NOT NULL COMMENT '体检项目ID',
  `item_name` varchar(100) NOT NULL COMMENT '项目名称快照',
  `result_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '数值型结果',
  `result_unit` varchar(50) NULL DEFAULT NULL COMMENT '结果单位',
  `result_text` varchar(1000) NULL DEFAULT NULL COMMENT '文本型结果',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待检查 1正常 2异常 3未完成',
  `abnormal` tinyint NOT NULL DEFAULT 0 COMMENT '是否异常：0正常 1异常',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '体检记录明细表' ROW_FORMAT = Dynamic;

INSERT INTO `exam_appointment_item` VALUES (12, 3, 5, '空腹血糖', 6.10, 'mmol/L', NULL, 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (13, 3, 6, '血脂', 6.20, 'mmol/L', NULL, 2, 1, '总胆固醇偏高', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (14, 3, 7, '心电图', NULL, NULL, '窦性心律，未见明显异常', 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (15, 3, 10, '胸部CT', NULL, NULL, '双肺未见明显异常', 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (16, 5, 1, '血常规', NULL, NULL, '红细胞、白细胞、血小板均正常', 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (17, 5, 2, '尿常规', NULL, NULL, '未见明显异常', 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (18, 5, 3, '肝功能', 25.00, 'U/L', NULL, 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (19, 5, 4, '肾功能', 78.00, 'μmol/L', NULL, 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (20, 5, 5, '空腹血糖', 6.80, 'mmol/L', NULL, 2, 1, '空腹血糖偏高', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (21, 5, 6, '血脂', 5.90, 'mmol/L', NULL, 2, 1, '总胆固醇偏高', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (22, 5, 7, '心电图', NULL, NULL, '窦性心律，未见明显异常', 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (23, 5, 8, '腹部彩超', NULL, NULL, '肝胆胰脾未见明显异常', 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (24, 5, 9, '骨密度检测', -2.10, 'T值', NULL, 2, 1, '骨量减少', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (25, 5, 10, '胸部CT', NULL, NULL, '双肺未见明显异常', 1, 0, '正常', '2026-08-30 16:14:48', '2026-08-30 16:14:48');
INSERT INTO `exam_appointment_item` VALUES (26, 3, 5, '空腹血糖', 6.10, 'mmol/L', NULL, 1, 0, '正常', '2026-08-30 16:16:03', '2026-08-30 16:16:03');
INSERT INTO `exam_appointment_item` VALUES (27, 3, 6, '血脂', 6.20, 'mmol/L', NULL, 2, 1, '总胆固醇偏高', '2026-08-30 16:16:03', '2026-08-30 16:16:03');
INSERT INTO `exam_appointment_item` VALUES (28, 3, 7, '心电图', NULL, NULL, '窦性心律，未见明显异常', 1, 0, '正常', '2026-08-30 16:16:03', '2026-08-30 16:16:03');
INSERT INTO `exam_appointment_item` VALUES (29, 3, 10, '胸部CT', NULL, NULL, '双肺未见明显异常', 1, 0, '正常', '2026-08-30 16:16:03', '2026-08-30 16:16:03');
INSERT INTO `exam_appointment_item` VALUES (32, 8, 1, '血常规', NULL, NULL, NULL, 0, 0, NULL, '2026-09-02 00:03:10', '2026-09-02 00:03:10');
INSERT INTO `exam_appointment_item` VALUES (33, 8, 4, '肾功能', NULL, NULL, NULL, 0, 0, NULL, '2026-09-02 00:03:10', '2026-09-02 00:03:10');
INSERT INTO `exam_appointment_item` VALUES (34, 8, 9, '骨密度检测', NULL, NULL, NULL, 0, 0, NULL, '2026-09-02 00:03:10', '2026-09-02 00:03:10');
