package org.largeorg.platform.audit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;

public final class AuditParamSanitizer {

    private AuditParamSanitizer() {}

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "oldPassword", "newPassword", "confirmPassword",
            "token", "satoken", "authorization", "Authorization",
            "accessToken", "refreshToken"
    );

    private static final Set<Class<?>> SKIP_TYPES = Set.of(
            HttpServletRequest.class, HttpServletResponse.class,
            MultipartFile.class, InputStream.class, OutputStream.class
    );

    private static final int MAX_PARAMS_LENGTH = 2000;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Object[] filterArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return new Object[0];
        }
        return Arrays.stream(args)
                .filter(arg -> arg != null)
                .filter(arg -> SKIP_TYPES.stream().noneMatch(t -> t.isInstance(arg)))
                .toArray();
    }

    public static String serializeAndMask(Object[] filteredArgs) {
        try {
            if (filteredArgs == null || filteredArgs.length == 0) {
                return null;
            }
            String json = OBJECT_MAPPER.writeValueAsString(filteredArgs);
            json = maskSensitive(json);
            return truncate(json, MAX_PARAMS_LENGTH);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String maskSensitive(String json) {
        if (json == null) {
            return null;
        }
        for (String field : SENSITIVE_FIELDS) {
            json = json.replaceAll(
                    "\"" + field + "\"\\s*:\\s*\"[^\"]*\"",
                    "\"" + field + "\":\"***\"");
        }
        return json;
    }

    public static String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() <= maxLen ? str : str.substring(0, maxLen);
    }
}
