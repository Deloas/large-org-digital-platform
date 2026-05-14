-- ===================================================
-- 知识库 RAG 文档问答模块 - 增量迁移脚本
-- 适用于已有第二阶段 + 第三阶段数据的数据库升级
-- 执行方式：mysql -u root -p large_org_platform < sql/migration/005_knowledge_rag.sql
-- ===================================================

-- 一、创建知识库相关表（CREATE TABLE IF NOT EXISTS 保证幂等）

CREATE TABLE IF NOT EXISTS knowledge_document (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    title          VARCHAR(255) NOT NULL                  COMMENT '文档标题',
    file_name      VARCHAR(255) NOT NULL                  COMMENT '原始文件名',
    file_type      VARCHAR(20)  NOT NULL                  COMMENT 'PDF / DOCX / TXT',
    file_size      BIGINT                                 COMMENT '文件大小（字节）',
    file_path      VARCHAR(500)                           COMMENT '存储路径',
    content_text   MEDIUMTEXT                             COMMENT '解析后的纯文本内容',
    chunk_count    INT          DEFAULT 0                 COMMENT 'chunk 数量',
    status         VARCHAR(20)  DEFAULT 'processing'      COMMENT 'processing / ready / failed',
    upload_user_id BIGINT                                 COMMENT '上传人ID',
    upload_username VARCHAR(64)                           COMMENT '上传人用户名',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_upload_user_id (upload_user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id          BIGINT    PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT    NOT NULL                  COMMENT '所属文档ID',
    chunk_index INT       NOT NULL                  COMMENT '块序号（从0开始）',
    content     TEXT      NOT NULL                  COMMENT '块文本内容',
    char_count  INT       DEFAULT 0                 COMMENT '字符数',
    embedding   JSON                                COMMENT '特征向量（JSON数组）',
    created_at  DATETIME  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_document_id (document_id),
    INDEX idx_chunk_index (document_id, chunk_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文本块表';

CREATE TABLE IF NOT EXISTS knowledge_qa_log (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL                  COMMENT '提问人ID',
    username    VARCHAR(64)  NOT NULL                  COMMENT '提问人用户名',
    question    TEXT         NOT NULL                  COMMENT '用户问题',
    answer      TEXT                                   COMMENT '系统回答',
    sources     JSON                                   COMMENT '引用来源列表',
    confidence  DECIMAL(5,4)                           COMMENT '置信度',
    status      VARCHAR(20)  DEFAULT 'answered'        COMMENT 'answered / no_match',
    cost_ms     BIGINT                                 COMMENT '耗时（毫秒）',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库问答日志表';

-- 二、新增知识库子菜单（INSERT IGNORE 保证幂等）
-- 父菜单：知识库（id=2，已在 init_data.sql 中创建）
-- 子菜单 ID 使用 30-39 范围

INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort_order, visible) VALUES
(30, 2, '知识库首页', 'menu', '/knowledge',            'knowledge/KnowledgeHome',  'Document', 'knowledge:home',    1, 1),
(31, 2, '文档管理',   'menu', '/knowledge/documents',  'knowledge/DocumentList',  'Folder',   'knowledge:doc:list', 2, 1),
(32, 2, '智能问答',   'menu', '/knowledge/qa',         'knowledge/QaChat',        'ChatDotRound', 'knowledge:qa:ask', 3, 1),
(33, 2, '问答日志',   'menu', '/knowledge/qa/logs',    'knowledge/QaLogList',     'Tickets',  'knowledge:qa:log',  4, 1);

-- 三、admin 角色获得知识库模块全部菜单权限（INSERT IGNORE 保证幂等）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 30),
(1, 31),
(1, 32),
(1, 33);

-- 四、employee 角色获得知识库首页、文档列表只读、智能问答权限
-- employee 不允许：上传文档、删除文档、查看问答日志、管理知识库
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 30),
(2, 31),
(2, 32);
