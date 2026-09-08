-- 体检登记菜单：挂在“体检管理(顶级,id按name查)”下，授管理员
USE `elder`;

INSERT INTO `permission` (`parent_id`,`name`,`type`,`path`,`icon`,`sort`,`status`,`deleted`,`create_time`,`update_time`)
SELECT p.`id`,'体检登记',1,'/exam-appointment','Document',3,1,0,NOW(),NOW()
FROM `permission` p
WHERE p.`name`='体检管理' AND p.`parent_id`=0 AND p.`deleted`=0
  AND NOT EXISTS (SELECT 1 FROM `permission` x WHERE x.`path`='/exam-appointment' AND x.`deleted`=0);

INSERT INTO `role_permission` (`role_id`,`permission_id`,`create_time`,`update_time`)
SELECT 1, p.`id`, NOW(), NOW()
FROM `permission` p
WHERE p.`path`='/exam-appointment' AND p.`deleted`=0
  AND NOT EXISTS (SELECT 1 FROM `role_permission` rp WHERE rp.`role_id`=1 AND rp.`permission_id`=p.`id`);
