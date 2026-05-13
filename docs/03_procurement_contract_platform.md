# 面向公共采购场景的采购审批与合同归档管理系统：子项目文档

## 1. 项目定位

本项目用于模拟大型组织在公共采购、内部采购申请、合同归档和付款节点管理中的流程化管理需求。

项目重点不是实现完整 ERP，而是实现采购申请、审批状态流转、供应商管理、合同归档、付款节点、权限控制和操作留痕。

---

## 2. 数据来源说明

本项目不使用真实企业采购数据和真实合同内容。

数据来源包括：

- 公开采购公告中的字段结构参考
- 自行构造的模拟采购申请
- 自行构造的模拟供应商
- 自行构造的模拟合同
- 自行构造的模拟付款节点

所有模拟数据应使用 demo、sample、mock 等标识，避免被误认为真实业务数据。

---

## 3. 业务目标

系统需要实现：

1. 普通员工提交采购申请。
2. 系统根据采购金额生成审批链路。
3. 部门负责人、财务负责人、采购管理员按权限审批。
4. 审批通过后可以归档合同。
5. 合同可以关联供应商和付款节点。
6. 全流程保留审批记录和操作日志。
7. 首页或采购看板展示采购统计数据。

---

## 4. 用户角色

| 角色 | 权限 |
|---|---|
| 普通员工 | 创建采购申请、查看本人申请、撤回草稿或待审批申请 |
| 部门负责人 | 审批本部门采购申请 |
| 财务负责人 | 审批涉及预算和付款的采购申请 |
| 采购管理员 | 采购终审、供应商管理、合同归档 |
| 系统管理员 | 查看和管理全部采购数据 |

---

## 5. 核心业务流程

## 5.1 采购申请流程

```text
新建草稿
  ↓
提交审批
  ↓
根据金额判断审批链路
  ↓
部门负责人审批
  ↓
财务负责人审批，可选
  ↓
采购管理员审批，可选
  ↓
审批通过
  ↓
合同归档
```

---

## 5.2 金额驱动审批规则

| 采购金额 | 审批链路 |
|---|---|
| 1 万元以下 | 部门负责人 |
| 1 万元到 10 万元 | 部门负责人 → 财务负责人 |
| 10 万元以上 | 部门负责人 → 财务负责人 → 采购管理员 |

---

## 5.3 状态设计

采购申请状态：

```text
DRAFT              草稿
PENDING_DEPT       待部门审批
PENDING_FINANCE    待财务审批
PENDING_PROCURE    待采购审批
APPROVED           已通过
REJECTED           已驳回
WITHDRAWN          已撤回
ARCHIVED           已归档
```

审批动作：

```text
SUBMIT             提交
APPROVE            通过
REJECT             驳回
WITHDRAW           撤回
ARCHIVE            归档
```

---

## 6. 功能模块

## 6.1 采购申请管理

### 字段设计

- 采购标题
- 采购类型
- 预算金额
- 申请部门
- 申请人
- 采购理由
- 期望交付日期
- 当前状态
- 创建时间
- 更新时间

### 功能点

- 新建采购申请
- 编辑草稿
- 提交审批
- 查看申请详情
- 撤回申请
- 查看我的申请
- 查看全部申请，管理员权限

---

## 6.2 审批管理

### 功能点

- 查看待我审批列表
- 审批通过
- 审批驳回
- 驳回原因必填
- 查看审批时间线
- 查看审批记录

### 审批记录字段

- 采购申请 ID
- 审批人 ID
- 审批层级
- 审批动作
- 审批意见
- 审批时间

---

## 6.3 供应商管理

### 字段设计

- 供应商名称
- 联系人
- 联系电话
- 模拟统一社会信用代码
- 供应类别
- 评级
- 状态

### 功能点

- 新增供应商
- 编辑供应商
- 禁用供应商
- 查询供应商
- 按类别筛选
- 按评级筛选

---

## 6.4 合同归档

### 字段设计

- 合同编号
- 合同名称
- 关联采购申请
- 供应商
- 合同金额
- 签订日期
- 到期日期
- 合同附件
- 合同状态

### 功能点

- 新建合同归档
- 关联已通过采购申请
- 上传合同附件
- 查看合同详情
- 合同状态管理
- 合同到期提醒，可选

---

## 6.5 付款节点管理

### 字段设计

- 合同 ID
- 节点名称
- 节点金额
- 付款比例
- 计划付款日期
- 付款状态

### 节点示例

- 预付款
- 到货款
- 验收款
- 尾款

### 功能点

- 新增付款节点
- 编辑付款节点
- 标记已付款
- 查看付款进度

---

## 6.6 操作日志

采购模块关键操作必须记录操作日志：

- 新建采购申请
- 编辑采购申请
- 提交审批
- 审批通过
- 审批驳回
- 撤回申请
- 新增供应商
- 修改供应商
- 上传合同
- 标记付款节点

---

## 6.7 采购统计看板

统计指标：

- 本月采购申请数量
- 待审批数量
- 已通过数量
- 已驳回数量
- 合同总金额
- 供应商数量
- 按采购类型统计
- 按申请状态统计
- 最近审批动态

---

## 7. 技术架构

### 7.1 后端模块结构

```text
procurement
├── controller
│   ├── PurchaseRequestController.java
│   ├── ApprovalController.java
│   ├── SupplierController.java
│   ├── ContractController.java
│   └── PaymentNodeController.java
├── service
│   ├── PurchaseRequestService.java
│   ├── ApprovalService.java
│   ├── SupplierService.java
│   ├── ContractService.java
│   └── PaymentNodeService.java
├── mapper
├── entity
├── dto
├── vo
└── enums
```

### 7.2 前端模块结构

