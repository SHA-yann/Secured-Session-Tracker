package com.um.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
public class RedisConfig {

	@Bean
	@Primary
	ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
		
		return new LettuceConnectionFactory("localhost",6379);
	}
	
	@Bean
	 ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
		
		return new ReactiveStringRedisTemplate(connectionFactory);
	}
}
