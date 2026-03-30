package com.um.configuration;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.um.service.RateLimitingService;

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
		
		String path = request.getRequestURI();

		if(path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator") || path.startsWith("/favicon.ico")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		RateLimitingPlan plan = rateLimitingService.determinePlan(path, 
																request.getHeader("Authorization"),
																request.getHeader("X-API-Key"));
		String clientIP = request.getRemoteAddr();
		String key = clientIP + ":" + plan.name();
		
		if(rateLimitingService.resolveBucket(key,plan).tryConsume(1)) {
			filterChain.doFilter(request, response);
		}else {
			response.setStatus(429);
			response.setContentType("application/json");
			response.getWriter().write(String.format("{\"error\":\"Trop de requêtes!...Réessayez dans 1 minute.\"}", plan.name()));
		}
	}
}
