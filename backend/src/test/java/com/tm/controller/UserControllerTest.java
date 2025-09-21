package com.tm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tm.Exceptions.UserNotFoundException;
import com.tm.model.Role;
import com.tm.model.User;
import com.tm.security.JwtAuthFilter;
import com.tm.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters=false)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private UserService userService;
	
	@MockitoBean
	private JwtAuthFilter jwtAuthFilter;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private User u;
	private User u1;
	private Pageable pageable;
	
	@BeforeEach
	void init() {
		u=new User("Yann","yannsteve@ymail.fr","secure_123");
		u.setRole(Role.ADMIN);
		u1=new User("john","john@free.fr","secure_123");
		u1.setRole(Role.USER);
		pageable=PageRequest.of(0, 10);
		
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
	    Authentication auth = new UsernamePasswordAuthenticationToken("Yann", "secure_123", authorities);
	    SecurityContextHolder.getContext().setAuthentication(auth);
		
	}
	
	@Test
	void shouldCreateUser() throws Exception {
		
		Mockito.when(userService.createUser(Mockito.any(User.class))).thenReturn(u);
		
		mockMvc.perform(post("/users")
				.with(user("Yann").roles("ADMIN"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(u)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("Yann"))
				.andExpect(jsonPath("$.email").value("yannsteve@ymail.fr"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.role").value(Role.ADMIN.name()));
		
		verify(userService, times(1)).createUser(any(User.class));
	}
	
	@Test
	void getAllUser_shouldReturnallUsers() throws Exception{

		Page<User> page= new PageImpl<>(List.of(u,u1));
		when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);
		
		mockMvc.perform(get("/users")
				.with(user("Yann").roles("ADMIN"))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[1].username").value("john"))
				.andExpect(jsonPath("$.content[1].password").doesNotExist())
				.andExpect(jsonPath("$.content[0].role").value(Role.ADMIN.name()));
	}
	
	@Test
	void getAllUser_When_Empty() throws Exception{
		
		when(userService.getAllUsers(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
		
		mockMvc.perform(get("/users")
				.with(user("Yann").roles("ADMIN"))
				.param("page", "0")
				.param("size", "10")
				.param("sort", "unsorted"))
				.andExpect(status().isOk());
		
		ArgumentCaptor<Pageable> pageableCaptor=ArgumentCaptor.forClass(Pageable.class);
		
		verify(userService).getAllUsers(pageableCaptor.capture());
		
		assertEquals(0,pageable.getPageNumber());
		assertEquals(10, pageable.getPageSize());
	}
	
	@Test
	void test_GetUserById_found() throws Exception {
		
		when(userService.getUserById(4L)).thenReturn(Optional.of(u));
		
		mockMvc.perform(get("/users/4")
				.with(user("Yann").roles("ADMIN"))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("Yann"))
				.andExpect(jsonPath("$.email").value("yannsteve@ymail.fr"))
				.andExpect(jsonPath("$.password").doesNotExist());
		verify(userService, times(1)).getUserById(4L);
	}
	
	@Test
	void test_GetUserById_notfound() throws Exception {
		
		when(userService.getUserById(99L)).thenReturn(Optional.empty());
		
		mockMvc.perform(get("/users/99").with(user("Yann").roles("ADMIN")))
										.andExpect(status().isNotFound());
		
		verify(userService, times(1)).getUserById(99L);
	}
	
	@Test
	void test_GetUserByMail_found() throws Exception{
		
		when(userService.getUserByEmail("john@free.fr")).thenReturn(Optional.of(u1));
		
		mockMvc.perform(get("/users/mail/{email}", "john@free.fr")
				.with(user("Yann").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("john"))
				.andExpect(jsonPath("$.password").doesNotExist());
		
		verify(userService, times(1)).getUserByEmail("john@free.fr");
	}
	
	@Test
	void test_GetUserByMail_notfound() throws Exception{
		
		when(userService.getUserByEmail("ghost@free.fr")).thenReturn(Optional.empty());
		
		mockMvc.perform(get("/users/mail/{email}", "ghost@free.fr")
				.with(user("Yann").roles("ADMIN")))
				.andExpect(status().isNotFound());
		
		verify(userService, times(1)).getUserByEmail("ghost@free.fr");
	}
	
	@Test
	void test_UpdateUser_found() throws Exception{
		
		when(userService.updateUser(eq(1L),any(User.class))).thenReturn(Optional.of(u1));
		
		mockMvc.perform(put("/users/1")
				.with(user("Yann").roles("ADMIN"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(u)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("john"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.role").value(Role.USER.name()));
		
		verify(userService, times(1)).updateUser(eq(1L),any(User.class));
	}
	
	@Test
	void test_UpdateUser_notFound() throws Exception{
	
		when(userService.updateUser(eq(99L),any(User.class))).thenReturn(Optional.empty());
		
		mockMvc.perform(put("/users/99")
				.with(user("Yann").roles("ADMIN"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(u1)))
				.andExpect(status().isNotFound());
		
		verify(userService, times(1)).updateUser(eq(99L),any(User.class));
	}
	
	@Test
	void test_DeleteUser_found() throws Exception{
		
		doNothing().when(userService).deleteUser(2L);
		
		mockMvc.perform(delete("/users/2")
		.with(user("John").roles("USER")))
		.andExpect(status().isNoContent());
		verify(userService, times(1)).deleteUser((2L));
	}
	
	@Test
	void test_DeleteUser_notFound() throws Exception{
		
		doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(99L);
		
		mockMvc.perform(delete("/users/99")).andExpect(status().isNotFound());
		verify(userService, times(1)).deleteUser((99L));
	}
}
