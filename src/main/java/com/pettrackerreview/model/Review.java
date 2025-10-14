package com.pettrackerreview.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class Review {
    private String title;
    private String author;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;
    
    private List<String> tags;
    
    @JsonProperty("metaDescription")
    private String metaDescription;
    
    @JsonProperty("metaTitle")
    private String metaTitle;
    
    private String content;
    private String slug;
    
    // Review specific fields
    private String productName;
    private String productBrand;
    private double rating; // 1-5 stars
    private String pros;
    private String cons;
    private String conclusion;
    
    // Changed from showOnHomepage to sortOrder for better control
    private int sortOrder = 0; // 0 means not displayed on homepage, positive values determine display order
    
    // Constructors
    public Review() {}
    
    public Review(String title, String author, LocalDateTime date, 
                 List<String> tags, String metaDescription, String metaTitle, String content, 
                 String slug, String productName, String productBrand, 
                 double rating, String pros, String cons, String conclusion) {
        this.title = title;
        this.author = author;
        this.date = date;
        this.tags = tags;
        this.metaDescription = metaDescription;
        this.metaTitle = metaTitle;
        this.content = content;
        this.slug = slug;
        this.productName = productName;
        this.productBrand = productBrand;
        this.rating = rating;
        this.pros = pros;
        this.cons = cons;
        this.conclusion = conclusion;
        this.sortOrder = 0; // Default to not displayed
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
    
    public String getMetaTitle() {
        return metaTitle;
    }
    
    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
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
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductBrand() {
        return productBrand;
    }
    
    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public String getPros() {
        return pros;
    }
    
    public void setPros(String pros) {
        this.pros = pros;
    }
    
    public String getCons() {
        return cons;
    }
    
    public void setCons(String cons) {
        this.cons = cons;
    }
    
    public String getConclusion() {
        return conclusion;
    }
    
    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }
    
    // Getter and setter for sortOrder
    public int getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    // Helper method to check if review should be displayed on homepage
    public boolean isShowOnHomepage() {
        return sortOrder > 0;
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
    
    // Helper method to get star rating display
    @JsonIgnore
    public String getStarRating() {
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5;
        
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar) {
            stars.append("☆");
        }
        for (int i = fullStars + (hasHalfStar ? 1 : 0); i < 5; i++) {
            stars.append("☆");
        }
        
        return stars.toString();
    }
    
    @Override
    public String toString() {
        return "Review{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", date=" + date +
                ", productName='" + productName + '\'' +
                ", productBrand='" + productBrand + '\'' +
                ", rating=" + rating +
                ", metaTitle='" + metaTitle + '\'' +
                ", slug='" + slug + '\'' +
                ", sortOrder=" + sortOrder +
                '}';
    }
}