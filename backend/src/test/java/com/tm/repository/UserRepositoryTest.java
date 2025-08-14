package com.tm.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
	void saveAndFindBymail_shouldPersistAndRetrieve(){
		User u= new User("Yann","yannsteve@ymail.fr");
		
		User saved= uRep.saveAndFlush(u);
		assertThat(saved.getId()).isNotNull();
		
		Optional<User> mail= uRep.findByEmail("yannsteve@ymail.fr");
		assertThat(mail).isPresent();
		assertThat(mail.get().getName()).isEqualTo("Yann");
	}
}
