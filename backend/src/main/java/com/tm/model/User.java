package com.tm.model;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Table(name="users")
@Getter @Setter 
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	@NotBlank
	@Column(nullable=false, length=20)
	private String username;
	
	@Email
	@Column(nullable=false,unique=true, length=50)
	private String email;
	
	@NotBlank
	@Column(nullable=false)
	private String password;
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private Role role;
	
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

	public User(@NotBlank String username, @Email String email, @NotBlank String password) {
		super();
		this.username = username;
		this.email = email;
		this.password = password;
	}

}

