package org.largeorg.platform.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("security_alert")
public class SecurityAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String alertType;
    private String severity;
    private String title;
    private String detail;
    private String relatedUser;
    private String relatedIp;
    private String status;
    private String handler;
    private String handleNote;
    private Integer duplicateCount;
    private LocalDateTime firstTime;
    private LocalDateTime lastTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
