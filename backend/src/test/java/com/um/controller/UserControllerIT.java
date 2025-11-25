package com.um.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.dto.UserRequest;
import com.um.dto.UserResponse;
import com.um.model.Role;
import com.um.model.Status;
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
     * 3. UPDATE emai and role of the user
     * 4. DELETE the user
     * 5. VERIFY user not found after deletion
     */
    @Test
    @WithMockUser(username="admin", roles= {"ADMIN"})
    void testFullCrudFlow() throws Exception {
        // 1. CREATE
        User user = new User("Yann","secure_123", "yannsteve@ymail.fr",Role.ADMIN,Status.ACTIVE);
        UserRequest uReq= new UserRequest(user.getUsername(), user.getPassword(), user.getEmail(), user.getRole(), user.getStatus());
        String userJson = objectMapper.writeValueAsString(uReq);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse created = objectMapper.readValue(response, UserResponse.class);
        assertNotNull(created.id());
        Long userId = created.id();

        // 2. READ
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Yann"))
                .andExpect(jsonPath("$.email").value("yannsteve@ymail.fr"));

        // 3. UPDATE
        UserRequest uUp= new UserRequest("John", user.getPassword(),"john@free.fr" , Role.USER, user.getStatus());
        String updatedJson = objectMapper.writeValueAsString(uUp);
        String name=created.username();

        mockMvc.perform(put("/users/" + name)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@free.fr"));

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
