package org.largeorg.platform.knowledge.service.impl;

import org.largeorg.platform.knowledge.service.ChatModelClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MockChatModelClient implements ChatModelClient {

    /**
     * 基于关键词匹配的 mock chat，始终返回带 [来源] 引用的答案。
     * 如果在 context 中找不到关键词匹配，返回特殊哨兵值 NO_MATCH，
     * 由调用方 QaService 替换为拒答文案。
     */
    private static final String NO_MATCH = "<<NO_MATCH>>";

    @Override
    public String chat(String systemPrompt, String userQuestion) {
        String contextPart = extractContext(systemPrompt);
        if (contextPart == null || contextPart.isBlank()) {
            return NO_MATCH;
        }

        String[] segments = contextPart.split("(?=\\[来源\\d+\\])");

        List<String> matchedSegments = new ArrayList<>();
        List<String> sourceLabels = new ArrayList<>();

        Pattern sourcePattern = Pattern.compile("\\[来源(\\d+)\\]");

        for (String seg : segments) {
            if (seg.isBlank()) continue;
            Matcher m = sourcePattern.matcher(seg);
            String label = m.find() ? m.group(0) : "";
            if (hasKeywordOverlap(userQuestion, seg)) {
                matchedSegments.add(seg);
                sourceLabels.add(label);
            }
        }

        if (matchedSegments.isEmpty()) {
            return NO_MATCH;
        }

        StringBuilder answer = new StringBuilder();
        answer.append("根据知识库中的相关制度文件，为您梳理如下：\n\n");

        for (int i = 0; i < matchedSegments.size(); i++) {
            answer.append(sourceLabels.get(i)).append("\n");
            answer.append(summarize(matchedSegments.get(i))).append("\n\n");
        }

        answer.append("以上内容均来源于已上传的制度文件，如有疑问请查阅原文。");
        return answer.toString();
    }

    private String extractContext(String systemPrompt) {
        String marker = "参考资料：\n";
        int idx = systemPrompt.indexOf(marker);
        if (idx < 0) return null;
        return systemPrompt.substring(idx + marker.length());
    }

    private boolean hasKeywordOverlap(String question, String segment) {
        String q = question.replaceAll("[，。、；：？！\\s]+", "");
        String s = segment.replaceAll("[，。、；：？！\\s\\[\\]0-9来源]+", "");

        // 按双字符滑动窗口匹配
        for (int i = 0; i < q.length() - 1; i++) {
            String bigram = q.substring(i, i + 2);
            if (s.contains(bigram)) {
                return true;
            }
        }

        // fallback: 单字匹配至少 3 个
        int match = 0;
        for (char c : q.toCharArray()) {
            if (s.indexOf(c) >= 0) {
                match++;
            }
        }
        return match >= 3;
    }

    private String summarize(String segment) {
        String clean = segment.replaceAll("\\[来源\\d+\\]", "").trim();
        if (clean.length() <= 160) {
            return clean;
        }
        return clean.substring(0, 157) + "...";
    }
}
