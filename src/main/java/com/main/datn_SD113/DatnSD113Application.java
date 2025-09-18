package com.main.datn_SD113;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatnSD113Application {

    public static void main(String[] args) {
        SpringApplication.run(DatnSD113Application.class, args);
    }

}
