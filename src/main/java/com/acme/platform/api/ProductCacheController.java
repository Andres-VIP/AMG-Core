package com.acme.platform.api;

import com.acme.platform.model.Product;
import com.acme.platform.service.ProductCacheService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cache/products")
public class ProductCacheController {
    
    private final ProductCacheService productCacheService;
    
    public ProductCacheController(ProductCacheService productCacheService) {
        this.productCacheService = productCacheService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productCacheService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productCacheService.findAll();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productCacheService.findByName(name);
        return ResponseEntity.ok(products);
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productCacheService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        Optional<Product> optionalProduct = productCacheService.findById(id);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Product product = optionalProduct.get();
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        
        Product updatedProduct = productCacheService.save(product);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productCacheService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/stats/total-value")
    public ResponseEntity<Map<String, BigDecimal>> getTotalInventoryValue() {
        BigDecimal totalValue = productCacheService.calculateTotalInventoryValue();
        return ResponseEntity.ok(Map.of("totalValue", totalValue));
    }
    
    @GetMapping("/stats/count")
    public ResponseEntity<Map<String, Long>> getProductCount() {
        Long count = productCacheService.getProductCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        productCacheService.clearAllCache();
        return ResponseEntity.ok(Map.of("message", "All caches cleared successfully"));
    }
}

