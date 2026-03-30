package com.um.configuration;

import java.time.Duration;

import io.github.bucket4j.Bucket;

public enum RateLimitingPlan {

	PUBLIC(20,10),
	AUTH(200,100),
	WEBHOOK(1000,500),
	SENSITIVE(10,5);
	
	private final int capacity;
	
	private final int tokens;
	
	RateLimitingPlan(int capacity, int tokens) {
		this.capacity = capacity;
		this.tokens = tokens;
	}
	
	public Bucket createBucket() {
		return Bucket.builder().addLimit(limit->limit.capacity(capacity)
													.refillIntervally(tokens, Duration.ofMinutes(1))
										).build();
	}
}
