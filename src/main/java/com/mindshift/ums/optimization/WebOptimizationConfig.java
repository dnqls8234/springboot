package com.mindshift.ums.optimization;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebOptimizationConfig implements WebMvcConfigurer {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                // Connection pool optimization
                connector.setProperty("maxThreads", "200");
                connector.setProperty("minSpareThreads", "20");
                connector.setProperty("maxConnections", "8192");
                connector.setProperty("acceptCount", "100");
                connector.setProperty("connectionTimeout", "20000");

                // Keep-alive optimization
                connector.setProperty("keepAliveTimeout", "30000");
                connector.setProperty("maxKeepAliveRequests", "100");

                // Compression
                connector.setProperty("compression", "on");
                connector.setProperty("compressibleMimeType",
                    "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml");

                // Buffer sizes
                connector.setProperty("socketBuffer", "65536");
                connector.setProperty("tcpNoDelay", "true");
            });
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Performance optimizations
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        return mapper;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper());
        converters.add(0, jsonConverter);
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(TimeUnit.MINUTES.toMillis(5));
    }
}