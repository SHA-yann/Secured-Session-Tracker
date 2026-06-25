package com.um.configuration;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.service.RateLimitingService;

import reactor.core.publisher.Mono;

import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Component
public class RateLimitingFilter implements WebFilter{

	RateLimitingFilter(RateLimitingService rateLimitingService) {
		this.rateLimitingService = rateLimitingService;
	}

	private RateLimitingService rateLimitingService ;
	private static final String ALREADY_FILTERED_ATTR = "RateLimitingFilter.FILTERED"; // to avoid double consumption of tokens that resulting in earlier exaustion of capacity

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		
		if (exchange.getAttribute(ALREADY_FILTERED_ATTR) != null) {
			return chain.filter(exchange);
		}

		String path = exchange.getRequest().getURI().getPath();
		
		if(path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator") || path.startsWith("/notifications") || path.startsWith("/favicon.ico")) {
			
			return chain.filter(exchange);
		}
		
		exchange.getAttributes().put(ALREADY_FILTERED_ATTR, Boolean.TRUE); // we mark as filtered
		
		RateLimitingPlan plan = rateLimitingService.determinePlan(path, 
																exchange.getRequest().getHeaders().getFirst("Authorization"),
																exchange.getRequest().getHeaders().getFirst("X-API-Key"));
		
		String clientIP=exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
		
		if(clientIP == null && exchange.getRequest().getRemoteAddress() != null)
			clientIP = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
		
		String key = (clientIP != null ? clientIP:"unknown") + ":" + plan.name();
		
		if(rateLimitingService.resolveBucket(key,plan).tryConsume(1)) {
			System.out.println("filtre activé pour : "+path+" | clé : "+key);
			return chain.filter(exchange);
		}else {
			exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
			exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		    Map<String, String> errorBody = Map.of("message", "Too many requests, please wait");
			return Mono.defer(() -> {
		        try {
		            byte[] bytes = new ObjectMapper().writeValueAsBytes(errorBody);
		            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
		            return exchange.getResponse().writeWith(Mono.just(buffer));
		        } catch (Exception e) {
		            return Mono.error(e);
		        }
		    });
		}		
	}
}


