package org.largeorg.platform.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import org.largeorg.platform.audit.annotation.AuditLog;
import org.largeorg.platform.common.Result;
import org.largeorg.platform.knowledge.dto.QuestionRequest;
import org.largeorg.platform.knowledge.service.QaService;
import org.largeorg.platform.knowledge.vo.QaResultVo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge/qa")
public class QaController {

    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
    }

    @AuditLog(module = "智能问答", action = "提交问题")
    @PostMapping("/ask")
    @SaCheckPermission("knowledge:qa:ask")
    public Result<QaResultVo> ask(@Valid @RequestBody QuestionRequest request) {
        long userId = StpUtil.getLoginIdAsLong();
        String username = (String) StpUtil.getSession().get("username");
        return Result.success(qaService.ask(request.getQuestion(), userId, username));
    }
}
