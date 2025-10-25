package com.pettrackerreview.model;

public class LocalizedContent {
    private String title;
    private String metaDescription;
    private String metaTitle;
    private String content;
    private String pros; // For reviews only
    private String cons; // For reviews only
    private String conclusion; // For reviews only
    
    // Constructors
    public LocalizedContent() {}
    
    public LocalizedContent(String title, String metaDescription, String metaTitle, String content) {
        this.title = title;
        this.metaDescription = metaDescription;
        this.metaTitle = metaTitle;
        this.content = content;
    }
    
    public LocalizedContent(String title, String metaDescription, String metaTitle, String content, String pros, String cons, String conclusion) {
        this.title = title;
        this.metaDescription = metaDescription;
        this.metaTitle = metaTitle;
        this.content = content;
        this.pros = pros;
        this.cons = cons;
        this.conclusion = conclusion;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    
    @Override
    public String toString() {
        return "LocalizedContent{" +
                "title='" + title + '\'' +
                ", metaDescription='" + metaDescription + '\'' +
                ", metaTitle='" + metaTitle + '\'' +
                ", content='" + content + '\'' +
                ", pros='" + pros + '\'' +
                ", cons='" + cons + '\'' +
                ", conclusion='" + conclusion + '\'' +
                '}';
    }
}