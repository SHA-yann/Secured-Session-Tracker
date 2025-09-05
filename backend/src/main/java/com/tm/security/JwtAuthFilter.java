package com.tm.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tm.service.MyUserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	
	private final JwtProvider jwtProvider;
	private final MyUserDetails myUserDetails;
	
	public JwtAuthFilter(JwtProvider jP,MyUserDetails myUserDetails) {
		this.jwtProvider=jP;
		this.myUserDetails=myUserDetails;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException,IOException{
		
		final String authHeader=request.getHeader("Authorization");
		
		if(authHeader == null || !authHeader.startsWith("Bearer")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		final String token = authHeader.substring(7);
		final String username;
		
		try {
			username=jwtProvider.extractUsername(token);
		}catch (Exception e) {
			filterChain.doFilter(request, response);
			return;
		}
		
		if(username != null && SecurityContextHolder.getContext().getAuthentication()==null) {
			UserDetails userDetails = myUserDetails.loadUserByUsername(username);
			
			if(jwtProvider.validToken(token, userDetails)) {
				UsernamePasswordAuthenticationToken upaToken= new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(upaToken);
			}
		}
		
		filterChain.doFilter(request, response);
	}
}
