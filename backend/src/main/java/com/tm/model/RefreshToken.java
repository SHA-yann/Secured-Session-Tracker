package com.tm.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter @Setter
@Table(name="Refresh_tokens", indexes= {@Index(name="idx_rt_token",columnList="token", unique=true),
										@Index(name="idx_rt_user",columnList="user_id")})
public class RefreshToken {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable=false,unique=true,updatable=false)
	private String token;
	
	@ManyToOne(optional=false,fetch=FetchType.LAZY)
	@JoinColumn(name="user_id",nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Instant expiresAt;
	
	@Column(nullable=false)
	private boolean revoked= false;
	
	
	public RefreshToken (User user, Instant expiresAt) {
		this.token= UUID.randomUUID().toString();
		this.user=user;
		this.expiresAt=expiresAt;
		
	}
}
