package com.tm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tm.model.AuthRequest;
import com.tm.model.AuthResponse;
import com.tm.model.User;
import com.tm.service.UserService;

import com.tm.configuration.JwtProvider;

@RestController
@RequestMapping("/login")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	//private final JwtProvider jwtProvider;
	
	
	public AuthController(AuthenticationManager authenticationManager,UserService userService/*,JwtProvider jwtProvider*/) {
		
		this.authenticationManager=authenticationManager;
		this.userService= userService;
		//this.jwtProvider= jwtProvider;
	}
	
	// LOGIN
	@PostMapping
	public ResponseEntity<?> login(@RequestBody AuthRequest request){
		
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		}catch(BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
		}
		
		UserDetails userDetails= userService.findByName(request.getUsername())
												.map(u-> new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPassword(), List.of(new SimpleGrantedAuthority(u.getRole().name()))))
												.orElseThrow();
		
		//String token = jwtProvider.generateToken(userDetails);
		
		return ResponseEntity.ok(new AuthResponse());
	}
	
}
