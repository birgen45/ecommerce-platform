package com.example.ecommerce;

import java.util.HashMap;

import jakarta.validation.Valid;//Validating objects passed in the request body
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;//loging messages and errors
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;//used to build http response allowing one to set OK 200
import org.springframework.web.bind.annotation.*;//brings all core annota,,,, for creating rest controllers below

import java.util.List;
import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;


@RestController //handle  web request and returndata 
@RequestMapping("/api/products") //maps all methods  in this class to a base url path 'api/pro'
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*") //allow request from any domain
public class ProductController {
    
    private final ProductService productService;
    
    // GET /api/products  Returns all products for frontend grid
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        try {
            List<ProductDTO> products = productService.getAllActiveProducts();
            return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
        } catch (Exception e) {
            log.error("Error retrieving products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve products"));
        } 
    }
    
    // GET /api/products/featured  For available Products section
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getFeaturedProducts() {
        try {
            List<ProductDTO> products = productService.getFeaturedProducts();
            return ResponseEntity.ok(ApiResponse.success("Featured products retrieved successfully", products));
        } catch (Exception e) {
            log.error("Error retrieving featured products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve featured products"));
        }
    }
    
    // GET /api/products/(id)  For individual product details
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        try {
            return productService.getProductById(id)
                    .map(product -> ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Product not found")));
        } catch (Exception e) {
            log.error("Error retrieving product with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve product"));
        }
    }
    
    // GET /api/products/category/(category)  For navigation menu 
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable String category) {
        try {
            List<ProductDTO> products = productService.getProductsByCategory(category);
            return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
        } catch (Exception e) {
            log.error("Error retrieving products for category: {}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve products"));
        }
    }
    
    // GET /api/products/(search)  For search bar
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(@RequestParam String term) {
        try {
            List<ProductDTO> products = productService.searchProducts(term);
            return ResponseEntity.ok(ApiResponse.success("Search completed successfully", products));
        } catch (Exception e) {
            log.error("Error searching products with term: {}", term, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Search failed"));
        }
    }
    
    // GET /api/products/(categories)  For category dropdown/navigation
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        try {
            List<String> categories = productService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
        } catch (Exception e) {
            log.error("Error retrieving categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve categories"));
        }
    }
    
    // POST /api/products Create new product Admin f
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Product created successfully", createdProduct));
        } catch (Exception e) {
            log.error("Error creating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create product"));
        }
    }
    
    // PUT /api/products/{id}  Update product Admin f
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
        } catch (RuntimeException e) {
            log.error("Error updating product with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating product with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update product"));
        }
    }
    
    // DELETE /api/products/(id)  Delete product Admin f
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", "Product deactivated"));
        } catch (RuntimeException e) {
            log.error("Error deleting product with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting product with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete product"));
        }
    }
}


@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
class CartController {
    
    private final CartService cartService;
    
   
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartDTO>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        try {
            CartDTO cart = cartService.addToCart(request);
            return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cart));
        } catch (RuntimeException e) {
            log.error("Error adding to cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding to cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add item to cart"));
        }
    }
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@PathVariable String sessionId) {
        try {
            CartDTO cart = cartService.getCartBySessionId(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
        } catch (Exception e) {
            log.error("Error retrieving cart for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve cart"));
        }
    }
    
    @GetMapping("/{sessionId}/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(@PathVariable String sessionId) {
        try {
            Integer count = cartService.getCartItemCount(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Cart count retrieved successfully", count));
        } catch (Exception e) {
            log.error("Error retrieving cart count for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve cart count"));
        }
    }
    
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request) {
        try {
            CartDTO cart = cartService.updateCartItem(request);
            return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
        } catch (RuntimeException e) {
            log.error("Error updating cart item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating cart item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update cart item"));
        }
    }
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeCartItem(
            @PathVariable Long cartItemId, @RequestParam String sessionId) {
        try {
            CartDTO cart = cartService.removeCartItem(cartItemId, sessionId);
            return ResponseEntity.ok(ApiResponse.success("Item removed successfully", cart));
        } catch (Exception e) {
            log.error("Error removing cart item: {} for session: {}", cartItemId, sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to remove item"));
        }
            }

    @DeleteMapping("/clear/{sessionId}")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable String sessionId) {
        try {
            cartService.clearCart(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", "Cart is now empty"));
        } catch (Exception e) {
            log.error("Error clearing cart for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to clear cart"));
        }
    }
}


