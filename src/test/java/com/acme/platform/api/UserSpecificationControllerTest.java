package com.acme.platform.api;

import com.acme.platform.model.User;
import com.acme.platform.service.UserSpecificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserSpecificationController.class)
class UserSpecificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSpecificationService userSpecificationService;

    @Test
    void searchUsers_shouldReturnFilteredUsers() throws Exception {
        User user = new User("John Doe", "john@example.com", "123 Main St", "password123");
        user.setId(1L);
        List<User> users = Arrays.asList(user);

        when(userSpecificationService.searchUsers(anyString(), anyString(), anyString())).thenReturn(users);

        mockMvc.perform(get("/api/spec/users/search")
                        .param("name", "john")
                        .param("email", "example")
                        .param("address", "Main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userSpecificationService).searchUsers("john", "example", "Main");
    }

    @Test
    void getUsersWithAddress_shouldReturnUsersWithAddress() throws Exception {
        User user = new User("John Doe", "john@example.com", "123 Main St", "password123");
        user.setId(1L);
        List<User> users = Arrays.asList(user);

        when(userSpecificationService.findUsersWithAddress()).thenReturn(users);

        mockMvc.perform(get("/api/spec/users/with-address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(userSpecificationService).findUsersWithAddress();
    }

    @Test
    void getUsersWithoutAddress_shouldReturnUsersWithoutAddress() throws Exception {
        User user = new User("John Doe", "john@example.com", null, "password123");
        user.setId(1L);
        List<User> users = Arrays.asList(user);

        when(userSpecificationService.findUsersWithoutAddress()).thenReturn(users);

        mockMvc.perform(get("/api/spec/users/without-address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(userSpecificationService).findUsersWithoutAddress();
    }
}
