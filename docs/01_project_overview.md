# 面向大型组织数字化办公与安全审计的一体化平台：项目总文档

## 1. 项目定位

本项目是一个面向央国企、公共机构和大型组织数字化办公场景的学生工程实践项目，目标是通过公开资料、模拟业务数据和自建系统日志，构建一个可演示、可部署、可扩展的一体化平台。

项目强调：

- 制度文件智能检索与问答
- 公共采购审批与合同归档
- Web 应用日志审计与异常登录检测
- 权限控制、操作留痕、数据合规
- 后台管理系统 UI/UX 与可视化看板

本项目不使用任何真实央国企内部数据，不涉及商业秘密、国家秘密、个人敏感信息或真实合同内容。

---

## 2. 项目总名称

推荐名称：

> 面向大型组织数字化办公与安全审计的一体化平台

项目包含三个子系统：

1. 面向大型组织的制度文件智能问答与溯源平台
2. 面向公共采购场景的采购审批与合同归档管理系统
3. 面向 Web 应用的日志审计与异常登录检测平台

---

## 3. 项目背景

大型组织在日常数字化办公中经常存在以下问题：

1. 制度、公文、流程文件数量庞大，人工查询效率低。
2. 采购申请、合同归档、付款节点等流程需要规范化管理。
3. 内部系统需要保留登录日志、操作日志和安全审计记录。
4. 不同角色需要访问不同数据，系统必须具备权限控制能力。
5. 面向组织管理场景的系统需要考虑数据来源、数据权限和合规边界。

本项目围绕这些问题构建完整工程实践，用模块化单体架构模拟大型组织数字化系统的核心能力。

---

## 4. 数据来源与合规说明

### 4.1 制度文件数据来源

制度文件智能问答模块使用公开资料或模拟文件，包括：

- 国务院、国资委等官方网站公开政策文件
- 网络安全、数据治理、数字经济等公开政策材料
- 高校公开学生手册、教务制度、实验室安全制度
- 自行编写的模拟组织制度文件

### 4.2 采购业务数据来源

采购审批模块可以参考公开采购公告的字段结构与流程特点，例如：

- 公开采购意向公告
- 公开招标公告
- 公开成交公告
- 公开废标公告

系统内部的采购申请、审批人、供应商、合同、付款节点等数据全部使用模拟数据。

### 4.3 日志审计数据来源

日志审计模块数据来自：

- 自建登录系统产生的登录日志
- 后端 AOP 自动采集的操作日志
- 脚本生成的模拟异常登录日志
- 模拟黑名单 IP 和安全告警数据

### 4.4 合规原则

项目遵循以下原则：

1. 不使用真实企业内部数据。
2. 不使用真实合同、真实供应商隐私信息或员工信息。
3. 所有模拟数据应使用 sample、demo、mock 标识。
4. 系统设计中保留权限控制、操作日志、异常告警、数据来源说明。
5. RAG 问答只能基于知识库资料回答，不能编造内部信息。

---

## 5. 总体技术栈

### 5.1 后端技术栈

- Java 17
- Spring Boot 3
- MyBatis Plus
- MySQL 或 PostgreSQL
- Sa-Token 或 Spring Security
- Redis，可选
- MinIO，可选
- Knife4j / Swagger
- Maven

### 5.2 前端技术栈

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Axios
- ECharts

### 5.3 AI 与检索技术栈

- RAG 检索增强生成
- Embedding 模型接口抽象
- Chat Model 接口抽象
- pgvector / Milvus / FAISS / 内存向量检索
- Apache PDFBox / Apache POI / Apache Tika

### 5.4 部署技术栈

- Docker
- Docker Compose
- Nginx
- MySQL / PostgreSQL 容器
- Redis 容器，可选
- MinIO 容器，可选

---

## 6. 总体架构设计

建议采用模块化单体架构，而不是一开始就拆成复杂微服务。

### 6.1 架构分层

