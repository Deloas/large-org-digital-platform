package org.largeorg.platform.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.largeorg.platform.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("app", "large-org-platform");
        info.put("status", "UP");
        info.put("time", LocalDateTime.now().toString());

        try (Connection conn = dataSource.getConnection()) {
            info.put("db", conn.isValid(3) ? "UP" : "UNAVAILABLE");
        } catch (Exception e) {
            log.warn("数据库连接检查失败: {}", e.getMessage());
            info.put("db", "UNAVAILABLE");
        }

        return Result.success(info);
    }
}
