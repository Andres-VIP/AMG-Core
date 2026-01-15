package com.acme.platform.api;

import com.acme.platform.model.Product;
import com.acme.platform.service.ProductSpecificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductSpecificationController.class)
class ProductSpecificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductSpecificationService productSpecificationService;

    @Test
    void searchProducts_shouldReturnFilteredProducts() throws Exception {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productSpecificationService.searchProducts(anyString(), any(), any(), any())).thenReturn(products);

        mockMvc.perform(get("/api/spec/products/search")
                        .param("name", "laptop")
                        .param("minPrice", "100")
                        .param("maxPrice", "1000")
                        .param("minStock", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productSpecificationService).searchProducts("laptop", new BigDecimal("100"), 
                new BigDecimal("1000"), 5);
    }

    @Test
    void getInStockProducts_shouldReturnInStockProducts() throws Exception {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productSpecificationService.findInStockProducts()).thenReturn(products);

        mockMvc.perform(get("/api/spec/products/in-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(productSpecificationService).findInStockProducts();
    }

    @Test
    void getOutOfStockProducts_shouldReturnOutOfStockProducts() throws Exception {
        Product product = new Product("Mouse", "Wireless mouse", new BigDecimal("29.99"), 0);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productSpecificationService.findOutOfStockProducts()).thenReturn(products);

        mockMvc.perform(get("/api/spec/products/out-of-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(productSpecificationService).findOutOfStockProducts();
    }

    @Test
    void getProductsByPriceRange_shouldReturnProductsInRange() throws Exception {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productSpecificationService.findProductsByPriceRange(any(), any())).thenReturn(products);

        mockMvc.perform(get("/api/spec/products/price-range")
                        .param("minPrice", "500")
                        .param("maxPrice", "1500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(productSpecificationService).findProductsByPriceRange(new BigDecimal("500"), 
                new BigDecimal("1500"));
    }

    @Test
    void getProductsByDescription_shouldReturnProductsWithDescription() throws Exception {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productSpecificationService.findProductsByDescription("high-performance")).thenReturn(products);

        mockMvc.perform(get("/api/spec/products/description")
                        .param("description", "high-performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(productSpecificationService).findProductsByDescription("high-performance");
    }
}
