package com.um.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bucket4j.Bucket;

import org.springframework.stereotype.Service;

import com.um.configuration.RateLimitingPlan;

@Service
public class RateLimitingService {
	
	private final Map<String,Bucket> buckets = new ConcurrentHashMap<>();
	
	public Bucket resolveBucket(String key, RateLimitingPlan plan) {
		return buckets.computeIfAbsent(key,k->plan.createBucket());
	}
	
	public RateLimitingPlan determinePlan(String path, String authHeader, String apiKey) {
		if(apiKey != null) return RateLimitingPlan.WEBHOOK;
		if(authHeader != null) return RateLimitingPlan.AUTH;
		if(isSensitive(path)) return RateLimitingPlan.SENSITIVE;
		return RateLimitingPlan.PUBLIC;
	}
	
	private boolean isSensitive(String path) {
		return path.contains("/auth/login") || path.contains("/auth/register");
	}
	
}
