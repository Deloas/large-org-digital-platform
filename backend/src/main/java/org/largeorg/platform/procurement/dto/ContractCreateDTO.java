package org.largeorg.platform.procurement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractCreateDTO {
    @NotNull(message = "关联采购申请ID不能为空")
    private Long requestId;
    @NotNull(message = "关联供应商ID不能为空")
    private Long supplierId;
    @NotBlank(message = "合同标题不能为空")
    private String title;
    @NotNull(message = "合同金额不能为空")
    private BigDecimal amount;
    private LocalDate signedDate;
    private LocalDate expiryDate;
}
