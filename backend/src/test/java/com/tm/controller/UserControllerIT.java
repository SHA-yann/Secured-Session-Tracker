package com.tm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tm.model.Role;
import com.tm.model.User;
import com.tm.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters=false)
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        userService.wipeAll();
    }

    @Test
    void testFullCrudFlow() throws Exception {
        // 1. CREATE
        User user = new User("Yann","yannsteve@ymail.fr","secure_123");
        user.setRole(Role.ADMIN);

        String userJson = objectMapper.writeValueAsString(user);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User created = objectMapper.readValue(response, User.class);
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
        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isNoContent());

        // 5. READ after DELETE → Not Found
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isNotFound());
    }
}
