package com.tm.service;

import com.tm.model.User;

public class UserService {

	public User createUser(String name, String email) {
		
		return new User(name, email);
	}

}
