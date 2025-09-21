package com.tm.controller;


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
import org.springframework.test.web.servlet.MockMvc;

import com.tm.model.User;
import com.tm.repository.UserRepository;
import com.tm.service.UserService;

import jakarta.transaction.Transactional;


@SpringBootTest
@AutoConfigureMockMvc(addFilters=false)
@Transactional
class AuthControllerIT {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserService userService;
	
	@BeforeEach
	void init() {
		
		User user=new User("Yann","yannsteve@gmail.com","plainPassword123");
		User saved=userService.createUser(user);
		System.out.println(saved.getUsername()+","+saved.getPassword()+","+saved.getEmail()+","+saved.getCreatedAt()+","+saved.getRole()+","+saved.getUpdatedAt());
	}
	
	@Test
	void shouldReturnAccesToken_AndRefreshCookie() throws Exception{
		
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"Yann\",\"password\":\"plainPassword123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").exists())
				.andExpect(cookie().exists("refresh_token"))
				.andExpect(cookie().httpOnly("refresh_token",true));
	}
	
	@Test
	void refresh_should_rotate_and_issueNewToken() throws Exception{
		
		var loginResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
															.content("{\"username\":\"Yann\",\"password\":\"plainPassword123\"}"))
															.andExpect(status().isOk())
															.andReturn();
		
		var refreshCookie=loginResult.getResponse().getCookie("refresh_token");
		
		
		mockMvc.perform(post("/auth/refresh")
				.cookie(refreshCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").exists())
				.andExpect(cookie().exists("refresh_token"));
	}
	
	@Test
	void logout_shouldClear_refreshCookie() throws Exception{
		
		var loginResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"Yann\",\"password\":\"plainPassword123\"}"))
				.andExpect(status().isOk())
				.andReturn();
		
		var refreshCookie=loginResult.getResponse().getCookie("refresh_token");
		
		mockMvc.perform(post("/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"Yann\"}")
				.cookie(refreshCookie))
		.andExpect(status().isNoContent())
		.andExpect(cookie().maxAge("refresh_token", 0));
	}
	
	@Test
	void login_withWrogPassword_should_return401() throws Exception{
		
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"Yann\",\"password\":\"plainWrong\"}"))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	void noCookie_should_return401() throws Exception {
		
		mockMvc.perform(post("/auth/refresh"))
				.andExpect(status().isUnauthorized());
	}
}
