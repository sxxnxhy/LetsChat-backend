package com.application.letschat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LetsChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(LetsChatApplication.class, args);
    }

}
