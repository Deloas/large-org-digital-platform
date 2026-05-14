package org.largeorg.platform.knowledge.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkVo {
    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String content;
    private Integer charCount;
}
