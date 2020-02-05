package com.example.gmall.user;




import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
//@MapperScan(basePackages = "com.example.gmall.user.mapper")
@MapperScan(basePackages = "com.example.gmall.user.mapper")
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
