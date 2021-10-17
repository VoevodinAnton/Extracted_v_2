package com.example.extracted_v;

import com.example.extracted_v.storage.StorageProperties;
import com.example.extracted_v.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ExtractedVApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExtractedVApplication.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }

}
