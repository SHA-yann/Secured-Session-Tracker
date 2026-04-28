package com.um.configuration;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.um.model.Status;
import com.um.repository.UserRepository;
import com.um.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Utility class for JWT generation, parsing, and validation.
 * Handles creation of JWTs with roles, expiration, and signature verification.
 */
@Getter
@Setter
@Component
@Slf4j
public class JwtProvider {

    private final SecretKey key;
    private final Long expiration;
    private final UserRepository userRepo;
    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Constructor initializing the secret key and token expiration time.
     *
     * @param secret JWT signing secret
     * @param expiration token expiration in milliseconds
     */
    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") long expiration,
                       UserRepository userRepo,
                       ReactiveStringRedisTemplate redisTemplate) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.userRepo = userRepo;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Creates a JWT token with given claims and subject.
     *
     * @param claims additional claims to include
     * @param subject token subject (usually username)
     * @return compact JWT string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates a JWT token for a given user.
     * Includes user roles as claims.
     *	
     * @param userDetails authenticated user details
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("Role", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        Long userAuthenticatedId = userRepo.findByUsername(userDetails.getUsername()).get().getId();
        String userAuthenticatedStatus = userRepo.findByUsername(userDetails.getUsername()).get().getStatus().name();
        claims.put("ID",userAuthenticatedId );
        claims.put("uStatus", userAuthenticatedStatus);
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Extracts username (subject) from JWT token.
     *
     * @param token JWT string
     * @return username
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts roles from JWT token.
     *
     * @param token JWT string
     * @return list of roles
     */
    public List<String> extractRole(String token) {
        return extractAllClaims(token).get("Role", List.class);
    }

    /**
     * Extracts expiration date from JWT token.
     *
     * @param token JWT string
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token JWT string
     * @return true if expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates a JWT token against user details.
     *
     * @param token JWT string
     * @param userDetails authenticated user
     * @return true if token is valid, false otherwise
     */
    public Boolean validToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Parses and extracts all claims from a JWT token.
     *
     * @param token JWT string
     * @return Claims object
     */
    Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public Mono<Boolean> isBlacklisted(String token) {
    	
    	return redisTemplate.hasKey("Blacklist:"+token);
    }
}
