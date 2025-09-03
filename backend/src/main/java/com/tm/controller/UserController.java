package com.tm.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tm.model.User;
import com.tm.service.UserService;

@RestController
@RequestMapping("/")
public class UserController {

	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService=userService;
	}
	
	// REGISTER
	@PostMapping("/users")
	public ResponseEntity<?> register(@RequestBody User user){
		
		try {
			User created=userService.createUser(user);
			
			URI location= ServletUriComponentsBuilder.fromCurrentRequest()
													.path("/{id}")
													.buildAndExpand(created.getId())
													.toUri();
			return ResponseEntity.created(location)
								.body(created);
		}catch(Exception e) {
			
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
		}
				
	}
	
	// GET all users
	@GetMapping("/users")
	public ResponseEntity<List<User>> getAllUsers(){
		List<User> users = userService.getAllUsers();
		return  ResponseEntity.ok(users);
	}
	
	// GET user by id
	@GetMapping("/users/{id}")
	public ResponseEntity<User> getUserById(@PathVariable Long id){
		
		return userService.getUserById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	// GET user by email
	@GetMapping("/users/mail/{email}")
	public ResponseEntity<User> getUserByEmail(@PathVariable String email){
		return userService.getUserByEmail(email).map(ResponseEntity::ok)
										.orElse(ResponseEntity.notFound().build());
	}
	
	@PutMapping("/users/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user){
		
			return userService.updateUser(id, user).map(updated->ResponseEntity.ok(updated))
												.orElse(ResponseEntity.notFound().build());
												
	}
	
	//DELETE user
	@DeleteMapping("/users/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id){
		
		return userService.deleteUser(id)?ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	}
}
