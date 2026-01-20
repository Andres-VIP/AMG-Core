package com.acme.platform.service;

import com.acme.platform.model.User;
import com.acme.platform.repository.UserRepository;
import com.acme.platform.specification.UserSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserSpecificationService {

    private static final Logger logger = LoggerFactory.getLogger(UserSpecificationService.class);
    
    private final UserRepository userRepository;
    
    public UserSpecificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(readOnly = true)
    public List<User> searchUsers(String name, String email, String address) {
        logger.info("Searching users with filters - name: {}, email: {}, address: {}", name, email, address);
        
        Specification<User> spec = Specification.where(null);
        
        if (name != null && !name.isEmpty()) {
            spec = spec.and(UserSpecification.hasName(name));
        }
        
        if (email != null && !email.isEmpty()) {
            spec = spec.and(UserSpecification.hasEmailContaining(email));
        }
        
        if (address != null && !address.isEmpty()) {
            spec = spec.and(UserSpecification.hasAddressContaining(address));
        }
        
        return userRepository.findAll(spec);
    }
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(readOnly = true)
    public List<User> findUsersWithAddress() {
        logger.info("Finding users with address");
        return userRepository.findAll(UserSpecification.hasAddress());
    }
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(readOnly = true)
    public List<User> findUsersWithoutAddress() {
        logger.info("Finding users without address");
        return userRepository.findAll(UserSpecification.hasNoAddress());
    }
}
