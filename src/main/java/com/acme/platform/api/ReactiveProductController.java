package com.acme.platform.api;

import com.acme.platform.model.Product;
import com.acme.platform.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/reactive/products")
public class ReactiveProductController {
    
    private final ProductRepository productRepository;
    
    public ReactiveProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @GetMapping(produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Product> getAllProducts() {
        return Flux.fromIterable(productRepository.findAll());
    }
    
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable Long id) {
        return Mono.fromCallable(() -> productRepository.findById(id))
                .map(optional -> optional.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()));
    }
    
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Product>> createProduct(@Valid @RequestBody Product product) {
        return Mono.fromCallable(() -> productRepository.save(product))
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> streamProducts() {
        return Flux.fromIterable(productRepository.findAll())
                .delayElements(java.time.Duration.ofSeconds(1));
    }
}

