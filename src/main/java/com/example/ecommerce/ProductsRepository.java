package com.example.ecommerce;

import org.springframework.data.jpa.repository.JpaRepository;//allow crud func
import org.springframework.data.jpa.repository.Query;// anotation for writing query
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Products Repository This is the ONLY public class
@Repository //spring component for database acces
public interface ProductsRepository extends JpaRepository<Products, Long> {
    
    List<Products> findByIsActiveTrue();
    // tells the method this is a query
    List<Products> findByCategoryAndIsActiveTrue(String category);
    
    @Query("SELECT p FROM Products p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Products> searchProducts(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT p FROM Products p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Products> findFeaturedProducts();
    
    @Query("SELECT DISTINCT p.category FROM Products p WHERE p.isActive = true")
    List<String> findAllActiveCategories();

/**
     * Find products by category ID and active status
     */
    List<Products> findByCategoryIdAndIsActiveTrue(Long categoryId);
    
    /**
     * Search products within a specific category (by ID)
     */
    @Query("SELECT p FROM Products p WHERE " +
           "p.categoryId = :categoryId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND p.isActive = true")
    List<Products> searchProductsInCategory(
            @Param("searchTerm") String searchTerm, 
            @Param("categoryId") Long categoryId);
}

@Repository
interface CartRepository extends JpaRepository<Cart, Long> {
    
    Optional<Cart> findBySessionId(String sessionId);
    
    @Query("DELETE FROM Cart c WHERE c.updatedAt < :cutoffTime")
    void deleteOldCarts(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);
}

// CartItem Repository 
@Repository
interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByCartId(Long cartId);
    
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    
    void deleteByCartId(Long cartId);
    
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer countByCartId(@Param("cartId") Long cartId);
}

// Category Repository 
@Repository
interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByIsActiveTrue();
    
    Optional<Category> findByNameAndIsActiveTrue(String name);
    
    boolean existsByName(String name);
}