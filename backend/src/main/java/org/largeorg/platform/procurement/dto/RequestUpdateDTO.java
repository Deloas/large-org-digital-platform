package org.largeorg.platform.procurement.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RequestUpdateDTO {
    private String title;
    private String description;
    private BigDecimal amount;
    private String category;
}
