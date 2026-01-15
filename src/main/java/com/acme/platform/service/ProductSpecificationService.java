package com.acme.platform.service;

import com.acme.platform.model.Product;
import com.acme.platform.repository.ProductRepository;
import com.acme.platform.specification.ProductSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductSpecificationService {

    private static final Logger logger = LoggerFactory.getLogger(ProductSpecificationService.class);
    
    private final ProductRepository productRepository;
    
    public ProductSpecificationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minStock) {
        logger.info("Searching products with filters - name: {}, minPrice: {}, maxPrice: {}, minStock: {}", 
                name, minPrice, maxPrice, minStock);
        
        Specification<Product> spec = Specification.where(null);
        
        if (name != null && !name.isEmpty()) {
            spec = spec.and(ProductSpecification.hasName(name));
        }
        
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(ProductSpecification.hasPriceBetween(minPrice, maxPrice));
        }
        
        if (minStock != null) {
            spec = spec.and(ProductSpecification.hasStockGreaterThan(minStock));
        }
        
        return productRepository.findAll(spec);
    }
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(readOnly = true)
    public List<Product> findInStockProducts() {
        logger.info("Finding products in stock");
        return productRepository.findAll(ProductSpecification.isInStock());
    }
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(readOnly = true)
    public List<Product> findOutOfStockProducts() {
        logger.info("Finding products out of stock");
        return productRepository.findAll(ProductSpecification.isOutOfStock());
    }
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(readOnly = true)
    public List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.info("Finding products by price range: {} - {}", minPrice, maxPrice);
        return productRepository.findAll(ProductSpecification.hasPriceBetween(minPrice, maxPrice));
    }
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(readOnly = true)
    public List<Product> findProductsByDescription(String description) {
        logger.info("Finding products by description: {}", description);
        return productRepository.findAll(ProductSpecification.hasDescriptionContaining(description));
    }
}
