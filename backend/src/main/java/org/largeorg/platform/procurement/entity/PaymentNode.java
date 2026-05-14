package org.largeorg.platform.procurement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("payment_node")
public class PaymentNode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contractId;
    private String nodeName;
    private BigDecimal amount;
    private BigDecimal ratio;
    private LocalDate plannedDate;
    private LocalDate actualDate;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
