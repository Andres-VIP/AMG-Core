package com.acme.platform.service;

import com.acme.platform.model.User;
import com.acme.platform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSpecificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSpecificationService userSpecificationService;

    @Test
    void searchUsers_withAllFilters_shouldReturnFilteredUsers() {
        User user = new User("John Doe", "john@example.com", "123 Main St", "password123");
        user.setId(1L);
        List<User> users = Arrays.asList(user);

        when(userRepository.findAll(any(Specification.class))).thenReturn(users);

        List<User> result = userSpecificationService.searchUsers("john", "example", "Main");

        assertEquals(1, result.size());
        verify(userRepository).findAll(any(Specification.class));
    }

    @Test
    void searchUsers_withNameOnly_shouldReturnFilteredUsers() {
        User user = new User("John Doe", "john@example.com", "123 Main St", "password123");
        user.setId(1L);
        List<User> users = Arrays.asList(user);

        when(userRepository.findAll(any(Specification.class))).thenReturn(users);

        List<User> result = userSpecificationService.searchUsers("john", null, null);

        assertEquals(1, result.size());
        verify(userRepository).findAll(any(Specification.class));
    }

    @Test
    void findUsersWithAddress_shouldReturnUsersWithAddress() {
        User user = new User("John Doe", "john@example.com", "123 Main St", "password123");
        user.setId(1L);
        List<User> users = Arrays.asList(user);

        when(userRepository.findAll(any(Specification.class))).thenReturn(users);

        List<User> result = userSpecificationService.findUsersWithAddress();

        assertEquals(1, result.size());
        verify(userRepository).findAll(any(Specification.class));
    }

    @Test
    void findUsersWithoutAddress_shouldReturnUsersWithoutAddress() {
        User user = new User("John Doe", "john@example.com", null, "password123");
        user.setId(1L);
        List<User> users = Arrays.asList(user);

        when(userRepository.findAll(any(Specification.class))).thenReturn(users);

        List<User> result = userSpecificationService.findUsersWithoutAddress();

        assertEquals(1, result.size());
        verify(userRepository).findAll(any(Specification.class));
    }
}
