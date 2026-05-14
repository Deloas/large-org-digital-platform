package org.largeorg.platform.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentUploadRequest {
    @NotBlank(message = "文档标题不能为空")
    private String title;
}
