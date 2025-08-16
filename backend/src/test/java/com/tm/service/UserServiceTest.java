package com.tm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.mockito.Mockito.*;

import com.tm.model.User;
import com.tm.repository.UserRepository;

class UserServiceTest {

	@Mock
	private UserRepository userR;
	
	@InjectMocks
	private UserService userS;
	
	@BeforeEach
	void init() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void createUser_shouldPersistAndReturnEntity() {

		User toSave = new User();
		toSave.setUsername("Yann");
		toSave.setPassword("secure_123");
		toSave.setEmail("yannsteve@ymail.fr");
		toSave.setRole("ADMIN");
		
		when(userR.save(any(User.class))).thenAnswer(Invocation->Invocation.getArgument(0));
		
		User result = userS.createUser(toSave);
		
		assertNotNull(result);
		
		assertEquals("Yann",result.getUsername());
		assertEquals("yannsteve@ymail.fr",result.getEmail());
		assertEquals("secure_123", result.getPassword());
		assertEquals("ADMIN", result.getRole());
		verify(userR, times(1)).save(any(User.class));
	}
}
