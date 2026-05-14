package org.largeorg.platform.procurement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierCreateDTO {
    @NotBlank(message = "供应商名称不能为空")
    private String name;
    private String contactPerson;
    private String contactPhone;
    private String email;
    private String address;
    private String qualification;
}
