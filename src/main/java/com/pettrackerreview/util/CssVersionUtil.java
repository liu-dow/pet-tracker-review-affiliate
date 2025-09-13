package com.pettrackerreview.util;

import com.pettrackerreview.config.CssVersionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class for CSS version control to prevent browser caching issues
 */
@Component
public class CssVersionUtil {
    
    @Autowired
    private CssVersionConfig cssVersionConfig;
    
    /**
     * Get CSS version parameter for cache busting
     * @return version parameter string
     */
    public String getVersionParam() {
        return "?v=" + cssVersionConfig.getCssVersion();
    }
    
    /**
     * Get project version from configuration
     * @return project version
     */
    public String getProjectVersion() {
        return cssVersionConfig.getCssVersion();
    }
    
    /**
     * Get current timestamp as version (for development)
     * @return timestamp string
     */
    public String getTimestampVersion() {
        return String.valueOf(System.currentTimeMillis());
    }
}