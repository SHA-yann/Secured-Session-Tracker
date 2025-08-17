package com.tm.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.tm.model.User;

@DataJpaTest
class UserRepositoryTest {
	
	@Autowired
	private UserRepository userRepository;
	
	private User u1;
	private User u2;
	
	@BeforeEach
	void setup() {
		
		u2=new User("Yann","yannsteve@ymail.fr","secure_123","ADMIN");
		u1=new User("john","john@free.fr","secure_123","USER");
	}
	
	@Test
	void shouldSaveAndFindUserWithPasswordAndRole(){
		
		User saved=userRepository.save(u2);
		
		Optional<User> found= userRepository.findById(saved.getId());
		
    assertNotNull(found);
		assertEquals("Yann", found.get().getUsername());
		assertEquals("secure_123", found.get().getPassword());
		assertEquals("ADMIN", found.get().getRole());
		
	}
	
	@Test
	void findAll_returnsUsers() {
		
		userRepository.save(u1);
		userRepository.save(u2);
		
		List<User> users = userRepository.findAll();
		
		assertThat(users).hasSize(2);
		assertThat(users.get(0).getUsername()).isIn("john","Yann");
	}
	
	@Test
	void findAll_whenEmptyList() {
		
		List<User> users= userRepository.findAll();
		
		assertThat(users).isEmpty();
	}
	
	@Test
	void findByEmail_shouldReturnUserWhenExists() {
		
		userRepository.save(u1);
		
		Optional<User> found= userRepository.findByEmail("john@free.fr");
		
		assertThat(found).isPresent();
		assertThat(found.get().getPassword()).isEqualTo("secure_123");
	}
	
	@Test
	void findByEmail_shouldReturnUserWhenNotExists() {
		
		Optional<User> found= userRepository.findByEmail("franck@sfr.fr");
		
		assertThat(found).isNotPresent();
	}

}
