-- ===================================================
-- 第四阶段：采购审批与合同归档模块 - 增量迁移脚本
-- 适用于已有第三阶段数据的数据库升级
-- 执行方式：mysql -u root -p large_org_platform < sql/migration/004_procurement.sql
-- ===================================================

-- 一、创建采购申请表（IF NOT EXISTS 保证幂等）
CREATE TABLE IF NOT EXISTS procurement_request (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    request_no    VARCHAR(30)  NOT NULL UNIQUE COMMENT '申请单号',
    title         VARCHAR(200) NOT NULL COMMENT '采购标题',
    description   TEXT         COMMENT '采购说明',
    amount        DECIMAL(12,2) NOT NULL COMMENT '采购金额',
    category      VARCHAR(50)  COMMENT '采购品类',
    status        VARCHAR(20)  NOT NULL DEFAULT 'draft' COMMENT 'draft/pending/approved/rejected/withdrawn',
    applicant_id  BIGINT       NOT NULL COMMENT '申请人ID',
    dept_id       BIGINT       COMMENT '申请部门ID',
    current_step  INT          NOT NULL DEFAULT 0 COMMENT '当前审批步骤',
    total_steps   INT          NOT NULL DEFAULT 0 COMMENT '总审批步骤数',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_request_no (request_no),
    INDEX idx_status (status),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请表';

-- 二、创建审批记录表
CREATE TABLE IF NOT EXISTS procurement_approval (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    request_id    BIGINT       NOT NULL COMMENT '关联申请ID',
    step_order    INT          NOT NULL COMMENT '审批步骤序号',
    expected_role VARCHAR(50)  NOT NULL COMMENT '预期审批角色',
    approver_id   BIGINT       COMMENT '实际审批人ID',
    status        VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
    comment       VARCHAR(500) COMMENT '审批意见',
    approved_at   DATETIME     COMMENT '审批时间',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_request_id (request_id),
    INDEX idx_status (status),
    INDEX idx_expected_role (expected_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 三、创建供应商表
CREATE TABLE IF NOT EXISTS supplier (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    supplier_no    VARCHAR(30)  NOT NULL UNIQUE COMMENT '供应商编号',
    name           VARCHAR(200) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50)  COMMENT '联系人',
    contact_phone  VARCHAR(30)  COMMENT '联系电话',
    email          VARCHAR(100) COMMENT '邮箱',
    address        VARCHAR(300) COMMENT '地址',
    qualification  VARCHAR(500) COMMENT '资质描述',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1=正常 0=停用',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier_no (supplier_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 四、创建合同表
CREATE TABLE IF NOT EXISTS contract (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    contract_no   VARCHAR(30)   NOT NULL UNIQUE COMMENT '合同编号',
    request_id    BIGINT        COMMENT '关联采购申请ID',
    supplier_id   BIGINT        COMMENT '关联供应商ID',
    title         VARCHAR(200)  NOT NULL COMMENT '合同标题',
    amount        DECIMAL(12,2) NOT NULL COMMENT '合同金额',
    signed_date   DATE          COMMENT '签订日期',
    expiry_date   DATE          COMMENT '到期日期',
    status        VARCHAR(20)   NOT NULL DEFAULT 'active' COMMENT 'active/completed/terminated',
    created_at    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_contract_no (contract_no),
    INDEX idx_request_id (request_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- 五、创建付款节点表
CREATE TABLE IF NOT EXISTS payment_node (
    id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
    contract_id  BIGINT        NOT NULL COMMENT '关联合同ID',
    node_name    VARCHAR(100)  NOT NULL COMMENT '节点名称',
    amount       DECIMAL(12,2) NOT NULL COMMENT '付款金额',
    ratio        DECIMAL(5,2)  COMMENT '付款比例',
    planned_date DATE          COMMENT '计划付款日期',
    actual_date  DATE          COMMENT '实际付款日期',
    status       VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT 'pending/paid',
    created_at   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_contract_id (contract_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款节点表';

-- 六、新增采购管理子菜单（INSERT IGNORE 保证幂等，ID 范围 20-24）
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(20, 3, '采购申请',   'menu', '/procurement/requests',    'procurement/RequestList',      'Document', 'procurement:request:list',    1, 1),
(21, 3, '待办审批',   'menu', '/procurement/approvals',   'procurement/ApprovalPending',  'Tickets',  'procurement:approval:pending', 2, 1),
(22, 3, '供应商管理', 'menu', '/procurement/suppliers',   'procurement/SupplierList',     'Avatar',   'procurement:supplier:list',    3, 1),
(23, 3, '合同管理',   'menu', '/procurement/contracts',   'procurement/ContractList',     'Notebook', 'procurement:contract:list',    4, 1),
(24, 3, '付款管理',   'menu', '/procurement/payments',    'procurement/PaymentManagement','Grid',     'procurement:payment:list',     5, 1);

-- 七、admin 角色获得全部采购子菜单（INSERT IGNORE 保证幂等）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 20),
(1, 21),
(1, 22),
(1, 23),
(1, 24);

-- 八、procurement 角色获得全部采购子菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(5, 20),
(5, 21),
(5, 22),
(5, 23),
(5, 24);

-- 九、employee 角色获得采购申请菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 20);

-- 十、dept_manager 角色获得采购申请 + 待办审批菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(3, 20),
(3, 21);

-- 十一、finance 角色获得采购申请 + 待办审批菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(4, 20),
(4, 21);
