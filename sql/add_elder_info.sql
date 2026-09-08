-- =============================================================
-- 老人信息增强：家属信息 + 收费项目 + 月度账单（+ 菜单改名“老人信息”）
-- 用法：在 elder 库执行（可重复执行：先 DROP 再建）
-- =============================================================
USE `elder`;

-- 老人家属表
DROP TABLE IF EXISTS `elder_family`;
CREATE TABLE `elder_family` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '家属ID',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `name` varchar(32) NOT NULL COMMENT '家属姓名',
  `relation` varchar(32) NOT NULL DEFAULT '家属' COMMENT '关系：儿子/女儿/配偶/其他',
  `phone` varchar(20) NULL DEFAULT NULL COMMENT '联系电话',
  `remark` varchar(200) NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family_elder` (`elder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '老人家属表' ROW_FORMAT = Dynamic;

-- 老人收费项目表（月度账单的手动部分）
DROP TABLE IF EXISTS `elder_charge`;
CREATE TABLE `elder_charge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收费ID',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `item_name` varchar(64) NOT NULL COMMENT '收费项目名称',
  `amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '金额(元)',
  `charge_type` tinyint NOT NULL DEFAULT 0 COMMENT '类型：0一次性 1月度(每月重复计)',
  `charge_month` varchar(7) NOT NULL COMMENT '归属月份 yyyy-MM（月度项为该月份起每月计入）',
  `remark` varchar(200) NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_charge_elder_month` (`elder_id`, `charge_month`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '老人收费项目表' ROW_FORMAT = Dynamic;

-- 菜单改名：老人管理 -> 老人信息（顶级菜单 /elder）
UPDATE `permission` SET `name` = '老人信息' WHERE `path` = '/elder' AND `deleted` = 0;
