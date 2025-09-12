package com.pettrackerreview.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/cache")
public class CacheController {
    
    @Autowired
    private CacheManager cacheManager;
    
    @PostMapping("/clear")
    public String clearAllCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                Objects.requireNonNull(cacheManager.getCache(name)).clear();
            });
            return "All caches cleared successfully";
        } else {
            return "Cache manager not available";
        }
    }
    
    @PostMapping("/clear/blogPosts")
    public String clearBlogPostsCache() {
        if (cacheManager != null && cacheManager.getCache("blogPosts") != null) {
            cacheManager.getCache("blogPosts").clear();
            return "Blog posts cache cleared successfully";
        } else {
            return "Blog posts cache not available";
        }
    }
    
    @PostMapping("/clear/reviews")
    public String clearReviewsCache() {
        if (cacheManager != null && cacheManager.getCache("reviews") != null) {
            cacheManager.getCache("reviews").clear();
            return "Reviews cache cleared successfully";
        } else {
            return "Reviews cache not available";
        }
    }
    
    @PostMapping("/clear/tags")
    public String clearTagsCache() {
        if (cacheManager != null && cacheManager.getCache("tags") != null) {
            cacheManager.getCache("tags").clear();
            return "Tags cache cleared successfully";
        } else {
            return "Tags cache not available";
        }
    }
    
    /**
     * Reload all caches by clearing them (they will be repopulated on next access)
     */
    @PostMapping("/reload")
    public String reloadAllCaches() {
        try {
            if (cacheManager != null) {
                cacheManager.getCacheNames().forEach(name -> {
                    Objects.requireNonNull(cacheManager.getCache(name)).clear();
                });
                return "All caches reloaded successfully";
            } else {
                return "Cache manager not available";
            }
        } catch (Exception e) {
            return "Error reloading caches: " + e.getMessage();
        }
    }
}