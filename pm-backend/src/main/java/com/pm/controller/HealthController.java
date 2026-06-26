package com.pm.controller;

import com.pm.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("time", LocalDateTime.now());
        data.put("profiles", environment.getActiveProfiles());
        data.put("database", checkDatabase());
        return Result.ok(data);
    }

    private String checkDatabase() {
        try {
            Integer value = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(value) ? "UP" : "UNKNOWN";
        } catch (Exception e) {
            return "DOWN";
        }
    }
}
