package org.largeorg.platform.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ip_blacklist")
public class IpBlacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ipAddress;
    private String reason;
    private Integer status;
    private LocalDateTime expiresAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
