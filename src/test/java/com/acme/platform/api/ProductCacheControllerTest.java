package com.acme.platform.api;

import com.acme.platform.model.Product;
import com.acme.platform.service.ProductCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductCacheController.class)
class ProductCacheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductCacheService productCacheService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllProducts_shouldReturnListOfProducts() throws Exception {
        Product product1 = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product1.setId(1L);
        Product product2 = new Product("Mouse", "Wireless mouse", new BigDecimal("29.99"), 5);
        product2.setId(2L);
        List<Product> products = Arrays.asList(product1, product2);

        when(productCacheService.findAll()).thenReturn(products);

        mockMvc.perform(get("/api/cache/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productCacheService).findAll();
    }

    @Test
    void getProductById_whenProductExists_shouldReturnProduct() throws Exception {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);

        when(productCacheService.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/cache/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productCacheService).findById(1L);
    }

    @Test
    void getProductById_whenProductNotExists_shouldReturn404() throws Exception {
        when(productCacheService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cache/products/1"))
                .andExpect(status().isNotFound());

        verify(productCacheService).findById(1L);
    }

    @Test
    void searchProducts_shouldReturnFilteredProducts() throws Exception {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productCacheService.findByName("laptop")).thenReturn(products);

        mockMvc.perform(get("/api/cache/products/search?name=laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productCacheService).findByName("laptop");
    }

    @Test
    void createProduct_shouldCreateProduct() throws Exception {
        Product newProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        Product savedProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        savedProduct.setId(1L);

        when(productCacheService.save(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/api/cache/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(productCacheService).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldUpdateProduct() throws Exception {
        Product existingProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        existingProduct.setId(1L);
        
        Product updatedDetails = new Product("Updated Laptop", "High-performance laptop", new BigDecimal("1299.99"), 5);
        
        Product savedProduct = new Product("Updated Laptop", "High-performance laptop", new BigDecimal("1299.99"), 5);
        savedProduct.setId(1L);

        when(productCacheService.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productCacheService.save(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(put("/api/cache/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Laptop"))
                .andExpect(jsonPath("$.price").value(1299.99));

        verify(productCacheService).findById(1L);
        verify(productCacheService).save(any(Product.class));
    }
    
    @Test
    void updateProduct_whenProductNotExists_shouldReturn404() throws Exception {
        Product updatedDetails = new Product("Updated Laptop", "High-performance laptop", new BigDecimal("1299.99"), 5);

        when(productCacheService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/cache/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());

        verify(productCacheService).findById(1L);
        verify(productCacheService, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldDeleteProduct() throws Exception {
        doNothing().when(productCacheService).deleteById(1L);

        mockMvc.perform(delete("/api/cache/products/1"))
                .andExpect(status().isNoContent());

        verify(productCacheService).deleteById(1L);
    }

    @Test
    void getTotalInventoryValue_shouldReturnTotalValue() throws Exception {
        BigDecimal totalValue = new BigDecimal("5000.00");

        when(productCacheService.calculateTotalInventoryValue()).thenReturn(totalValue);

        mockMvc.perform(get("/api/cache/products/stats/total-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalValue").value(5000.00));

        verify(productCacheService).calculateTotalInventoryValue();
    }

    @Test
    void getProductCount_shouldReturnCount() throws Exception {
        Long count = 10L;

        when(productCacheService.getProductCount()).thenReturn(count);

        mockMvc.perform(get("/api/cache/products/stats/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(10));

        verify(productCacheService).getProductCount();
    }

    @Test
    void clearCache_shouldClearCache() throws Exception {
        doNothing().when(productCacheService).clearAllCache();

        mockMvc.perform(post("/api/cache/products/cache/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All caches cleared successfully"));

        verify(productCacheService).clearAllCache();
    }
}
