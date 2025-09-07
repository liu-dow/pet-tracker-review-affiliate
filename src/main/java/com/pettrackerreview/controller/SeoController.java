package com.pettrackerreview.controller;

import com.pettrackerreview.service.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Controller
public class SeoController {
    
    @Autowired
    private SeoService seoService;
    
    /**
     * Main sitemap.xml endpoint
     */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        String sitemapContent = seoService.generateSitemap();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setCacheControl("public, max-age=" + TimeUnit.HOURS.toSeconds(1));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(sitemapContent);
    }
    
    /**
     * Sitemap index for large websites
     */
    @GetMapping(value = "/sitemap-index.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemapIndex() {
        String indexContent = seoService.generateSitemapIndex();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setCacheControl("public, max-age=" + TimeUnit.HOURS.toSeconds(1));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(indexContent);
    }
    
    /**
     * Blog-specific sitemap
     */
    @GetMapping(value = "/sitemap-blog.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> blogSitemap() {
        String sitemapContent = seoService.generateBlogSitemap();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setCacheControl("public, max-age=" + TimeUnit.HOURS.toSeconds(2));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(sitemapContent);
    }
    
    /**
     * Reviews-specific sitemap
     */
    @GetMapping(value = "/sitemap-reviews.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> reviewsSitemap() {
        String sitemapContent = seoService.generateReviewsSitemap();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setCacheControl("public, max-age=" + TimeUnit.HOURS.toSeconds(2));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(sitemapContent);
    }
    
    /**
     * Robots.txt endpoint with comprehensive rules
     */
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        String robotsContent = seoService.generateRobots();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setCacheControl("public, max-age=" + TimeUnit.DAYS.toSeconds(1));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(robotsContent);
    }
}