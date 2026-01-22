package com.ao;

import com.ao.service.ScraperService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.ao")
@EnableScheduling
public class AOMVPApplication {

    public static void main(String[] args) {
        SpringApplication.run(AOMVPApplication.class, args);
    }

}

