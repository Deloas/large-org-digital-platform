package org.largeorg.platform.knowledge.service.impl;

import org.largeorg.platform.knowledge.service.EmbeddingClient;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Component
public class MockEmbeddingClient implements EmbeddingClient {

    private static final int DIM = 128;

    @Override
    public int dimension() {
        return DIM;
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isEmpty()) {
            float[] zero = new float[DIM];
            Arrays.fill(zero, 0f);
            return zero;
        }
        float[] vec = new float[DIM];
        text = text.toLowerCase().trim();

        // char unigram feature hashing
        for (int i = 0; i < text.length(); i++) {
            int c = text.charAt(i);
            int idx = hash(c, 0) % DIM;
            vec[idx] += 1.0f;
        }

        // char bigram feature hashing
        for (int i = 0; i < text.length() - 1; i++) {
            int c1 = text.charAt(i);
            int c2 = text.charAt(i + 1);
            int idx = hash(c1, c2) % DIM;
            vec[idx] += 0.8f;
        }

        // L2 normalize
        float norm = 0f;
        for (float v : vec) {
            norm += v * v;
        }
        if (norm > 0) {
            norm = (float) Math.sqrt(norm);
            for (int i = 0; i < DIM; i++) {
                vec[i] /= norm;
            }
        }

        return vec;
    }

    private int hash(int a, int b) {
        int h = 17;
        h = h * 31 + a;
        h = h * 31 + b;
        return Math.abs(h);
    }
}
