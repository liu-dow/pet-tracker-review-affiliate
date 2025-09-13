package com.pettrackerreview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for CSS version management
 */
@Configuration
public class CssVersionConfig {
    
    @Value("${app.css.version:1.0.0}")
    private String cssVersion;
    
    public String getCssVersion() {
        return cssVersion;
    }
}