package com.tm.controller;

import java.time.Instant;

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
}
