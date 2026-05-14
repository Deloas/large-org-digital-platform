package org.largeorg.platform.knowledge.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DocumentVo {
    private Long id;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String contentText;
    private Integer chunkCount;
    private String status;
    private Long uploadUserId;
    private String uploadUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
