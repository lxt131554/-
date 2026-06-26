package com.pm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.pm.mapper")
@EnableScheduling
public class PmApplication {
    public static void main(String[] args) {
        SpringApplication.run(PmApplication.class, args);
    }
}
