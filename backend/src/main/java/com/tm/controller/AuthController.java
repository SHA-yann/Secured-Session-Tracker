package com.tm.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tm.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/auth")
public class AuthController {
	
	private final AuthService authService;
	
	public AuthController(AuthService authService) {
		
		this.authService= authService;
	}
	
	// LOGIN
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthRequest request) throws BadCredentialsException{
		
		Map<String, Cookie> login= new HashMap<String,Cookie>();
		
		try {
			login = authService.login(request);
		}catch(BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
		}
		return ResponseEntity.ok()
							.header(HttpHeaders.SET_COOKIE,login.values().toString())
							.body(new AuthResponse(login.keySet().toString(),null));
	}
	
	//REFRESH
	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(HttpServletRequest request){
		
		Map<String, Cookie> refresh= authService.refresh(request);;
		
		if(refresh.equals(Collections.emptyMap()))
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No cookie to refresh authentication ");

		return ResponseEntity.ok()
							.header(HttpHeaders.SET_COOKIE,refresh.values().toString())
							.body(new AuthResponse(refresh.keySet().toString(),null));
		
	}
	
	//LOGOUT
	@PostMapping("/logout")
	public ResponseEntity<?> logout(@RequestBody Map<String,String> req){
		
		String username=req.get("username");
		authService.logout(username);
		
		return ResponseEntity.noContent()
							.header(HttpHeaders.SET_COOKIE, "")
							.build();
		
	}
	
}
