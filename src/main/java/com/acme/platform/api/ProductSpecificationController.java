package com.acme.platform.api;

import com.acme.platform.model.Product;
import com.acme.platform.service.ProductSpecificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/spec/products")
public class ProductSpecificationController {
    
    private final ProductSpecificationService productSpecificationService;
    
    public ProductSpecificationController(ProductSpecificationService productSpecificationService) {
        this.productSpecificationService = productSpecificationService;
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minStock) {
        List<Product> products = productSpecificationService.searchProducts(name, minPrice, maxPrice, minStock);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/in-stock")
    public ResponseEntity<List<Product>> getInStockProducts() {
        List<Product> products = productSpecificationService.findInStockProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStockProducts() {
        List<Product> products = productSpecificationService.findOutOfStockProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<Product> products = productSpecificationService.findProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/description")
    public ResponseEntity<List<Product>> getProductsByDescription(@RequestParam String description) {
        List<Product> products = productSpecificationService.findProductsByDescription(description);
        return ResponseEntity.ok(products);
    }
}
