package com.tm.service;

import java.util.List;

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
	public User createUser(User u) {
		
		return uRep.save(u);
	}
	
	public List<User> getAllUsers(){
		return uRep.findAll();
	}

}
