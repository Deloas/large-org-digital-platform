package org.largeorg.platform.knowledge.service;

public interface EmbeddingClient {
    float[] embed(String text);
    int dimension();
}
