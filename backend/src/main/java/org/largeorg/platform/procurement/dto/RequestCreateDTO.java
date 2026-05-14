package org.largeorg.platform.procurement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RequestCreateDTO {
    @NotBlank(message = "采购标题不能为空")
    private String title;
    private String description;
    @NotNull(message = "采购金额不能为空")
    private BigDecimal amount;
    private String category;
}
