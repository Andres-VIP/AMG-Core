package com.acme.platform.service;

import com.acme.platform.model.Product;
import com.acme.platform.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReactiveProductService reactiveProductService;

    @Test
    void findById_whenProductExists_shouldReturnProduct() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Mono<Product> result = reactiveProductService.findById(1L);

        StepVerifier.create(result)
                .expectNext(product)
                .verifyComplete();

        verify(productRepository).findById(1L);
    }

    @Test
    void findById_whenProductNotExists_shouldReturnEmpty() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Mono<Product> result = reactiveProductService.findById(1L);

        StepVerifier.create(result)
                .verifyComplete();

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

        Flux<Product> result = reactiveProductService.findAll();

        StepVerifier.create(result)
                .expectNext(product1)
                .expectNext(product2)
                .verifyComplete();

        verify(productRepository).findAll();
    }

    @Test
    void findByName_shouldReturnProductsByName() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        List<Product> products = Arrays.asList(product);

        when(productRepository.findByNameContainingIgnoreCase("laptop")).thenReturn(products);

        Flux<Product> result = reactiveProductService.findByName("laptop");

        StepVerifier.create(result)
                .expectNext(product)
                .verifyComplete();

        verify(productRepository).findByNameContainingIgnoreCase("laptop");
    }

    @Test
    void save_shouldSaveProduct() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        Product savedProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        savedProduct.setId(1L);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Mono<Product> result = reactiveProductService.save(product);

        StepVerifier.create(result)
                .expectNext(savedProduct)
                .verifyComplete();

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteById_shouldDeleteProduct() {
        doNothing().when(productRepository).deleteById(1L);

        Mono<Void> result = reactiveProductService.deleteById(1L);

        StepVerifier.create(result)
                .verifyComplete();

        verify(productRepository).deleteById(1L);
    }

    @Test
    void count_shouldReturnCount() {
        when(productRepository.count()).thenReturn(10L);

        Mono<Long> result = reactiveProductService.count();

        StepVerifier.create(result)
                .expectNext(10L)
                .verifyComplete();

        verify(productRepository).count();
    }

    @Test
    void streamAllWithDelay_shouldStreamProductsWithDelay() {
        Product product1 = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product1.setId(1L);
        Product product2 = new Product("Mouse", "Wireless mouse", new BigDecimal("29.99"), 5);
        product2.setId(2L);
        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(products);

        Flux<Product> result = reactiveProductService.streamAllWithDelay(Duration.ofMillis(100));

        StepVerifier.create(result)
                .expectNext(product1)
                .expectNext(product2)
                .verifyComplete();

        verify(productRepository).findAll();
    }
}
