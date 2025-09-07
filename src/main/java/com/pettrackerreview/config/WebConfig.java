package com.pettrackerreview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC configuration for serving uploaded files
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.image.upload.dir:uploads/images}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path for uploads directory
        String workingDir = System.getProperty("user.dir");
        Path uploadsBasePath = Paths.get(workingDir, "uploads");
        String uploadsBaseUri = uploadsBasePath.toUri().toString();
        
        System.out.println("Mapping /uploads/** to: " + uploadsBaseUri);
        
        // Map /uploads/** to the uploads directory (includes both images and metadata)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsBaseUri);
        
        // Default static resources mapping
        registry.addResourceHandler("/css/**", "/js/**", "/images/**", "/favicon.ico")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/", 
                                    "classpath:/static/images/", "classpath:/static/");
    }
}