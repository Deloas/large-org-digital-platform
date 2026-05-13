# 大型组织数字化办公与安全审计一体化平台

面向央国企、公共机构和大型组织数字化办公场景的学生工程实践项目，构建可演示、可部署、可扩展的一体化平台。

## 当前阶段

**第二阶段：认证与权限模块**

已完成用户认证（Sa-Token）和 RBAC 权限管理（用户/角色/部门/菜单 CRUD），前端实现登录拦截、动态菜单、系统管理后台。

## 子系统

| 子系统 | 文档 |
|---|---|
| 制度文件智能问答与溯源平台 | [docs/02_knowledge_rag_platform.md](docs/02_knowledge_rag_platform.md) |
| 采购审批与合同归档管理系统 | [docs/03_procurement_contract_platform.md](docs/03_procurement_contract_platform.md) |
| 日志审计与异常登录检测平台 | [docs/04_audit_security_platform.md](docs/04_audit_security_platform.md) |

## 技术栈

- **后端**：Java 17 / Spring Boot 3 / MyBatis Plus / Sa-Token / jBCrypt / MySQL / Knife4j / Maven
- **前端**：Vue 3 / TypeScript / Vite / Element Plus / Pinia / Axios / SCSS

## 端口说明

| 服务 | 地址 |
|---|---|
| 后端 API | http://localhost:18080 |
| 前端页面 | http://localhost:15173 |
| API 文档 (Knife4j) | http://localhost:18080/doc.html |
| 健康检查 | http://localhost:18080/api/health |

## 数据库初始化

本阶段需要 MySQL 数据库，请先执行 SQL 脚本：

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS large_org_platform DEFAULT CHARACTER SET utf8mb4"

# 2. 导入表结构和初始数据
mysql -u root -p large_org_platform < sql/schema.sql
mysql -u root -p large_org_platform < sql/init_data.sql
```

数据库配置见 `backend/src/main/resources/application-dev.yml`（默认 root/123456，请按本地环境修改）。

## 后端启动方式

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

## 前端启动方式

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器配置了代理，`/api` 请求自动转发到 `http://localhost:18080`。

## 测试账号

| 用户名 | 密码 | 角色 | 权限 |
|---|---|---|---|
| admin | Admin@123456 | 系统管理员 | 全部权限 |
| employee | User@123456 | 普通员工 | 仅工作台 |
| dept_manager | User@123456 | 部门负责人 | 仅工作台 |
| finance | User@123456 | 财务负责人 | 仅工作台 |
| procurement | User@123456 | 采购管理员 | 仅工作台 |
| auditor | User@123456 | 安全审计员 | 仅工作台 |

## 健康检查验证

```bash
curl http://localhost:18080/api/health
```

正常响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "app": "large-org-platform",
    "status": "UP",
    "time": "2026-05-13T...",
    "db": "AVAILABLE"
  }
}
```

## 第二阶段暂不包含的功能

- 制度文件上传、解析与 RAG 智能问答
- 采购申请、审批流程、供应商管理、合同归档
- 登录日志、操作日志采集与异常检测
- 安全告警管理与 IP 黑名单
- 大模型 API 接入
- 微服务拆分

以上功能将在后续阶段逐步实现。

## 数据合规说明

本项目是学生工程实践项目，使用公开资料、公开采购公告、模拟业务数据和自建系统日志，不使用任何真实企业内部数据。
