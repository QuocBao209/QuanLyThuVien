package com.project.demo.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class UserCodes {
    private static final Map<String, String> ERROR_MESSAGES = new HashMap<>();
    private static final Map<String, String> SUCCESS_MESSAGES = new HashMap<>();

    static {
        loadMessages("error/userErrorCode", ERROR_MESSAGES);
        loadMessages("error/userSuccessCode", SUCCESS_MESSAGES);
    }

    private static void loadMessages(String filePath, Map<String, String> map) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                reader.lines().skip(1).forEach(line -> {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) {
                        map.put(parts[0].trim(), parts[1].trim());
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + filePath + " - " + e.getMessage());
        }
    }


    public static String getErrorMessage(String code) {
        return ERROR_MESSAGES.getOrDefault(code, "Mã lỗi không xác định.");
    }

    public static String getSuccessMessage(String code) {
        return SUCCESS_MESSAGES.getOrDefault(code, "Mã thành công không xác định.");
    }
}
