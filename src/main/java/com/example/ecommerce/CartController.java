package com.example.ecommerce;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CartController {
    
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
    @GetMapping
public ResponseEntity<ApiResponse<String>> getCartInfo() {
    return ResponseEntity.ok(ApiResponse.success(
        "Cart API is running. Use GET /api/cart/{sessionId} to retrieve a cart", 
        "Please provide a sessionId"
    ));
}
}
