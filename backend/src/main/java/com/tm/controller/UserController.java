package com.tm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tm.model.User;
import com.tm.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userS;
	
	public UserController(UserService userService) {
		this.userS=userService;
	}
	
	@PostMapping
	public ResponseEntity<User> createUser(User user){
		User created=userS.createUser(user);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}
	
	@GetMapping
	public ResponseEntity<List<User>> getAllUsers(){
		List<User> users = userS.getAllUsers();
		return new ResponseEntity<>(users,HttpStatus.OK);
	}
}
