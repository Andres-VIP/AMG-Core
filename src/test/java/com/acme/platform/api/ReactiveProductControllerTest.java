package com.acme.platform.api;

import com.acme.platform.model.Product;
import com.acme.platform.service.ReactiveProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(ReactiveProductController.class)
class ReactiveProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveProductService reactiveProductService;

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        Product product1 = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product1.setId(1L);
        Product product2 = new Product("Mouse", "Wireless mouse", new BigDecimal("29.99"), 5);
        product2.setId(2L);

        when(reactiveProductService.findAll()).thenReturn(Flux.just(product1, product2));

        webTestClient.get()
                .uri("/api/reactive/products")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON);

        verify(reactiveProductService).findAll();
    }

    @Test
    void getProductById_whenProductExists_shouldReturnProduct() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);

        when(reactiveProductService.findById(1L)).thenReturn(Mono.just(product));

        webTestClient.get()
                .uri("/api/reactive/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Laptop");

        verify(reactiveProductService).findById(1L);
    }

    @Test
    void getProductById_whenProductNotExists_shouldReturn404() {
        when(reactiveProductService.findById(1L)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/reactive/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(reactiveProductService).findById(1L);
    }

    @Test
    void searchProducts_shouldReturnFilteredProducts() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);

        when(reactiveProductService.findByName("laptop")).thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/reactive/products/search?name=laptop")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON);

        verify(reactiveProductService).findByName("laptop");
    }

    @Test
    void createProduct_shouldCreateProduct() {
        Product newProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        Product savedProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        savedProduct.setId(1L);

        when(reactiveProductService.save(any(Product.class))).thenReturn(Mono.just(savedProduct));

        webTestClient.post()
                .uri("/api/reactive/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1);

        verify(reactiveProductService).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductExists_shouldUpdateProduct() {
        Product existingProduct = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        existingProduct.setId(1L);
        Product updatedData = new Product("Laptop Pro", "Updated description", new BigDecimal("1299.99"), 15);

        when(reactiveProductService.findById(1L)).thenReturn(Mono.just(existingProduct));
        when(reactiveProductService.save(any(Product.class))).thenReturn(Mono.just(existingProduct));

        webTestClient.put()
                .uri("/api/reactive/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedData)
                .exchange()
                .expectStatus().isOk();

        verify(reactiveProductService).findById(1L);
        verify(reactiveProductService).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductNotExists_shouldReturn404() {
        Product updatedData = new Product("Laptop Pro", "Updated description", new BigDecimal("1299.99"), 15);

        when(reactiveProductService.findById(1L)).thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/reactive/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedData)
                .exchange()
                .expectStatus().isNotFound();

        verify(reactiveProductService).findById(1L);
        verify(reactiveProductService, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_whenProductExists_shouldDeleteProduct() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);

        when(reactiveProductService.findById(1L)).thenReturn(Mono.just(product));
        when(reactiveProductService.deleteById(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/reactive/products/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(reactiveProductService).findById(1L);
        verify(reactiveProductService).deleteById(1L);
    }

    @Test
    void deleteProduct_whenProductNotExists_shouldReturn404() {
        when(reactiveProductService.findById(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/reactive/products/1")
                .exchange()
                .expectStatus().isNotFound();

        verify(reactiveProductService).findById(1L);
        verify(reactiveProductService, never()).deleteById(anyLong());
    }

    @Test
    void getProductCount_shouldReturnCount() {
        when(reactiveProductService.count()).thenReturn(Mono.just(10L));

        webTestClient.get()
                .uri("/api/reactive/products/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.count").isEqualTo(10);

        verify(reactiveProductService).count();
    }

    @Test
    void streamProducts_shouldReturnStream() {
        Product product = new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);

        when(reactiveProductService.streamAllWithDelay(any(java.time.Duration.class))).thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/reactive/products/stream?delaySeconds=1")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Content-Type", ".*text/event-stream.*");

        verify(reactiveProductService).streamAllWithDelay(any(java.time.Duration.class));
    }
}
