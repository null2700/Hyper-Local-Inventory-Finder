package com.antigravity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AntigravityStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntigravityStoreApplication.class, args);
    }

}