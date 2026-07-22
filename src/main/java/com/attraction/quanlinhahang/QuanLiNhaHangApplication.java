package com.attraction.quanlinhahang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class QuanLiNhaHangApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanLiNhaHangApplication.class, args);
    }

}
