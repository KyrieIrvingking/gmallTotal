package com.example.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.example.gmall.order.mapper")
public class GmallOrderSerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallOrderSerApplication.class, args);
    }

}
