package com.tm.controller;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.tm.model.Role;
import com.tm.model.User;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Component
public class UserToAdmin {
	
	private Long Id;
	
	@NotBlank
	private String username;
	
	@NotBlank
	private Role role;
	
	@NotBlank
	private String email;
	private Instant createdAt;
	private Instant updatedAt;
	
	public static UserToAdmin fromEntity(User user) {

		return new UserToAdmin(user.getId(),
		user.getUsername(),
		user.getRole(),
		user.getEmail(),
		user.getCreatedAt(),
		user.getUpdatedAt());
	}
	
	public User toEntity() {
		
		User user= new User();
		user.setId(this.Id);
		user.setUsername(this.username);
		user.setEmail(this.email);
		user.setRole(this.role);
		
		return user;
	}
}
