# 面向 Web 应用的日志审计与异常登录检测平台：子项目文档

## 1. 项目定位

本项目用于记录 Web 系统中的登录日志、操作日志和异常访问行为，并基于规则检测风险事件，生成安全告警。

项目目标是为大型组织内部系统提供基础安全审计能力，包括日志采集、日志检索、异常检测、告警处理和安全看板。

---

## 2. 数据来源说明

本项目不使用真实企业安全日志。

数据来源包括：

- 自建登录系统产生的登录成功/失败日志
- 后端 AOP 采集的业务操作日志
- 脚本生成的模拟异常登录日志
- 模拟黑名单 IP
- 模拟安全告警

所有日志数据均为 demo/sample/mock 数据。

---

## 3. 业务目标

系统需要实现：

1. 记录用户登录成功和失败日志。
2. 记录关键业务操作日志。
3. 支持按用户、IP、时间、模块检索日志。
4. 通过规则识别异常登录行为。
5. 自动生成安全告警。
6. 支持告警处理和误报标记。
7. 提供安全态势看板。

---

## 4. 用户角色

| 角色 | 权限 |
|---|---|
| 系统管理员 | 查看全部日志和告警，管理黑名单 IP |
| 安全审计员 | 查看日志、处理告警、分析异常行为 |
| 普通用户 | 只能查看与自己相关的基础操作记录，可选 |

---

## 5. 功能模块

## 5.1 登录日志采集

### 记录时机

- 登录成功
- 登录失败
- 账号不存在
- 密码错误
- 账号禁用
- 黑名单 IP 尝试登录

### 记录字段

- 用户 ID
- 用户名
- IP 地址
- User-Agent
- 登录状态
- 失败原因
- 登录时间
- 地理位置，可选

---

## 5.2 操作日志采集

### 实现方式

使用 Spring AOP 和自定义注解 `@AuditLog` 对关键业务操作进行记录。

### 注解示例

```java
@AuditLog(module = "采购管理", action = "提交采购申请")
@PostMapping("/{id}/submit")
public Result<Void> submit(@PathVariable Long id) {
    purchaseRequestService.submit(id);
    return Result.success();
}
```

### 记录字段

- 操作人 ID
- 操作人用户名
- 操作模块
- 操作类型
- 请求路径
- 请求方法
- 请求参数摘要
- 操作结果
- 执行耗时
- 操作时间

### 参数处理原则

为避免日志中保存敏感信息：

1. 不记录密码明文。
2. 不记录 token。
3. 请求参数过长时截断。
4. 文件内容不写入日志。
5. 仅记录参数摘要。

---

## 5.3 异常登录检测

### 规则 1：暴力破解风险

条件：

- 5 分钟内
- 同一账号
- 登录失败超过 5 次

结果：

- 生成中高风险告警

---

### 规则 2：撞库风险

条件：

- 5 分钟内
- 同一 IP
- 尝试登录 3 个及以上不同账号

结果：

- 生成高风险告警

---

### 规则 3：非工作时间管理员登录

条件：

- 管理员账号
- 在 22:00 到次日 06:00 登录

结果：

- 生成可疑登录告警

---

### 规则 4：同账号多 IP 登录

条件：

- 10 分钟内
- 同一账号
- 从 3 个及以上不同 IP 登录

结果：

- 生成账号异常告警

---

### 规则 5：黑名单 IP 登录

条件：

- 登录 IP 存在于黑名单

结果：

- 生成高危告警
- 可以阻断登录，可选

---

## 5.4 告警管理

### 告警等级

```text
LOW       低风险
MEDIUM    中风险
HIGH      高风险
CRITICAL  严重风险
```

### 告警状态

```text
PENDING      待处理
PROCESSING   处理中
RESOLVED     已处理
FALSE_POSITIVE 误报
```

### 告警字段

- 告警类型
- 风险等级
- 告警主体
- 告警描述
- 告警状态
- 处理人
- 处理备注
- 创建时间
- 处理时间

