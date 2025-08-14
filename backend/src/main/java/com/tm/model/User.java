package com.tm.model;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="users")
@Getter @Setter 
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	@NotBlank
	@Column(nullable=false, unique=true, length=20)
	private String Username;
	
	@Email
	@Column(nullable=false, length=50)
	private String email;
	
	@NotBlank
	@Column(nullable=false)
	private String password;
	
	@Column(nullable=false)
	private String role;
	
	@Column(nullable=false, updatable=false)
	private Instant createdAt;
	
	@Column(nullable=false)
	private Instant updatedAt;
	

	@PrePersist
	void onCreate() {
		Instant now= Instant.now();
		createdAt=now;
		updatedAt=now;
		}
	
	@PreUpdate
	void onUpdate() {
		updatedAt=Instant.now();
	}

}
