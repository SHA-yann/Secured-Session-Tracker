package com.tm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tm.model.User;
import com.tm.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService=userService;
	}
	
	// POST a user
	@PostMapping
	public ResponseEntity<User> createUser(@RequestBody User user){
		User created=userService.createUser(user);
		return ResponseEntity.status(201).body(created);
	}
	
	// GET all users
	@GetMapping
	public ResponseEntity<List<User>> getAllUsers(){
		List<User> users = userService.getAllUsers();
		return  ResponseEntity.ok(users);
	}
	
	// GET user by id
	@GetMapping("/{id}")
	public ResponseEntity<User> getUserById(@PathVariable Long id){
		
		return userService.getUserById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	// GET user by email
	@GetMapping("/mail/{email}")
	public ResponseEntity<User> getUserByEmail(@PathVariable String email){
		return userService.getUserByEmail(email).map(ResponseEntity::ok)
										.orElse(ResponseEntity.notFound().build());
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user){
		
		return userService.updateUser(id, user).map(updated->ResponseEntity.ok(updated))
												.orElse(ResponseEntity.notFound().build());
	}
	
	//DELETE user
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id){
		
		return userService.deleteUser(id)?ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	}
}
