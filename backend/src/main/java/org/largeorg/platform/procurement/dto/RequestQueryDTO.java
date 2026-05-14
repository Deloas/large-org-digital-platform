package org.largeorg.platform.procurement.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RequestQueryDTO {
    private String keyword;
    private String status;
    private BigDecimal amountMin;
    private BigDecimal amountMax;
    private String startDate;
    private String endDate;
}
