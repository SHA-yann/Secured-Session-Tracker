package com.tm.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.tm.model.User;

@DataJpaTest
class UserRepositoryTest {
	
	@Autowired
	private UserRepository uRep;
	
	@Test
	void shouldSaveAndFindUserWithPasswordAndRole(){
		
		User u= new User();
		u.setUsername("Yann");
		u.setPassword("secure_123");
		u.setRole("ADMIN");
		u.setEmail("yannsteve@ymail.fr");
		
		User saved=uRep.save(u);
		
		Optional<User> found= uRep.findById(saved.getId());
		assertNotNull(found);
		assertEquals("Yann", found.get().getUsername());
		assertEquals("secure_123", found.get().getPassword());
		assertEquals("ADMIN", found.get().getRole());
		
	}
}
