package com.mindshift.ums.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database and JPA configuration.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.mindshift.ums.repository")
@EntityScan(basePackages = "com.mindshift.ums.domain.entity")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    // Configuration is handled through application.yml
    // This class provides explicit configuration for JPA components
}