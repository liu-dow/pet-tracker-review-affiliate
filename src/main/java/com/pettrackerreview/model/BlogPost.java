package com.pettrackerreview.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class BlogPost {
    private String title;
    private String author;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;
    
    private List<String> tags;
    
    @JsonProperty("metaDescription")
    private String metaDescription;
    
    private String content;
    private String slug;
    
    // Constructors
    public BlogPost() {}
    
    public BlogPost(String title, String author, LocalDateTime date, 
                   List<String> tags, String metaDescription, String content, String slug) {
        this.title = title;
        this.author = author;
        this.date = date;
        this.tags = tags;
        this.metaDescription = metaDescription;
        this.content = content;
        this.slug = slug;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getMetaDescription() {
        return metaDescription;
    }
    
    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    // Helper method to generate slug from title if not provided
    public String generateSlug() {
        if (this.slug != null && !this.slug.trim().isEmpty()) {
            return this.slug;
        }
        if (this.title != null) {
            return this.title.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
        }
        return "";
    }
    
    @Override
    public String toString() {
        return "BlogPost{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", date=" + date +
                ", tags=" + tags +
                ", metaDescription='" + metaDescription + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }
}