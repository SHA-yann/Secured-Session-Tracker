package com.tm.service;

import static org.assertj.core.api.Assertions.assertThat;
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
		toSave.setName("Yann");
		toSave.setEmail("yannsteve@ymail.fr");
		
		User saved = new User();
		saved.setId(12L);
		saved.setName("Yann");
		saved.setEmail("yannsteve@ymail.fr");
		
		when(userR.save(any(User.class))).thenReturn(saved);
		
		User result = userS.createUser("Yann", "yannsteve@ymail.fr");
		
		assertThat(result.getId()).isEqualTo(12L);
		assertThat(result.getEmail()).isEqualTo("yannsteve@ymail.fr");
		verify(userR, times(1)).save(any(User.class));
	}
}
