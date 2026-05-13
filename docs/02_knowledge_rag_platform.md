# 面向大型组织的制度文件智能问答与溯源平台：子项目文档

## 1. 项目定位

本项目用于解决大型组织制度文件、公文政策、流程说明、办事指南等资料分散、查询效率低的问题。

系统支持上传公开制度文件或模拟制度文件，自动完成文档解析、文本切分、向量化检索，并通过 RAG 技术实现自然语言问答。系统回答时必须提供引用来源，避免大模型脱离资料编造内容。

---

## 2. 数据来源说明

本项目仅使用公开资料或模拟数据，包括：

- 公开政策文件
- 公开管理制度
- 高校公开学生手册、教务制度、实验室安全制度
- 自行编写的模拟组织制度文件
- sample/demo/mock 文档

不使用真实企业内部制度、公文、通知、会议纪要或涉密资料。

---

## 3. 业务目标

系统需要实现：

1. 统一管理制度文件。
2. 自动解析 PDF、Word、TXT 等文档。
3. 将长文档切分为可检索片段。
4. 对文档片段建立向量索引。
5. 用户可以用自然语言提问。
6. 系统根据知识库内容生成回答。
7. 回答必须展示引用来源。
8. 未找到依据时明确提示无法回答。
9. 记录问答日志，便于后续分析。

---

## 4. 用户角色

| 角色 | 权限 |
|---|---|
| 系统管理员 | 管理全部文档、重建索引、查看全部问答记录 |
| 普通员工 | 查询公开文档、进行智能问答 |
| 部门用户 | 查询公开文档和本部门文档 |
| 安全审计员 | 查看问答日志和敏感查询记录 |

---

## 5. 功能模块

## 5.1 文档管理

### 功能说明

用于管理知识库文件。

### 功能点

- 上传 PDF、Word、TXT、Markdown 文件
- 设置文档标题
- 设置文档分类
- 设置来源类型
- 查看解析状态
- 删除文档
- 重新解析文档
- 禁用文档

### 文档分类示例

- 政策文件
- 制度文件
- 流程手册
- 安全规范
- 采购制度
- 学生手册
- 实验室制度

---

## 5.2 文档解析

### 功能说明

系统从上传文件中提取纯文本内容。

### 支持格式

- PDF：Apache PDFBox
- Word：Apache POI
- TXT：直接读取
- Markdown：直接读取

### 解析流程

```text
文件上传
  ↓
保存文件元数据
  ↓
提取正文内容
  ↓
文本清洗
  ↓
文本切分
  ↓
保存 chunk
  ↓
等待向量化
```

---

## 5.3 文本切分

### 切分目标

将长文档拆分成适合向量检索和大模型上下文输入的片段。

### 推荐策略

基础版本：

- 按段落切分
- 每个 chunk 控制在 500 到 800 字
- 相邻 chunk 保留 50 到 100 字重叠

进阶版本：

- 按标题层级切分
- 记录章节标题
- 记录页码
- 记录段落位置

### chunk 字段

- 文档 ID
- chunk 序号
- chunk 内容
- 页码
- 章节标题
- 向量 ID
- 创建时间

---

## 5.4 向量化检索

### 功能说明

对文档 chunk 生成 embedding，并根据用户问题召回相关片段。

### 推荐实现路线

#### 方案 A：内存向量检索

适合早期开发，简单可跑。

特点：

- 实现成本低
- 不依赖额外服务
- 适合小规模演示

#### 方案 B：PostgreSQL + pgvector

适合中等复杂度版本。

特点：

- 数据和向量统一存储
- 部署相对简单
- 适合学生项目展示

#### 方案 C：Milvus

适合进阶版本。

特点：

- 专业向量数据库
- 适合大规模数据
- 部署复杂度更高

---

## 5.5 RAG 智能问答

### 问答流程

```text
用户提问
  ↓
问题向量化
  ↓
召回相关 chunk
  ↓
构造 Prompt
  ↓
调用大模型
  ↓
返回答案和引用来源
  ↓
保存问答日志
```

### 回答约束

系统必须遵守：

1. 只能基于召回的文档片段回答。
2. 不能编造资料中没有的信息。
3. 如果资料不足，必须明确提示：当前知识库未找到足够依据，无法回答该问题。
4. 每个回答必须展示引用来源。
5. 引用来源应包含文档标题、片段内容、页码或段落位置。

### RAG Prompt 模板

```text
你是一个大型组织制度文件问答助手。
请只基于下方提供的资料回答用户问题，不要使用资料以外的信息。
如果资料中没有足够依据，请回答：当前知识库未找到足够依据，无法回答该问题。

资料：
{retrieved_chunks}

用户问题：
{question}

回答要求：
1. 回答要简洁、准确。
2. 不能编造制度条款。
3. 必须说明依据来自哪些资料。
```

---

## 5.6 答案溯源

### 功能说明

每个回答都需要展示依据。

### 引用信息

- 文档标题
- 文档分类
- 页码或段落号
- chunk 原文
- 相似度分数

### 页面展示方式

智能问答页面建议分为三栏：

