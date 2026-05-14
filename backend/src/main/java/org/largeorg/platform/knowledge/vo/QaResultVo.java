package org.largeorg.platform.knowledge.vo;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class QaResultVo {
    private String question;
    private String answer;
    private BigDecimal confidence;
    private String status;
    private Long costMs;
    private List<SourceVo> sources;
    private String disclaimer;

    @Data
    @Builder
    public static class SourceVo {
        private Long documentId;
        private String documentTitle;
        private Integer chunkIndex;
        private String snippet;
    }
}
