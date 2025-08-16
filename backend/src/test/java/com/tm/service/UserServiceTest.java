package com.tm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import com.tm.model.User;
import com.tm.repository.UserRepository;

class UserServiceTest {

	@Mock
	private UserRepository userR;
	
	@InjectMocks
	private UserService userS;
	
	private User toSave;
	private User u1;
	
	@BeforeEach
	void init() {
		MockitoAnnotations.openMocks(this);
		toSave=new User("Yann","yannsteve@ymail.fr","secure_123","ADMIN");
		u1=new User("john","john@free.fr","secure_123","USER");
	}
	
	@Test
	void createUser_shouldPersistAndReturnEntity() {
		
		when(userR.save(any(User.class))).thenAnswer(Invocation->Invocation.getArgument(0));
		
		User result = userS.createUser(toSave);
		
		assertNotNull(result);
		
		assertEquals("Yann",result.getUsername());
		assertEquals("yannsteve@ymail.fr",result.getEmail());
		assertEquals("secure_123", result.getPassword());
		assertEquals("ADMIN", result.getRole());
		verify(userR, times(1)).save(any(User.class));
	}
	
	@Test
	void test_souldreturnAllusers() {
		
		when(userR.findAll()).thenReturn(java.util.Arrays.asList(toSave,u1));
		
		List<User> result = userS.getAllUsers();
				
		assertThat(result).hasSize(2);
		assertThat(result.get(1).getUsername()).isEqualTo("john");
		verify(userR, times(1)).findAll();
	}
	
	@Test
	void test_emptyList() {
		
		when(userR.findAll()).thenReturn(Collections.emptyList());
		
		List<User> result= userS.getAllUsers();
		
		assertThat(result).isEmpty();
		verify(userR, times(1)).findAll();
	}
	
	@Test
	void test_shouldReturnUser_when_foundById() {
		
		
	}
}
