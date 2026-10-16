-- 学习抽记卡模块 SQL
-- Study flashcard module SQL

-- 首次执行时创建表，避免重复执行时误删已有学习数据
-- Create tables only when absent to avoid deleting existing study data on reapply

-- 卡组表
-- Flashcard book table
CREATE TABLE IF NOT EXISTS `study_flashcard_book` (
  `book_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '卡组ID',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `book_name` varchar(100) NOT NULL COMMENT '卡组名称',
  `book_description` varchar(500) DEFAULT '' COMMENT '卡组描述',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT sysdate() COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `uk_study_flashcard_book_user_name` (`user_id`, `book_name`),
  KEY `idx_study_flashcard_book_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习抽记卡卡组表';

-- 卡片表
-- Flashcard item table
CREATE TABLE IF NOT EXISTS `study_flashcard_item` (
  `item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '卡片ID',
  `book_id` bigint(20) NOT NULL COMMENT '卡组ID',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `front_content` text NOT NULL COMMENT '正面内容',
  `back_content` text NOT NULL COMMENT '背面内容',
  `quadrant_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '象限类型（1-4）',
  `review_count` int(11) NOT NULL DEFAULT 0 COMMENT '复习次数',
  `difficulty_score` tinyint(4) DEFAULT NULL COMMENT '难度评分',
  `last_user_answer` text COMMENT '最近一次输入答案',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT sysdate() COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`item_id`),
  KEY `idx_study_flashcard_item_user_book` (`user_id`, `book_id`),
  KEY `idx_study_flashcard_item_user_book_quadrant` (`user_id`, `book_id`, `quadrant_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习抽记卡卡片表';

-- 学习菜单
-- Study menu
INSERT INTO `sys_menu`
SELECT * FROM (
    SELECT '2000', '学习', '0', '5', 'study', '', '', '', 1, 0, 'M', '0', '0', '', 'edit', 'admin', sysdate(), '', NULL, '学习目录'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = '2000');

INSERT INTO `sys_menu`
SELECT * FROM (
    SELECT '2001', '抽记卡', '2000', '1', 'flashcard', 'study/flashcard/index', '', '', 1, 0, 'C', '0', '0', 'study:flashcard:list', 'form', 'admin', sysdate(), '', NULL, '抽记卡菜单'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = '2001');

INSERT INTO `sys_menu`
SELECT * FROM (
    SELECT '2002', '抽记卡查询', '2001', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:query', '#', 'admin', sysdate(), '', NULL, ''
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = '2002');

INSERT INTO `sys_menu`
SELECT * FROM (
    SELECT '2003', '抽记卡新增', '2001', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:add', '#', 'admin', sysdate(), '', NULL, ''
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = '2003');

INSERT INTO `sys_menu`
SELECT * FROM (
    SELECT '2004', '抽记卡修改', '2001', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:edit', '#', 'admin', sysdate(), '', NULL, ''
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = '2004');

INSERT INTO `sys_menu`
SELECT * FROM (
    SELECT '2005', '抽记卡删除', '2001', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:remove', '#', 'admin', sysdate(), '', NULL, ''
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = '2005');

INSERT INTO `sys_menu`
SELECT * FROM (
    SELECT '2006', '抽记卡导入', '2001', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:import', '#', 'admin', sysdate(), '', NULL, ''
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = '2006');

INSERT INTO `sys_menu`
SELECT * FROM (
    SELECT '2007', '抽记卡练习', '2001', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:practice', '#', 'admin', sysdate(), '', NULL, ''
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = '2007');

INSERT INTO `sys_menu`
SELECT * FROM (
    SELECT '2008', '抽记卡管理', '2000', '2', 'flashcard-manage', 'study/flashcard/manage', '', '', 1, 0, 'C', '0', '0', 'study:flashcard:list', 'form', 'admin', sysdate(), '', NULL, '抽记卡管理菜单'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = '2008');

-- 回滚提示：删除上述 study_flashcard_* 表和 2000-2007 菜单即可
-- Rollback note: delete study_flashcard_* tables and menu rows 2000-2007