### 告警去重

为避免同一风险短时间内重复刷屏，可以设计去重规则：

- 相同告警类型
- 相同账号或 IP
- 10 分钟内只生成一次未处理告警

---

## 5.5 IP 黑名单管理

功能点：

- 新增黑名单 IP
- 编辑黑名单原因
- 启用/禁用黑名单
- 删除黑名单 IP
- 登录时检查黑名单

---

## 5.6 日志检索

### 登录日志筛选条件

- 用户名
- IP 地址
- 登录状态
- 时间范围
- 失败原因

### 操作日志筛选条件

- 用户名
- 操作模块
- 操作类型
- 请求路径
- 时间范围
- 操作结果

---

## 5.7 安全看板

展示指标：

- 今日登录次数
- 今日登录失败次数
- 今日异常告警数量
- 待处理高危告警数量
- 高频失败账号排行
- 高危 IP 排行
- 最近告警列表
- 登录趋势图
- 告警类型分布图

---

## 6. 技术架构

### 6.1 后端模块结构

```text
audit
├── controller
│   ├── LoginLogController.java
│   ├── OperationLogController.java
│   ├── AlertController.java
│   ├── IpBlacklistController.java
│   └── SecurityDashboardController.java
├── service
│   ├── LoginLogService.java
│   ├── OperationLogService.java
│   ├── AlertService.java
│   ├── RiskDetectService.java
│   └── IpBlacklistService.java
├── aspect
│   └── AuditLogAspect.java
├── annotation
│   └── AuditLog.java
├── mapper
├── entity
├── dto
├── vo
└── enums
```

### 6.2 前端模块结构

```text
views/audit
├── LoginLogList.vue
├── OperationLogList.vue
├── AlertList.vue
├── AlertDetail.vue
├── IpBlacklist.vue
└── SecurityDashboard.vue
```

---

## 7. 数据库设计

```sql
CREATE TABLE audit_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(100),
    ip VARCHAR(64),
    user_agent VARCHAR(500),
    status VARCHAR(30),
    fail_reason VARCHAR(255),
    created_at DATETIME
);

CREATE TABLE audit_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(100),
    module VARCHAR(100),
    action VARCHAR(100),
    request_uri VARCHAR(255),
    method VARCHAR(20),
    params_summary TEXT,
    result VARCHAR(30),
    cost_ms BIGINT,
    created_at DATETIME
);

CREATE TABLE audit_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_type VARCHAR(100),
    risk_level VARCHAR(30),
    subject VARCHAR(200),
    description TEXT,
    status VARCHAR(30),
    handler_id BIGINT,
    handle_comment TEXT,
    created_at DATETIME,
    handled_at DATETIME
);

CREATE TABLE audit_ip_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ip VARCHAR(64) NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(30),
    created_at DATETIME,
    updated_at DATETIME
);
```

---

## 8. 核心接口规划

### 8.1 登录日志接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/audit/login-logs | 登录日志分页查询 |
| GET | /api/audit/login-logs/{id} | 登录日志详情 |

### 8.2 操作日志接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/audit/operation-logs | 操作日志分页查询 |
| GET | /api/audit/operation-logs/{id} | 操作日志详情 |

### 8.3 告警接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/audit/alerts | 告警分页查询 |
| GET | /api/audit/alerts/{id} | 告警详情 |
| POST | /api/audit/alerts/{id}/process | 处理告警 |
| POST | /api/audit/alerts/{id}/false-positive | 标记误报 |

### 8.4 黑名单接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/audit/ip-blacklist | 新增黑名单 IP |
| PUT | /api/audit/ip-blacklist/{id} | 编辑黑名单 IP |
| GET | /api/audit/ip-blacklist | 黑名单列表 |
| DELETE | /api/audit/ip-blacklist/{id} | 删除黑名单 IP |