```text
前端展示层
  ↓
API 接口层
  ↓
业务服务层
  ↓
数据访问层
  ↓
数据存储层 / 文件存储层 / 向量检索层
  ↓
AI 模型服务层
```

### 6.2 后端模块划分

```text
org.example.platform
├── common              # 通用响应、异常处理、工具类
├── config              # Spring 配置、安全配置、跨域配置
├── auth                # 登录、认证、权限
├── user                # 用户、角色、部门
├── knowledge           # 文档管理、文档解析、RAG 问答
├── procurement         # 采购申请、审批、供应商、合同
├── audit               # 登录日志、操作日志、异常检测、告警
├── dashboard           # 首页看板
└── infrastructure      # 文件存储、AI 客户端、向量库客户端
```

### 6.3 前端模块划分

```text
src
├── api                 # 接口封装
├── router              # 路由配置
├── store               # Pinia 状态管理
├── layouts             # 后台布局
├── views
│   ├── login
│   ├── dashboard
│   ├── knowledge
│   ├── procurement
│   ├── audit
│   └── system
├── components
└── utils
```

---

## 7. 用户角色设计

| 角色 | 主要权限 |
|---|---|
| 系统管理员 | 用户管理、角色管理、全部模块管理、系统配置 |
| 普通员工 | 查询公开制度、提交采购申请、查看本人申请 |
| 部门负责人 | 审批本部门采购申请、查看本部门采购数据 |
| 财务负责人 | 审批预算相关采购、查看合同付款节点 |
| 采购管理员 | 供应商管理、合同归档、采购终审 |
| 安全审计员 | 查看日志、处理告警、维护黑名单 IP |

---

## 8. 核心数据库表规划

### 8.1 用户与权限表

```sql
sys_user(id, username, password, real_name, dept_id, status, created_at)
sys_role(id, role_code, role_name, description)
sys_user_role(id, user_id, role_id)
sys_dept(id, dept_name, parent_id, leader_id)
sys_permission(id, permission_code, permission_name, menu_path)
sys_role_permission(id, role_id, permission_id)
```

### 8.2 知识库表

```sql
kb_document(id, title, category, source_type, file_url, status, created_by, created_at)
kb_chunk(id, document_id, chunk_index, content, page_no, vector_id, created_at)
kb_qa_record(id, user_id, question, answer, referenced_chunks, hit_status, latency_ms, created_at)
```

### 8.3 采购审批表

```sql
proc_purchase_request(id, title, type, amount, dept_id, applicant_id, reason, expected_date, status, created_at)
proc_approval_record(id, request_id, approver_id, approval_level, action, comment, created_at)
proc_supplier(id, name, contact_name, contact_phone, credit_code, category, rating, status)
proc_contract(id, contract_no, contract_name, request_id, supplier_id, amount, sign_date, expire_date, file_url, status)
proc_payment_node(id, contract_id, node_name, amount, ratio, planned_date, paid_status)
```

### 8.4 日志审计表

```sql
audit_login_log(id, user_id, username, ip, user_agent, status, fail_reason, created_at)
audit_operation_log(id, user_id, module, action, request_uri, method, params_summary, result, cost_ms, created_at)
audit_alert(id, alert_type, risk_level, subject, description, status, handler_id, handle_comment, created_at, handled_at)
audit_ip_blacklist(id, ip, reason, status, created_at)
```

---

## 9. 页面规划

### 9.1 登录与首页

- 登录页
- 首页工作台
- 数据总览卡片
- 最近采购动态
- 最近安全告警
- 登录趋势图
- 采购类型占比图

### 9.2 制度知识库

- 文档列表
- 文档上传
- 文档详情
- 文档片段预览
- 智能问答页面
- 问答记录

### 9.3 采购管理

- 采购申请列表
- 新建采购申请
- 采购申请详情
- 我的审批
- 供应商管理
- 合同归档
- 付款节点管理

### 9.4 安全审计

