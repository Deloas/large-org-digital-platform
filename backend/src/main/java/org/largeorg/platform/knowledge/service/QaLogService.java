package org.largeorg.platform.knowledge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.knowledge.dto.QaLogQueryRequest;
import org.largeorg.platform.knowledge.vo.QaLogVo;

public interface QaLogService {
    Page<QaLogVo> page(QaLogQueryRequest request);
    QaLogVo getById(Long id);
}
