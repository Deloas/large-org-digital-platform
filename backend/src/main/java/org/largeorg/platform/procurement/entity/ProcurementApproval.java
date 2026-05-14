package org.largeorg.platform.procurement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("procurement_approval")
public class ProcurementApproval {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long requestId;
    private Integer stepOrder;
    private String expectedRole;
    private Long approverId;
    private String status;
    private String comment;
    private LocalDateTime approvedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
