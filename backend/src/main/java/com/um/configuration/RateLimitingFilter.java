package com.um.configuration;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.um.service.RateLimitingService;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter{

	RateLimitingFilter(RateLimitingService rateLimitingService) {
		this.rateLimitingService = rateLimitingService;
	}

	private RateLimitingService rateLimitingService ;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException,IOException{
		
		String key;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if(auth!=null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
			key=auth.getName();
		}else {
			key=request.getRemoteAddr();
		}
		
		
		Bucket bucket= rateLimitingService.resolveBucket(key);
		
				if(bucket.tryConsume(1)) {
					filterChain.doFilter(request, response);
				}else {
					response.setStatus(429);
					response.setContentType("application/json");
					response.getWriter().write("{\"error\":\"Vous avez effectué un trop grand nombre de requêtes!...Réessayez plus tard.\"}");
				}
	}
}
