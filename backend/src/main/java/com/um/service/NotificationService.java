package com.um.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

	private final ReactiveStringRedisTemplate redisTemplate;
	private static final String ONLINE_USERS_KEY = "presence:online-users";
	private static final String STATUS_CHANNEL ="user-status-changes";
	
	public Mono<Void> addOnlineUser(String userId) {
		log.debug("Adding user {} to Redis", userId);
		return redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId)
				.then(redisTemplate.convertAndSend(STATUS_CHANNEL, userId+" Connected")).then();
	}
	
	public Mono<Void> removeOnlineUser(String userId) {
		log.debug("Removing user {} from Redis", userId);
		return redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId)
				.then(redisTemplate.convertAndSend(STATUS_CHANNEL, userId+" Disconnected")).then();
	}
	
	public Flux<List<String>> getOnlineUsers() {
		
		return Flux.interval(Duration.ZERO, Duration.ofSeconds(10))
					.flatMap(tick -> redisTemplate.opsForSet().members(ONLINE_USERS_KEY).collectList())
					.distinctUntilChanged();
	}
	
	public Flux<String> getStatusDeltas(){
		
		return redisTemplate.listenTo(ChannelTopic.of(STATUS_CHANNEL))
							.map(ReactiveSubscription.Message::getMessage);
	}
}
