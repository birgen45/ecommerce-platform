package com.example.ecommerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private String id;
    
    @JsonProperty("url")  
    private String checkoutUrl;
    
    @JsonProperty("api_ref")  
    private String apiRef;
    
    private String signature;
    private String state;
}