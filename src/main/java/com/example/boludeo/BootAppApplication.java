package com.example.boludeo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class BootAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootAppApplication.class, args);
    }
}

