package com.tm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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
	private UserService userService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private User u;
	private User u1;
	
	@BeforeEach
	void init() {
		u=new User("Yann","yannsteve@ymail.fr","secure_123","ADMIN");
		u1=new User("john","john@free.fr","secure_123","USER");
	}
	
	@Test
	void shouldCreateUser() throws Exception {
		
		Mockito.when(userService.createUser(Mockito.any(User.class))).thenReturn(u);
		
		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(u)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("Yann"))
				.andExpect(jsonPath("$.email").value("yannsteve@ymail.fr"))
				.andExpect(jsonPath("$.password").value("secure_123"))
				.andExpect(jsonPath("$.role").value("ADMIN"));
		
		verify(userService, times(1)).createUser(any(User.class));
	}
	
	@Test
	void getAllUser_shouldReturnallUserList() throws Exception{
		
		when(userService.getAllUsers()).thenReturn(Arrays.asList(u,u1));
		
		mockMvc.perform(get("/api/users")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[1].username").value("john"))
				.andExpect(jsonPath("$[0].role").value("ADMIN"));
	}
	
	@Test
	void getAllUser_When_EmptyList() throws Exception{
		
		when(userService.getAllUsers()).thenReturn(Collections.emptyList());
		
		mockMvc.perform(get("/api/users")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
		
		verify(userService, times(1)).getAllUsers();
	}
	
	@Test
	void test_GetUserById_found() throws Exception {
		
		when(userService.getUserById(4L)).thenReturn(Optional.of(u));
		
		mockMvc.perform(get("/api/users/4")).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("Yann"))
				.andExpect(jsonPath("$.email").value("yannsteve@ymail.fr"));
		
		verify(userService, times(1)).getUserById(4L);
	}
	
	@Test
	void test_GetUserById_notfound() throws Exception {
		
		when(userService.getUserById(99L)).thenReturn(Optional.empty());
		
		mockMvc.perform(get("/api/users/99")).andExpect(status().isNotFound());
		
		verify(userService, times(1)).getUserById(99L);
	}
	
	@Test
	void test_GetUserByMail_found() throws Exception{
		
		when(userService.getUserByEmail("john@free.fr")).thenReturn(Optional.of(u1));
		
		mockMvc.perform(get("/api/users/email/{email}", "john@free.fr"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("john"));
		
		verify(userService, times(1)).getUserByEmail("john@free.fr");
	}
	
	@Test
	void test_GetUserByMail_notfound() throws Exception{
		
		when(userService.getUserByEmail("ghost@free.fr")).thenReturn(Optional.empty());
		
		mockMvc.perform(get("/api/users/email/{email}", "ghost@free.fr"))
				.andExpect(status().isNotFound());
		
		verify(userService, times(1)).getUserByEmail("ghost@free.fr");
	}
	
	@Test
	void test_UpdateUser_found() throws Exception{
		
		when(userService.updateUser(eq(1L),any(User.class))).thenReturn(Optional.of(u1));
		
		mockMvc.perform(put("/api/users/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(u)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("john"))
				.andExpect(jsonPath("$.role").value("USER"));
		
		verify(userService, times(1)).updateUser(eq(1L),any(User.class));
	}
	
	@Test
	void test_UpdateUser_notFound() throws Exception{
	
		when(userService.updateUser(eq(99L),any(User.class))).thenReturn(Optional.empty());
		
		mockMvc.perform(put("/api/users/99")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(u1)))
				.andExpect(status().isNotFound());
		
		verify(userService, times(1)).updateUser(eq(99L),any(User.class));
	}
}
