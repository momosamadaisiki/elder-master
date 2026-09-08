-- =============================================================
-- 探视/访客登记模块 + 数据大屏辅助（新增表 & 管理员菜单授权）
-- 用法：在 elder 库执行本文件（幂等考虑：先删后建）
-- =============================================================
USE `elder`;

-- 探视/访客流水表
DROP TABLE IF EXISTS `visit_record`;
CREATE TABLE `visit_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '探视/访客记录ID',
  `elder_id` bigint NOT NULL COMMENT '被访老人ID',
  `visitor_name` varchar(32) NOT NULL COMMENT '访客姓名',
  `visitor_phone` varchar(20) NULL DEFAULT NULL COMMENT '访客手机号',
  `relation` varchar(32) NOT NULL DEFAULT '家属' COMMENT '关系：家属/朋友/维修/其他',
  `id_card_no` varchar(18) NULL DEFAULT NULL COMMENT '身份证号(选填)',
  `visit_date` date NOT NULL COMMENT '探视日期',
  `start_time` varchar(10) NULL DEFAULT NULL COMMENT '预约开始时间 HH:mm',
  `end_time` varchar(10) NULL DEFAULT NULL COMMENT '预约结束时间 HH:mm',
  `purpose` varchar(200) NULL DEFAULT NULL COMMENT '探视事由',
  `apply_way` tinyint NOT NULL DEFAULT 0 COMMENT '申请方式：0线上申请 1现场登记',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待审批 1已通过/在访 2已拒绝 3已完成(已离院) 4已取消 5已过期',
  `approve_user_id` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `arrive_time` datetime NULL DEFAULT NULL COMMENT '实际到达时间',
  `leave_time` datetime NULL DEFAULT NULL COMMENT '离院时间',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注/审批意见',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_visit_elder` (`elder_id`) USING BTREE,
  INDEX `idx_visit_date_status` (`visit_date`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '探视/访客登记表' ROW_FORMAT = Dynamic;

-- 管理员(role_id=1)菜单授权：护理管理(parent_id=3) 下新增“探视访客”菜单 /visit
-- 注意：先检查是否已存在，避免重复执行报错
INSERT INTO `permission` (`parent_id`, `name`, `type`, `path`, `permission_value`, `icon`, `sort`, `status`, `deleted`, `create_time`, `update_time`)
SELECT 3, '探视访客', 1, '/visit', NULL, 'Calendar', 5, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `permission` WHERE `path` = '/visit' AND `deleted` = 0);

SET @visit_perm_id = (SELECT `id` FROM `permission` WHERE `path` = '/visit' AND `deleted` = 0 LIMIT 1);

INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`)
SELECT 1, @visit_perm_id, NOW(), NOW()
WHERE @visit_perm_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `role_permission` WHERE `role_id` = 1 AND `permission_id` = @visit_perm_id);
