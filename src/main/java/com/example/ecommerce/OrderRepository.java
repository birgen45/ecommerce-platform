package com.example.ecommerce;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByApiRef(String apiRef);
    
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String email);
    
    List<Order> findAllByOrderByCreatedAtDesc();
    
    List<Order> findByPaymentStatus(String paymentStatus);
}