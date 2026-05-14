package org.largeorg.platform.procurement.dto;

import lombok.Data;

@Data
public class SupplierUpdateDTO {
    private String name;
    private String contactPerson;
    private String contactPhone;
    private String email;
    private String address;
    private String qualification;
}
