package com.um.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;
import com.um.repository.UserRepository;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    void init() {
        // Clean database before each test
    	userRepository.deleteAll();

        // Create a test user and set authentication context
        User user = new User("Yann",passwordEncoder.encode("password123")
        		,"yannsteve@gmail.com",Role.ADMIN,Status.ACTIVE);
        user.setCreatedBy("System");
        user.setUpdatedBy("David");
        userRepository.save(user);
        
        
        
    }

    // -------------------- LOGIN --------------------
    @Test
    void shouldReturnAccessToken_AndRefreshCookie() throws Exception {
        // Test login endpoint returns JWT access token and HttpOnly refresh cookie
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"Yann\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    // -------------------- REFRESH TOKEN --------------------
    @Test
    void refresh_should_rotate_and_issueNewToken() throws Exception {
        // Perform login to get refresh token cookie
        var loginResult = mockMvc.perform(post("/auth/login")
        		.contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"Yann\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        var refreshCookie = loginResult.getResponse()   .getCookie("refresh_token");

        // Use refresh endpoint to get new access token and rotated refresh cookie
        mockMvc.perform(post("/auth/refresh")
        		.cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refresh_token"));
    }

    // -------------------- LOGOUT --------------------
    @Test
    void logout_shouldClear_refreshCookie() throws Exception {
        // Perform login to get refresh token cookie
        var loginResult = mockMvc.perform(post("/auth/login")
        		.contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"Yann\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        var json=loginResult.getResponse().getContentAsString();
        var accessToken= JsonPath.read(json, "$.accessToken");
        var refreshCookie = loginResult.getResponse().getCookie("refresh_token");

        // Call logout and validate that refresh cookie is cleared
        mockMvc.perform(post("/auth/logout")
        		.header("Authorization", "Bearer "+accessToken)
        		.contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"Yann\"}")
                .cookie(refreshCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));
    }

    // -------------------- INVALID LOGIN --------------------
    @Test
    void login_withWrongPassword_should_return401() throws Exception {
        // Validate login failure with incorrect password
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"Yann\",\"password\":\"plainWrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------- REFRESH WITHOUT COOKIE --------------------
    @Test
    void noCookie_should_return401() throws Exception {
        // Validate refresh request fails when no cookie is provided
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }
}
