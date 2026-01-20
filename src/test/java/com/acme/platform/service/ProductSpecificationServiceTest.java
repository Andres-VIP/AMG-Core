package com.acme.platform.service;

import com.acme.platform.model.Product;
import com.acme.platform.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSpecificationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductSpecificationService productSpecificationService;

    @Test
    void searchProducts_withAllFilters_shouldReturnFilteredProducts() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findAll(any(Specification.class))).thenReturn(products);

        List<Product> result = productSpecificationService.searchProducts("laptop", 
                new BigDecimal("100"), new BigDecimal("1000"), 5);

        assertEquals(1, result.size());
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void searchProducts_withNameOnly_shouldReturnFilteredProducts() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findAll(any(Specification.class))).thenReturn(products);

        List<Product> result = productSpecificationService.searchProducts("laptop", null, null, null);

        assertEquals(1, result.size());
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void findInStockProducts_shouldReturnInStockProducts() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findAll(any(Specification.class))).thenReturn(products);

        List<Product> result = productSpecificationService.findInStockProducts();

        assertEquals(1, result.size());
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void findOutOfStockProducts_shouldReturnOutOfStockProducts() {
        Product product = new Product("Mouse", "Wireless mouse", new BigDecimal("29.99"), 0);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findAll(any(Specification.class))).thenReturn(products);

        List<Product> result = productSpecificationService.findOutOfStockProducts();

        assertEquals(1, result.size());
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void findProductsByPriceRange_shouldReturnProductsInRange() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findAll(any(Specification.class))).thenReturn(products);

        List<Product> result = productSpecificationService.findProductsByPriceRange(
                new BigDecimal("500"), new BigDecimal("1500"));

        assertEquals(1, result.size());
        verify(productRepository).findAll(any(Specification.class));
    }

    @Test
    void findProductsByDescription_shouldReturnProductsWithDescription() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findAll(any(Specification.class))).thenReturn(products);

        List<Product> result = productSpecificationService.findProductsByDescription("high-performance");

        assertEquals(1, result.size());
        verify(productRepository).findAll(any(Specification.class));
    }
}
