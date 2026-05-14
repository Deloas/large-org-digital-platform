package org.largeorg.platform.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_document")
public class KnowledgeDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private String contentText;
    private Integer chunkCount;
    private String status;
    private Long uploadUserId;
    private String uploadUsername;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
