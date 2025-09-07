package com.pettrackerreview.service;

import com.pettrackerreview.model.BlogPost;
import com.pettrackerreview.model.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SeoService {
    
    @Autowired
    private YamlContentService contentService;
    
    @Value("${seo.base.url:https://pettrackerreview.com}")
    private String baseUrl;
    
    @Value("${seo.sitemap.maxUrls:50000}")
    private int maxUrlsPerSitemap;
    
    @Value("${seo.robots.crawlDelay:1}")
    private int crawlDelay;
    
    @Value("${seo.robots.allowAll:true}")
    private boolean allowAll;
    
    /**
     * Generate XML sitemap with all website URLs
     */
    public String generateSitemap() {
        StringBuilder sitemap = new StringBuilder();
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" ");
        sitemap.append("xmlns:news=\"http://www.google.com/schemas/sitemap-news/0.9\" ");
        sitemap.append("xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" ");
        sitemap.append("xmlns:mobile=\"http://www.google.com/schemas/sitemap-mobile/1.0\" ");
        sitemap.append("xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\" ");
        sitemap.append("xmlns:video=\"http://www.google.com/schemas/sitemap-video/1.1\">\n");
        
        // Add static pages
        addStaticUrls(sitemap);
        
        // Add blog posts
        addBlogUrls(sitemap);
        
        // Add reviews
        addReviewUrls(sitemap);
        
        sitemap.append("</urlset>");
        return sitemap.toString();
    }
    
    /**
     * Generate sitemap index for large websites
     */
    public String generateSitemapIndex() {
        StringBuilder index = new StringBuilder();
        index.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        index.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        // Main sitemap
        index.append("  <sitemap>\n");
        index.append("    <loc>").append(baseUrl).append("/sitemap-main.xml</loc>\n");
        index.append("    <lastmod>").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("</lastmod>\n");
        index.append("  </sitemap>\n");
        
        // Blog sitemap
        index.append("  <sitemap>\n");
        index.append("    <loc>").append(baseUrl).append("/sitemap-blog.xml</loc>\n");
        index.append("    <lastmod>").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("</lastmod>\n");
        index.append("  </sitemap>\n");
        
        // Reviews sitemap
        index.append("  <sitemap>\n");
        index.append("    <loc>").append(baseUrl).append("/sitemap-reviews.xml</loc>\n");
        index.append("    <lastmod>").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("</lastmod>\n");
        index.append("  </sitemap>\n");
        
        index.append("</sitemapindex>");
        return index.toString();
    }
    
    /**
     * Generate robots.txt content
     */
    public String generateRobots() {
        StringBuilder robots = new StringBuilder();
        
        // User-agent rules
        robots.append("User-agent: *\n");
        if (allowAll) {
            robots.append("Allow: /\n");
        }
        
        // Disallow admin areas
        robots.append("Disallow: /admin/\n");
        robots.append("Disallow: /admin/*\n");
        
        // Disallow temporary and system files
        robots.append("Disallow: /temp/\n");
        robots.append("Disallow: /cache/\n");
        robots.append("Disallow: /*.tmp\n");
        robots.append("Disallow: /*.log\n");
        
        // Crawl delay
        robots.append("Crawl-delay: ").append(crawlDelay).append("\n");
        
        // Special rules for different bots
        robots.append("\n# Special rules for search engines\n");
        robots.append("User-agent: Googlebot\n");
        robots.append("Allow: /\n");
        robots.append("Crawl-delay: 1\n");
        
        robots.append("\nUser-agent: Bingbot\n");
        robots.append("Allow: /\n");
        robots.append("Crawl-delay: 2\n");
        
        robots.append("\nUser-agent: facebookexternalhit\n");
        robots.append("Allow: /\n");
        
        robots.append("\nUser-agent: Twitterbot\n");
        robots.append("Allow: /\n");
        
        // Block bad bots
        robots.append("\n# Block problematic bots\n");
        robots.append("User-agent: SemrushBot\n");
        robots.append("Disallow: /\n");
        
        robots.append("User-agent: AhrefsBot\n");
        robots.append("Disallow: /\n");
        
        robots.append("User-agent: MJ12bot\n");
        robots.append("Disallow: /\n");
        
        // Sitemap location
        robots.append("\n# Sitemap location\n");
        robots.append("Sitemap: ").append(baseUrl).append("/sitemap.xml\n");
        robots.append("Sitemap: ").append(baseUrl).append("/sitemap-index.xml\n");
        
        return robots.toString();
    }
    
    /**
     * Generate blog-specific sitemap
     */
    public String generateBlogSitemap() {
        StringBuilder sitemap = new StringBuilder();
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        addBlogUrls(sitemap);
        
        sitemap.append("</urlset>");
        return sitemap.toString();
    }
    
    /**
     * Generate reviews-specific sitemap
     */
    public String generateReviewsSitemap() {
        StringBuilder sitemap = new StringBuilder();
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        addReviewUrls(sitemap);
        
        sitemap.append("</urlset>");
        return sitemap.toString();
    }
    
    private void addStaticUrls(StringBuilder sitemap) {
        // Homepage
        addUrl(sitemap, baseUrl + "/", "daily", "1.0", null);
        
        // Blog list page
        addUrl(sitemap, baseUrl + "/blog", "daily", "0.8", null);
        
        // Reviews list page
        addUrl(sitemap, baseUrl + "/reviews", "daily", "0.8", null);
        
        // Search page
        addUrl(sitemap, baseUrl + "/search", "weekly", "0.5", null);
    }
    
    private void addBlogUrls(StringBuilder sitemap) {
        List<BlogPost> blogPosts = contentService.getAllBlogPosts();
        for (BlogPost post : blogPosts) {
            String url = baseUrl + "/blog/" + post.getSlug();
            String lastMod = post.getDate() != null ? 
                post.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
            addUrl(sitemap, url, "weekly", "0.6", lastMod);
        }
    }
    
    private void addReviewUrls(StringBuilder sitemap) {
        List<Review> reviews = contentService.getAllReviews();
        for (Review review : reviews) {
            String url = baseUrl + "/reviews/" + review.getSlug();
            String lastMod = review.getDate() != null ? 
                review.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
            addUrl(sitemap, url, "weekly", "0.7", lastMod);
        }
    }
    
    private void addUrl(StringBuilder sitemap, String loc, String changefreq, 
                       String priority, String lastmod) {
        sitemap.append("  <url>\n");
        sitemap.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        if (lastmod != null) {
            sitemap.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        }
        sitemap.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        sitemap.append("    <priority>").append(priority).append("</priority>\n");
        sitemap.append("  </url>\n");
    }
    
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}