### 8.5 看板接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/audit/dashboard/summary | 安全指标汇总 |
| GET | /api/audit/dashboard/login-trend | 登录趋势 |
| GET | /api/audit/dashboard/top-risk-ip | 高危 IP 排行 |
| GET | /api/audit/dashboard/recent-alerts | 最近告警 |

---

## 9. 页面设计

### 9.1 登录日志页面

包含：

- 用户名搜索
- IP 搜索
- 登录状态筛选
- 时间范围筛选
- 表格分页
- 日志详情弹窗

### 9.2 操作日志页面

包含：

- 模块筛选
- 操作类型筛选
- 操作人搜索
- 时间范围筛选
- 参数摘要查看
- 耗时展示

### 9.3 异常告警页面

包含：

- 风险等级筛选
- 告警类型筛选
- 告警状态筛选
- 告警详情
- 处理按钮
- 标记误报按钮

### 9.4 IP 黑名单页面

包含：

- IP 搜索
- 新增黑名单
- 启用/禁用
- 删除
- 原因说明

### 9.5 安全看板页面

包含：

- 今日登录次数
- 失败次数
- 告警数量
- 高危告警数量
- 登录趋势图
- 告警类型分布图
- 高频失败账号排行
- 高危 IP 排行
- 最近告警列表

---

## 10. 开发步骤

### 阶段 1：登录日志

- 创建登录日志表
- 登录成功记录日志
- 登录失败记录日志
- 实现日志分页查询
- 实现前端列表页

### 阶段 2：操作日志

- 定义 `@AuditLog` 注解
- 实现 AOP 切面
- 记录请求路径、参数、结果和耗时
- 实现操作日志列表页

### 阶段 3：异常检测

- 实现暴力破解检测
- 实现撞库风险检测
- 实现非工作时间管理员登录检测
- 实现同账号多 IP 检测
- 实现黑名单 IP 检测

### 阶段 4：告警管理

- 创建告警表
- 实现告警生成
- 实现告警去重
- 实现告警处理
- 实现告警列表页

### 阶段 5：安全看板

- 统计登录次数
- 统计失败次数
- 统计告警数量
- 统计高危 IP
- 实现 ECharts 图表

---

## 11. 测试数据设计

### 11.1 暴力破解模拟

同一账号 5 分钟内连续失败 6 次：

```text
user=demo_user ip=192.168.1.10 status=fail
user=demo_user ip=192.168.1.10 status=fail
user=demo_user ip=192.168.1.10 status=fail
user=demo_user ip=192.168.1.10 status=fail
user=demo_user ip=192.168.1.10 status=fail
user=demo_user ip=192.168.1.10 status=fail
```

### 11.2 撞库风险模拟

同一 IP 尝试多个账号：

```text
ip=10.0.0.8 user=demo_a status=fail
ip=10.0.0.8 user=demo_b status=fail
ip=10.0.0.8 user=demo_c status=fail
```

### 11.3 多 IP 登录模拟

同一账号短时间多 IP 登录：

```text
user=demo_admin ip=10.0.0.1 status=success
user=demo_admin ip=10.0.0.2 status=success
user=demo_admin ip=10.0.0.3 status=success
```

---

## 12. 验收标准

最低可运行版本应满足：

1. 登录成功和失败都能记录日志。
2. 关键业务操作可以自动记录操作日志。
3. 可以分页检索登录日志和操作日志。
4. 连续登录失败可以触发告警。
5. 黑名单 IP 登录可以触发高危告警。
6. 告警可以处理和标记误报。
7. 安全看板可以展示核心指标。

---

## 13. 后续扩展

可扩展方向：

- 接入 Elasticsearch / OpenSearch
- 接入 Kafka 异步日志采集
- 接入 Filebeat / Logstash
- 增加 IP 地理位置解析
- 增加接口限流
- 增加设备指纹
- 增加登录二次验证
- 增加异常行为评分模型
- 增加日志归档策略
- 增加导出审计报告
