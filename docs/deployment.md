# 部署文档

## 环境要求

| 组件 | 版本要求 | 说明 |
|---|---|---|
| JDK | 17+ | 推荐 OpenJDK 17 或 Oracle JDK 17 |
| Maven | 3.8+ | 用于后端构建和启动 |
| Node.js | 18+ | 用于前端构建和启动 |
| MySQL | 5.7+ / 8.0 | 数据库服务 |

---

## 数据库初始化

### 方式一：Docker Compose（推荐）

```bash
# 在项目根目录执行
docker-compose up -d
```

这会自动启动 MySQL 8.0 容器，并执行 `sql/schema.sql` 和 `sql/init_data.sql` 完成建表和初始化数据。

### 方式二：手动 MySQL

**全新环境（第一次部署）：**

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 创建数据库
CREATE DATABASE IF NOT EXISTS large_org_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

# 3. 执行建表脚本
mysql -u root -p large_org_platform < sql/schema.sql

# 4. 执行初始化数据
mysql -u root -p large_org_platform < sql/init_data.sql
```

**已有环境增量升级：**

如果你的数据库已经拥有前一阶段的数据，请不要重复执行 `schema.sql` 和 `init_data.sql`，改用增量迁移脚本：

```bash
mysql -u root -p large_org_platform < sql/migration/audit_foundation.sql
mysql -u root -p large_org_platform < sql/migration/004_procurement.sql
mysql -u root -p large_org_platform < sql/migration/005_knowledge_rag.sql
mysql -u root -p large_org_platform < sql/migration/006_security_audit.sql
```

迁移脚本全部使用 `CREATE TABLE IF NOT EXISTS` 和 `INSERT IGNORE`，可安全重复执行。

> **注意：** 全新环境和增量升级是两种不同的初始化方式，不要混合使用。

---

## 后端启动

### 1. 配置数据库连接

编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/large_org_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

Docker Compose 启动的 MySQL 使用上述默认配置，无需修改。

### 2. 编译并启动

```bash
cd backend

# 编译
mvn -U clean compile

# 启动（开发模式）
mvn spring-boot:run
```

### 3. 验证

后端启动后访问：http://localhost:18080

Knife4j API 文档：http://localhost:18080/doc.html

---

## 前端启动

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev -- --port 15173
```

### 3. 验证

浏览器访问：http://localhost:15173

---

## 端口说明

| 服务 | 端口 | 说明 |
|---|---|---|
| 后端 API | 18080 | Spring Boot 内嵌 Tomcat |
| 前端开发 | 15173 | Vite 开发服务器 |
| MySQL | 3306 | 数据库（Docker Compose 映射） |

---

## 常见错误排查

### 1. 后端启动报 `Communications link failure`

- 检查 MySQL 是否已启动。
- 检查 `application-dev.yml` 中数据库连接信息是否正确。
- 如果使用 Docker Compose，确保容器正在运行：`docker-compose ps`。

### 2. 前端访问 API 跨域报错

- 检查后端是否已启动且监听 18080 端口。
- Vite 开发服务器已配置代理到 `localhost:18080`，不需要额外配置。

### 3. 登录失败 / 密码错误

- 确认使用的是初始化账号密码（见 README）。
- 如果数据库是全新初始化的，密码为 `Admin@123456`（admin）/ `User@123456`（其他用户）。

### 4. `mvn` 命令不可用

- 确认已安装 Maven 3.8+ 并配置到 PATH。
- Windows 下可在 https://maven.apache.org/download.cgi 下载。

### 5. `npm` 命令不可用

- 确认已安装 Node.js 18+。
- 在项目前端目录下执行 `npm install` 安装依赖。

### 6. 菜单不显示或权限不足

- 如果使用了增量迁移，确保已按顺序执行所有 migration SQL。
- 确认用户角色分配正确，可查询 `sys_user_role` 表验证。

### 7. Docker Compose 端口冲突

- 如本地已运行 MySQL 占用 3306 端口，可修改 `docker-compose.yml` 中的 `ports` 映射。
