package org.largeorg.platform.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.largeorg.platform.knowledge.dto.QaLogQueryRequest;
import org.largeorg.platform.knowledge.entity.KnowledgeQaLog;
import org.largeorg.platform.knowledge.mapper.KnowledgeQaLogMapper;
import org.largeorg.platform.knowledge.service.QaLogService;
import org.largeorg.platform.knowledge.vo.QaLogVo;
import org.largeorg.platform.knowledge.vo.QaResultVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QaLogServiceImpl implements QaLogService {

    private final KnowledgeQaLogMapper qaLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QaLogServiceImpl(KnowledgeQaLogMapper qaLogMapper) {
        this.qaLogMapper = qaLogMapper;
    }

    @Override
    public Page<QaLogVo> page(QaLogQueryRequest request) {
        LambdaQueryWrapper<KnowledgeQaLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(KnowledgeQaLog::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(KnowledgeQaLog::getStatus, request.getStatus());
        }
        if (StringUtils.hasText(request.getStartTime())) {
            wrapper.ge(KnowledgeQaLog::getCreatedAt, request.getStartTime());
        }
        if (StringUtils.hasText(request.getEndTime())) {
            wrapper.le(KnowledgeQaLog::getCreatedAt, request.getEndTime());
        }
        wrapper.orderByDesc(KnowledgeQaLog::getCreatedAt);
        Page<KnowledgeQaLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<KnowledgeQaLog> result = qaLogMapper.selectPage(page, wrapper);
        Page<QaLogVo> voPage = new Page<>(request.getPageNum(), request.getPageSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public QaLogVo getById(Long id) {
        KnowledgeQaLog log = qaLogMapper.selectById(id);
        if (log == null) return null;
        return toVo(log);
    }

    private QaLogVo toVo(KnowledgeQaLog log) {
        List<QaResultVo.SourceVo> sources = parseSources(log.getSources());
        return QaLogVo.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .question(log.getQuestion())
                .answer(log.getAnswer())
                .sources(sources)
                .confidence(log.getConfidence())
                .status(log.getStatus())
                .costMs(log.getCostMs())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private List<QaResultVo.SourceVo> parseSources(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<QaResultVo.SourceVo>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
