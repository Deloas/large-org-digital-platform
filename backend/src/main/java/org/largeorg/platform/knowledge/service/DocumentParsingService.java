package org.largeorg.platform.knowledge.service;

public interface DocumentParsingService {
    String parse(byte[] fileBytes, String fileType);
}
