package com.tm.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tm.model.User;
import com.tm.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private UserService userS;
	
	@Autowired
	private ObjectMapper oM;
	
	private User u;
	private User u1;
	
	@BeforeEach
	void init() {
		u=new User("Yann","yannsteve@ymail.fr","secure_123","ADMIN");
		u1=new User("john","john@free.fr","secure_123","USER");
	}
	
	@Test
	void shouldCreateUser() throws Exception {
		
		Mockito.when(userS.createUser(Mockito.any(User.class))).thenReturn(u);
		
		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(oM.writeValueAsString(u)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("Yann"))
				.andExpect(jsonPath("$.email").value("yannsteve@ymail.fr"))
				.andExpect(jsonPath("$.password").value("secure_123"))
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}
	
	@Test
	void detAllUser_shouldReturnallUserList() throws Exception{
		
		when(userS.getAllUsers()).thenReturn(Arrays.asList(u,u1));
		
		mockMvc.perform(get("/api/users")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[1].username").value("john"))
				.andExpect(jsonPath("$[0].role").value("ADMIN"));
	}
	
	@Test
	void getAllUser_shouldReturnsEmptyList() throws Exception{
		
		when(userS.getAllUsers()).thenReturn(Collections.emptyList());
		
		mockMvc.perform(get("/api/users")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
		
		verify(userS, times(1)).getAllUsers();
	}
}
