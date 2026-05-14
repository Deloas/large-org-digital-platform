package org.largeorg.platform.knowledge.vo;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QaLogVo {
    private Long id;
    private Long userId;
    private String username;
    private String question;
    private String answer;
    private List<QaResultVo.SourceVo> sources;
    private BigDecimal confidence;
    private String status;
    private Long costMs;
    private LocalDateTime createdAt;
}
