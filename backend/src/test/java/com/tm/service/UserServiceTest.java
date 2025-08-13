package com.tm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.tm.model.User;

class UserServiceTest {

	@Test
	void shouldCreateValidUser() {
		UserService uServ = new UserService();
		User user = uServ.createUser("Yann Steve", "yannsteve@ymail.fr");
		
		assertNotNull(user);
		assertEquals("Yann Steve", user.getName());
		assertEquals("yannsteve@ymail.fr", user.getEmail());
	}
}
