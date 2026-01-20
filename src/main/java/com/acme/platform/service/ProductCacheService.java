package com.acme.platform.service;

import com.acme.platform.model.Product;
import com.acme.platform.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ProductCacheService.class);
    
    private final ProductRepository productRepository;
    
    public ProductCacheService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> findById(Long id) {
        logger.info("Fetching product from database: {}", id);
        return productRepository.findById(id);
    }
    
    @Cacheable(value = "products", key = "'all'")
    public List<Product> findAll() {
        logger.info("Fetching all products from database");
        return productRepository.findAll();
    }
    
    @Cacheable(value = "products", key = "'name:' + #name")
    public List<Product> findByName(String name) {
        logger.info("Searching products by name from database: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    @CacheEvict(value = {"products", "productStats"}, allEntries = true)
    public Product save(Product product) {
        logger.info("Saving product to database: {}", product.getName());
        Product saved = productRepository.save(product);
        logger.info("Product saved with ID: {}", saved.getId());
        return saved;
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public void deleteById(Long id) {
        logger.info("Deleting product from database: {}", id);
        productRepository.deleteById(id);
    }
    
    @Cacheable(value = "productStats", key = "'totalValue'")
    public BigDecimal calculateTotalInventoryValue() {
        logger.info("Calculating total inventory value from database");
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Cacheable(value = "productStats", key = "'count'")
    public Long getProductCount() {
        logger.info("Counting products from database");
        return productRepository.count();
    }
    
    @CacheEvict(value = {"products", "productStats"}, allEntries = true)
    public void clearAllCache() {
        logger.info("Clearing all product caches");
    }
}

