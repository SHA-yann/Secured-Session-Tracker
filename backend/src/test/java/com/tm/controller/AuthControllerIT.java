package com.tm.controller;


import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.tm.model.User;
import com.tm.service.UserService;

class AuthControllerIT {

	@Autowired
	private UserService userService;
	
	@Test
	void password_shouldBe_hashed() {
		
		User u= new User();
		u.setPassword("Plain1234");
		User saved= userService.createUser(u);
		
		assertNotEquals("Plain1234",saved.getPassword());
		assertTrue(saved.getPassword().startsWith("$2a$"));
	}
}
