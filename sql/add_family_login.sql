-- 家属登录支持：elder_family 增加密码列(BCrypt 存储)
-- 用法：在 elder 库执行，可重复执行(幂等)
USE `elder`;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA='elder' AND TABLE_NAME='elder_family' AND COLUMN_NAME='password');
SET @sql := IF(@exist = 0,
  'ALTER TABLE elder_family ADD COLUMN password varchar(100) NOT NULL DEFAULT '''' COMMENT ''登录密码(BCrypt)，空则默认123456'' AFTER phone',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
