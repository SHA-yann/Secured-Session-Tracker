package com.tm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.controller.UserToAdmin;
import com.um.model.User;
import com.um.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for UserController covering full CRUD flow:
 * - CREATE user
 * - READ user by ID
 * - UPDATE user
 * - DELETE user
 * - Verify Not Found after deletion
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Clear the database before each test and set up authentication context.
     */
    @BeforeEach
    void cleanDatabase() {
        userService.wipeAll(); // remove all users

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Full CRUD integration test:
     * 1. CREATE a user
     * 2. READ the user
     * 3. UPDATE the username
     * 4. DELETE the user
     * 5. VERIFY user not found after deletion
     */
    @Test
    @WithMockUser(username="admin", roles= {"ADMIN"})
    void testFullCrudFlow() throws Exception {
        // 1. CREATE
        User user = new User("Yann", "yannsteve@ymail.fr", "secure_123");
        String userJson = objectMapper.writeValueAsString(user);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserToAdmin created = objectMapper.readValue(response, UserToAdmin.class);
        assertNotNull(created.getId());
        Long userId = created.getId();

        // 2. READ
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Yann"))
                .andExpect(jsonPath("$.email").value("yannsteve@ymail.fr"));

        // 3. UPDATE
        created.setUsername("john");
        String updatedJson = objectMapper.writeValueAsString(created);

        mockMvc.perform(put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        // 4. DELETE
        mockMvc.perform(delete("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 5. READ after DELETE → Not Found
        mockMvc.perform(get("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
