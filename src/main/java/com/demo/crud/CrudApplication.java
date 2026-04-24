package com.demo.crud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.demo.crud.mapper")
public class CrudApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrudApplication.class, args);
    }
}
