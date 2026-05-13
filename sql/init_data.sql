-- ===================================================
-- 大型组织数字化办公与安全审计一体化平台
-- 第二阶段：认证与权限模块 - 初始化数据
-- ===================================================

-- 一、初始化部门
INSERT INTO sys_dept (id, dept_name, parent_id, leader_name, phone, sort_order) VALUES
(1, '综合管理部', 0, '李部长', '010-1001', 1),
(2, '财务部',     0, '王财务', '010-1002', 2),
(3, '采购部',     0, '赵采购', '010-1003', 3),
(4, '信息技术部', 0, '孙工',   '010-1004', 4),
(5, '安全审计部', 0, '陈审计', '010-1005', 5);

-- 二、初始化角色
INSERT INTO sys_role (id, role_code, role_name, description, sort_order) VALUES
(1, 'admin',        '系统管理员', '拥有全部系统权限',           1),
(2, 'employee',     '普通员工',   '查询公开制度、提交采购申请', 2),
(3, 'dept_manager', '部门负责人', '审批本部门采购申请',         3),
(4, 'finance',      '财务负责人', '审批预算相关采购',           4),
(5, 'procurement',  '采购管理员', '采购终审、供应商管理',       5),
(6, 'auditor',      '安全审计员', '查看日志、处理告警',         6);

-- 三、初始化用户（密码均为 BCrypt 加密）
-- 明文: Admin@123456 -> BCrypt hash
INSERT INTO sys_user (id, username, password, real_name, email, phone, dept_id, status) VALUES
(1, 'admin',        '$2a$10$A2GT3aVefLN1R0jFY8BBpeRwogIwd.o8Wp1/KKFZbhYzBI7VP7HOy', '系统管理员', 'admin@platform.local', '13800000001', 1, 1),
(2, 'employee',     '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '张小明',     'employee@platform.local',  '13800000002', 1, 1),
(3, 'dept_manager', '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '李部长',     'dept@platform.local',      '13800000003', 1, 1),
(4, 'finance',      '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '王财务',     'finance@platform.local',   '13800000004', 2, 1),
(5, 'procurement',  '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '赵采购',     'procurement@platform.local','13800000005', 3, 1),
(6, 'auditor',      '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '陈审计',     'auditor@platform.local',   '13800000006', 5, 1);

-- 四、用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6);

-- 五、初始化菜单
-- 一级目录
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(1,  0, '工作台',   'menu',  '/dashboard',   'dashboard/DashboardView',   'Monitor',      NULL,              1, 1),
(2,  0, '知识库',   'directory', '/knowledge',  NULL,                       'Document',     NULL,              2, 1),
(3,  0, '采购管理', 'directory', '/procurement', NULL,                       'ShoppingCart', NULL,              3, 1),
(4,  0, '安全审计', 'directory', '/audit',       NULL,                       'Lock',         NULL,              4, 1),
(5,  0, '系统管理', 'directory', '/system',      NULL,                       'Setting',      NULL,              5, 1);

-- 二级菜单 - 系统管理
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(6, 5, '用户管理', 'menu', '/system/users', 'system/UserList', 'User',   'sys:user:list',   1, 1),
(7, 5, '角色管理', 'menu', '/system/roles', 'system/RoleList', 'Avatar', 'sys:role:list',   2, 1),
(8, 5, '部门管理', 'menu', '/system/depts', 'system/DeptList', 'Grid', 'sys:dept:list',   3, 1),
(9, 5, '菜单管理', 'menu', '/system/menus', 'system/MenuList', 'Menu',   'sys:menu:list',   4, 1);

-- 六、admin 角色菜单权限（全部菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 七、employee / dept_manager / finance / procurement / auditor 仅分配工作台
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(6, 1);
