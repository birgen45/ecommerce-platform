package com.example.ecommerce;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// Product Service  This is the ONLY public class in this file
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductsRepository productsRepository;
    
    public List<ProductDTO> getAllActiveProducts() {
        return productsRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProductDTO> getProductsByCategory(String category) {
        return productsRepository.findByCategoryAndIsActiveTrue(category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProductDTO> searchProducts(String searchTerm) {
        return productsRepository.searchProducts(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<ProductDTO> getProductById(Long id) {
        return productsRepository.findById(id)
                .filter(product -> product.getIsActive())
                .map(this::convertToDTO);
    }
    
    public List<ProductDTO> getFeaturedProducts() {
        return productsRepository.findFeaturedProducts()
                .stream()
                .limit(12)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<String> getAllCategories() {
        return productsRepository.findAllActiveCategories();
    }
    
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Products product = convertToEntity(productDTO);
        Products savedProduct = productsRepository.save(product);
        log.info("Created new product: {}", savedProduct.getName());
        return convertToDTO(savedProduct);
    }
    
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        return productsRepository.findById(id)
                .map(existingProduct -> {
                    updateProductFields(existingProduct, productDTO);
                    Products savedProduct = productsRepository.save(existingProduct);
                    log.info("Updated product: {}", savedProduct.getName());
                    return convertToDTO(savedProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        productsRepository.findById(id)
                .ifPresentOrElse(product -> {
                    product.setIsActive(false);
                    productsRepository.save(product);
                    log.info("Deactivated product: {}", product.getName());
                }, () -> {
                    throw new RuntimeException("Product not found with id: " + id);
                });
    }
    
    public ProductDTO convertToDTO(Products product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOldPrice(product.getOldPrice());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setIsActive(product.getIsActive());
        dto.setRating(product.getRating());
        dto.setRatingCount(product.getRatingCount());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
    
    private Products convertToEntity(ProductDTO dto) {
        Products product = new Products();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOldPrice(dto.getOldPrice());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setStockQuantity(dto.getStockQuantity());
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive(): true);
        product.setRating(dto.getRating());
        product.setRatingCount(dto.getRatingCount());
        return product;
    }
    
    
    private void updateProductFields(Products product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOldPrice(dto.getOldPrice());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setStockQuantity(dto.getStockQuantity());
        if (dto.getIsActive() != null) {
            product.setIsActive(dto.getIsActive());
        }
        product.setRating(dto.getRating());
        product.setRatingCount(dto.getRatingCount());
    }
}

// Cart Service - for  frontend cart functionality
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductsRepository productsRepository;
    private final ProductService productService;
    
    @Transactional
    public CartDTO addToCart(AddToCartRequest request) {
        Products product = productsRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not available");
        }
        
        if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
        throw new RuntimeException("Product is out of stock");
    }
    
    if ("Out of Stock".equals(product.getStockStatus())) {
        throw new RuntimeException("This product is currently unavailable");
    }
        
        Cart cart = getOrCreateCart(request.getSessionId());
        
        Optional<CartItem> existingCartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId());
        
        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
           if (product.getStockQuantity() < newQuantity) {
            throw new RuntimeException("Cannot add more items. Only " + product.getStockQuantity() + " available in stock");
        }
            
            cartItem.setQuantity(newQuantity);
            cartItem.setPrice(product.getPrice());
            cartItem.setUpdatedAt(java.time.LocalDateTime.now());
            cartItemRepository.save(cartItem);
            log.info("Updated cart item quantity for product: {}", product.getName());
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(product.getPrice());
            cartItem.setCreatedAt(java.time.LocalDateTime.now());
            cartItem.setUpdatedAt(java.time.LocalDateTime.now());
            cartItemRepository.save(cartItem);
            log.info("Added new item to cart: {}", product.getName());
        }
        
        return getCartBySessionId(request.getSessionId());
    }
    
    public CartDTO getCartBySessionId(String sessionId) {
        Optional<Cart> cart = cartRepository.findBySessionId(sessionId);
        return cart.map(this::convertToDTO).orElse(createEmptyCart(sessionId));
    }
    
    @Transactional
    public CartDTO updateCartItem(UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        Products product = cartItem.getProduct();
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }
        
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        
        log.info("Updated cart item quantity: {}", request.getQuantity());
        return convertToDTO(cartItem.getCart());
    }
    
    @Transactional
    public CartDTO removeCartItem(Long cartItemId, String sessionId) {
        cartItemRepository.deleteById(cartItemId);
        log.info("Removed cart item: {}", cartItemId);
        return getCartBySessionId(sessionId);
    }
    
    @Transactional
    public void clearCart(String sessionId) {
        Optional<Cart> cart = cartRepository.findBySessionId(sessionId);
        cart.ifPresent(c -> {
            cartItemRepository.deleteByCartId(c.getId());
            log.info("Cleared cart for session: {}", sessionId);
        });
    }
    
    //  for frontend cart badge
    public Integer getCartItemCount(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .map(cart -> cartItemRepository.countByCartId(cart.getId()))
                .orElse(0);
    }
    
    private Cart getOrCreateCart(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(sessionId);
                    return cartRepository.save(newCart);
                });
    }
    
    private CartDTO createEmptyCart(String sessionId) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setSessionId(sessionId);
        cartDTO.setCartItems(new ArrayList<>());
        cartDTO.setTotalAmount(BigDecimal.ZERO);
        cartDTO.setTotalItems(0);
        return cartDTO;
    }
    
    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setSessionId(cart.getSessionId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        
        List<CartItemDTO> cartItemDTOs = cartItemRepository.findByCartId(cart.getId())
                .stream()
                .map(this::convertCartItemToDTO)
                .collect(Collectors.toList());
        
        dto.setCartItems(cartItemDTOs);
        dto.setTotalItems(cartItemDTOs.size());
        dto.setTotalAmount(cartItemDTOs.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return dto;
    }
    
    private CartItemDTO convertCartItemToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setCreatedAt(cartItem.getCreatedAt());
        
        dto.setProduct(productService.convertToDTO(cartItem.getProduct()));
        
        BigDecimal subtotal = cartItem.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        dto.setSubtotal(subtotal);
        
        return dto;
    }
}


// Category Service -  for category management
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .filter(category -> category.getIsActive())
                .map(this::convertToDTO);
    }
    
    public Optional<CategoryDTO> getCategoryByName(String name) {
        return categoryRepository.findByNameAndIsActiveTrue(name)
                .map(this::convertToDTO);
    }
    
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new RuntimeException("Category already exists with name: " + categoryDTO.getName());
        }
        
        Category category = convertToEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        log.info("Created new category: {}", savedCategory.getName());
        return convertToDTO(savedCategory);
    }
    
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    updateCategoryFields(existingCategory, categoryDTO);
                    Category savedCategory = categoryRepository.save(existingCategory);
                    log.info("Updated category: {}", savedCategory.getName());
                    return convertToDTO(savedCategory);
                })
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.findById(id)
                .ifPresentOrElse(category -> {
                    category.setIsActive(false);
                    categoryRepository.save(category);
                    log.info("Deactivated category: {}", category.getName());
                }, () -> {
                    throw new RuntimeException("Category not found with id: " + id);
                });
    }
    
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIconEmoji(category.getIconEmoji());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }
    
    private Category convertToEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIconEmoji(dto.getIconEmoji());
        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return category;
    }
    
    private void updateCategoryFields(Category category, CategoryDTO dto) {
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIconEmoji(dto.getIconEmoji());
        if (dto.getIsActive() != null) {
            category.setIsActive(dto.getIsActive());
        }
    }
}
    