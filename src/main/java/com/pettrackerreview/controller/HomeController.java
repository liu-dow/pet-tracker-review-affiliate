package com.pettrackerreview.controller;

import com.pettrackerreview.model.BlogPost;
import com.pettrackerreview.model.LocalizedContent;
import com.pettrackerreview.model.Review;
import com.pettrackerreview.service.YamlContentService;
import com.pettrackerreview.util.CssVersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Controller
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    
    @Autowired
    private YamlContentService contentService;
    
    @Autowired
    private CssVersionUtil cssVersionUtil;
    
    @GetMapping("/")
    public String home(Model model) {
        // Get latest content for homepage
        List<BlogPost> latestBlogs = contentService.getLatestBlogPosts(6);
        List<Review> latestReviews = contentService.getLatestReviews(6);
        Set<String> validTags = contentService.getValidTags();
        
        model.addAttribute("latestBlogs", latestBlogs);
        model.addAttribute("latestReviews", latestReviews);
        model.addAttribute("allTags", validTags);
        model.addAttribute("pageTitle", "Pet Tracker Reviews - Best GPS Trackers for Dogs & Cats");
        model.addAttribute("metaDescription", "Find the best pet trackers and GPS collars for your dogs and cats. In-depth reviews, comparisons, and buying guides to keep your pets safe.");
        model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
        
        return "index";
    }
    
    @GetMapping("/blogs")
    public String blogList(Model model, @RequestParam(required = false) String tag) {
        List<BlogPost> blogPosts;
        String pageTitle;
        String metaDescription;
        
        if (tag != null && !tag.trim().isEmpty()) {
            blogPosts = contentService.getBlogPostsByTag(tag);
            
            // 如果该标签在博客中没有匹配的内容，重定向到评测页面
            if (blogPosts.isEmpty()) {
                return "redirect:/reviews?tag=" + tag;
            }
            
            pageTitle = "Pet Tracker Blog - " + tag + " Articles";
            metaDescription = "Read our latest blog posts about " + tag + " and pet tracker technology.";
        } else {
            blogPosts = contentService.getAllBlogPosts();
            pageTitle = "Pet Tracker Blog - Latest Articles and Guides";
            metaDescription = "Stay updated with the latest pet tracker news, guides, and tips. Learn how to keep your pets safe with GPS technology.";
        }
        
        Set<String> blogTags = contentService.getBlogTags(); // 使用博客标签
        
        model.addAttribute("blogPosts", blogPosts);
        model.addAttribute("allTags", blogTags); // 使用博客标签
        model.addAttribute("selectedTag", tag);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("metaDescription", metaDescription);
        model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
        
        return "blog/list";
    }
    
    @GetMapping("/blogs/{slug}")
    public String blogDetail(@PathVariable String slug, 
                            @RequestParam(required = false) String lang,
                            Model model, 
                            Locale locale) {
        BlogPost blogPost = contentService.getBlogPostBySlug(slug);
        
        if (blogPost == null) {
            return "redirect:/blogs";
        }
        
        // Use the lang parameter if provided, otherwise use the locale
        String languageToUse = lang != null ? lang : (locale != null ? locale.getLanguage() : "en");
        
        // Check if we have localized content for the requested language
        if (blogPost.getLocalizedContent() != null) {
            LocalizedContent localizedContent = blogPost.getLocalizedContent().get(languageToUse);
            if (localizedContent != null) {
                // Override with localized content
                if (localizedContent.getTitle() != null && !localizedContent.getTitle().isEmpty()) {
                    model.addAttribute("pageTitle", localizedContent.getMetaTitle() != null && !localizedContent.getMetaTitle().isEmpty() ? localizedContent.getMetaTitle() : localizedContent.getTitle());
                }
                if (localizedContent.getMetaDescription() != null && !localizedContent.getMetaDescription().isEmpty()) {
                    model.addAttribute("metaDescription", localizedContent.getMetaDescription());
                }
                if (localizedContent.getContent() != null && !localizedContent.getContent().isEmpty()) {
                    model.addAttribute("blogPostContent", localizedContent.getContent());
                }
            }
        }
        
        // Get related posts by tags
        List<BlogPost> relatedPosts = contentService.getAllBlogPosts()
                .stream()
                .filter(post -> !post.getSlug().equals(slug) && 
                       post.getTags() != null && blogPost.getTags() != null &&
                       post.getTags().stream().anyMatch(tag -> blogPost.getTags().contains(tag)))
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("blogPost", blogPost);
        model.addAttribute("relatedPosts", relatedPosts);
        // If we haven't set page title from localized content, use default
        if (!model.containsAttribute("pageTitle")) {
            model.addAttribute("pageTitle", blogPost.getMetaTitle() != null && !blogPost.getMetaTitle().isEmpty() ? blogPost.getMetaTitle() : blogPost.getTitle());
        }
        // If we haven't set meta description from localized content, use default
        if (!model.containsAttribute("metaDescription")) {
            model.addAttribute("metaDescription", blogPost.getMetaDescription());
        }
        model.addAttribute("keywords", String.join(", ", blogPost.getTags() != null ? blogPost.getTags() : java.util.Collections.emptyList()));
        model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
        
        return "blog/detail";
    }
    
    @GetMapping("/reviews")
    public String reviewsList(Model model, @RequestParam(required = false) String tag) {
        List<Review> reviews;
        String pageTitle;
        String metaDescription;
        
        if (tag != null && !tag.trim().isEmpty()) {
            reviews = contentService.getReviewsByTag(tag);
            pageTitle = "Pet Tracker Reviews - " + tag + " Products";
            metaDescription = "Read our detailed reviews of " + tag + " pet trackers and GPS collars.";
        } else {
            reviews = contentService.getAllReviews();
            pageTitle = "Pet Tracker Reviews - Detailed Product Analysis";
            metaDescription = "Comprehensive reviews of the best pet trackers and GPS collars. Find the perfect tracking device for your dog or cat.";
        }
        
        Set<String> reviewTags = contentService.getReviewTags(); // 使用评测标签
        
        model.addAttribute("reviews", reviews);
        model.addAttribute("allTags", reviewTags); // 使用评测标签
        model.addAttribute("selectedTag", tag);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("metaDescription", metaDescription);
        model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
        
        return "reviews/list";
    }
    
    @GetMapping("/reviews/{slug}")
    public String reviewDetail(@PathVariable String slug, 
                              @RequestParam(required = false) String lang,
                              Model model, 
                              Locale locale) {
        Review review = contentService.getReviewBySlug(slug);
        
        if (review == null) {
            return "redirect:/reviews";
        }
        
        // Use the lang parameter if provided, otherwise use the locale
        String languageToUse = lang != null ? lang : (locale != null ? locale.getLanguage() : "en");
        
        // Check if we have localized content for the requested language
        if (review.getLocalizedContent() != null) {
            LocalizedContent localizedContent = review.getLocalizedContent().get(languageToUse);
            if (localizedContent != null) {
                // Override with localized content
                if (localizedContent.getTitle() != null && !localizedContent.getTitle().isEmpty()) {
                    model.addAttribute("pageTitle", localizedContent.getMetaTitle() != null && !localizedContent.getMetaTitle().isEmpty() ? localizedContent.getMetaTitle() : localizedContent.getTitle());
                }
                if (localizedContent.getMetaDescription() != null && !localizedContent.getMetaDescription().isEmpty()) {
                    model.addAttribute("metaDescription", localizedContent.getMetaDescription());
                }
                if (localizedContent.getContent() != null && !localizedContent.getContent().isEmpty()) {
                    model.addAttribute("reviewContent", localizedContent.getContent());
                }
                if (localizedContent.getPros() != null && !localizedContent.getPros().isEmpty()) {
                    model.addAttribute("reviewPros", localizedContent.getPros());
                }
                if (localizedContent.getCons() != null && !localizedContent.getCons().isEmpty()) {
                    model.addAttribute("reviewCons", localizedContent.getCons());
                }
                if (localizedContent.getConclusion() != null && !localizedContent.getConclusion().isEmpty()) {
                    model.addAttribute("reviewConclusion", localizedContent.getConclusion());
                }
            }
        }
        
        // Get related reviews by tags or brand
        List<Review> relatedReviews = contentService.getAllReviews()
                .stream()
                .filter(r -> !r.getSlug().equals(slug) && 
                       (r.getProductBrand() != null && r.getProductBrand().equals(review.getProductBrand()) ||
                        (r.getTags() != null && review.getTags() != null &&
                         r.getTags().stream().anyMatch(tag -> review.getTags().contains(tag)))))
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("review", review);
        model.addAttribute("relatedReviews", relatedReviews);
        // If we haven't set page title from localized content, use default
        if (!model.containsAttribute("pageTitle")) {
            model.addAttribute("pageTitle", review.getMetaTitle() != null && !review.getMetaTitle().isEmpty() ? review.getMetaTitle() : review.getTitle());
        }
        // If we haven't set meta description from localized content, use default
        if (!model.containsAttribute("metaDescription")) {
            model.addAttribute("metaDescription", review.getMetaDescription());
        }
        model.addAttribute("keywords", String.join(", ", review.getTags() != null ? review.getTags() : java.util.Collections.emptyList()));
        model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
        
        return "reviews/detail";
    }
    
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        // Handle empty or null query
        if (q == null || q.trim().isEmpty()) {
            model.addAttribute("query", "");
            model.addAttribute("blogResults", java.util.Collections.emptyList());
            model.addAttribute("reviewResults", java.util.Collections.emptyList());
            model.addAttribute("pageTitle", "Search - Pet Tracker Review");
            model.addAttribute("metaDescription", "Search our pet tracker reviews and guides to find the perfect GPS tracker for your pet.");
            model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
            return "search";
        }
        
        String query = q.toLowerCase().trim();
        
        // Simple search implementation
        List<BlogPost> blogResults = contentService.getAllBlogPosts()
                .stream()
                .filter(post -> post.getTitle().toLowerCase().contains(query) ||
                               post.getContent().toLowerCase().contains(query) ||
                               (post.getTags() != null && post.getTags().stream()
                                       .anyMatch(tag -> tag.toLowerCase().contains(query))))
                .collect(java.util.stream.Collectors.toList());
        
        List<Review> reviewResults = contentService.getAllReviews()
                .stream()
                .filter(review -> review.getTitle().toLowerCase().contains(query) ||
                                 review.getContent().toLowerCase().contains(query) ||
                                 review.getProductName().toLowerCase().contains(query) ||
                                 review.getProductBrand().toLowerCase().contains(query) ||
                                 (review.getTags() != null && review.getTags().stream()
                                         .anyMatch(tag -> tag.toLowerCase().contains(query))))
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("query", q);
        model.addAttribute("blogResults", blogResults);
        model.addAttribute("reviewResults", reviewResults);
        model.addAttribute("pageTitle", "Search Results for: " + q);
        model.addAttribute("metaDescription", "Search results for " + q + " on Pet Tracker Review.");
        model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
        
        return "search";
    }
    
    @GetMapping("/affiliate-disclosure")
    public String affiliateDisclosure(Model model) {
        model.addAttribute("pageTitle", "Affiliate Disclosure - Pet Tracker Review");
        model.addAttribute("metaDescription", "Learn about our affiliate partnerships and how we maintain editorial independence while providing valuable pet tracker reviews.");
        model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
        
        return "affiliate-disclosure";
    }
    
    @GetMapping("/subscription")
    public String subscriptionPlans(Model model) {
        model.addAttribute("pageTitle", "Pet Tracker Subscription Plans - Compare GPS Tracker Subscriptions");
        model.addAttribute("metaDescription", "Compare subscription plans for popular GPS pet trackers including Tractive, Fi, Pawfit, PitPat, Weenect, Kippy & more. Find the best value pet tracking service.");
        model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
        
        return "subscription";
    }
    
    @GetMapping("/about-us")
    public String aboutUs(Model model) {
        model.addAttribute("pageTitle", "About Us - Pet Tracker Review");
        model.addAttribute("metaDescription", "Learn about Pet Tracker Review, our mission to help pet owners keep their furry friends safe with the best GPS tracking technology.");
        model.addAttribute("cssVersion", cssVersionUtil.getVersionParam());
        
        return "about-us";
    }
    
    /**
     * Handle newsletter subscription
     */
    @PostMapping("/newsletter/subscribe")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> subscribeNewsletter(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate email format
            if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
                logger.warn("Invalid email subscription attempt: {}", email);
                response.put("success", false);
                response.put("message", "Please enter a valid email address.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Log the subscription
            logger.info("Newsletter subscription received - Email: {}, Timestamp: {}, IP: Request IP not available in this context", 
                       email.trim(), java.time.LocalDateTime.now());
            
            // TODO: Here you would typically:
            // 1. Save email to database
            // 2. Send welcome email
            // 3. Add to mailing list service (Mailchimp, SendGrid, etc.)
            
            // For now, just log and return success
            logger.info("Newsletter subscription processed successfully for email: {}", email.trim());
            
            response.put("success", true);
            response.put("message", "Thank you for subscribing! Get the latest pet tracker reviews delivered to your inbox.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing newsletter subscription for email: {}", email, e);
            response.put("success", false);
            response.put("message", "An error occurred. Please try again later.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}