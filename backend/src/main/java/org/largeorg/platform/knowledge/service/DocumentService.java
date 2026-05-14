package org.largeorg.platform.knowledge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.knowledge.entity.KnowledgeDocument;
import org.largeorg.platform.knowledge.vo.ChunkVo;
import org.largeorg.platform.knowledge.vo.DocumentVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentVo upload(MultipartFile file, String title, Long userId, String username);
    Page<DocumentVo> page(int pageNum, int pageSize, String keyword, String fileType, String status);
    DocumentVo getById(Long id);
    void delete(Long id);
    List<ChunkVo> getChunks(Long documentId);
}
