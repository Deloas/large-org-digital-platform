# 大型组织数字化办公与安全审计一体化平台

面向央国企、公共机构和大型组织数字化办公场景的学生工程实践项目，构建可演示、可部署、可扩展的一体化平台。

---

## 项目简介

本项目实现了一个完整的数字化办公与安全审计一体化平台，涵盖：

- 基于 RBAC 的用户认证与权限管理
- 系统管理后台（用户/角色/部门/菜单）
- 全链路操作日志审计（AOP 自动记录 + 敏感字段脱敏）
- 金额驱动多级采购审批流与合同归档
- 知识库文档管理、文本解析与 RAG 智能问答（mock embedding + 规则回答）
- 安全审计增强（异常登录检测、安全告警、IP 黑名单、安全看板）

---

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Java 17 / Spring Boot 3.2.5 / MyBatis Plus 3.5.6 / Sa-Token 1.45.0 / jBCrypt / Knife4j 4.5.0 |
| 前端 | Vue 3 / TypeScript / Vite 5 / Element Plus / Pinia / Axios / SCSS |
| 数据库 | MySQL 5.7+ / 8.0 |
| 构建 | Maven 3.8+ / npm |
| 文档解析 | Apache PDFBox 3.0.1 / Apache POI 5.2.5 |

---

## 功能模块

| 阶段 | 模块 | 核心能力 |
|---|---|---|
| Phase 1 | 基础工程 | Spring Boot 3 + Vue 3 项目骨架、政企后台 UI 设计系统 |
| Phase 2 | 认证与 RBAC | Sa-Token 登录认证、角色权限、动态菜单、系统管理 CRUD |
| Phase 3 | 日志审计基础 | 登录日志记录、操作日志 AOP（@AuditLog）、敏感字段脱敏、日志查询 |
| Phase 4 | 采购审批 | 采购申请、金额驱动多级审批流、供应商管理、合同归档、付款节点 |
| Phase 5 | 知识库 RAG | 文档上传解析、文本切分、mock embedding + 规则问答、答案溯源 |
| Phase 6 | 安全审计增强 | 异常登录检测、安全告警、IP 黑名单、安全看板 |

数据库包含完整的业务表结构（18 张核心表）。

---

## 目录结构

```
├── backend/                     # Spring Boot 后端
│   └── src/main/java/org/largeorg/platform/
│       ├── auth/                # 认证模块
│       ├── system/              # 系统管理模块
│       ├── audit/               # 审计模块（日志 + 安全增强）
│       ├── procurement/         # 采购审批模块
│       ├── knowledge/           # 知识库 RAG 模块
│       └── common/              # 公共模块
├── frontend/                    # Vue 3 前端
│   └── src/
│       ├── api/                 # API 请求封装
│       ├── views/               # 页面组件
│       ├── layouts/             # 布局组件
│       ├── router/              # 路由配置
│       └── store/               # Pinia 状态管理
├── sql/                         # SQL 脚本
│   ├── schema.sql               # 全新环境建表
│   ├── init_data.sql            # 全新环境初始化数据
│   └── migration/               # 增量迁移脚本
├── docs/                        # 项目文档
├── docker-compose.yml           # Docker Compose（MySQL 服务）
└── CLAUDE.md                    # 项目工作规范
```

---

## 环境要求

| 组件 | 版本 |
|---|---|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 5.7+ / 8.0（或 Docker Compose 提供） |

---

## 快速启动

### 1. 数据库初始化

**方式一：Docker Compose（推荐，一键启动 MySQL）**

```bash
docker-compose up -d
```

容器启动后自动执行 `sql/schema.sql` 和 `sql/init_data.sql`，完成建表和初始化数据。

**方式二：手动 MySQL**

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS large_org_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"

# 建表
mysql -u root -p large_org_platform < sql/schema.sql

# 初始化数据（菜单、角色、权限、用户）
mysql -u root -p large_org_platform < sql/init_data.sql
```

> **已有环境的增量升级请使用 migration 脚本，不要重复执行 schema.sql 和 init_data.sql。** 详见 [docs/deployment.md](docs/deployment.md)。

### 2. 后端启动

```bash
cd backend
mvn -U clean compile
mvn spring-boot:run
```

后端默认监听 `http://localhost:18080`。

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev -- --port 15173
```

前端默认监听 `http://localhost:15173`。

