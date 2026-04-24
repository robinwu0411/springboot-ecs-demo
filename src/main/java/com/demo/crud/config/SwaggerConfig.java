package com.demo.crud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SpringBoot ECS Demo API")
                        .description("Spring Boot 3.2 + MyBatis + MySQL CRUD 接口文档")
                        .version("1.0.0"));
    }
}
