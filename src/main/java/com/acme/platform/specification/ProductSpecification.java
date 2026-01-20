package com.acme.platform.specification;

import com.acme.platform.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasPriceGreaterThan(BigDecimal price) {
        return (root, query, cb) -> {
            if (price == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("price"), price);
        };
    }

    public static Specification<Product> hasPriceLessThan(BigDecimal price) {
        return (root, query, cb) -> {
            if (price == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("price"), price);
        };
    }

    public static Specification<Product> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }
            if (minPrice == null) {
                return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            }
            if (maxPrice == null) {
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            }
            return cb.between(root.get("price"), minPrice, maxPrice);
        };
    }

    public static Specification<Product> hasStockGreaterThan(Integer stock) {
        return (root, query, cb) -> {
            if (stock == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("stock"), stock);
        };
    }

    public static Specification<Product> hasStockLessThan(Integer stock) {
        return (root, query, cb) -> {
            if (stock == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("stock"), stock);
        };
    }

    public static Specification<Product> hasDescriptionContaining(String description) {
        return (root, query, cb) -> {
            if (description == null || description.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }

    public static Specification<Product> isInStock() {
        return (root, query, cb) -> cb.greaterThan(root.get("stock"), 0);
    }

    public static Specification<Product> isOutOfStock() {
        return (root, query, cb) -> cb.equal(root.get("stock"), 0);
    }
}
