-- ===================================================
-- 大型组织数字化办公与安全审计一体化平台
-- 全阶段初始化数据（Phase 1-6）
-- 适用于全新环境初始化
-- 执行方式：mysql -u root -p <database> < sql/init_data.sql
-- 幂等：全部使用 INSERT IGNORE，可重复执行
-- ===================================================

-- 一、初始化部门
INSERT IGNORE INTO sys_dept (id, dept_name, parent_id, leader_name, phone, sort_order) VALUES
(1, '综合管理部', 0, '李部长', '010-1001', 1),
(2, '财务部',     0, '王财务', '010-1002', 2),
(3, '采购部',     0, '赵采购', '010-1003', 3),
(4, '信息技术部', 0, '孙工',   '010-1004', 4),
(5, '安全审计部', 0, '陈审计', '010-1005', 5);

-- 二、初始化角色
INSERT IGNORE INTO sys_role (id, role_code, role_name, description, sort_order) VALUES
(1, 'admin',        '系统管理员', '拥有全部系统权限',           1),
(2, 'employee',     '普通员工',   '查询公开制度、提交采购申请', 2),
(3, 'dept_manager', '部门负责人', '审批本部门采购申请',         3),
(4, 'finance',      '财务负责人', '审批预算相关采购',           4),
(5, 'procurement',  '采购管理员', '采购终审、供应商管理',       5),
(6, 'auditor',      '安全审计员', '查看日志、处理告警',         6);

-- 三、初始化用户（密码均为 BCrypt 加密）
-- 明文: Admin@123456 -> BCrypt hash
INSERT IGNORE INTO sys_user (id, username, password, real_name, email, phone, dept_id, status) VALUES
(1, 'admin',        '$2a$10$A2GT3aVefLN1R0jFY8BBpeRwogIwd.o8Wp1/KKFZbhYzBI7VP7HOy', '系统管理员', 'admin@platform.local', '13800000001', 1, 1),
(2, 'employee',     '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '张小明',     'employee@platform.local',  '13800000002', 1, 1),
(3, 'dept_manager', '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '李部长',     'dept@platform.local',      '13800000003', 1, 1),
(4, 'finance',      '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '王财务',     'finance@platform.local',   '13800000004', 2, 1),
(5, 'procurement',  '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '赵采购',     'procurement@platform.local','13800000005', 3, 1),
(6, 'auditor',      '$2a$10$WfJltQmPSv3SdbbxaPypEOaCvtGawU59AAEhcK0mChvwnHBEcBK76', '陈审计',     'auditor@platform.local',   '13800000006', 5, 1);

-- 四、用户角色关联
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6);

-- ===================================================
-- 五、初始化菜单（全部模块，Phase 1-6）
-- ===================================================

-- 一级目录（ID 1-5）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(1, 0, '工作台',   'menu',      '/dashboard',   'dashboard/DashboardView',   'Monitor',      NULL,              1, 1),
(2, 0, '知识库',   'directory', '/knowledge',   NULL,                       'Document',     NULL,              2, 1),
(3, 0, '采购管理', 'directory', '/procurement',  NULL,                       'ShoppingCart', NULL,              3, 1),
(4, 0, '安全审计', 'directory', '/audit',        NULL,                       'Lock',         NULL,              4, 1),
(5, 0, '系统管理', 'directory', '/system',       NULL,                       'Setting',      NULL,              5, 1);

-- 系统管理子菜单（ID 6-9）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(6, 5, '用户管理', 'menu', '/system/users',  'system/UserList', 'User',   'sys:user:list',   1, 1),
(7, 5, '角色管理', 'menu', '/system/roles',  'system/RoleList', 'Avatar', 'sys:role:list',   2, 1),
(8, 5, '部门管理', 'menu', '/system/depts',  'system/DeptList', 'Grid',   'sys:dept:list',   3, 1),
(9, 5, '菜单管理', 'menu', '/system/menus',  'system/MenuList', 'Menu',   'sys:menu:list',   4, 1);

-- 安全审计基础子菜单（ID 10-11，Phase 3）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(10, 4, '登录日志', 'menu', '/audit/login-logs',     'audit/LoginLogList',      'Notebook', 'audit:log', 1, 1),
(11, 4, '操作日志', 'menu', '/audit/operation-logs', 'audit/OperationLogList',  'Tickets',  'audit:log', 2, 1);

-- 知识库子菜单（ID 30-33，Phase 5）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(30, 2, '知识库首页', 'menu', '/knowledge',          'knowledge/KnowledgeHome', 'Document',     'knowledge:home',    1, 1),
(31, 2, '文档管理',   'menu', '/knowledge/documents','knowledge/DocumentList',  'Folder',       'knowledge:doc:list', 2, 1),
(32, 2, '智能问答',   'menu', '/knowledge/qa',       'knowledge/QaChat',        'ChatDotRound', 'knowledge:qa:ask',  3, 1),
(33, 2, '问答日志',   'menu', '/knowledge/qa/logs',  'knowledge/QaLogList',     'Tickets',      'knowledge:qa:log',  4, 1);

-- 安全审计增强子菜单（ID 40-42，Phase 6）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(40, 4, '安全看板', 'menu', '/audit/dashboard', 'audit/SecurityDashboard', 'DataAnalysis', 'audit:dashboard', 0, 1),
(41, 4, '安全告警', 'menu', '/audit/alerts',    'audit/AlertList',         'Warning',      'audit:alert',     3, 1),
(42, 4, 'IP黑名单', 'menu', '/audit/blacklist', 'audit/BlacklistList',     'VideoCamera',  'audit:blacklist', 4, 1);

-- 采购管理子菜单（ID 50-54，Phase 4）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(50, 3, '采购申请',   'menu', '/procurement/requests',   'procurement/RequestList',      'Document', 'procurement:request:list',    1, 1),
(51, 3, '待办审批',   'menu', '/procurement/approvals',  'procurement/ApprovalPending',  'Tickets',  'procurement:approval:pending', 2, 1),
(52, 3, '供应商管理', 'menu', '/procurement/suppliers',  'procurement/SupplierList',     'Avatar',   'procurement:supplier:list',    3, 1),
(53, 3, '合同管理',   'menu', '/procurement/contracts',  'procurement/ContractList',     'Notebook', 'procurement:contract:list',    4, 1),
(54, 3, '付款管理',   'menu', '/procurement/payments',   'procurement/PaymentManagement','Grid',     'procurement:payment:list',     5, 1);

-- ===================================================
-- 六、角色-菜单授权（通过 role_code 查询角色，INSERT IGNORE 保证幂等）
-- ===================================================

-- admin：全部菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'admin';

-- employee：工作台 + 知识库（首页/文档/问答）+ 采购申请
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'employee'
  AND m.id IN (1, 30, 31, 32, 50);

-- dept_manager：工作台 + 采购申请 + 待办审批
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'dept_manager'
  AND m.id IN (1, 50, 51);

-- finance：工作台 + 采购申请 + 待办审批
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'finance'
  AND m.id IN (1, 50, 51);

-- procurement：工作台 + 采购模块全部子菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'procurement'
  AND m.id IN (1, 50, 51, 52, 53, 54);

-- auditor：工作台 + 安全审计模块全部子菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m
WHERE r.role_code = 'auditor'
  AND m.id IN (1, 4, 10, 11, 40, 41, 42);
