package com.pettrackerreview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC configuration for serving uploaded files
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final LocaleChangeInterceptor localeChangeInterceptor;

    public WebConfig(LocaleChangeInterceptor localeChangeInterceptor) {
        this.localeChangeInterceptor = localeChangeInterceptor;
    }
    
    @Value("${app.image.upload.dir:uploads/images}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Handle both relative and absolute paths
        String uploadsBaseUri;
        
        if (uploadDir.startsWith("/")) {
            // Absolute path - map /uploads/** to the absolute directory
            // We assume that /home/project/affiliate/uploads should be accessible via /uploads/
            Path uploadsBasePath = Paths.get("/home/project/affiliate/uploads");
            uploadsBaseUri = uploadsBasePath.toUri().toString();
        } else {
            // Relative path - make it absolute based on working directory
            String workingDir = System.getProperty("user.dir");
            Path uploadsBasePath = Paths.get(workingDir, "uploads");
            uploadsBaseUri = uploadsBasePath.toUri().toString();
        }
        
        System.out.println("Mapping /uploads/** to: " + uploadsBaseUri);
        
        // Map /uploads/** to the uploads directory (includes both images and metadata)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsBaseUri);
        
        // Default static resources mapping
        registry.addResourceHandler("/css/**", "/js/**", "/images/**", "/favicon.ico")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/", 
                                    "classpath:/static/images/", "classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor);
    }
}