```text
views/procurement
├── PurchaseList.vue
├── PurchaseForm.vue
├── PurchaseDetail.vue
├── MyApproval.vue
├── SupplierList.vue
├── ContractList.vue
├── ContractDetail.vue
└── ProcurementDashboard.vue
```

---

## 8. 数据库设计

```sql
CREATE TABLE proc_purchase_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(50),
    amount DECIMAL(12,2) NOT NULL,
    dept_id BIGINT,
    applicant_id BIGINT,
    reason TEXT,
    expected_date DATE,
    status VARCHAR(50),
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE proc_approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    approval_level VARCHAR(50),
    action VARCHAR(50),
    comment TEXT,
    created_at DATETIME
);

CREATE TABLE proc_supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(50),
    credit_code VARCHAR(100),
    category VARCHAR(100),
    rating VARCHAR(20),
    status VARCHAR(30),
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE proc_contract (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_no VARCHAR(100) NOT NULL,
    contract_name VARCHAR(200) NOT NULL,
    request_id BIGINT,
    supplier_id BIGINT,
    amount DECIMAL(12,2),
    sign_date DATE,
    expire_date DATE,
    file_url VARCHAR(500),
    status VARCHAR(50),
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE proc_payment_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    node_name VARCHAR(100),
    amount DECIMAL(12,2),
    ratio DECIMAL(5,2),
    planned_date DATE,
    paid_status VARCHAR(30),
    created_at DATETIME,
    updated_at DATETIME
);
```

---

## 9. 核心接口规划

### 9.1 采购申请接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/procurement/requests | 新建采购申请 |
| PUT | /api/procurement/requests/{id} | 编辑采购申请 |
| GET | /api/procurement/requests | 采购申请列表 |
| GET | /api/procurement/requests/my | 我的申请 |
| GET | /api/procurement/requests/{id} | 申请详情 |
| POST | /api/procurement/requests/{id}/submit | 提交审批 |
| POST | /api/procurement/requests/{id}/withdraw | 撤回申请 |

### 9.2 审批接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/procurement/approvals/todo | 待我审批 |
| POST | /api/procurement/approvals/{requestId}/approve | 审批通过 |
| POST | /api/procurement/approvals/{requestId}/reject | 审批驳回 |
| GET | /api/procurement/approvals/{requestId}/records | 审批记录 |

### 9.3 供应商接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/procurement/suppliers | 新增供应商 |
| PUT | /api/procurement/suppliers/{id} | 编辑供应商 |
| GET | /api/procurement/suppliers | 供应商列表 |
| GET | /api/procurement/suppliers/{id} | 供应商详情 |
| POST | /api/procurement/suppliers/{id}/disable | 禁用供应商 |

### 9.4 合同接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/procurement/contracts | 新建合同 |
| GET | /api/procurement/contracts | 合同列表 |
| GET | /api/procurement/contracts/{id} | 合同详情 |
| POST | /api/procurement/contracts/{id}/upload | 上传附件 |
| POST | /api/procurement/contracts/{id}/archive | 合同归档 |

### 9.5 付款节点接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/procurement/contracts/{contractId}/payment-nodes | 新增付款节点 |
| PUT | /api/procurement/payment-nodes/{id} | 编辑付款节点 |
| POST | /api/procurement/payment-nodes/{id}/paid | 标记已付款 |
| GET | /api/procurement/contracts/{contractId}/payment-nodes | 付款节点列表 |

---

## 10. 页面设计

### 10.1 采购申请列表页

包含：

- 标题搜索
- 状态筛选
- 类型筛选
- 时间范围筛选
- 新建申请按钮
- 表格分页
- 状态标签
- 查看详情入口

### 10.2 采购申请详情页

包含：

- 申请基础信息
- 当前状态
- 审批时间线
- 审批记录
- 操作按钮
- 关联合同信息

### 10.3 我的审批页

包含：

- 待审批列表
- 申请摘要
- 审批按钮
- 驳回原因弹窗

### 10.4 合同详情页

包含：

- 合同基础信息
- 供应商信息
- 关联采购申请
- 合同附件
- 付款节点表格
- 付款进度

---

## 11. 开发步骤

### 阶段 1：采购申请基础

- 创建采购申请表
- 实现申请 CRUD
- 实现我的申请列表
- 实现申请详情页

### 阶段 2：审批流程

- 实现金额判断规则
- 实现提交审批
- 实现通过和驳回
- 实现审批记录
- 实现审批时间线

### 阶段 3：供应商管理

- 创建供应商表
- 实现供应商 CRUD
- 实现供应商筛选
- 实现供应商状态管理

### 阶段 4：合同归档

- 创建合同表
- 实现合同新增
- 关联已通过采购申请
- 实现附件上传
- 实现合同详情

### 阶段 5：付款节点

- 创建付款节点表
- 实现节点 CRUD
- 实现付款状态变更
- 实现付款进度展示

### 阶段 6：统计看板

- 统计采购数量
- 统计合同金额
- 统计待审批数量
- 统计采购类型占比
- 展示最近审批动态

---

## 12. 验收标准

最低可运行版本应满足：

1. 普通员工可以创建并提交采购申请。
2. 系统可以根据金额进入不同审批节点。
3. 不同角色只能处理自己权限范围内的审批。
4. 驳回必须填写原因。
5. 审批记录完整可查。
6. 已通过申请可以创建合同。
7. 合同可以维护付款节点。
8. 关键操作可以写入操作日志。

---

## 13. 后续扩展

可扩展方向：

- 引入 Flowable 工作流引擎
- 增加采购预算控制
- 增加合同到期提醒
- 增加付款提醒
- 增加附件预览
- 增加审批消息通知
- 增加导出 Excel
- 增加采购风险规则
- 增加供应商评价机制
