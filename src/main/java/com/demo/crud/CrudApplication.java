package com.demo.crud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.demo.crud.mapper")
public class CrudApplication {
    public static void main(String[] args) {
            System.out.println("===== JVM System Properties (proxy) =====");
    System.getProperties().forEach((k, v) -> {
        String key = k.toString().toLowerCase();
        if (key.contains("proxy")) {
            System.out.println(k + " = " + v);
        }
    });
    System.out.println("===== Environment Variables (proxy) =====");
    System.getenv().forEach((k, v) -> {
        if (k.toLowerCase().contains("proxy")) {
            System.out.println(k + " = " + v);
        }
    });
    System.out.println("==========================================");


        SpringApplication.run(CrudApplication.class, args);
    }
}
