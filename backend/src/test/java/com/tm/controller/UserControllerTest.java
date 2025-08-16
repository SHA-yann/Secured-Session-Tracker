package com.tm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	
	@Test
	void shouldCreateUser() throws Exception {
		
		User u = new User();
		u.setUsername("Yann");
		u.setPassword("secure_123");
		u.setEmail("yannsteve@ymail.fr");
		u.setRole("USER");
		
		Mockito.when(userS.createUser(Mockito.any(User.class))).thenReturn(u);
		
		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(oM.writeValueAsString(u)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("Yann"))
				.andExpect(jsonPath("$.email").value("yannsteve@ymail.fr"))
				.andExpect(jsonPath("$.password").value("secure_123"))
				.andExpect(jsonPath("$.role").value("USER"));
	}
}
