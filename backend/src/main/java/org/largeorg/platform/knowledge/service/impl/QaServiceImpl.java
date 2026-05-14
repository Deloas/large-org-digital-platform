package org.largeorg.platform.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.largeorg.platform.knowledge.entity.KnowledgeChunk;
import org.largeorg.platform.knowledge.entity.KnowledgeDocument;
import org.largeorg.platform.knowledge.entity.KnowledgeQaLog;
import org.largeorg.platform.knowledge.mapper.KnowledgeChunkMapper;
import org.largeorg.platform.knowledge.mapper.KnowledgeDocumentMapper;
import org.largeorg.platform.knowledge.mapper.KnowledgeQaLogMapper;
import org.largeorg.platform.knowledge.service.ChatModelClient;
import org.largeorg.platform.knowledge.service.EmbeddingClient;
import org.largeorg.platform.knowledge.service.QaService;
import org.largeorg.platform.knowledge.vo.QaResultVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QaServiceImpl implements QaService {

    private static final int TOP_K = 5;
    private static final float SIMILARITY_THRESHOLD = 0.3f;
    private static final String NO_MATCH = "<<NO_MATCH>>";
    private static final String NO_ANSWER = "当前知识库未找到足够依据，无法回答该问题";

    private final KnowledgeChunkMapper chunkMapper;
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeQaLogMapper qaLogMapper;
    private final EmbeddingClient embeddingClient;
    private final ChatModelClient chatModelClient;

    public QaServiceImpl(KnowledgeChunkMapper chunkMapper,
                         KnowledgeDocumentMapper documentMapper,
                         KnowledgeQaLogMapper qaLogMapper,
                         EmbeddingClient embeddingClient,
                         ChatModelClient chatModelClient) {
        this.chunkMapper = chunkMapper;
        this.documentMapper = documentMapper;
        this.qaLogMapper = qaLogMapper;
        this.embeddingClient = embeddingClient;
        this.chatModelClient = chatModelClient;
    }

    @Override
    @Transactional
    public QaResultVo ask(String question, Long userId, String username) {
        long start = System.currentTimeMillis();

        // 1. 加载所有状态为 ready 的文档的 chunk
        List<KnowledgeDocument> readyDocs = documentMapper.selectList(
                new LambdaQueryWrapper<KnowledgeDocument>().eq(KnowledgeDocument::getStatus, "ready"));
        if (readyDocs.isEmpty()) {
            long costMs = System.currentTimeMillis() - start;
            return buildNoMatchResult(question, costMs);
        }

        Set<Long> readyDocIds = readyDocs.stream().map(KnowledgeDocument::getId).collect(Collectors.toSet());
        List<KnowledgeChunk> allChunks = chunkMapper.selectList(
                new LambdaQueryWrapper<KnowledgeChunk>().in(KnowledgeChunk::getDocumentId, readyDocIds));
        if (allChunks.isEmpty()) {
            long costMs = System.currentTimeMillis() - start;
            return buildNoMatchResult(question, costMs);
        }

        // 2. 问题 embedding
        float[] questionEmb = embeddingClient.embed(question);

        // 3. 计算综合相似度并排序
        Map<Long, KnowledgeDocument> docMap = readyDocs.stream()
                .collect(Collectors.toMap(KnowledgeDocument::getId, d -> d));

        List<ScoredChunk> scoredChunks = new ArrayList<>();
        for (KnowledgeChunk chunk : allChunks) {
            float[] chunkEmb = parseEmbedding(chunk.getEmbedding());
            if (chunkEmb == null) continue;

            float cosineSim = cosineSimilarity(questionEmb, chunkEmb);
            float keywordSim = keywordOverlapScore(question, chunk.getContent());

            // 综合分数：向量相似度 0.6 + 关键词重合度 0.4
            float combinedScore = cosineSim * 0.6f + keywordSim * 0.4f;

            if (combinedScore >= SIMILARITY_THRESHOLD) {
                scoredChunks.add(new ScoredChunk(chunk, combinedScore));
            }
        }

        // 4. 如果无合格结果，拒答
        if (scoredChunks.isEmpty()) {
            long costMs = System.currentTimeMillis() - start;
            saveLog(question, NO_ANSWER, "[]", BigDecimal.ZERO, "no_match", costMs, userId, username);
            return buildNoMatchResult(question, costMs);
        }

        // 5. 取 top-K
        scoredChunks.sort((a, b) -> Float.compare(b.score, a.score));
        List<ScoredChunk> topChunks = scoredChunks.subList(0, Math.min(TOP_K, scoredChunks.size()));

        // 6. 构建 context prompt（带来源编号）
        StringBuilder contextBuilder = new StringBuilder();
        List<QaResultVo.SourceVo> sources = new ArrayList<>();
        for (int i = 0; i < topChunks.size(); i++) {
            ScoredChunk sc = topChunks.get(i);
            KnowledgeDocument doc = docMap.get(sc.chunk.getDocumentId());
            String docTitle = doc != null ? doc.getTitle() : "未知文档";
            String label = "[来源" + (i + 1) + "]";
            contextBuilder.append(label).append(" 文档《").append(docTitle)
                    .append("》第").append(sc.chunk.getChunkIndex()).append("段：\n");
            contextBuilder.append(sc.chunk.getContent()).append("\n\n");

            String snippet = sc.chunk.getContent().length() > 120
                    ? sc.chunk.getContent().substring(0, 117) + "..."
                    : sc.chunk.getContent();
            sources.add(QaResultVo.SourceVo.builder()
                    .documentId(sc.chunk.getDocumentId())
                    .documentTitle(docTitle)
                    .chunkIndex(sc.chunk.getChunkIndex())
                    .snippet(snippet)
                    .build());
        }

        String systemPrompt = "你是一个制度文件问答助手。请仅根据以下参考资料回答问题，不要编造信息。"
                + "如果资料中找不到答案，请明确说明。回答中必须标注引用来源编号。\n\n参考资料：\n" + contextBuilder;

        // 7. 调用 mock chat model
        String rawAnswer = chatModelClient.chat(systemPrompt, question);

        long costMs = System.currentTimeMillis() - start;

        // 8. 处理拒答
        if (NO_MATCH.equals(rawAnswer)) {
            saveLog(question, NO_ANSWER, toSourcesJson(sources), BigDecimal.ZERO, "no_match", costMs, userId, username);
            return buildNoMatchResult(question, costMs);
        }

        // 9. 计算置信度
        float avgScore = (float) topChunks.stream().mapToDouble(sc -> sc.score).average().orElse(0);
        BigDecimal confidence = BigDecimal.valueOf(avgScore).setScale(4, RoundingMode.HALF_UP);

        // 10. 保存日志
        saveLog(question, rawAnswer, toSourcesJson(sources), confidence, "answered", costMs, userId, username);

        return QaResultVo.builder()
                .question(question)
                .answer(rawAnswer)
                .confidence(confidence)
                .status("answered")
                .costMs(costMs)
                .sources(sources)
                .disclaimer("本回答基于知识库中已上传的制度文件生成，仅供参考，不构成法律或合规建议。")
                .build();
    }

    private QaResultVo buildNoMatchResult(String question, long costMs) {
        return QaResultVo.builder()
                .question(question)
                .answer(NO_ANSWER)
                .confidence(BigDecimal.ZERO)
                .status("no_match")
                .costMs(costMs)
                .sources(Collections.emptyList())
                .disclaimer("本回答基于知识库中已上传的制度文件生成，仅供参考，不构成法律或合规建议。")
                .build();
    }

    private void saveLog(String question, String answer, String sourcesJson,
                         BigDecimal confidence, String status, long costMs,
                         Long userId, String username) {
        KnowledgeQaLog log = new KnowledgeQaLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setQuestion(question);
        log.setAnswer(answer);
        log.setSources(sourcesJson);
        log.setConfidence(confidence);
        log.setStatus(status);
        log.setCostMs(costMs);
        qaLogMapper.insert(log);
    }

    private String toSourcesJson(List<QaResultVo.SourceVo> sources) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < sources.size(); i++) {
            if (i > 0) sb.append(",");
            QaResultVo.SourceVo s = sources.get(i);
            sb.append("{\"documentId\":").append(s.getDocumentId())
              .append(",\"documentTitle\":\"").append(escapeJson(s.getDocumentTitle())).append("\"")
              .append(",\"chunkIndex\":").append(s.getChunkIndex())
              .append(",\"snippet\":\"").append(escapeJson(s.getSnippet())).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private float[] parseEmbedding(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            String inner = json.trim();
            if (inner.startsWith("[")) inner = inner.substring(1);
            if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);
            String[] parts = inner.split(",");
            float[] result = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Float.parseFloat(parts[i].trim());
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0f;
        float dot = 0f, normA = 0f, normB = 0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0f;
        return dot / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private float keywordOverlapScore(String question, String chunk) {
        String q = question.replaceAll("[，。、；：？！\\s]+", "");
        String c = chunk.replaceAll("[，。、；：？！\\s]+", "");

        // bigram overlap
        Set<String> qBigrams = new HashSet<>();
        for (int i = 0; i < q.length() - 1; i++) {
            qBigrams.add(q.substring(i, i + 2));
        }
        int overlap = 0;
        for (int i = 0; i < c.length() - 1; i++) {
            if (qBigrams.contains(c.substring(i, i + 2))) {
                overlap++;
            }
        }
        if (qBigrams.isEmpty()) return 0f;
        return Math.min(1f, (float) overlap / qBigrams.size());
    }

    private static class ScoredChunk {
        final KnowledgeChunk chunk;
        final float score;
        ScoredChunk(KnowledgeChunk chunk, float score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}
