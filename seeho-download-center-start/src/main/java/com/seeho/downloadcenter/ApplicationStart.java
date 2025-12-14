package com.seeho.downloadcenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.seeho.downloadcenter.persistence.mapper")
@SpringBootApplication(scanBasePackages = {"com.seeho.downloadcenter"})
@EnableScheduling
public class ApplicationStart {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationStart.class, args);
    }

}