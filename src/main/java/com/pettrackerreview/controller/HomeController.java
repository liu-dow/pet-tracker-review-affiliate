package com.pettrackerreview.controller;

import com.pettrackerreview.model.BlogPost;
import com.pettrackerreview.model.Review;
import com.pettrackerreview.service.YamlContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
public class HomeController {
    
    @Autowired
    private YamlContentService contentService;
    
    @GetMapping("/")
    public String home(Model model) {
        // Get latest content for homepage
        List<BlogPost> latestBlogs = contentService.getLatestBlogPosts(6);
        List<Review> latestReviews = contentService.getLatestReviews(6);
        Set<String> allTags = contentService.getAllTags();
        
        model.addAttribute("latestBlogs", latestBlogs);
        model.addAttribute("latestReviews", latestReviews);
        model.addAttribute("allTags", allTags);
        model.addAttribute("pageTitle", "Pet Tracker Reviews - Best GPS Trackers for Dogs & Cats");
        model.addAttribute("metaDescription", "Find the best pet trackers and GPS collars for your dogs and cats. In-depth reviews, comparisons, and buying guides to keep your pets safe.");
        
        return "index";
    }
    
    @GetMapping("/blog")
    public String blogList(Model model, @RequestParam(required = false) String tag) {
        List<BlogPost> blogPosts;
        String pageTitle;
        String metaDescription;
        
        if (tag != null && !tag.trim().isEmpty()) {
            blogPosts = contentService.getBlogPostsByTag(tag);
            pageTitle = "Pet Tracker Blog - " + tag + " Articles";
            metaDescription = "Read our latest blog posts about " + tag + " and pet tracker technology.";
        } else {
            blogPosts = contentService.getAllBlogPosts();
            pageTitle = "Pet Tracker Blog - Latest Articles and Guides";
            metaDescription = "Stay updated with the latest pet tracker news, guides, and tips. Learn how to keep your pets safe with GPS technology.";
        }
        
        Set<String> allTags = contentService.getAllTags();
        
        model.addAttribute("blogPosts", blogPosts);
        model.addAttribute("allTags", allTags);
        model.addAttribute("selectedTag", tag);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("metaDescription", metaDescription);
        
        return "blog/list";
    }
    
    @GetMapping("/blog/{slug}")
    public String blogDetail(@PathVariable String slug, Model model) {
        BlogPost blogPost = contentService.getBlogPostBySlug(slug);
        
        if (blogPost == null) {
            return "redirect:/blog";
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
        model.addAttribute("pageTitle", blogPost.getTitle());
        model.addAttribute("metaDescription", blogPost.getMetaDescription());
        model.addAttribute("keywords", String.join(", ", blogPost.getTags() != null ? blogPost.getTags() : java.util.Collections.emptyList()));
        
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
        
        Set<String> allTags = contentService.getAllTags();
        
        model.addAttribute("reviews", reviews);
        model.addAttribute("allTags", allTags);
        model.addAttribute("selectedTag", tag);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("metaDescription", metaDescription);
        
        return "reviews/list";
    }
    
    @GetMapping("/reviews/{slug}")
    public String reviewDetail(@PathVariable String slug, Model model) {
        Review review = contentService.getReviewBySlug(slug);
        
        if (review == null) {
            return "redirect:/reviews";
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
        model.addAttribute("pageTitle", review.getTitle());
        model.addAttribute("metaDescription", review.getMetaDescription());
        model.addAttribute("keywords", String.join(", ", review.getTags() != null ? review.getTags() : java.util.Collections.emptyList()));
        
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
        
        return "search";
    }
    
    @GetMapping("/affiliate-disclosure")
    public String affiliateDisclosure(Model model) {
        model.addAttribute("pageTitle", "Affiliate Disclosure - Pet Tracker Review");
        model.addAttribute("metaDescription", "Learn about our affiliate partnerships and how we maintain editorial independence while providing valuable pet tracker reviews.");
        
        return "affiliate-disclosure";
    }
}