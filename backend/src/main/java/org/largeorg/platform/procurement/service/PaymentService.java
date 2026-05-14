package org.largeorg.platform.procurement.service;

import org.largeorg.platform.procurement.dto.PaymentCreateDTO;
import org.largeorg.platform.procurement.dto.PaymentUpdateDTO;
import org.largeorg.platform.procurement.entity.PaymentNode;

import java.util.List;

public interface PaymentService {
    List<PaymentNode> listByContractId(Long contractId);
    void create(PaymentCreateDTO dto);
    void update(Long id, PaymentUpdateDTO dto);
    void delete(Long id);
    void confirmPay(Long id);
}
