package com.mindshift.ums.optimization;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.mindshift.ums.repository")
public class DatabaseOptimizationConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();

        // Connection pool sizing for high throughput
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // Performance optimizations
        config.setLeakDetectionThreshold(60000);
        config.setValidationTimeout(5000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        return config;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.mindshift.ums.domain.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();

        // Basic Hibernate properties
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");

        // Performance optimizations
        properties.put("hibernate.jdbc.batch_size", "25");
        properties.put("hibernate.jdbc.batch_versioned_data", "true");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.jdbc.fetch_size", "50");
        properties.put("hibernate.connection.provider_disables_autocommit", "true");

        // Second-level cache configuration
        properties.put("hibernate.cache.use_second_level_cache", "true");
        properties.put("hibernate.cache.use_query_cache", "true");
        properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
        properties.put("hibernate.javax.cache.provider", "org.ehcache.jsr107.EhcacheCachingProvider");

        // Statistics for monitoring
        properties.put("hibernate.generate_statistics", "true");

        return properties;
    }
}