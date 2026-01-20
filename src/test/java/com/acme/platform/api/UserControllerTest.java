package com.acme.platform.api;

import com.acme.platform.model.User;
import com.acme.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        User user1 = new User("John Doe", "john@example.com", "123 Main St", "password123");
        user1.setId(1L);
        User user2 = new User("Jane Smith", "jane@example.com", "456 Oak Ave", "password123");
        user2.setId(2L);
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() throws Exception {
        User user = new User("John Doe", "john@example.com", "123 Main St", "password123");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_whenUserNotExists_shouldReturn404() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(1L);
    }

    @Test
    void createUser_whenEmailNotExists_shouldCreateUser() throws Exception {
        User newUser = new User("John Doe", "john@example.com", "123 Main St", "password123");
        User savedUser = new User("John Doe", "john@example.com", "123 Main St", "password123");
        savedUser.setId(1L);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userRepository).findByEmail("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenEmailExists_shouldReturn409() throws Exception {
        User newUser = new User("John Doe", "john@example.com", "123 Main St", "password123");
        User existingUser = new User("Existing User", "john@example.com", "Address", "password123");
        existingUser.setId(1L);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isConflict());

        verify(userRepository).findByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenUserExists_shouldUpdateUser() throws Exception {
        User existingUser = new User("John Doe", "john@example.com", "123 Main St", "password123");
        existingUser.setId(1L);
        User updatedData = new User("John Updated", "john@example.com", "456 New St", "password123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isOk());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_whenUserNotExists_shouldReturn404() throws Exception {
        User updatedData = new User("John Updated", "john@example.com", "456 New St", "password123");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isNotFound());

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenEmailConflict_shouldReturn409() throws Exception {
        User existingUser = new User("John Doe", "john@example.com", "123 Main St", "password123");
        existingUser.setId(1L);
        User otherUser = new User("Other User", "other@example.com", "Address", "password123");
        otherUser.setId(2L);
        User updatedData = new User("John Updated", "other@example.com", "456 New St", "password123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isConflict());

        verify(userRepository).findById(1L);
        verify(userRepository).findByEmail("other@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUser() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_whenUserNotExists_shouldReturn404() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound());

        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
