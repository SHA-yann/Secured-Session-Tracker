package com.um.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RateLimitingService {

	private final Map<String,Bucket> Buckets = new ConcurrentHashMap<>();
	
	public Bucket resolveBucket(String key) {
		return buckets.computeIfAbsent(key,this::newBucket);
	}
	
	private Bucket newBucket(String key) {
		return Bucket.builder().addLimit(limit->limit.capacity(5)
													.refillGreedy(5, Duration.ofMinutes(1)))
								.build();
	}
}
