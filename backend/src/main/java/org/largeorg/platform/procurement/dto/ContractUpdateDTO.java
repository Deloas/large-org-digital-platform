package org.largeorg.platform.procurement.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractUpdateDTO {
    private String title;
    private BigDecimal amount;
    private LocalDate signedDate;
    private LocalDate expiryDate;
}