---

## 端口说明

| 服务 | 端口 | 说明 |
|---|---|---|
| 后端 API | 18080 | Spring Boot 内嵌 Tomcat |
| 前端开发 | 15173 | Vite 开发服务器 |
| API 文档 | 18080/doc.html | Knife4j OpenAPI |
| 健康检查 | 18080/api/health | 服务可用性 |
| MySQL | 3306 | 数据库（Docker Compose 映射） |

---

## 初始化账号

| 用户名 | 密码 | 角色 | 可见菜单 |
|---|---|---|---|
| admin | Admin@123456 | 系统管理员 | 全部模块 |
| employee | User@123456 | 普通员工 | 工作台、知识库（首页/文档/问答）、采购申请 |
| dept_manager | User@123456 | 部门负责人 | 工作台、采购申请、待办审批 |
| finance | User@123456 | 财务负责人 | 工作台、采购申请、待办审批 |
| procurement | User@123456 | 采购管理员 | 工作台、采购模块全部子菜单 |
| auditor | User@123456 | 安全审计员 | 工作台、安全审计模块全部子菜单 |

---

## 各角色权限说明

| 权限模块 | admin | employee | dept_manager | finance | procurement | auditor |
|---|---|---|---|---|---|---|
| 工作台 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 系统管理 | ✅ | — | — | — | — | — |
| 审计日志查询 | ✅ | — | — | — | — | ✅ |
| 安全告警/黑名单 | ✅ | — | — | — | — | ✅ |
| 安全看板 | ✅ | — | — | — | — | ✅ |
| 采购申请 | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| 待办审批 | ✅ | — | ✅ | ✅ | ✅ | — |
| 供应商管理 | ✅ | — | — | — | ✅ | — |
| 合同管理 | ✅ | — | — | — | ✅ | — |
| 付款管理 | ✅ | — | — | — | ✅ | — |
| 知识库首页 | ✅ | ✅ | — | — | — | — |
| 文档管理 | ✅ | ✅（只读） | — | — | — | — |
| 智能问答 | ✅ | ✅ | — | — | — | — |
| 问答日志 | ✅ | — | — | — | — | — |

---

## 人工验收流程

参见 [docs/demo-guide.md](docs/demo-guide.md) 获取完整演示指南和推荐话术。

快速验证步骤：

1. **admin** 登录 → 确认全部菜单可见
2. **employee** 登录 → 确认仅工作台、知识库、采购申请 → 访问 `/audit` 返回 403
3. **auditor** 登录 → 确认仅工作台 + 安全审计
4. 查看操作日志 → 确认敏感字段 password 显示为 `***`

---

## 文档索引

| 文档 | 说明 |
|---|---|
| [docs/deployment.md](docs/deployment.md) | 部署文档（环境要求、初始化、启动、排错） |
| [docs/demo-guide.md](docs/demo-guide.md) | 演示指南（分角色演示流程和话术） |
| [docs/api-overview.md](docs/api-overview.md) | 接口清单（按模块汇总 API 和权限） |
| [docs/final-test-checklist.md](docs/final-test-checklist.md) | 最终测试清单（83 项验收 Check List） |

---

## 常见问题

**Q：端口被占用？**
修改 `application-dev.yml` 中的 `server.port` 和前端 `vite.config.ts` 中的端口号。

**Q：登录失败？**
确认使用 README 中的初始化账号密码。如果数据库是从旧版升级的，请确认已执行所有 migration SQL。

**Q：菜单不显示？**
菜单由后端 `/api/auth/me` 返回的 `menus` 动态渲染。请确认对应角色的 `sys_role_menu` 记录正确。

**Q：知识库问答没有匹配结果？**
请确认已上传文档且文档状态为"已就绪"（ready）。当前使用 mock embedding 和关键词匹配规则。

---

## 数据合规说明

本项目是学生工程实践项目，使用公开资料、模拟业务数据和自建系统日志，不使用任何真实企业内部数据。
