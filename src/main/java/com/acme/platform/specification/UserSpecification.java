package com.acme.platform.specification;

import com.acme.platform.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("email")), email.toLowerCase());
        };
    }

    public static Specification<User> hasEmailContaining(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasAddressContaining(String address) {
        return (root, query, cb) -> {
            if (address == null || address.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasAddress() {
        return (root, query, cb) -> cb.isNotNull(root.get("address"));
    }

    public static Specification<User> hasNoAddress() {
        return (root, query, cb) -> cb.isNull(root.get("address"));
    }
}
