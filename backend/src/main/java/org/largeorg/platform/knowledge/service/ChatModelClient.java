package org.largeorg.platform.knowledge.service;

public interface ChatModelClient {
    String chat(String systemPrompt, String userQuestion);
}
