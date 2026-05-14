package org.largeorg.platform.knowledge.service.impl;

import org.largeorg.platform.knowledge.service.ChunkingService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChunkingServiceImpl implements ChunkingService {

    private static final int TARGET_LENGTH = 800;
    private static final int OVERLAP = 100;
    private static final int MIN_LENGTH = 100;

    @Override
    public List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        // 按段落（双换行）切分
        String[] paragraphs = text.split("\n\n+");

        StringBuilder buffer = new StringBuilder();

        for (String para : paragraphs) {
            String p = para.trim();
            if (p.isEmpty()) continue;

            if (p.length() > TARGET_LENGTH) {
                // 先 flush buffer
                flushBuffer(buffer, chunks);
                // 超长段落强制按 TARGET_LENGTH 切分
                splitLongParagraph(p, chunks);
                continue;
            }

            if (p.length() < MIN_LENGTH) {
                // 短段落：尝试合并到 buffer
                if (buffer.length() > 0 && buffer.length() + p.length() + 1 > TARGET_LENGTH) {
                    flushBuffer(buffer, chunks);
                }
                appendToBuffer(buffer, p);
                continue;
            }

            // 正常段落
            if (buffer.length() > 0 && buffer.length() + p.length() + 1 > TARGET_LENGTH) {
                flushBuffer(buffer, chunks);
            }
            appendToBuffer(buffer, p);

            if (buffer.length() >= TARGET_LENGTH) {
                flushBuffer(buffer, chunks);
            }
        }

        flushBuffer(buffer, chunks);

        // 为相邻 chunk 添加 overlap
        return addOverlap(chunks);
    }

    private void appendToBuffer(StringBuilder buf, String text) {
        if (buf.length() > 0) {
            buf.append("\n");
        }
        buf.append(text);
    }

    private void flushBuffer(StringBuilder buf, List<String> chunks) {
        if (buf.length() > 0) {
            chunks.add(buf.toString());
            buf.setLength(0);
        }
    }

    private void splitLongParagraph(String text, List<String> chunks) {
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + TARGET_LENGTH, text.length());
            if (end < text.length()) {
                // 尝试在句号、分号、换行处断句
                int breakPoint = -1;
                for (int i = end; i > start + TARGET_LENGTH / 2; i--) {
                    char c = text.charAt(i);
                    if (c == '。' || c == '；' || c == '\n') {
                        breakPoint = i + 1;
                        break;
                    }
                }
                if (breakPoint > start) {
                    end = breakPoint;
                }
            }
            chunks.add(text.substring(start, end).trim());
            start = end;
        }
    }

    private List<String> addOverlap(List<String> chunks) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            StringBuilder sb = new StringBuilder(chunks.get(i));
            if (i > 0) {
                // 从前一个 chunk 尾部取 overlap 字符
                String prev = chunks.get(i - 1);
                int ol = Math.min(OVERLAP, prev.length());
                if (ol > 0) {
                    sb.insert(0, "..." + prev.substring(prev.length() - ol) + "\n");
                }
            }
            result.add(sb.toString());
        }
        return result;
    }
}
