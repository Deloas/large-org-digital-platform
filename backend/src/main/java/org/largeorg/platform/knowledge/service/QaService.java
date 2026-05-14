package org.largeorg.platform.knowledge.service;

import org.largeorg.platform.knowledge.vo.QaResultVo;

public interface QaService {
    QaResultVo ask(String question, Long userId, String username);
}
