package com.example.ecommerce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:8080")
@Slf4j
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
public ResponseEntity<?> createCheckout(@RequestBody CheckoutRequest request) {
    try {
        log.info("Received checkout request for: {}", request.getEmail());
        
        CheckoutResponse checkoutData = orderService.createIntaSendCheckout(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        // Nest the data in a "data" object
        Map<String, Object> data = new HashMap<>();
        data.put("url", checkoutData.getCheckoutUrl());  // Frontend expects "url"
        data.put("id", checkoutData.getId());
        data.put("apiRef", checkoutData.getApiRef());
        
        response.put("data", data);
        
        log.info("Returning checkout URL: {}", checkoutData.getCheckoutUrl());
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        log.error("Checkout failed: {}", e.getMessage(), e);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Failed to create checkout: " + e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmOrder(@RequestBody OrderConfirmationRequest request) {
        try {
            // Fixed: OrderService returns OrderDTO, not Order
            OrderDTO order = orderService.saveOrder(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", order);
            response.put("message", "Order saved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to save order: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            var orders = orderService.getAllOrders();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            // Fixed: OrderService returns OrderDTO, not Order
            OrderDTO order = orderService.getOrderById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", order);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Order not found");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

   @PostMapping("/webhook/intasend")
@CrossOrigin(origins = "*")
public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload) {
    log.info("=== IntaSend Webhook Received ===");
    log.info("Full payload: {}", payload);
    
    String state = (String) payload.get("state");
    String checkoutId = (String) payload.get("id");
    String apiRef = (String) payload.get("api_ref");
    
    if ("COMPLETE".equals(state)) {
        // Get cart items from database using api_ref or checkout_id
        // Create OrderConfirmationRequest
        // Call orderService.saveOrder()
        
        log.info("Payment completed: checkout_id={}, api_ref={}", checkoutId, apiRef);
    }
    
    return ResponseEntity.ok(Map.of("received", true));
}


@PostMapping("/create-pending")
public ResponseEntity<?> createPendingOrder(@RequestBody OrderConfirmationRequest request) {
    try {
        request.setPaymentStatus("PENDING");
        OrderDTO order = orderService.saveOrder(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", order);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        // Return error but don't fail
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
       }
     }
    
}
