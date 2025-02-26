package com.project.admin.startup;

import com.project.admin.service.DataTransferService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {
    private final DataTransferService dataTransferService;

    public StartupRunner(DataTransferService dataTransferService) {
        this.dataTransferService = dataTransferService;
    }

    @Override
    public void run(String... args) {
        dataTransferService.transferData();
    }
}
