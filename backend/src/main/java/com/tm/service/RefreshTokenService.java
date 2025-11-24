package com.tm.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tm.model.RefreshToken;
import com.tm.model.User;
import com.tm.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

	public RefreshTokenService(RefreshTokenRepository refp) {
		this.refp = refp;
	}

	private final RefreshTokenRepository refp;
	
	@Value("${refresh.expiration-days}")
	private long refreshDays;
	
	public RefreshToken issue(User user) {
		Instant exp= Instant.now().plus(refreshDays,ChronoUnit.DAYS);
		RefreshToken rt= new RefreshToken(user, exp);
		return refp.save(rt);
	}
	
	public RefreshToken verify(String token) {
		
		RefreshToken rt= refp.findByToken(token).orElseThrow(()->new IllegalArgumentException("Invalid refresh token"));
		if(rt.isRevoked() || Instant.now().isAfter(rt.getExpiresAt()))
			throw new IllegalStateException("refresh token expired or revoked");
		return rt;
	}
	
	public RefreshToken rotate (RefreshToken oldToken) {
		oldToken.setRevoked(true);
		refp.save(oldToken);
		return issue(oldToken.getUser());
	}
	
	public void revokeUserTokens(long userId) {
		refp.findAll().stream()
						.filter(rt->(rt.getUser().getId()==userId))
						.forEach(rt->{rt.setRevoked(true);
										refp.save(rt);});
	}
}
