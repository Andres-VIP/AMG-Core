package com.acme.platform.service;

import com.acme.platform.model.Product;
import com.acme.platform.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCacheServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductCacheService productCacheService;

    @Test
    void findById_whenProductExists_shouldReturnProduct() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productCacheService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void findById_whenProductNotExists_shouldReturnEmpty() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Product> result = productCacheService.findById(1L);

        assertFalse(result.isPresent());
        verify(productRepository).findById(1L);
    }

    @Test
    void findAll_shouldReturnAllProducts() {
        Product product1 = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product1.setId(1L);
        Product product2 = new Product("Mouse", "Wireless mouse", new BigDecimal("29.99"), 5);
        product2.setId(2L);
        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productCacheService.findAll();

        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void findByName_shouldReturnProductsByName() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findByNameContainingIgnoreCase("laptop")).thenReturn(products);

        List<Product> result = productCacheService.findByName("laptop");

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        verify(productRepository).findByNameContainingIgnoreCase("laptop");
    }

    @Test
    void save_shouldSaveProduct() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        Product savedProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        savedProduct.setId(1L);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productCacheService.save(product);

        assertNotNull(result.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteById_shouldDeleteProduct() {
        doNothing().when(productRepository).deleteById(1L);

        productCacheService.deleteById(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void calculateTotalInventoryValue_shouldReturnTotalValue() {
        Product product1 = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product1.setId(1L);
        Product product2 = new Product("Mouse", "Wireless mouse", new BigDecimal("29.99"), 5);
        product2.setId(2L);
        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(products);

        BigDecimal result = productCacheService.calculateTotalInventoryValue();

        BigDecimal expected = new BigDecimal("999.99").multiply(BigDecimal.valueOf(10))
                .add(new BigDecimal("29.99").multiply(BigDecimal.valueOf(5)));
        assertEquals(0, expected.compareTo(result));
        verify(productRepository).findAll();
    }

    @Test
    void getProductCount_shouldReturnCount() {
        when(productRepository.count()).thenReturn(10L);

        Long result = productCacheService.getProductCount();

        assertEquals(10L, result);
        verify(productRepository).count();
    }

    @Test
    void clearAllCache_shouldNotThrowException() {
        assertDoesNotThrow(() -> productCacheService.clearAllCache());
    }
}
