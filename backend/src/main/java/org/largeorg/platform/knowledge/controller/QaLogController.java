package org.largeorg.platform.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.knowledge.dto.QaLogQueryRequest;
import org.largeorg.platform.knowledge.service.QaLogService;
import org.largeorg.platform.knowledge.vo.QaLogVo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge/qa/logs")
public class QaLogController {

    private final QaLogService qaLogService;

    public QaLogController(QaLogService qaLogService) {
        this.qaLogService = qaLogService;
    }

    @GetMapping
    @SaCheckPermission("knowledge:qa:log")
    public Result<Page<QaLogVo>> list(@ModelAttribute QaLogQueryRequest request) {
        return Result.success(qaLogService.page(request));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("knowledge:qa:log")
    public Result<QaLogVo> getById(@PathVariable Long id) {
        return Result.success(qaLogService.getById(id));
    }
}
