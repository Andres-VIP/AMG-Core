package com.acme.platform.service;

import com.acme.platform.model.Product;
import com.acme.platform.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ReactiveProductService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveProductService.class);
    
    private final ProductRepository productRepository;
    
    public ReactiveProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public Mono<Product> findById(Long id) {
        logger.info("Reactive: Finding product by ID: {}", id);
        return Mono.fromCallable(() -> productRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(Mono::just)
                        .orElse(Mono.empty()))
                .doOnNext(product -> logger.debug("Reactive: Found product: {}", product.getName()))
                .doOnError(error -> logger.error("Reactive: Error finding product: {}", error.getMessage()));
    }
    
    public Flux<Product> findAll() {
        logger.info("Reactive: Finding all products");
        return Mono.fromCallable(productRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .doOnNext(product -> logger.debug("Reactive: Streaming product: {}", product.getName()))
                .doOnComplete(() -> logger.info("Reactive: Finished streaming all products"))
                .doOnError(error -> logger.error("Reactive: Error finding all products: {}", error.getMessage()));
    }
    
    public Flux<Product> findByName(String name) {
        logger.info("Reactive: Finding products by name: {}", name);
        return Mono.fromCallable(() -> productRepository.findByNameContainingIgnoreCase(name))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .doOnNext(product -> logger.debug("Reactive: Found product matching name: {}", product.getName()))
                .doOnError(error -> logger.error("Reactive: Error finding products by name: {}", error.getMessage()));
    }
    
    public Mono<Product> save(Product product) {
        logger.info("Reactive: Saving product: {}", product.getName());
        return Mono.fromCallable(() -> productRepository.save(product))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(saved -> logger.info("Reactive: Product saved with ID: {}", saved.getId()))
                .doOnError(error -> logger.error("Reactive: Error saving product: {}", error.getMessage()));
    }
    
    public Mono<Void> deleteById(Long id) {
        logger.info("Reactive: Deleting product by ID: {}", id);
        return Mono.fromRunnable(() -> productRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .doOnSuccess(v -> logger.info("Reactive: Product deleted successfully: {}", id))
                .doOnError(error -> logger.error("Reactive: Error deleting product: {}", error.getMessage()));
    }
    
    public Mono<Long> count() {
        logger.info("Reactive: Counting products");
        return Mono.fromCallable(productRepository::count)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(count -> logger.debug("Reactive: Total products: {}", count))
                .doOnError(error -> logger.error("Reactive: Error counting products: {}", error.getMessage()));
    }
    
    public Flux<Product> streamAllWithDelay(java.time.Duration delay) {
        logger.info("Reactive: Streaming all products with delay: {}", delay);
        return findAll()
                .delayElements(delay)
                .doOnNext(product -> logger.debug("Reactive: Streaming product: {}", product.getName()));
    }
}
