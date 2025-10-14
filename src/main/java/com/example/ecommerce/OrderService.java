
package com.example.ecommerce;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductsRepository productsRepository;
    private final CartService cartService;
    
    // ADD THESE TWO:
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    
    @Value("${intasend.api.key}")
    private String intasendApiKey;
    
    @Value("${intasend.api.url:https://api.intasend.com/api/v1}")
    private String intasendApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Creates IntaSend checkout session
     */
    @Transactional
    public CheckoutResponse createIntaSendCheckout(CheckoutRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-IntaSend-Public-API-Key", intasendApiKey);
            
            Map<String, Object> intasendPayload = new HashMap<>();
            intasendPayload.put("first_name", request.getFirst_name());
            intasendPayload.put("last_name", request.getLast_name());
            intasendPayload.put("email", request.getEmail());
            intasendPayload.put("phone_number", request.getPhone_number());
            intasendPayload.put("amount", request.getAmount());
            intasendPayload.put("currency", request.getCurrency());
            intasendPayload.put("api_ref", request.getApi_ref());
            intasendPayload.put("redirect_url", request.getRedirect_url());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(intasendPayload, headers);
            
            log.info("Sending to IntaSend API: {}", intasendPayload);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                intasendApiUrl + "/checkout/",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            log.info("IntaSend response: {}", responseBody);
            
            CheckoutResponse checkoutResponse = new CheckoutResponse();
            checkoutResponse.setId((String) responseBody.get("id"));
            checkoutResponse.setCheckoutUrl((String) responseBody.get("url"));
            checkoutResponse.setApiRef((String) responseBody.get("api_ref"));
            checkoutResponse.setState((String) responseBody.get("state"));
            
            log.info("Checkout created successfully: {}", checkoutResponse.getId());
            return checkoutResponse;
            
        } catch (Exception e) {
            log.error("IntaSend API error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create checkout session: " + e.getMessage());
        }
    }
    
    @Transactional
    public OrderDTO saveOrder(OrderConfirmationRequest request) {
        try {
            // UPDATED: Find or create customer first
            Customer customer = customerService.findOrCreateCustomer(
                request.getCustomerInfo().getFirstName(),
                request.getCustomerInfo().getLastName(),
                request.getCustomerEmail(),
                request.getCustomerPhone()
            );
            log.info("Customer info collected: {}", customer);
            
            // Create order entity
            Order order = new Order();
            order.setApiRef(request.getApiRef());
            order.setIntasendCheckoutId(request.getIntasendCheckoutId());
            order.setIntasendTrackingId(request.getIntasendTrackingId());
            
            // CHANGED: Set customer object instead of individual fields
            order.setCustomer(customer);
            
            order.setTotalAmount(BigDecimal.valueOf(request.getAmount()));
            order.setCurrency(request.getCurrency());
            order.setPaymentStatus(request.getPaymentStatus());
            
            Order savedOrder = orderRepository.save(order);
            log.info("Created order: {} for customer: {}", savedOrder.getId(), customer.getCustomerEmail());
            
            // Save order items
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderConfirmationRequest.CartItemData item : request.getItems()) {
                Products product = productsRepository.findById(item.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + item.getId()));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(savedOrder.getId());
                orderItem.setProductId(item.getId());
                orderItem.setProductName(item.getName());
                orderItem.setPrice(BigDecimal.valueOf(item.getPrice()));
                orderItem.setQuantity(item.getQuantity());
                orderItem.setSubtotal(BigDecimal.valueOf(item.getPrice() * item.getQuantity()));
                
                orderItems.add(orderItemRepository.save(orderItem));
            }
            
            log.info("Saved {} order items for order: {}", orderItems.size(), savedOrder.getId());
            
            // Updates product stock quantities
            updateProductStock(request.getItems());
            
            return convertToDTO(savedOrder, orderItems);
            
        } catch (Exception e) {
            log.error("Failed to save order: {}", e.getMessage());
            throw new RuntimeException("Failed to save order: " + e.getMessage());
        }
    }
    
    /**
     * Update product stock after order
     */
    private void updateProductStock(List<OrderConfirmationRequest.CartItemData> items) {
        for (OrderConfirmationRequest.CartItemData item : items) {
            productsRepository.findById(item.getId()).ifPresent(product -> {
                int newStock = product.getStockQuantity() - item.getQuantity();
                product.setStockQuantity(Math.max(0, newStock));
                
                if (newStock <= 0) {
                    product.setStockStatus("Out of Stock");
                }
                
                productsRepository.save(product);
                log.info("Updated stock for product {}: {} remaining", product.getName(), newStock);
            });
        }
    }
    
    /**
     * Get all orders
     */
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return convertToDTO(order, items);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get order by ID
     */
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        return convertToDTO(order, items);
    }
    
    /**
 * Get orders by customer email
 */
public List<OrderDTO> getOrdersByCustomerEmail(String email) {
    // Directly search by email using the customer relationship
    List<Order> orders = orderRepository.findByCustomer_CustomerEmailOrderByCreatedAtDesc(email);
    
    return orders.stream()
            .map(order -> {
                List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                return convertToDTO(order, items);
            })
            .collect(Collectors.toList());
}
    
    /**
     * Update order payment status
     */
    @Transactional
    public OrderDTO updatePaymentStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setPaymentStatus(status);
        Order updated = orderRepository.save(order);
        
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        log.info("Updated payment status for order {}: {}", orderId, status);
        
        return convertToDTO(updated, items);
    }
   
    /**
     * Update order status after payment confirmation
     */
    @Transactional
    public OrderDTO updateOrderStatus(
            String apiRef, 
            String intasendCheckoutId,
            String intasendTrackingId,
            String paymentStatus) {
        
        log.info("Searching for order with api_ref: {}", apiRef);
        
        Order order = orderRepository.findByApiRef(apiRef)
            .orElseThrow(() -> new RuntimeException("Order not found with api_ref: " + apiRef));
        
        log.info("Found order ID: {}, updating status from {} to {}...", 
            order.getId(), order.getPaymentStatus(), paymentStatus);
        
        order.setIntasendCheckoutId(intasendCheckoutId);
        order.setIntasendTrackingId(intasendTrackingId);
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order {} updated successfully to status: {}", savedOrder.getId(), paymentStatus);
        
        List<OrderItem> items = orderItemRepository.findByOrderId(savedOrder.getId());
        
        return convertToDTO(savedOrder, items);
    }
    
    /**
     * Convert Order entity to DTO
     * UPDATED: Now gets customer info from customer relationship
     */
    private OrderDTO convertToDTO(Order order, List<OrderItem> items) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setIntasendCheckoutId(order.getIntasendCheckoutId());
        dto.setIntasendTrackingId(order.getIntasendTrackingId());
        dto.setApiRef(order.getApiRef());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCurrency(order.getCurrency());
        dto.setPaymentStatus(order.getPaymentStatus());
        
        // ADDED: Get customer information from the customer relationship
        if (order.getCustomer() != null) {
            dto.setCustomerFirstName(order.getCustomer().getCustomerFirstName());
            dto.setCustomerLastName(order.getCustomer().getCustomerLastName());
            dto.setCustomerEmail(order.getCustomer().getCustomerEmail());
            dto.setCustomerPhone(order.getCustomer().getCustomerPhone());
        }
        
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        List<OrderItemDTO> itemDTOs = items.stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setOrderItems(itemDTOs);
        
        return dto;
    }
    
    /**
     * Convert OrderItem entity to DTO
     */
    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrderId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }
}