package org.largeorg.platform.procurement.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentUpdateDTO {
    private String nodeName;
    private BigDecimal amount;
    private BigDecimal ratio;
    private LocalDate plannedDate;
}
