package com.acme.platform.api;

import com.acme.platform.model.Product;
import com.acme.platform.repository.ProductRepository;
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

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllProducts_shouldReturnListOfProducts() throws Exception {
        Product product1 = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product1.setId(1L);
        Product product2 = new Product("Mouse", "Wireless mouse", new BigDecimal("29.99"), 5);
        product2.setId(2L);
        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));

        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_withNameFilter_shouldReturnFilteredProducts() throws Exception {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findByNameContainingIgnoreCase("laptop")).thenReturn(products);

        mockMvc.perform(get("/api/products?name=laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));

        verify(productRepository).findByNameContainingIgnoreCase("laptop");
        verify(productRepository, never()).findAll();
    }

    @Test
    void getProductById_whenProductExists_shouldReturnProduct() throws Exception {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_whenProductNotExists_shouldReturn404() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());

        verify(productRepository).findById(1L);
    }

    @Test
    void createProduct_shouldCreateProduct() throws Exception {
        Product newProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        Product savedProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        savedProduct.setId(1L);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductExists_shouldUpdateProduct() throws Exception {
        Product existingProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        existingProduct.setId(1L);
        Product updatedData = new Product("Laptop Pro", "Updated description", new BigDecimal("1299.99"), 15);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isOk());

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductNotExists_shouldReturn404() throws Exception {
        Product updatedData = new Product("Laptop Pro", "Updated description", new BigDecimal("1299.99"), 15);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isNotFound());

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_whenProductExists_shouldDeleteProduct() throws Exception {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_whenProductNotExists_shouldReturn404() throws Exception {
        when(productRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNotFound());

        verify(productRepository).existsById(1L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
