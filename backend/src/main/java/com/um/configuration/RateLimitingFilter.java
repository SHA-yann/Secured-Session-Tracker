package com.um.configuration;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.um.service.RateLimitingService;

import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@Component
public class RateLimitingFilter implements WebFilter{

	RateLimitingFilter(RateLimitingService rateLimitingService) {
		this.rateLimitingService = rateLimitingService;
	}

	private RateLimitingService rateLimitingService ;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();

		if(path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator") || path.startsWith("/favicon.ico")) {
			
			return chain.filter(exchange);
		}
		
		RateLimitingPlan plan = rateLimitingService.determinePlan(path, 
																exchange.getRequest().getHeaders().getFirst("Authorization"),
																exchange.getRequest().getHeaders().getFirst("X-API-Key"));
		
		String clientIP=exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
		if(clientIP == null)
			clientIP = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
		
		String key = clientIP + ":" + plan.name();
		
		if(rateLimitingService.resolveBucket(key,plan).tryConsume(1)) {
			return chain.filter(exchange);
		}else {
			exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
			exchange.getResponse().setComplete();
		}
		
		return chain.filter(exchange);
	}
}
