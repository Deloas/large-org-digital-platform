# 接口清单

> 完整 OpenAPI 文档请启动后端后访问：http://localhost:18080/doc.html

---

## 1. Auth 认证模块

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/api/auth/login` | 无 | 用户登录，返回 token 和用户信息 |
| POST | `/api/auth/logout` | 登录即可 | 退出登录 |
| GET | `/api/auth/me` | 登录即可 | 获取当前用户信息、角色、权限 |

---

## 2. System 系统管理模块

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/system/users` | sys:user:list | 用户分页列表（支持 keyword/status/deptId 筛选） |
| GET | `/api/system/users/{id}` | sys:user:list | 用户详情 |
| POST | `/api/system/users` | sys:user:create | 新增用户 |
| PUT | `/api/system/users/{id}` | sys:user:update | 修改用户 |
| PUT | `/api/system/users/{id}/status` | sys:user:update | 启用/禁用用户 |
| PUT | `/api/system/users/{id}/reset-password` | sys:user:update | 重置密码 |
| GET | `/api/system/roles` | sys:role:list | 角色列表 |
| GET | `/api/system/roles/{id}` | sys:role:list | 角色详情 |
| POST | `/api/system/roles` | sys:role:create | 新增角色 |
| PUT | `/api/system/roles/{id}` | sys:role:update | 修改角色 |
| DELETE | `/api/system/roles/{id}` | sys:role:delete | 删除角色 |
| GET | `/api/system/roles/{id}/menus` | sys:role:list | 获取角色菜单 |
| PUT | `/api/system/roles/{id}/menus` | sys:role:update | 分配角色权限 |
| GET | `/api/system/depts` | sys:dept:list | 部门列表 |
| POST | `/api/system/depts` | sys:dept:create | 新增部门 |
| PUT | `/api/system/depts/{id}` | sys:dept:update | 修改部门 |
| DELETE | `/api/system/depts/{id}` | sys:dept:delete | 删除部门 |
| GET | `/api/system/menus` | sys:menu:list | 菜单树 |
| POST | `/api/system/menus` | sys:menu:create | 新增菜单 |
| PUT | `/api/system/menus/{id}` | sys:menu:update | 修改菜单 |
| DELETE | `/api/system/menus/{id}` | sys:menu:delete | 删除菜单 |

---

## 3. Audit 审计日志模块

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/audit/login-logs` | audit:log | 登录日志分页查询（username/status/ip/时间范围） |
| GET | `/api/audit/login-logs/{id}` | audit:log | 登录日志详情 |
| GET | `/api/audit/operation-logs` | audit:log | 操作日志分页查询（username/module/action/requestPath/result/时间范围） |
| GET | `/api/audit/operation-logs/{id}` | audit:log | 操作日志详情 |

---

## 4. Procurement 采购审批模块

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/procurement/requests` | procurement:request:list | 采购申请分页列表 |
| GET | `/api/procurement/requests/{id}` | procurement:request:list | 采购申请详情（含审批历史） |
| POST | `/api/procurement/requests` | procurement:request:list | 创建采购申请 |
| PUT | `/api/procurement/requests/{id}` | procurement:request:list | 修改采购申请 |
| POST | `/api/procurement/requests/{id}/submit` | procurement:request:list | 提交申请（进入审批流） |
| POST | `/api/procurement/requests/{id}/withdraw` | procurement:request:list | 撤回申请 |
| GET | `/api/procurement/approvals` | procurement:approval:pending | 待办审批列表 |
| POST | `/api/procurement/requests/{id}/approve` | procurement:approval:pending | 审批操作 |
| GET | `/api/procurement/suppliers` | procurement:supplier:list | 供应商分页列表 |
| POST | `/api/procurement/suppliers` | procurement:supplier:list | 新增供应商 |
| PUT | `/api/procurement/suppliers/{id}` | procurement:supplier:list | 修改供应商 |
| DELETE | `/api/procurement/suppliers/{id}` | procurement:supplier:list | 删除供应商 |
| GET | `/api/procurement/contracts` | procurement:contract:list | 合同分页列表 |
| POST | `/api/procurement/contracts` | procurement:contract:list | 新建合同 |
| PUT | `/api/procurement/contracts/{id}` | procurement:contract:list | 修改合同 |
| DELETE | `/api/procurement/contracts/{id}` | procurement:contract:list | 删除合同 |
| GET | `/api/procurement/payments` | procurement:payment:list | 付款节点列表 |
| POST | `/api/procurement/payments` | procurement:payment:list | 创建付款记录 |

---

## 5. Knowledge 知识库模块

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/knowledge/documents` | knowledge:doc:list | 文档分页列表 |
| GET | `/api/knowledge/documents/{id}` | knowledge:doc:list | 文档详情 |
| POST | `/api/knowledge/documents` | knowledge:doc:upload | 上传文档（multipart） |
| DELETE | `/api/knowledge/documents/{id}` | knowledge:doc:delete | 删除文档 |
| GET | `/api/knowledge/documents/{id}/chunks` | knowledge:doc:list | 获取文档文本块 |
| POST | `/api/knowledge/qa/ask` | knowledge:qa:ask | RAG 智能问答 |
| GET | `/api/knowledge/qa/logs` | knowledge:qa:log | 问答日志分页列表 |
| GET | `/api/knowledge/qa/logs/{id}` | knowledge:qa:log | 问答日志详情 |

---

## 6. Security Audit 安全审计增强模块

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/audit/security/dashboard` | audit:dashboard | 安全看板数据 |
| GET | `/api/audit/security/alerts` | audit:alert | 告警分页列表 |
| GET | `/api/audit/security/alerts/{id}` | audit:alert | 告警详情 |
| PUT | `/api/audit/security/alerts/{id}/status` | audit:alert | 处理告警（标记已处理/忽略） |
| GET | `/api/audit/security/blacklist` | audit:blacklist | IP 黑名单分页列表 |
| POST | `/api/audit/security/blacklist` | audit:blacklist | 添加 IP 黑名单 |
| PUT | `/api/audit/security/blacklist/{id}` | audit:blacklist | 修改黑名单 |
| DELETE | `/api/audit/security/blacklist/{id}` | audit:blacklist | 删除黑名单 |

---

## 权限标识汇总

| 权限标识 | 授予角色 |
|---|---|
| `sys:user:list` / `sys:user:create` / `sys:user:update` | admin |
| `sys:role:list` / `sys:role:create` / `sys:role:update` / `sys:role:delete` | admin |
| `sys:dept:list` / `sys:dept:create` / `sys:dept:update` / `sys:dept:delete` | admin |
| `sys:menu:list` / `sys:menu:create` / `sys:menu:update` / `sys:menu:delete` | admin |
| `audit:log` | admin, auditor |
| `audit:dashboard` | admin, auditor |
| `audit:alert` | admin, auditor |
| `audit:blacklist` | admin, auditor |
| `procurement:request:list` | admin, employee, dept_manager, finance, procurement |
| `procurement:approval:pending` | admin, dept_manager, finance, procurement |
| `procurement:supplier:list` | admin, procurement |
| `procurement:contract:list` | admin, procurement |
| `procurement:payment:list` | admin, procurement |
| `knowledge:home` | admin, employee |
| `knowledge:doc:list` | admin, employee |
| `knowledge:qa:ask` | admin, employee |
| `knowledge:qa:log` | admin |
| `knowledge:doc:upload` | admin |
| `knowledge:doc:delete` | admin |
