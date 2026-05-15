package org.largeorg.platform.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.common.BusinessException;
import org.largeorg.platform.common.ErrorCode;
import org.largeorg.platform.knowledge.entity.KnowledgeChunk;
import org.largeorg.platform.knowledge.entity.KnowledgeDocument;
import org.largeorg.platform.knowledge.mapper.KnowledgeChunkMapper;
import org.largeorg.platform.knowledge.mapper.KnowledgeDocumentMapper;
import org.largeorg.platform.knowledge.service.*;
import org.largeorg.platform.knowledge.vo.ChunkVo;
import org.largeorg.platform.knowledge.vo.DocumentVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_CONTENT_LENGTH = 100000;      // 存储最大字符数
    private static final int MAX_CHUNKS = 300;                 // 最大 chunk 数
    private static final int MAX_CHUNK_LENGTH = 2000;          // 单个 chunk 最大字符数

    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final DocumentParsingService parsingService;
    private final ChunkingService chunkingService;
    private final EmbeddingClient embeddingClient;

    @Value("${knowledge.upload-dir:./data/knowledge}")
    private String uploadDir;

    public DocumentServiceImpl(KnowledgeDocumentMapper documentMapper,
                               KnowledgeChunkMapper chunkMapper,
                               DocumentParsingService parsingService,
                               ChunkingService chunkingService,
                               EmbeddingClient embeddingClient) {
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.parsingService = parsingService;
        this.chunkingService = chunkingService;
        this.embeddingClient = embeddingClient;
    }

    @Override
    public DocumentVo upload(MultipartFile file, String title, Long userId, String username) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件大小不能超过 10MB");
        }

        String originalName = file.getOriginalFilename();
        String fileType = detectFileType(originalName);
        if (!isSupported(fileType)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的文件类型: " + fileType + "，仅支持 PDF/DOCX/TXT");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件失败");
        }

        // 保存文件到磁盘
        Path uploadPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建上传目录失败");
        }
        String storedName = UUID.randomUUID() + "." + fileType.toLowerCase();
        Path filePath = uploadPath.resolve(storedName);
        try {
            Files.write(filePath, fileBytes);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存文件失败");
        }

        // 创建文档记录（先入库，再解析，解析失败文档保留为 failed 状态）
        KnowledgeDocument doc = new KnowledgeDocument();
        try {
        doc.setTitle(title != null && !title.isBlank() ? title : originalName);
        doc.setFileName(originalName);
        doc.setFileType(fileType);
        doc.setFileSize(file.getSize());
        doc.setFilePath(filePath.toString());
        doc.setContentText("");
        doc.setStatus("processing");
        doc.setUploadUserId(userId);
        doc.setUploadUsername(username);
        documentMapper.insert(doc);

        // 解析文档文本（解析失败不抛异常，文档标记为 failed）
        String contentText = "";
        try {
            contentText = parsingService.parse(fileBytes, fileType);
            if (contentText.length() > MAX_CONTENT_LENGTH) {
                contentText = contentText.substring(0, MAX_CONTENT_LENGTH);
            }
            doc.setContentText(contentText);
        } catch (Exception e) {
            doc.setStatus("failed");
            String errMsg = e.getMessage();
            if (errMsg == null) errMsg = "未知解析错误";
            if (errMsg.length() > 500) errMsg = errMsg.substring(0, 500);
            doc.setContentText("PDF 无可提取文本，可能是扫描版、加密文件或特殊编码文件。错误: " + errMsg);
            documentMapper.updateById(doc);
            return toVo(doc);
        }

        // 文本切分 + embedding（限制 chunk 数量，防止超大数据集）
        try {
            List<String> rawChunks = chunkingService.split(contentText);
            // 限制最大 chunk 数
            List<String> chunks = rawChunks.size() > MAX_CHUNKS
                    ? rawChunks.subList(0, MAX_CHUNKS) : rawChunks;
            int successCount = 0;
            for (int i = 0; i < chunks.size(); i++) {
                try {
                    String chunkContent = chunks.get(i);
                    if (chunkContent.length() > MAX_CHUNK_LENGTH) {
                        chunkContent = chunkContent.substring(0, MAX_CHUNK_LENGTH);
                    }
                    KnowledgeChunk ck = new KnowledgeChunk();
                    ck.setDocumentId(doc.getId());
                    ck.setChunkIndex(i);
                    ck.setContent(chunkContent);
                    ck.setCharCount(chunkContent.length());
                    float[] emb = embeddingClient.embed(chunkContent);
                    ck.setEmbedding(toJsonArray(emb));
                    chunkMapper.insert(ck);
                    successCount++;
                } catch (Exception ignored) {
                    // 单个 chunk 失败不影响其他 chunk
                }
            }
            doc.setChunkCount(successCount);
            doc.setStatus(successCount > 0 ? "ready" : "partial");
        } catch (Exception e) {
            doc.setStatus("partial");
        }
        documentMapper.updateById(doc);

        return toVo(doc);
        } catch (Exception outerEx) {
            // 兜底：任何未预见的异常都不应返回 500
            try {
                doc.setStatus("failed");
                doc.setContentText("处理异常: " + (outerEx.getMessage() != null ? outerEx.getMessage().substring(0, Math.min(outerEx.getMessage().length(), 400)) : "未知错误"));
                documentMapper.updateById(doc);
            } catch (Exception ignored) {}
            return toVo(doc);
        }
    }

    @Override
    public Page<DocumentVo> page(int pageNum, int pageSize, String keyword, String fileType, String status) {
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(KnowledgeDocument::getTitle, keyword)
                    .or().like(KnowledgeDocument::getFileName, keyword));
        }
        if (StringUtils.hasText(fileType)) {
            wrapper.eq(KnowledgeDocument::getFileType, fileType.toUpperCase());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(KnowledgeDocument::getStatus, status);
        }
        wrapper.orderByDesc(KnowledgeDocument::getCreatedAt);
        Page<KnowledgeDocument> page = new Page<>(pageNum, pageSize);
        Page<KnowledgeDocument> result = documentMapper.selectPage(page, wrapper);
        Page<DocumentVo> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public DocumentVo getById(Long id) {
        KnowledgeDocument doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文档不存在");
        }
        return toVo(doc);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        KnowledgeDocument doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文档不存在");
        }
        // 删除 chunk
        LambdaQueryWrapper<KnowledgeChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeChunk::getDocumentId, id);
        chunkMapper.delete(wrapper);
        // 删除文件
        if (doc.getFilePath() != null) {
            try { Files.deleteIfExists(Paths.get(doc.getFilePath())); } catch (IOException ignored) {}
        }
        documentMapper.deleteById(id);
    }

    @Override
    public List<ChunkVo> getChunks(Long documentId) {
        LambdaQueryWrapper<KnowledgeChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeChunk::getDocumentId, documentId)
               .orderByAsc(KnowledgeChunk::getChunkIndex);
        return chunkMapper.selectList(wrapper).stream().map(c -> ChunkVo.builder()
                .id(c.getId())
                .documentId(c.getDocumentId())
                .chunkIndex(c.getChunkIndex())
                .content(c.getContent())
                .charCount(c.getCharCount())
                .build()).collect(Collectors.toList());
    }

    private String detectFileType(String fileName) {
        if (fileName == null) return "UNKNOWN";
        String upper = fileName.toUpperCase();
        if (upper.endsWith(".PDF")) return "PDF";
        if (upper.endsWith(".DOCX")) return "DOCX";
        if (upper.endsWith(".TXT")) return "TXT";
        return upper.contains(".") ? upper.substring(upper.lastIndexOf('.') + 1) : "UNKNOWN";
    }

    private boolean isSupported(String fileType) {
        return "PDF".equals(fileType) || "DOCX".equals(fileType) || "TXT".equals(fileType);
    }

    private DocumentVo toVo(KnowledgeDocument doc) {
        return DocumentVo.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .fileName(doc.getFileName())
                .fileType(doc.getFileType())
                .fileSize(doc.getFileSize())
                .contentText(doc.getContentText())
                .chunkCount(doc.getChunkCount())
                .status(doc.getStatus())
                .uploadUserId(doc.getUploadUserId())
                .uploadUsername(doc.getUploadUsername())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private String toJsonArray(float[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", arr[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