@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
class CategoryController {
    
    private final CategoryService categoryService;
    
 
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getAllActiveCategories();
            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
        } catch (Exception e) {
            log.error("Error retrieving categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve categories"));
        }
    }
    
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        try {
            return categoryService.getCategoryById(id)
                    .map(category -> ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Category not found")));
        } catch (Exception e) {
            log.error("Error retrieving category with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve category"));
        }
    }
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryByName(@PathVariable String name) {
        try {
            return categoryService.getCategoryByName(name)
                    .map(category -> ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Category not found")));
        } catch (Exception e) {
            log.error("Error retrieving category with name: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve category"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Category created successfully", createdCategory));
        } catch (RuntimeException e) {
            log.error("Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create category"));
        }
    }
    
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
            return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updatedCategory));
        } catch (RuntimeException e) {
            log.error("Error updating category with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating category with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update category"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", "Category deactivated"));
        } catch (RuntimeException e) {
            log.error("Error deleting category with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting category with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete category"));
        }
    }
}
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
class OrderController {
    
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<String>> confirmOrder(@RequestBody OrderConfirmationRequest request) {
        try {
            log.info("Confirming order: {}", request.getApi_ref());
            return ResponseEntity.ok(ApiResponse.success("Order confirmed successfully", "Order saved"));
        } catch (Exception e) {
            log.error("Error confirming order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to confirm order"));
        }
    }

   @PostMapping("/checkout")
public ResponseEntity<ApiResponse<Map<String, Object>>> createCheckout(@RequestBody CheckoutRequest request) {
    try {
        log.info("Creating IntaSend checkout for amount: {}", request.getAmount());
        
        // checkout data for IntaSend
        Map<String, Object> checkoutData = new HashMap<>();
        checkoutData.put("first_name", request.getFirst_name());
        checkoutData.put("last_name", request.getLast_name());
        checkoutData.put("email", request.getEmail());
        checkoutData.put("phone_number", request.getPhone_number());
        checkoutData.put("amount", request.getAmount());
        checkoutData.put("currency", "KES");
        checkoutData.put("api_ref", request.getApi_ref());
        checkoutData.put("redirect_url", request.getRedirect_url());
        checkoutData.put("comment", request.getComment());
        
        
        WebClient webClient = WebClient.create();
        
        Map<String, Object> intasendResponse = webClient.post()
            .uri("https://api.intasend.com/api/v1/checkout/")
            .header("X-IntaSend-Public-API-Key", "ISPubKey_test_f8ae9370-dee6-42f8-a090-f7e4fbf6c383")
            .header("Content-Type", "application/json")
            .bodyValue(checkoutData)
            .retrieve()
            .onStatus(
                status -> status.isError(),
                clientResponse -> clientResponse.bodyToMono(String.class)
                    .map(error -> new RuntimeException("IntaSend API error: " + error))
            )
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();
        
        // Extract the checkout URL from IntaSend response
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("url", intasendResponse.get("url"));
        responseData.put("id", intasendResponse.get("id"));
        responseData.put("signature", intasendResponse.get("signature"));
        
        log.info("Checkout created successfully. URL: {}", responseData.get("url"));
        
        return ResponseEntity.ok(ApiResponse.success("Checkout created successfully", responseData));
        
    } catch (Exception e) {
        log.error("Error creating checkout: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create checkout: " + e.getMessage()));
    }
  }
}
