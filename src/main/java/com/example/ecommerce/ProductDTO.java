package com.example.ecommerce;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Product Data transfer objects  This is the ONLY public class 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    private BigDecimal oldPrice;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    private String imageUrl;
    
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    
    private Boolean isActive;
    
    private BigDecimal rating;
    
    private Integer ratingCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

// Cart DTO - Package-private (no 'public' keyword)
@Data
@NoArgsConstructor
@AllArgsConstructor
class CartDTO {
    private Long id;
    private String sessionId;
    private List<CartItemDTO> cartItems;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// CartItem DTO - Package-private (no 'public' keyword)
@Data
@NoArgsConstructor
@AllArgsConstructor
class CartItemDTO {
    private Long id;
    private ProductDTO product;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
}

// Add to Cart Request DTO - Package-private (no 'public' keyword)
@Data
@NoArgsConstructor
@AllArgsConstructor
class AddToCartRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class UpdateCartItemRequest {
    @NotNull(message = "Cart item ID is required")
    private Long cartItemId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CategoryDTO {
    private Long id;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
    private String iconEmoji;
    private Boolean isActive;
    private LocalDateTime createdAt;
}


@Data
@NoArgsConstructor
@AllArgsConstructor
class SearchRequest {
    private String searchTerm;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy; // name, price, createdAt
    private String sortDirection; // asc, desc
}

// API Response DTO 
@Data
@NoArgsConstructor
@AllArgsConstructor
class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}


@Data
@NoArgsConstructor
@AllArgsConstructor
class RemoveCartItemRequest {
    @NotNull(message = "Cart item ID is required")
    private Long cartItemId;
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ClearCartRequest {
    @NotBlank(message = "Session ID is required")
    private String sessionId;
}