package com.tm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tm.model.User;
import com.tm.repository.UserRepository;

@Service
public class UserService {
	
	private final UserRepository uRep;
	
	public UserService(UserRepository uRep) {
		this.uRep=uRep;
	}

	@Transactional
	public User createUser(String name, String email) {
		
		User u = new User();
		u.setName(name);
		u.setEmail(email);
		
		return uRep.save(u);
	}

}
