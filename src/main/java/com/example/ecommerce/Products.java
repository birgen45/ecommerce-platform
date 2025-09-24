package com.example.ecommerce;
// database anotation
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
 //for automatic code generation
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity// tells springboot this class represents a database table
@Table(name = "products")
@Data  //lombok generates getters and setters 
@NoArgsConstructor
@AllArgsConstructor
public class Products {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @NotBlank(message = "Product name is required")
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Column(name = "old_price", precision = 10, scale = 2)
    private BigDecimal oldPrice;
    
    @Column(nullable = false)
    @NotBlank(message = "Category is required")
    private String category;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(nullable = false)
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
   @Column(precision = 3, scale = 2)
    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "rating_count")
    @Min(value = 0, message = "Rating count cannot be negative")
    private Integer ratingCount = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}