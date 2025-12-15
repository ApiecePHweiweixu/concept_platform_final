package com.example.concept_platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration for Static Resources
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /files/** to the uploads directory in the project root
        // "file:uploads/" works for relative path to working directory
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:uploads/");
    }
}

