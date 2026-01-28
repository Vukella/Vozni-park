package com.example.vozni_park.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI vozniParkOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("Vozni Park Development Team");
        contact.setEmail("support@voznipark.com");

        License license = new License()
                .name("Proprietary")
                .url("https://voznipark.com/license");

        Info info = new Info()
                .title("Vozni Park API")
                .version("1.0.0")
                .description("Fleet Management System REST API - Comprehensive vehicle fleet management including vehicle tracking, driver management, travel order coordination, and document expiration monitoring")
                .contact(contact)
                .license(license)
                .termsOfService("https://voznipark.com/terms");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}