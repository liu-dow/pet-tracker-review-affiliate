package com.pettrackerreview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pettrackerreview.model.BlogPost;
import com.pettrackerreview.model.Review;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

@Service
public class YamlContentService {
    
    private final ObjectMapper yamlMapper;
    private final String BLOGS_DIR = "blogs";
    private final String REVIEWS_DIR = "reviews";
    
    // 从配置文件中获取内容目录路径
    @Value("${app.content.dir:src/main/resources}")
    private String contentDir;
    
    public YamlContentService() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
    }
    
    @PostConstruct
    public void init() {
        // Initialize directories if needed
        try {
            ensureDirectoryExists(BLOGS_DIR);
            ensureDirectoryExists(REVIEWS_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize content directories", e);
        }
    }
    
    private void ensureDirectoryExists(String dir) throws IOException {
        try {
            // 检查内容目录下的文件夹是否存在
            File directory = new File(contentDir + "/" + dir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
        } catch (Exception e) {
            // Directory creation will be handled by the file system
        }
    }
    
    // 获取内容目录路径
    private String getContentDir() {
        return contentDir;
    }
    
    // Blog Post Methods
    @Cacheable(value = "blogPosts", key = "'allBlogPosts'")
    public List<BlogPost> getAllBlogPosts() {
        try {
            List<BlogPost> blogPosts = new ArrayList<>();
            File blogsDir = new File(getContentDir() + "/" + BLOGS_DIR);
            
            if (blogsDir.exists() && blogsDir.isDirectory()) {
                File[] yamlFiles = blogsDir.listFiles((dir, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));
                if (yamlFiles != null) {
                    for (File file : yamlFiles) {
                        try {
                            BlogPost post = yamlMapper.readValue(file, BlogPost.class);
                            if (post.getSlug() == null || post.getSlug().trim().isEmpty()) {
                                System.out.println(post.getSlug());
                                post.setSlug(post.generateSlug());
                            }
                            blogPosts.add(post);
                        } catch (Exception e) {
                            System.err.println("Error reading blog file: " + file.getName() + " - " + e.getMessage());
                        }
                    }
                }
            }
            
            // Sort by date (newest first)
            return blogPosts.stream()
                    .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("Error getting all blog posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Cacheable(value = "blogPosts", key = "#slug")
    public BlogPost getBlogPostBySlug(String slug) {
        return getAllBlogPosts().stream()
                .filter(post -> slug.equals(post.getSlug()) || slug.equals(post.generateSlug()))
                .findFirst()
                .orElse(null);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "blogPosts", key = "'allBlogPosts'"),
        @CacheEvict(value = "blogPosts", key = "#blogPost.slug", condition = "#blogPost.slug != null"),
        @CacheEvict(value = "tags", allEntries = true)
    })
    public void saveBlogPost(BlogPost blogPost) throws IOException {
        if (blogPost.getSlug() == null || blogPost.getSlug().trim().isEmpty()) {
            blogPost.setSlug(blogPost.generateSlug());
        }
        
        File file = new File(getContentDir() + "/" + BLOGS_DIR + "/" + blogPost.getSlug() + ".yaml");
        yamlMapper.writeValue(file, blogPost);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "blogPosts", key = "'allBlogPosts'"),
        @CacheEvict(value = "blogPosts", key = "#slug"),
        @CacheEvict(value = "tags", allEntries = true)
    })
    public void deleteBlogPost(String slug) throws IOException {
        File file = new File(getContentDir() + "/" + BLOGS_DIR + "/" + slug + ".yaml");
        if (file.exists()) {
            file.delete();
        }
    }
    
    // Review Methods
    @Cacheable(value = "reviews", key = "'allReviews'")
    public List<Review> getAllReviews() {
        try {
            List<Review> reviews = new ArrayList<>();
            File reviewsDir = new File(getContentDir() + "/" + REVIEWS_DIR);
            
            if (reviewsDir.exists() && reviewsDir.isDirectory()) {
                File[] yamlFiles = reviewsDir.listFiles((dir, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));
                if (yamlFiles != null) {
                    for (File file : yamlFiles) {
                        try {
                            Review review = yamlMapper.readValue(file, Review.class);
                            if (review.getSlug() == null || review.getSlug().trim().isEmpty()) {
                                review.setSlug(review.generateSlug());
                            }
                            reviews.add(review);
                        } catch (Exception e) {
                            System.err.println("Error reading review file: " + file.getName() + " - " + e.getMessage());
                        }
                    }
                }
            }
            
            // Sort by date (newest first)
            return reviews.stream()
                    .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("Error getting all reviews: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Cacheable(value = "reviews", key = "#slug")
    public Review getReviewBySlug(String slug) {
        return getAllReviews().stream()
                .filter(review -> slug.equals(review.getSlug()) || slug.equals(review.generateSlug()))
                .findFirst()
                .orElse(null);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "reviews", key = "'allReviews'"),
        @CacheEvict(value = "reviews", key = "#review.slug", condition = "#review.slug != null"),
        @CacheEvict(value = "tags", allEntries = true)
    })
    public void saveReview(Review review) throws IOException {
        if (review.getSlug() == null || review.getSlug().trim().isEmpty()) {
            review.setSlug(review.generateSlug());
        }
        
        File file = new File(getContentDir() + "/" + REVIEWS_DIR + "/" + review.getSlug() + ".yaml");
        yamlMapper.writeValue(file, review);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "reviews", key = "'allReviews'"),
        @CacheEvict(value = "reviews", key = "#slug"),
        @CacheEvict(value = "tags", allEntries = true)
    })
    public void deleteReview(String slug) throws IOException {
        File file = new File(getContentDir() + "/" + REVIEWS_DIR + "/" + slug + ".yaml");
        if (file.exists()) {
            file.delete();
        }
    }
    
    // Utility Methods
    @Cacheable(value = "blogPosts", key = "'latestBlogPosts-' + #limit")
    public List<BlogPost> getLatestBlogPosts(int limit) {
        return getAllBlogPosts().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "reviews", key = "'latestReviews-' + #limit")
    public List<Review> getLatestReviews(int limit) {
        return getAllReviews().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "tags", key = "'allTags'")
    public Set<String> getAllTags() {
        Set<String> allTags = new HashSet<>();
        
        getAllBlogPosts().forEach(post -> {
            if (post.getTags() != null) {
                allTags.addAll(post.getTags());
            }
        });
        
        getAllReviews().forEach(review -> {
            if (review.getTags() != null) {
                allTags.addAll(review.getTags());
            }
        });
        
        return allTags;
    }
    
    /**
     * 获取博客标签集合
     * @return 博客标签集合
     */
    @Cacheable(value = "tags", key = "'blogTags'")
    public Set<String> getBlogTags() {
        Set<String> blogTags = new HashSet<>();
        
        getAllBlogPosts().forEach(post -> {
            if (post.getTags() != null) {
                blogTags.addAll(post.getTags());
            }
        });
        
        // 过滤出有实际博客文章的标签
        Set<String> validBlogTags = new HashSet<>();
        for (String tag : blogTags) {
            List<BlogPost> blogPostsWithTag = getBlogPostsByTag(tag);
            if (!blogPostsWithTag.isEmpty()) {
                validBlogTags.add(tag);
            }
        }
        
        return validBlogTags;
    }
    
    /**
     * 获取评测标签集合
     * @return 评测标签集合
     */
    @Cacheable(value = "tags", key = "'reviewTags'")
    public Set<String> getReviewTags() {
        Set<String> reviewTags = new HashSet<>();
        
        getAllReviews().forEach(review -> {
            if (review.getTags() != null) {
                reviewTags.addAll(review.getTags());
            }
        });
        
        // 过滤出有实际评测的标签
        Set<String> validReviewTags = new HashSet<>();
        for (String tag : reviewTags) {
            List<Review> reviewsWithTag = getReviewsByTag(tag);
            if (!reviewsWithTag.isEmpty()) {
                validReviewTags.add(tag);
            }
        }
        
        return validReviewTags;
    }
    
    /**
     * 获取有效的标签集合（在搜索中有匹配内容的标签）
     * @return 有效标签集合
     */
    @Cacheable(value = "tags", key = "'validTags'")
    public Set<String> getValidTags() {
        Set<String> validTags = new HashSet<>();
        Set<String> allTags = getAllTags();
        
        // 对每个标签进行过滤测试，只保留有实际过滤效果的标签
        for (String tag : allTags) {
            // 检查是否有博客文章匹配此标签
            List<BlogPost> blogPostsWithTag = getBlogPostsByTag(tag);
            // 检查是否有评测匹配此标签
            List<Review> reviewsWithTag = getReviewsByTag(tag);
            
            // 如果有匹配的内容，则认为是有效标签
            if (!blogPostsWithTag.isEmpty() || !reviewsWithTag.isEmpty()) {
                validTags.add(tag);
            }
        }
        
        return validTags;
    }
    
    @Cacheable(value = "blogPosts", key = "'blogPostsByTag-' + #tag")
    public List<BlogPost> getBlogPostsByTag(String tag) {
        return getAllBlogPosts().stream()
                .filter(post -> post.getTags() != null && post.getTags().contains(tag))
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "reviews", key = "'reviewsByTag-' + #tag")
    public List<Review> getReviewsByTag(String tag) {
        return getAllReviews().stream()
                .filter(review -> review.getTags() != null && review.getTags().contains(tag))
                .collect(Collectors.toList());
    }
    
    /**
     * Export all blog and review YAML files as ZIP archive
     * @return ZIP byte array containing all YAML files
     * @throws IOException If IO error occurs during export
     */
    public byte[] exportAllYamlFiles() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        
        try {
            // Export blog files
            exportDirectoryToZip(zos, getContentDir() + "/" + BLOGS_DIR, "blogs/");
            
            // Export review files
            exportDirectoryToZip(zos, getContentDir() + "/" + REVIEWS_DIR, "reviews/");
            
        } finally {
            zos.close();
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Add all YAML files from specified directory to ZIP archive
     * @param zos ZIP output stream
     * @param dirPath Directory path to export
     * @param zipPrefix Path prefix within ZIP
     * @throws IOException If IO error occurs while reading files
     */
    private void exportDirectoryToZip(ZipOutputStream zos, String dirPath, String zipPrefix) throws IOException {
        File directory = new File(dirPath);
        
        if (directory.exists() && directory.isDirectory()) {
            File[] yamlFiles = directory.listFiles((dir, name) -> 
                name.endsWith(".yaml") || name.endsWith(".yml"));
            
            if (yamlFiles != null) {
                for (File file : yamlFiles) {
                    // Create ZIP entry
                    ZipEntry zipEntry = new ZipEntry(zipPrefix + file.getName());
                    zos.putNextEntry(zipEntry);
                    
                    // Write file content
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    
                    zos.closeEntry();
                }
            }
        }
    }
    
    /**
     * Get export statistics
     * @return Statistics containing number of exported files
     */
    public Map<String, Integer> getExportStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        // Count blog files
        File blogsDir = new File(getContentDir() + "/" + BLOGS_DIR);
        int blogCount = 0;
        if (blogsDir.exists() && blogsDir.isDirectory()) {
            File[] blogFiles = blogsDir.listFiles((dir, name) -> 
                name.endsWith(".yaml") || name.endsWith(".yml"));
            blogCount = blogFiles != null ? blogFiles.length : 0;
        }
        
        // Count review files
        File reviewsDir = new File(getContentDir() + "/" + REVIEWS_DIR);
        int reviewCount = 0;
        if (reviewsDir.exists() && reviewsDir.isDirectory()) {
            File[] reviewFiles = reviewsDir.listFiles((dir, name) -> 
                name.endsWith(".yaml") || name.endsWith(".yml"));
            reviewCount = reviewFiles != null ? reviewFiles.length : 0;
        }
        
        stats.put("blogFiles", blogCount);
        stats.put("reviewFiles", reviewCount);
        stats.put("totalFiles", blogCount + reviewCount);
        
        return stats;
    }
    
    /**
     * Import YAML file to specified content type directory
     * @param file Uploaded file
     * @param contentType Content type ("blogs" or "reviews")
     * @return Import result information
     * @throws IOException If IO error occurs during import
     */
    @Caching(evict = {
        @CacheEvict(value = "blogPosts", key = "'allBlogPosts'", condition = "#contentType == 'blogs'"),
        @CacheEvict(value = "reviews", key = "'allReviews'", condition = "#contentType == 'reviews'"),
        @CacheEvict(value = "tags", allEntries = true)
    })
    public ImportResult importYamlFile(MultipartFile file, String contentType) throws IOException {
        ImportResult result = new ImportResult();
        
        // Validate content type
        if (!"blogs".equals(contentType) && !"reviews".equals(contentType)) {
            result.setSuccess(false);
            result.setMessage("Unsupported content type: " + contentType);
            return result;
        }
        
        // Validate file name
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || (!originalFileName.endsWith(".yaml") && !originalFileName.endsWith(".yml"))) {
            result.setSuccess(false);
            result.setMessage("File must be in YAML format (.yaml or .yml)");
            return result;
        }
        
        try {
            // Read file content
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            // Validate YAML syntax first
            if (content.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("File is empty or contains no content");
                return result;
            }
            
            // Validate and parse YAML content
            if ("blogs".equals(contentType)) {
                try {
                    BlogPost blogPost = validateAndParseBlogPost(content);
                    saveBlogPost(blogPost);
                    result.setSuccess(true);
                    result.setMessage("Blog post imported successfully: " + blogPost.getTitle());
                    result.setImportedFileName(blogPost.getSlug() + ".yaml");
                } catch (IllegalArgumentException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                } catch (RuntimeException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                }
            } else {
                try {
                    Review review = validateAndParseReview(content);
                    saveReview(review);
                    result.setSuccess(true);
                    result.setMessage("Review imported successfully: " + review.getTitle());
                    result.setImportedFileName(review.getSlug() + ".yaml");
                } catch (IllegalArgumentException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                } catch (RuntimeException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Import failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Validate and parse blog post YAML content
     */
    public BlogPost validateAndParseBlogPost(String yamlContent) {
        try {
            BlogPost blogPost = yamlMapper.readValue(yamlContent, BlogPost.class);
            
            // Validate required fields with specific error messages
            if (blogPost.getTitle() == null || blogPost.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Blog post validation failed: Missing required field 'title'");
            }
            
            if (blogPost.getContent() == null || blogPost.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Blog post validation failed: Missing required field 'content'");
            }
            
            // Set default values
            if (blogPost.getDate() == null) {
                blogPost.setDate(LocalDateTime.now());
            }
            
            if (blogPost.getSlug() == null || blogPost.getSlug().trim().isEmpty()) {
                blogPost.setSlug(blogPost.generateSlug());
            }
            
            if (blogPost.getAuthor() == null || blogPost.getAuthor().trim().isEmpty()) {
                blogPost.setAuthor("Admin");
            }
            
            return blogPost;
            
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors with original message
            throw e;
        } catch (Exception e) {
            // Handle YAML parsing errors with detailed information
            String errorMsg = "Blog post YAML parsing error: ";
            if (e.getMessage().contains("Cannot deserialize")) {
                errorMsg += "Invalid YAML structure or data type mismatch";
            } else if (e.getMessage().contains("Unrecognized field")) {
                errorMsg += "Unknown field found in YAML - " + e.getMessage();
            } else if (e.getMessage().contains("missing")) {
                errorMsg += "Missing required YAML structure";
            } else {
                errorMsg += e.getMessage();
            }
            throw new RuntimeException(errorMsg);
        }
    }
    
    /**
     * Validate and parse review YAML content
     */
    public Review validateAndParseReview(String yamlContent) {
        try {
            Review review = yamlMapper.readValue(yamlContent, Review.class);
            
            // Validate required fields with specific error messages
            if (review.getTitle() == null || review.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Review validation failed: Missing required field 'title'");
            }
            
            if (review.getContent() == null || review.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Review validation failed: Missing required field 'content'");
            }
            
            if (review.getProductBrand() == null || review.getProductBrand().trim().isEmpty()) {
                throw new IllegalArgumentException("Review validation failed: Missing required field 'productBrand'");
            }
            
            // Validate rating range
            if (review.getRating() < 1 || review.getRating() > 5) {
                System.out.println("Warning: Rating out of range (1-5), setting to default value 5.0");
                review.setRating(5.0); // Default rating
            }
            
            // Set default values
            if (review.getDate() == null) {
                review.setDate(LocalDateTime.now());
            }
            
            if (review.getSlug() == null || review.getSlug().trim().isEmpty()) {
                review.setSlug(review.generateSlug());
            }
            
            return review;
            
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors with original message
            throw e;
        } catch (Exception e) {
            // Handle YAML parsing errors with detailed information
            String errorMsg = "Review YAML parsing error: ";
            if (e.getMessage().contains("Cannot deserialize")) {
                errorMsg += "Invalid YAML structure or data type mismatch";
            } else if (e.getMessage().contains("Unrecognized field")) {
                errorMsg += "Unknown field found in YAML - " + e.getMessage();
            } else if (e.getMessage().contains("missing")) {
                errorMsg += "Missing required YAML structure";
            } else {
                errorMsg += e.getMessage();
            }
            throw new RuntimeException(errorMsg);
        }
    }
    
    /**
     * Preview YAML content without saving
     * @param content YAML content as string
     * @param contentType Content type ("blogs" or "reviews")
     * @return Preview result with parsed content
     */
    public PreviewResult previewYamlContent(String content, String contentType) {
        PreviewResult result = new PreviewResult();
        
        // Validate content type
        if (!"blogs".equals(contentType) && !"reviews".equals(contentType)) {
            result.setSuccess(false);
            result.setMessage("Unsupported content type: " + contentType);
            return result;
        }
        
        try {
            // Validate YAML syntax first
            if (content.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Content is empty or contains no content");
                return result;
            }
            
            result.setContentType(contentType);
            result.setRawContent(content);
            
            // Parse and validate YAML content
            if ("blogs".equals(contentType)) {
                try {
                    BlogPost blogPost = validateAndParseBlogPost(content);
                    result.setBlogPost(blogPost);
                    result.setSuccess(true);
                    result.setMessage("Blog post preview generated successfully");
                } catch (IllegalArgumentException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                } catch (RuntimeException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                }
            } else {
                try {
                    Review review = validateAndParseReview(content);
                    result.setReview(review);
                    result.setSuccess(true);
                    result.setMessage("Review preview generated successfully");
                } catch (IllegalArgumentException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                } catch (RuntimeException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Preview failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Import YAML content to specified content type directory
     * @param content YAML content as string
     * @param contentType Content type ("blogs" or "reviews")
     * @return Import result information
     * @throws IOException If IO error occurs during import
     */
    @Caching(evict = {
        @CacheEvict(value = "blogPosts", key = "'allBlogPosts'", condition = "#contentType == 'blogs'"),
        @CacheEvict(value = "reviews", key = "'allReviews'", condition = "#contentType == 'reviews'"),
        @CacheEvict(value = "tags", allEntries = true)
    })
    public ImportResult importYamlContent(String content, String contentType) throws IOException {
        ImportResult result = new ImportResult();
        
        // Validate content type
        if (!"blogs".equals(contentType) && !"reviews".equals(contentType)) {
            result.setSuccess(false);
            result.setMessage("Unsupported content type: " + contentType);
            return result;
        }
        
        try {
            // Validate content
            if (content.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Content is empty or contains no content");
                return result;
            }
            
            // Validate and parse YAML content
            if ("blogs".equals(contentType)) {
                try {
                    BlogPost blogPost = validateAndParseBlogPost(content);
                    saveBlogPost(blogPost);
                    result.setSuccess(true);
                    result.setMessage("Blog post imported successfully: " + blogPost.getTitle());
                    result.setImportedFileName(blogPost.getSlug() + ".yaml");
                } catch (IllegalArgumentException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                } catch (RuntimeException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                }
            } else {
                try {
                    Review review = validateAndParseReview(content);
                    saveReview(review);
                    result.setSuccess(true);
                    result.setMessage("Review imported successfully: " + review.getTitle());
                    result.setImportedFileName(review.getSlug() + ".yaml");
                } catch (IllegalArgumentException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                } catch (RuntimeException e) {
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Import failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Preview result class
     */
    public static class PreviewResult {
        private boolean success;
        private String message;
        private String fileName;
        private String contentType;
        private String rawContent;
        private BlogPost blogPost;
        private Review review;
        
        // Constructors
        public PreviewResult() {}
        
        public PreviewResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
        
        public String getRawContent() {
            return rawContent;
        }
        
        public void setRawContent(String rawContent) {
            this.rawContent = rawContent;
        }
        
        public BlogPost getBlogPost() {
            return blogPost;
        }
        
        public void setBlogPost(BlogPost blogPost) {
            this.blogPost = blogPost;
        }
        
        public Review getReview() {
            return review;
        }
        
        public void setReview(Review review) {
            this.review = review;
        }
    }
    
    /**
     * Import result class
     */
    public static class ImportResult {
        private boolean success;
        private String message;
        private String importedFileName;
        
        // Constructors
        public ImportResult() {}
        
        public ImportResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getImportedFileName() {
            return importedFileName;
        }
        
        public void setImportedFileName(String importedFileName) {
            this.importedFileName = importedFileName;
        }
    }
}