```text
左侧：文档分类 / 知识库范围
中间：聊天问答窗口
右侧：引用来源 / 相关片段
```

---

## 5.7 问答日志

### 记录字段

- 用户 ID
- 用户问题
- 模型回答
- 引用 chunk
- 是否命中知识库
- 消耗时间
- 创建时间

### 用途

- 统计高频问题
- 排查错误回答
- 分析知识库缺口
- 审计敏感查询

---

## 6. 技术架构

### 6.1 后端模块结构

```text
knowledge
├── controller
│   ├── DocumentController.java
│   └── QaController.java
├── service
│   ├── DocumentService.java
│   ├── DocumentParseService.java
│   ├── ChunkService.java
│   ├── VectorSearchService.java
│   └── QaService.java
├── mapper
├── entity
├── dto
├── vo
└── client
    ├── EmbeddingClient.java
    ├── ChatModelClient.java
    ├── MockEmbeddingClient.java
    └── MockChatModelClient.java
```

### 6.2 前端模块结构

```text
views/knowledge
├── DocumentList.vue
├── DocumentUpload.vue
├── DocumentDetail.vue
├── ChunkPreview.vue
├── QaChat.vue
└── QaHistory.vue
```

---

## 7. 数据库设计

```sql
CREATE TABLE kb_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    source_type VARCHAR(50),
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    file_type VARCHAR(50),
    parse_status VARCHAR(30),
    status VARCHAR(30),
    created_by BIGINT,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE kb_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    page_no INT,
    section_title VARCHAR(255),
    vector_id VARCHAR(100),
    created_at DATETIME
);

CREATE TABLE kb_qa_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    question TEXT NOT NULL,
    answer TEXT,
    referenced_chunks TEXT,
    hit_status VARCHAR(30),
    latency_ms BIGINT,
    created_at DATETIME
);
```

---

## 8. 核心接口规划

### 8.1 文档接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/kb/documents/upload | 上传文档 |
| GET | /api/kb/documents | 文档分页列表 |
| GET | /api/kb/documents/{id} | 文档详情 |
| DELETE | /api/kb/documents/{id} | 删除文档 |
| POST | /api/kb/documents/{id}/parse | 重新解析文档 |
| GET | /api/kb/documents/{id}/chunks | 查看文档片段 |

### 8.2 问答接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/kb/qa/ask | 智能问答 |
| GET | /api/kb/qa/history | 问答历史 |
| GET | /api/kb/qa/{id} | 问答详情 |

### 8.3 问答请求示例

```json
{
  "question": "采购申请超过十万元需要哪些审批？",
  "category": "采购制度",
  "topK": 5
}
```

### 8.4 问答响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "answer": "根据知识库中的采购制度文件，十万元以上采购需要经过部门负责人、财务负责人和采购管理员审批。",
    "references": [
      {
        "documentTitle": "模拟采购管理制度",
        "pageNo": 3,
        "content": "预算金额超过十万元的采购事项，应依次提交部门负责人、财务负责人和采购管理员审批。",
        "score": 0.86
      }
    ]
  }
}
```

---

## 9. 页面设计

### 9.1 文档列表页

包含：

- 搜索栏
- 分类筛选
- 解析状态筛选
- 文档表格
- 上传按钮
- 重新解析按钮
- 删除按钮

### 9.2 文档详情页

包含：

- 文档基本信息
- 文件来源说明
- 解析状态
- chunk 片段列表
- 重新解析入口

### 9.3 智能问答页

包含：

- 左侧知识库分类
- 中间对话窗口
- 输入框
- 右侧引用来源面板
- 问答历史入口

---

## 10. 开发步骤

### 阶段 1：文档管理

- 完成文档表设计
- 实现上传接口
- 实现文档列表
- 实现文档详情
- 实现删除和重新解析入口

### 阶段 2：文档解析

- 接入 PDFBox
- 接入 POI
- 实现 TXT 解析
- 实现文本清洗
- 实现文本切分
- 保存 chunk

### 阶段 3：检索能力

- 设计 EmbeddingClient
- 实现 MockEmbeddingClient
- 实现基础相似度计算
- 实现 topK 召回

### 阶段 4：RAG 问答

- 设计 ChatModelClient
- 实现 MockChatModelClient
- 实现 RAG Prompt
- 实现问答接口
- 保存问答日志

### 阶段 5：前端页面

- 文档管理页面
- 文档详情页面
- 智能问答页面
- 引用来源展示
- 问答历史页面

---

## 11. 验收标准

最低可运行版本应满足：

1. 可以上传 TXT 或 PDF 文件。
2. 可以解析文本并生成 chunk。
3. 可以对问题召回相关片段。
4. 可以生成带引用来源的回答。
5. 找不到资料时不会编造答案。
6. 可以查看问答记录。
7. 页面可完成完整演示流程。

---

## 12. 后续扩展

可扩展方向：

- 接入真实 embedding API
- 接入真实大模型 API
- 使用 pgvector 或 Milvus
- 增加文档权限隔离
- 增加敏感词检测
- 增加高频问题分析
- 增加知识库命中率统计
- 增加文档版本管理