- 登录日志
- 操作日志
- 异常告警
- 告警处理
- IP 黑名单
- 安全看板

### 9.5 系统管理

- 用户管理
- 角色管理
- 部门管理
- 菜单权限管理

---

## 10. UI/UX 设计规范

### 10.1 视觉风格

整体采用政企数字化后台风格：

- 稳定
- 克制
- 清晰
- 可信赖
- 强调数据可读性
- 强调状态和风险提示

### 10.2 页面布局

- 左侧固定菜单
- 顶部用户信息栏
- 中间主内容区
- 表格页面支持搜索、筛选、分页
- 详情页面使用卡片、步骤条、时间线
- 看板页面使用指标卡片和图表

### 10.3 关键交互

- 审批按钮根据用户角色动态显示
- 风险告警按等级突出展示
- 智能问答回答必须显示引用来源
- 上传文件需要显示解析状态
- 删除和驳回等操作需要二次确认

---

## 11. 推荐开发里程碑

### 阶段 1：基础工程

- 创建后端 Spring Boot 项目
- 创建前端 Vue 3 项目
- 配置数据库连接
- 实现统一响应和全局异常处理
- 接入接口文档工具
- 搭建后台页面布局

### 阶段 2：认证与权限

- 实现登录、退出、当前用户信息
- 实现用户、角色、部门、权限基础表
- 实现菜单权限控制
- 初始化多角色测试账号

### 阶段 3：日志审计基础能力

- 登录成功/失败记录登录日志
- AOP 自动记录操作日志
- 登录日志和操作日志分页查询
- 日志筛选和详情查看

### 阶段 4：异常登录检测

- 实现暴力破解检测
- 实现撞库风险检测
- 实现非工作时间管理员登录检测
- 实现同账号多 IP 登录检测
- 实现黑名单 IP 告警
- 实现安全看板

### 阶段 5：采购审批

- 实现采购申请 CRUD
- 实现提交审批、通过、驳回、撤回
- 实现金额驱动审批链路
- 实现审批时间线

### 阶段 6：合同与供应商

- 实现供应商管理
- 实现合同归档
- 实现付款节点管理
- 实现采购统计看板

### 阶段 7：知识库文档管理

- 实现文件上传
- 实现 PDF/Word/TXT 文本解析
- 实现文本切分
- 保存文档片段
- 支持文档重新解析

### 阶段 8：RAG 问答

- 抽象 EmbeddingClient
- 抽象 ChatModelClient
- 实现向量检索
- 实现问答接口
- 实现答案来源引用
- 实现问答记录

### 阶段 9：部署与文档完善

- 编写 README
- 编写 Docker Compose
- 编写模拟数据脚本
- 完善演示数据
- 优化 UI 细节

---

## 12. 项目目录建议

```text
large-org-digital-platform
├── backend
│   ├── src/main/java/org/example/platform
│   ├── src/main/resources
│   ├── pom.xml
│   └── README.md
├── frontend
│   ├── src
│   ├── package.json
│   └── README.md
├── docs
│   ├── 01_project_overview.md
│   ├── 02_knowledge_rag.md
│   ├── 03_procurement_contract.md
│   └── 04_audit_security.md
├── sql
│   ├── schema.sql
│   └── sample_data.sql
├── deploy
│   └── docker-compose.yml
└── README.md
```

---

## 13. 最低可运行版本范围

如果时间有限，建议优先完成：

1. 登录与角色权限
2. 操作日志和登录日志
3. 异常登录告警
4. 采购申请审批主流程
5. 文档上传和简单文本检索
6. 首页看板
7. 基础部署文档

---

## 14. 后续扩展方向

可扩展能力包括：

- Elasticsearch 日志检索
- Kafka 异步日志采集
- Flowable 工作流引擎
- pgvector / Milvus 向量库
- MinIO 文件存储
- Redis 缓存与限流
- 多租户组织隔离
- 数据脱敏
- 国产数据库兼容适配说明
- Docker Compose 一键部署
