package org.largeorg.platform.procurement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("procurement_request")
public class ProcurementRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requestNo;
    private String title;
    private String description;
    private BigDecimal amount;
    private String category;
    private String status;
    private Long applicantId;
    private Long deptId;
    private Integer currentStep;
    private Integer totalSteps;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
