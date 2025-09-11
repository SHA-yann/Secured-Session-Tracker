package com.tm.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tm.model.Role;
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
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> register(@RequestBody User user){
		
		try {
			UserToAdmin created=UserToAdmin.fromEntity(userService.createUser(user));
			
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
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<UserToAdmin>> getAllUsers(@PageableDefault(size=10,sort="username") Pageable pageable){
		Page<User> users = userService.getAllUsers(pageable);
		Page<UserToAdmin> toPage= users.map(UserToAdmin::fromEntity);
		
		return  ResponseEntity.ok(toPage);
	}
	
	// GET user by id
	@GetMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN') or #id==principal.id")
	public ResponseEntity<UserToAdmin> getUserById(@PathVariable Long id){
		
		return userService.getUserById(id)
				.map(UserToAdmin::fromEntity)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	// GET user by email
	@GetMapping("/users/mail/{email}")
	@PreAuthorize("hasRole('ADMIN') or #id==principal.id")
	public ResponseEntity<UserToAdmin> getUserByEmail(@PathVariable String email){
		return userService.getUserByEmail(email)
							.map(UserToAdmin::fromEntity)
							.map(ResponseEntity::ok)
							.orElse(ResponseEntity.notFound().build());
	}
	
	@PutMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN') or #id==principal.id")
	public ResponseEntity<UserToAdmin> updateUser(@PathVariable Long id, @RequestBody User user){
		
			return userService.updateUser(id, user)
								.map(UserToAdmin::fromEntity)
								.map(updated->ResponseEntity.ok(updated))
								.orElse(ResponseEntity.notFound().build());
												
	}
	
	//DELETE user
	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id){
		
		return userService.deleteUser(id)?ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	}
	
	//SEARCH
	
	public ResponseEntity<Page<UserToAdmin>> searchUsers(
		@RequestParam(required=false) String username, @RequestParam(required=false) Role role, Pageable pageable){
			
			Page<User> users= userService.searchUsers(username, role, pageable);
			Page<UserToAdmin> toPage= users.map(UserToAdmin::fromEntity);
			
			return ResponseEntity.ok(toPage);
	}
}
