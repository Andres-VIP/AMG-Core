package com.acme.platform.api;

import com.acme.platform.model.Product;
import com.acme.platform.service.ReactiveProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/reactive/products")
public class ReactiveProductController {
    
    private final ReactiveProductService reactiveProductService;
    
    public ReactiveProductController(ReactiveProductService reactiveProductService) {
        this.reactiveProductService = reactiveProductService;
    }
    
    @GetMapping(produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Product> getAllProducts() {
        return reactiveProductService.findAll();
    }
    
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable Long id) {
        return reactiveProductService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Product> searchProducts(@RequestParam String name) {
        return reactiveProductService.findByName(name);
    }
    
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Product>> createProduct(@Valid @RequestBody Product product) {
        return reactiveProductService.save(product)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }
    
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return reactiveProductService.findById(id)
                .flatMap(existing -> {
                    existing.setName(product.getName());
                    existing.setDescription(product.getDescription());
                    existing.setPrice(product.getPrice());
                    existing.setStock(product.getStock());
                    return reactiveProductService.save(existing);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping(value = "/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable Long id) {
        return reactiveProductService.findById(id)
                .flatMap(product -> reactiveProductService.deleteById(id)
                        .then(Mono.just(ResponseEntity.<Void>noContent().build())))
                .hasElement()
                .flatMap(hasElement -> hasElement 
                    ? Mono.just(ResponseEntity.<Void>noContent().build())
                    : Mono.just(ResponseEntity.<Void>notFound().build()));
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> streamProducts(@RequestParam(defaultValue = "1") long delaySeconds) {
        return reactiveProductService.streamAllWithDelay(Duration.ofSeconds(delaySeconds));
    }
    
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Long>>> getProductCount() {
        return reactiveProductService.count()
                .map(count -> ResponseEntity.ok(Map.of("count", count)));
    }
}

