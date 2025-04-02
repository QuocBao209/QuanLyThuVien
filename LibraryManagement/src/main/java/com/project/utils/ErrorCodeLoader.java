package com.project.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ErrorCodeLoader {
    private final Map<String, String> errorMessages = new HashMap<>();

    public ErrorCodeLoader() {
        loadErrorMessages();
    }

    private void loadErrorMessages() {
        try {
            ClassPathResource resource = new ClassPathResource("error_codes.csv");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                errorMessages.putAll(br.lines()
                        .skip(1)
                        .map(line -> line.split(",", 2))
                        .filter(parts -> parts.length == 2)
                        .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim())));
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tải file error_codes.csv: " + e.getMessage());
        }
    }

    public String getErrorMessage(String errorCode) {
        return errorMessages.getOrDefault(errorCode, "Lỗi không xác định.");
    }
}
