package com.tm.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.tm.controller.AuthRequest;
import com.tm.model.RefreshToken;
import com.tm.model.User;
import com.tm.security.CookieProvider;
import com.tm.security.JwtProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;

public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService ;
	
	@Value("${security.cookie.domain}")
	private String cookieDomain;
	
	@Value("${security.cookie.secure}")
	private boolean cookieSecure;
	
	private static final String REFRESH_COOKIE="refresh_token";
	
	public AuthService(AuthenticationManager authenticationManager,
			UserService userService,
			JwtProvider jwtProvider,
			RefreshTokenService refreshTokenService) {
		this.authenticationManager = authenticationManager;
		this.userService = userService;
		this.jwtProvider = jwtProvider;
		this.refreshTokenService = refreshTokenService;
	}

	public Map<String,Cookie> login(AuthRequest request) throws BadCredentialsException{
	
		var result= new HashMap<String,Cookie>();
		
		Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		
		String token = jwtProvider.generateToken((UserDetails)auth.getPrincipal());
		
		RefreshToken rt = refreshTokenService.issue(userService.findByName(auth.getName()).get());
		
		int maxAge= (int) (rt.getExpiresAt().getEpochSecond()-java.time.Instant.now().getEpochSecond());
		
		Cookie cook=CookieProvider.createCookie(REFRESH_COOKIE, rt.getToken(), cookieDomain, cookieSecure, maxAge);
		result.put(token, cook);
		
		return result;
	}
	
	public Map<String, Cookie> refresh(HttpServletRequest request) {
		
		var result= new HashMap<String,Cookie>();
		var cookies= request.getCookies();
		if(cookies== null)
			return Collections.emptyMap() ;
			
		String raw=null;
		for(var c:cookies) {
			if(REFRESH_COOKIE.equals(c.getName())) {
				raw=c.getValue();
				break;
			}
		}
		
		if(raw==null ||raw.isBlank())
			return Collections.emptyMap();
		
		RefreshToken currentRt=refreshTokenService.verify(raw);
		RefreshToken nextRt= refreshTokenService.rotate(currentRt);
		String usersame = currentRt.getUser().getUsername();
		UserDetails uD= new MyUserDetails().loadUserByUsername(usersame);
		String access= jwtProvider.generateToken(uD);
		int maxAge=(int) (nextRt.getExpiresAt().getEpochSecond()-java.time.Instant.now().getEpochSecond());
		Cookie cook=CookieProvider.createCookie(REFRESH_COOKIE, nextRt.getToken(), cookieDomain, cookieSecure, maxAge);
		
		result.put(access, cook);
		return result;
	}
	
	public void logout(String username) {
		
		Optional<User> user=userService.findByName(username);
		if(user.isPresent())
			refreshTokenService.revokeUserTokens(user.get().getId());
		
	}
}
