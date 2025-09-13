package com.pettrackerreview.controller;

import com.pettrackerreview.model.BlogPost;
import com.pettrackerreview.model.Review;
import com.pettrackerreview.model.Image;
import com.pettrackerreview.service.YamlContentService;
import com.pettrackerreview.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.pettrackerreview.service.YamlContentService.ImportResult;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private YamlContentService contentService;
    
    @Autowired
    private ImageService imageService;
    
    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<BlogPost> recentBlogs = contentService.getLatestBlogPosts(5);
        List<Review> recentReviews = contentService.getLatestReviews(5);
        Map<String, Object> imageStats = imageService.getImageStatistics();
        
        model.addAttribute("recentBlogs", recentBlogs);
        model.addAttribute("recentReviews", recentReviews);
        model.addAttribute("totalBlogs", contentService.getAllBlogPosts().size());
        model.addAttribute("totalReviews", contentService.getAllReviews().size());
        model.addAttribute("totalImages", imageStats.get("totalImages"));
        model.addAttribute("imageStats", imageStats);
        
        return "admin/dashboard";
    }
    
    // Blog Management
    @GetMapping("/blogs")
    public String blogList(Model model) {
        List<BlogPost> blogs = contentService.getAllBlogPosts();
        model.addAttribute("blogs", blogs);
        return "admin/blogs/list";
    }
    
    @GetMapping("/blogs/new")
    public String newBlog(Model model) {
        model.addAttribute("blog", new BlogPost());
        model.addAttribute("action", "create");
        return "admin/blogs/form";
    }
    
    @GetMapping("/blogs/edit/{slug}")
    public String editBlog(@PathVariable String slug, Model model) {
        BlogPost blog = contentService.getBlogPostBySlug(slug);
        if (blog == null) {
            return "redirect:/admin/blogs";
        }
        
        model.addAttribute("blog", blog);
        model.addAttribute("action", "edit");
        return "admin/blogs/form";
    }
    
    @PostMapping("/blogs/save")
    public String saveBlog(@ModelAttribute BlogPost blog, 
                          @RequestParam(required = false) String originalSlug,
                          RedirectAttributes redirectAttributes) {
        try {
            // Set current time if creating new blog
            if (blog.getDate() == null) {
                blog.setDate(LocalDateTime.now());
            }
            
            // Convert tags string to list if needed
            // This would typically be handled in the form with proper binding
            
            // Delete old file if slug changed
            if (originalSlug != null && !originalSlug.isEmpty() && 
                !originalSlug.equals(blog.getSlug()) && !originalSlug.equals(blog.generateSlug())) {
                contentService.deleteBlogPost(originalSlug);
            }
            
            contentService.saveBlogPost(blog);
            redirectAttributes.addFlashAttribute("successMessage", "Blog post saved successfully!");
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving blog post: " + e.getMessage());
        }
        
        return "redirect:/admin/blogs";
    }
    
    @PostMapping("/blogs/delete/{slug}")
    public String deleteBlog(@PathVariable String slug, RedirectAttributes redirectAttributes) {
        try {
            contentService.deleteBlogPost(slug);
            redirectAttributes.addFlashAttribute("successMessage", "Blog post deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting blog post: " + e.getMessage());
        }
        
        return "redirect:/admin/blogs";
    }
    
    // Review Management
    @GetMapping("/reviews")
    public String reviewList(Model model) {
        List<Review> reviews = contentService.getAllReviews();
        model.addAttribute("reviews", reviews);
        return "admin/reviews/list";
    }
    
    @GetMapping("/reviews/new")
    public String newReview(Model model) {
        model.addAttribute("review", new Review());
        model.addAttribute("action", "create");
        return "admin/reviews/form";
    }
    
    @GetMapping("/reviews/edit/{slug}")
    public String editReview(@PathVariable String slug, Model model) {
        Review review = contentService.getReviewBySlug(slug);
        if (review == null) {
            return "redirect:/admin/reviews";
        }
        
        model.addAttribute("review", review);
        model.addAttribute("action", "edit");
        return "admin/reviews/form";
    }
    
    @PostMapping("/reviews/save")
    public String saveReview(@ModelAttribute Review review, 
                           @RequestParam(required = false) String originalSlug,
                           RedirectAttributes redirectAttributes) {
        try {
            // Set current time if creating new review
            if (review.getDate() == null) {
                review.setDate(LocalDateTime.now());
            }
            
            // Delete old file if slug changed
            if (originalSlug != null && !originalSlug.isEmpty() && 
                !originalSlug.equals(review.getSlug()) && !originalSlug.equals(review.generateSlug())) {
                contentService.deleteReview(originalSlug);
            }
            
            contentService.saveReview(review);
            redirectAttributes.addFlashAttribute("successMessage", "Review saved successfully!");
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving review: " + e.getMessage());
        }
        
        return "redirect:/admin/reviews";
    }
    
    @PostMapping("/reviews/delete/{slug}")
    public String deleteReview(@PathVariable String slug, RedirectAttributes redirectAttributes) {
        try {
            contentService.deleteReview(slug);
            redirectAttributes.addFlashAttribute("successMessage", "Review deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting review: " + e.getMessage());
        }
        
        return "redirect:/admin/reviews";
    }
    
    // Utility method to convert comma-separated string to list
    private List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(str.split(",\\s*"));
    }
    
    // Export functionality
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAllContent() {
        try {
            byte[] zipData = contentService.exportAllYamlFiles();
            Map<String, Integer> stats = contentService.getExportStatistics();
            
            // Create filename with current date and time
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("pet-tracker-content-export_%s.zip", timestamp);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(zipData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipData);
                    
        } catch (IOException e) {
            // Return error response
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/export/info")
    @ResponseBody
    public Map<String, Integer> getExportInfo() {
        return contentService.getExportStatistics();
    }
    
    // Import functionality
    @GetMapping("/import")
    public String importPage() {
        return "admin/import";
    }
    
    @PostMapping("/import")
    public String importContent(@RequestParam("file") MultipartFile file,
                              @RequestParam("contentType") String contentType,
                              RedirectAttributes redirectAttributes) {
        
        // Validate if file is empty
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to import");
            return "redirect:/admin/import";
        }
        
        try {
            ImportResult result = contentService.importYamlFile(file, contentType);
            
            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
                
                // Redirect to appropriate management page based on type
                if ("blogs".equals(contentType)) {
                    return "redirect:/admin/blogs";
                } else {
                    return "redirect:/admin/reviews";
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
                return "redirect:/admin/import";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Import failed: " + e.getMessage());
            return "redirect:/admin/import";
        }
    }
    
    // Image Management
    @GetMapping("/images")
    public String imageList(@RequestParam(required = false) String category,
                           @RequestParam(required = false) String search,
                           Model model) {
        List<Image> images = imageService.getAllImages(category, search);
        List<String> categories = imageService.getCategories();
        Map<String, Object> stats = imageService.getImageStatistics();
        
        model.addAttribute("images", images);
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        
        return "admin/images/list";
    }
    
    @GetMapping("/images/new")
    public String newImage(Model model) {
        List<String> categories = imageService.getCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("action", "create");
        return "admin/images/form";
    }
    
    @GetMapping("/images/edit/{id}")
    public String editImage(@PathVariable String id, Model model) {
        Image image = imageService.getImageById(id);
        if (image == null) {
            return "redirect:/admin/images";
        }
        
        List<String> categories = imageService.getCategories();
        model.addAttribute("image", image);
        model.addAttribute("categories", categories);
        model.addAttribute("action", "edit");
        return "admin/images/form";
    }
    
    @PostMapping("/images/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file,
                             @RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String altText,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String tags,
                             RedirectAttributes redirectAttributes) {
        try {
            Image image = imageService.uploadImage(file, title, description, altText, category, tags);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Image uploaded successfully! ID: " + image.getId());
            return "redirect:/admin/images";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error uploading image: " + e.getMessage());
            return "redirect:/admin/images/new";
        }
    }
    
    @PostMapping("/images/update/{id}")
    public String updateImage(@PathVariable String id,
                             @RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String altText,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String tags,
                             RedirectAttributes redirectAttributes) {
        try {
            Image image = imageService.getImageById(id);
            if (image == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Image not found");
                return "redirect:/admin/images";
            }
            
            image.setTitle(title);
            image.setDescription(description);
            image.setAltText(altText);
            image.setCategory(category);
            image.setTags(tags);
            
            imageService.updateImage(image);
            redirectAttributes.addFlashAttribute("successMessage", "Image updated successfully!");
            return "redirect:/admin/images";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating image: " + e.getMessage());
            return "redirect:/admin/images/edit/" + id;
        }
    }
    

    
    @PostMapping("/images/delete/{id}")
    public String deleteImage(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            imageService.deleteImage(id);
            redirectAttributes.addFlashAttribute("successMessage", "Image deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting image: " + e.getMessage());
        }
        return "redirect:/admin/images";
    }
    
    @GetMapping("/images/stats")
    @ResponseBody
    public Map<String, Object> getImageStats() {
        return imageService.getImageStatistics();
    }
    
    @GetMapping("/images/debug")
    @ResponseBody
    public Map<String, Object> debugImages() {
        Map<String, Object> debugInfo = new HashMap<>();
        try {
            List<Image> images = imageService.getAllImages();
            debugInfo.put("totalImages", images.size());
            debugInfo.put("images", images);
            debugInfo.put("success", true);
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            debugInfo.put("success", false);
        }
        return debugInfo;
    }
    
    @GetMapping("/ai-content-generator")
    public String aiContentGenerator(Model model) {
        // Add any necessary model attributes here
        return "admin/ai-content-generator";
    }
}