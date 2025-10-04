package com.pettrackerreview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.pettrackerreview.model.Image;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing images - upload, compress, crop, and metadata operations
 */
@Service
public class ImageService {
    
    private final ObjectMapper yamlMapper;
    
    @Value("${app.image.upload.dir:uploads/images}")
    private String uploadDir;
    
    @Value("${app.image.metadata.dir:uploads/metadata}")
    private String metadataDir;
    
    @Value("${app.image.max.size:10485760}") // 10MB default
    private long maxFileSize;
    
    @Value("${app.image.thumbnail.size:300}")
    private int thumbnailSize;
    
    @Value("${app.image.compression.quality:0.8}")
    private double compressionQuality;
    
    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp"
    ));
    
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "jpg", "jpeg", "png", "gif", "webp", "bmp"
    ));
    
    // Store absolute paths
    private String absoluteUploadDir;
    private String absoluteMetadataDir;
    
    public ImageService() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        
        // Properly configure the ObjectMapper for Java 8 time handling
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        this.yamlMapper.registerModule(javaTimeModule);
        this.yamlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.yamlMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    @PostConstruct
    public void initDirectories() {
        try {
            // Register WebP ImageReader
            IIORegistry registry = IIORegistry.getDefaultInstance();
            registry.registerServiceProvider(new com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi());
            
            // Handle both relative and absolute paths
            if (uploadDir.startsWith("/")) {
                // Absolute path
                absoluteUploadDir = uploadDir;
            } else {
                // Relative path - make it absolute
                String workingDir = System.getProperty("user.dir");
                absoluteUploadDir = Paths.get(workingDir, uploadDir).toString();
            }
            
            if (metadataDir.startsWith("/")) {
                // Absolute path
                absoluteMetadataDir = metadataDir;
            } else {
                // Relative path - make it absolute
                String workingDir = System.getProperty("user.dir");
                absoluteMetadataDir = Paths.get(workingDir, metadataDir).toString();
            }
            
            // Create upload directory
            Path uploadPath = Paths.get(absoluteUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Create thumbnails directory
            Path thumbnailPath = Paths.get(absoluteUploadDir, "thumbnails");
            if (!Files.exists(thumbnailPath)) {
                Files.createDirectories(thumbnailPath);
            }
            
            // Create metadata directory
            Path metadataPath = Paths.get(absoluteMetadataDir);
            if (!Files.exists(metadataPath)) {
                Files.createDirectories(metadataPath);
            }
            
            System.out.println("Image upload directory: " + absoluteUploadDir);
            System.out.println("Image metadata directory: " + absoluteMetadataDir);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize image directories", e);
        }
    }
    
    /**
     * Upload and process a new image
     */
    public Image uploadImage(MultipartFile file, String title, String description, 
                           String altText, String category, String tags) throws IOException {
        
        // Validate file
        validateImageFile(file);
        
        // Create image metadata
        Image image = new Image();
        image.setTitle(StringUtils.isNotBlank(title) ? title : file.getOriginalFilename());
        image.setDescription(description);
        image.setOriginalFilename(file.getOriginalFilename());
        image.setMimeType(file.getContentType());
        image.setFileSize(file.getSize());
        image.setAltText(altText);
        image.setCategory(category);
        image.setTags(tags);
        
        // Generate unique filename
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String baseFilename = generateUniqueFilename(image.getTitle(), extension);
        image.setFilename(baseFilename);
        image.setId(image.generateId());
        
        // Save original file
        Path originalPath = Paths.get(absoluteUploadDir, baseFilename);
        file.transferTo(originalPath.toFile());
        
        // Set file path relative to web root for web access
        if (uploadDir.startsWith("/")) {
            // For absolute paths, we need to construct the web-accessible path
            // Assuming the web server maps /uploads/ to /home/project/affiliate/uploads/
            image.setFilePath("uploads/images/" + baseFilename);
        } else {
            // For relative paths, keep the existing logic
            image.setFilePath("uploads/images/" + baseFilename);
        }
        
        // Get image dimensions
        BufferedImage bufferedImage = ImageIO.read(originalPath.toFile());
        if (bufferedImage != null) {
            image.setWidth(bufferedImage.getWidth());
            image.setHeight(bufferedImage.getHeight());
        }
        
        // Create compressed version if needed
        compressImageIfNeeded(originalPath.toFile(), image);
        
        // Create thumbnail
        createThumbnail(originalPath.toFile(), image);
        
        // Save metadata
        saveImageMetadata(image);
        
        return image;
    }
    
    /**
     * Compress image if file size exceeds threshold
     */
    private void compressImageIfNeeded(File originalFile, Image image) throws IOException {
        if (image.getFileSize() > maxFileSize / 2) { // Compress if over 5MB
            String extension = FilenameUtils.getExtension(image.getFilename()).toLowerCase();
            String compressedFilename = FilenameUtils.getBaseName(image.getFilename()) + 
                                      "_compressed." + extension;
            
            Path compressedPath = Paths.get(absoluteUploadDir, compressedFilename);
            
            // 对于WebP格式，使用ImageIO进行转换处理
            if ("webp".equals(extension)) {
                // 读取WebP图片
                BufferedImage bufferedImage = ImageIO.read(originalFile);
                if (bufferedImage != null) {
                    // 转换为JPEG格式进行压缩
                    String jpegCompressedFilename = FilenameUtils.getBaseName(image.getFilename()) + 
                                                  "_compressed.jpg";
                    Path jpegCompressedPath = Paths.get(absoluteUploadDir, jpegCompressedFilename);
                    
                    Thumbnails.of(bufferedImage)
                            .scale(1.0)
                            .outputQuality(compressionQuality)
                            .outputFormat("JPEG")
                            .toFile(jpegCompressedPath.toFile());
                    
                    // 替换原文件如果压缩后的文件更小
                    if (Files.size(jpegCompressedPath) < Files.size(originalFile.toPath())) {
                        Files.delete(originalFile.toPath());
                        Files.move(jpegCompressedPath, originalFile.toPath());
                        image.setFileSize(Files.size(originalFile.toPath()));
                        image.setFilename(FilenameUtils.getBaseName(image.getFilename()) + ".jpg");
                        image.setMimeType("image/jpeg");
                        
                        // 更新文件路径
                        if (uploadDir.startsWith("/")) {
                            image.setFilePath("uploads/images/" + FilenameUtils.getBaseName(image.getFilename()) + ".jpg");
                        } else {
                            image.setFilePath("uploads/images/" + FilenameUtils.getBaseName(image.getFilename()) + ".jpg");
                        }
                    } else {
                        Files.deleteIfExists(jpegCompressedPath);
                    }
                } else {
                    // 如果无法读取，跳过压缩
                    System.out.println("无法读取WebP图片进行压缩: " + originalFile.getName());
                }
            } else {
                // 其他格式使用原有处理方式
                Thumbnails.of(originalFile)
                        .scale(1.0)
                        .outputQuality(compressionQuality)
                        .toFile(compressedPath.toFile());
                
                // Replace original with compressed if smaller
                if (Files.size(compressedPath) < Files.size(originalFile.toPath())) {
                    Files.delete(originalFile.toPath());
                    Files.move(compressedPath, originalFile.toPath());
                    image.setFileSize(Files.size(originalFile.toPath()));
                } else {
                    Files.deleteIfExists(compressedPath);
                }
            }
        }
    }
    
    /**
     * Create thumbnail for image
     */
    private void createThumbnail(File originalFile, Image image) throws IOException {
        String extension = FilenameUtils.getExtension(image.getFilename()).toLowerCase();
        String thumbnailFilename = FilenameUtils.getBaseName(image.getFilename()) + 
                                 "_thumb." + extension;
        
        Path thumbnailPath = Paths.get(absoluteUploadDir, "thumbnails", thumbnailFilename);
        
        // 对于WebP格式，使用ImageIO进行转换
        if ("webp".equals(extension)) {
            // 读取WebP图片
            BufferedImage bufferedImage = ImageIO.read(originalFile);
            if (bufferedImage != null) {
                // 转换为PNG格式保存缩略图
                String pngThumbnailFilename = FilenameUtils.getBaseName(image.getFilename()) + 
                                            "_thumb.png";
                Path pngThumbnailPath = Paths.get(absoluteUploadDir, "thumbnails", pngThumbnailFilename);
                
                Thumbnails.of(bufferedImage)
                        .size(thumbnailSize, thumbnailSize)
                        .outputQuality(0.8)
                        .outputFormat("PNG")
                        .toFile(pngThumbnailPath.toFile());
                
                // 设置缩略图路径
                if (uploadDir.startsWith("/")) {
                    image.setThumbnailPath("uploads/images/thumbnails/" + pngThumbnailFilename);
                } else {
                    image.setThumbnailPath("uploads/images/thumbnails/" + pngThumbnailFilename);
                }
            } else {
                // 如果无法读取，使用默认处理方式
                Thumbnails.of(originalFile)
                        .size(thumbnailSize, thumbnailSize)
                        .outputQuality(0.8)
                        .toFile(thumbnailPath.toFile());
                
                // Set thumbnail path relative to web root for web access
                if (uploadDir.startsWith("/")) {
                    image.setThumbnailPath("uploads/images/thumbnails/" + thumbnailFilename);
                } else {
                    image.setThumbnailPath("uploads/images/thumbnails/" + thumbnailFilename);
                }
            }
        } else {
            // 其他格式使用原有处理方式
            Thumbnails.of(originalFile)
                    .size(thumbnailSize, thumbnailSize)
                    .outputQuality(0.8)
                    .toFile(thumbnailPath.toFile());
            
            // Set thumbnail path relative to web root for web access
            if (uploadDir.startsWith("/")) {
                image.setThumbnailPath("uploads/images/thumbnails/" + thumbnailFilename);
            } else {
                image.setThumbnailPath("uploads/images/thumbnails/" + thumbnailFilename);
            }
        }
    }
    

    
    /**
     * Get all images with optional filtering
     */
    public List<Image> getAllImages(String category, String searchTerm) {
        List<Image> allImages = getAllImages();
        
        return allImages.stream()
                .filter(image -> category == null || category.isEmpty() || 
                               category.equals(image.getCategory()))
                .filter(image -> searchTerm == null || searchTerm.isEmpty() ||
                               containsSearchTerm(image, searchTerm))
                .sorted((a, b) -> b.getUploadDate().compareTo(a.getUploadDate()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all images
     */
    public List<Image> getAllImages() {
        List<Image> images = new ArrayList<>();
        
        try {
            Path metadataPath = Paths.get(absoluteMetadataDir);
            System.out.println("Checking metadata directory: " + metadataPath.toAbsolutePath());
            if (Files.exists(metadataPath)) {
                System.out.println("Metadata directory exists, scanning for YAML files...");
                Files.walk(metadataPath)
                        .filter(path -> path.toString().endsWith(".yaml"))
                        .forEach(path -> {
                            try {
                                System.out.println("Processing file: " + path + ", size: " + Files.size(path) + " bytes");
                                // Check if file is not empty
                                if (Files.size(path) > 0) {
                                    Image image = yamlMapper.readValue(path.toFile(), Image.class);
                                    if (image != null && image.getId() != null) {
                                        System.out.println("Successfully loaded image: " + image.getTitle() + " (ID: " + image.getId() + ")");
                                        images.add(image);
                                    } else {
                                        System.err.println("Invalid image data in file: " + path);
                                    }
                                } else {
                                    System.err.println("Empty metadata file found, skipping: " + path);
                                }
                            } catch (IOException e) {
                                System.err.println("Error reading image metadata: " + path + ", Error: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                System.out.println("Total images loaded: " + images.size());
            } else {
                System.err.println("Metadata directory does not exist: " + metadataPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error reading image metadata directory: " + e.getMessage());
            throw new RuntimeException("Error reading image metadata directory", e);
        }
        
        return images;
    }
    
    /**
     * Get image by ID
     */
    public Image getImageById(String id) {
        return getAllImages().stream()
                .filter(image -> id.equals(image.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Update image metadata
     */
    public void updateImage(Image image) throws IOException {
        image.setModifiedDate(LocalDateTime.now());
        saveImageMetadata(image);
    }
    
    /**
     * Delete image and its files
     */
    public void deleteImage(String id) throws IOException {
        Image image = getImageById(id);
        if (image == null) {
            throw new IllegalArgumentException("Image not found: " + id);
        }
        
        // Delete image files - use the full file path properly
        if (StringUtils.isNotBlank(image.getFilename())) {
            Path imagePath = Paths.get(absoluteUploadDir, image.getFilename());
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
                System.out.println("Deleted main image file: " + imagePath);
            } else {
                System.out.println("Main image file not found: " + imagePath);
            }
        }
        
        // Delete thumbnail - properly construct the path
        if (StringUtils.isNotBlank(image.getThumbnailPath())) {
            // Extract filename from thumbnail path like "uploads/images/thumbnails/filename_thumb.png"
            String thumbnailFileName = Paths.get(image.getThumbnailPath()).getFileName().toString();
            Path thumbnailPath = Paths.get(absoluteUploadDir, "thumbnails", thumbnailFileName);
            if (Files.exists(thumbnailPath)) {
                Files.delete(thumbnailPath);
                System.out.println("Deleted thumbnail file: " + thumbnailPath);
            } else {
                System.out.println("Thumbnail file not found: " + thumbnailPath);
            }
        }
        
        // Delete metadata file - construct proper metadata file path
        Path metadataPath = Paths.get(absoluteMetadataDir, id + ".yaml");
        if (Files.exists(metadataPath)) {
            Files.delete(metadataPath);
            System.out.println("Deleted metadata file: " + metadataPath);
        } else {
            System.out.println("Metadata file not found: " + metadataPath);
        }
    }
    
    /**
     * Get image categories
     */
    public List<String> getCategories() {
        return getAllImages().stream()
                .map(Image::getCategory)
                .filter(Objects::nonNull)
                .filter(category -> !category.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Get image statistics
     */
    public Map<String, Object> getImageStatistics() {
        List<Image> images = getAllImages();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalImages", images.size());
        stats.put("totalSize", images.stream().mapToLong(Image::getFileSize).sum());
        stats.put("categories", getCategories().size());
        
        // Group by category
        Map<String, Long> categoryStats = images.stream()
                .filter(image -> StringUtils.isNotBlank(image.getCategory()))
                .collect(Collectors.groupingBy(Image::getCategory, Collectors.counting()));
        stats.put("categoryStats", categoryStats);
        
        return stats;
    }
    
    // Helper methods
    
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + 
                                             (maxFileSize / (1024 * 1024)) + "MB");
        }
        
        String mimeType = file.getContentType();
        if (!ALLOWED_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: " + 
                                             String.join(", ", ALLOWED_TYPES));
        }
        
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension. Allowed extensions: " + 
                                             String.join(", ", ALLOWED_EXTENSIONS));
        }
    }
    
    private String generateUniqueFilename(String title, String extension) {
        String baseName = StringUtils.isNotBlank(title) ? 
                         title.toLowerCase().replaceAll("[^a-z0-9]", "-") :
                         "image";
        
        baseName = baseName.replaceAll("-+", "-").replaceAll("^-|-$", "");
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return baseName + "-" + timestamp + "." + extension;
    }
    
    private boolean containsSearchTerm(Image image, String searchTerm) {
        String term = searchTerm.toLowerCase();
        return (image.getTitle() != null && image.getTitle().toLowerCase().contains(term)) ||
               (image.getDescription() != null && image.getDescription().toLowerCase().contains(term)) ||
               (image.getTags() != null && image.getTags().toLowerCase().contains(term)) ||
               (image.getAltText() != null && image.getAltText().toLowerCase().contains(term));
    }
    
    private void saveImageMetadata(Image image) throws IOException {
        Path metadataPath = Paths.get(absoluteMetadataDir, image.getId() + ".yaml");
        yamlMapper.writeValue(metadataPath.toFile(), image);
    }
}