package com.tm.configuration;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.tm.model.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
@Component
public class JwtProvider {
    private final SecretKey key;
    private final Long expiration;

    public JwtProvider(@Value("${jwt.secret}")String secret,@Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }
	
	private String createToken(Map<String,Object>claims, String subject) {
		
		return Jwts.builder().setClaims(claims)
							.setSubject(subject)
							.setIssuedAt(new Date(System.currentTimeMillis()))
							.setExpiration(new Date(System.currentTimeMillis()+expiration))
							.signWith(key,SignatureAlgorithm.HS256)
							.compact();
	}
	
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims=new HashMap<>();
		claims.put("roles", userDetails.getAuthorities().stream()
										.map(auth->auth.getAuthority())
										.toList());
		return createToken(claims,userDetails.getUsername());
	}
	
	public String extractUsername(String token) {
		
		return extractAllClaims(token).getSubject();
	}
	
	public List<Role> extractRole(String token) {
		
		return extractAllClaims(token).get("Roles", List.class);
	}
	
	public Date  extractExpiration(String token) {
		
		return  extractAllClaims(token).getExpiration();
	}
	
	public Boolean isTokenExpired(String token) {
		
		return extractExpiration(token).before(new Date());
		
	}
	
	public Boolean validToken(String token, UserDetails userDetails) {
		
		return extractUsername(token).equals(userDetails.getUsername())&& !isTokenExpired(token);
	}
	
	private Claims extractAllClaims(String token) {
		
		return Jwts.parserBuilder().setSigningKey(key)
									.build()
									.parseClaimsJws(token)
									.getBody();
	}
}