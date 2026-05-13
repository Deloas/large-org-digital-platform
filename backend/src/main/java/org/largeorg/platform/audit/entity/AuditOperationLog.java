package org.largeorg.platform.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_operation_log")
public class AuditOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String requestPath;
    private String requestMethod;
    private String requestParams;
    private String result;
    private String errorMsg;
    private Long costMs;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;
}
