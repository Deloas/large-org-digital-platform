package org.largeorg.platform.procurement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentCreateDTO {
    @NotNull(message = "关联合同ID不能为空")
    private Long contractId;
    @NotBlank(message = "付款节点名称不能为空")
    private String nodeName;
    @NotNull(message = "付款金额不能为空")
    private BigDecimal amount;
    private BigDecimal ratio;
    private LocalDate plannedDate;
}
