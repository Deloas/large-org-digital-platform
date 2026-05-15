package org.largeorg.platform.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.knowledge.service.DocumentService;
import org.largeorg.platform.knowledge.vo.ChunkVo;
import org.largeorg.platform.knowledge.vo.DocumentVo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @AuditLog(module = "文档管理", action = "上传文档")
    @PostMapping
    @SaCheckRole("admin")
    public Result<DocumentVo> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "title", required = false) String title) {
        long userId = StpUtil.getLoginIdAsLong();
        String username = (String) StpUtil.getSession().get("username");
        return Result.success(documentService.upload(file, title, userId, username));
    }

    @GetMapping
    @SaCheckPermission("knowledge:doc:list")
    public Result<Page<DocumentVo>> list(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String fileType,
                                          @RequestParam(required = false) String status) {
        return Result.success(documentService.page(pageNum, pageSize, keyword, fileType, status));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("knowledge:doc:list")
    public Result<DocumentVo> getById(@PathVariable Long id) {
        return Result.success(documentService.getById(id));
    }

    @AuditLog(module = "文档管理", action = "删除文档")
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/chunks")
    @SaCheckPermission("knowledge:doc:list")
    public Result<List<ChunkVo>> getChunks(@PathVariable Long id) {
        return Result.success(documentService.getChunks(id));
    }
}
