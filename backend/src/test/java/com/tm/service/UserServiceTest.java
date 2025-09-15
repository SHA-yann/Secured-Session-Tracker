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
import java.util.Optional;

import com.tm.model.User;
import com.tm.repository.UserRepository;

class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	
	@InjectMocks
	private UserService userService;
	
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
		
		when(userRepository.save(any(User.class))).thenAnswer(Invocation->Invocation.getArgument(0));
		
		User result = userService.createUser(toSave);
		
		assertNotNull(result);
		
		assertEquals("Yann",result.getUsername());
		assertEquals("yannsteve@ymail.fr",result.getEmail());
		assertEquals("secure_123", result.getPassword());
		assertEquals("ADMIN", result.getRole());
		verify(userRepository, times(1)).save(any(User.class));
	}
	
	@Test
	void test_souldreturnAllusers() {
		
		when(userRepository.findAll()).thenReturn(java.util.Arrays.asList(toSave,u1));
		
		List<User> result = userService.getAllUsers();
				
		assertThat(result).hasSize(2);
		assertThat(result.get(1).getUsername()).isEqualTo("john");
		verify(userRepository, times(1)).findAll();
	}
	
	@Test
	void test_emptyList() {
		
		when(userRepository.findAll()).thenReturn(Collections.emptyList());
		
		List<User> result= userService.getAllUsers();
		
		assertThat(result).isEmpty();
		verify(userRepository, times(1)).findAll();
	}
	
	@Test
	void test_foundById() {
		
		when(userRepository.findById(2L)).thenReturn(Optional.of(u1));
		
		Optional<User> result = userService.getUserById(2L);
		
		assertThat(result).isPresent();
		assertThat(result.get().getEmail()).isEqualTo("john@free.fr");
		
		verify(userRepository, times(1)).findById(2L);
		
	}
	
	@Test
	void test_notFoundById() {
		
		when(userRepository.findById(2L)).thenReturn(Optional.empty());
		
		Optional<User> result = userService.getUserById(2L);
		
		assertThat(result).isNotPresent();
		verify(userRepository, times(1)).findById(2L);
	}
	
	@Test
	void test_foundByEmail() {
		
		when(userRepository.findByEmail("john@free.fr")).thenReturn(Optional.of(u1));
		
		Optional<User> result = userService.getUserByEmail("john@free.fr");
		
		assertThat(result).isPresent();
		assertThat(result.get().getUsername()).isEqualTo("john");
		
		verify(userRepository, times(1)).findByEmail("john@free.fr");
		
	}
	
	@Test
	void test_notfoundByEmail() {
		
when(userRepository.findByEmail("frank@wanado.fr")).thenReturn(Optional.empty());
		
		Optional<User> result = userService.getUserByEmail("frank@wanado.fr");
		
		assertThat(result).isNotPresent();
		
		verify(userRepository, times(1)).findByEmail("frank@wanado.fr");
	}
	
	@Test
	void test_UpdateUser_found() {
		
		when(userRepository.findById(2L)).thenReturn(Optional.of(toSave));
		
		when(userRepository.save(any(User.class))).thenAnswer(invocation->invocation.getArgument(0));
		
		Optional<User> result= userService.updateUser(2L,u1);
		
		assertThat(result).isPresent();
		assertThat(result.get().getUsername()).isEqualTo("john");
		assertThat(result.get().getPassword()).isEqualTo("secure_123");
		assertThat(result.get().getRole()).isEqualTo("USER");
		
		verify(userRepository, times(1)).findById(2L);
		verify(userRepository, times(1)).save(toSave);
	}
	
	@Test
	void test_UpdateUser_notFound() {
		
		when(userRepository.findById(99L)).thenReturn(Optional.empty());
		
		Optional<User> result= userService.updateUser(99L, u1);
		
		assertThat(result).isNotPresent();
		verify(userRepository, times(1)).findById(99L);
		verify(userRepository, never()).save(any(User.class));
		
	}
}
