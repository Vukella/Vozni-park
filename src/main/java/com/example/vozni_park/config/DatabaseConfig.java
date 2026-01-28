package com.example.vozni_park.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${spring.liquibase.change-log}")
    private String changeLog;

    private final ResourceLoader resourceLoader;

    public DatabaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        System.out.println("========================================");
        System.out.println("INITIALIZING LIQUIBASE");
        System.out.println("Change Log: " + changeLog);
        System.out.println("DataSource: " + dataSource);

        try {
            var resource = resourceLoader.getResource(changeLog);
            System.out.println("Resource exists: " + resource.exists());
            System.out.println("Resource URL: " + resource.getURL());
        } catch (Exception e) {
            System.out.println("❌ ERROR loading resource: " + e.getMessage());
            e.printStackTrace();
        }

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(true);
        liquibase.setDropFirst(false);

        System.out.println("✅ Liquibase bean configured");
        System.out.println("========================================");

        return liquibase;
    }
}