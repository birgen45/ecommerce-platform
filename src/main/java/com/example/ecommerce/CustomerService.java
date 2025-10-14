package com.example.ecommerce;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Customer findOrCreateCustomer(String firstName, String lastName, String email, String phone) {
        log.info("Finding or creating customer with email: {}", email);
        
        return customerRepository.findByCustomerEmail(email)
                .orElseGet(() -> {
                    // Create new customer if not found
                    Customer newCustomer = new Customer();
                    newCustomer.setCustomerFirstName(firstName);
                    newCustomer.setCustomerLastName(lastName);
                    newCustomer.setCustomerEmail(email);
                    newCustomer.setCustomerPhone(phone);
                    
                    
                    Customer saved = customerRepository.saveAndFlush(newCustomer);
                    
                    log.info("Created and committed new customer with email: {} and ID: {}", email, saved.getId());
                    return saved;
                });
            }
}