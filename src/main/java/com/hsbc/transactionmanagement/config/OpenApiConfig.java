package com.hsbc.transactionmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transactionManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transaction Management System API")
                        .description("Transaction Management System RESTful API Doc")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("HSBC Dev Team")
                                .email("dev@example.com"))
                        .license(new License()
                                .name("API License")
                                .url("http://www.example.com/licenses")));
    }
}