package com.project.demo.startup;

import com.project.demo.service.DataTransferService;
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
