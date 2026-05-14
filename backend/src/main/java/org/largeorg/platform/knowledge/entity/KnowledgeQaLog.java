package org.largeorg.platform.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_qa_log")
public class KnowledgeQaLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String question;
    private String answer;
    private String sources;
    private BigDecimal confidence;
    private String status;
    private Long costMs;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
