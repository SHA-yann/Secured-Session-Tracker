package com.um.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.Disposable;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

	private final ReactiveStringRedisTemplate redisTemplate;
	private static final String ONLINE_USERS_KEY = "presence:online-users";
	private static final String STATUS_CHANNEL ="user-status-changes";
	private final Map<Long, Disposable> pendingRemovals = new ConcurrentHashMap<>();
	
	public Mono<Void> addOnlineUser(Long userId, String username) {
        // CRITIQUE : Si une suppression était prévue, on l'annule de suite
        Disposable pending = pendingRemovals.remove(userId);
        if (pending != null && !pending.isDisposed()) {
            pending.dispose();
            log.info("Session restored for {}.", username);
            // On met quand même à jour le score dans Redis pour le heartbeat
        }

        String idStr = String.valueOf(userId);
        double now = System.currentTimeMillis();
        
        return redisTemplate.opsForZSet().add(ONLINE_USERS_KEY, idStr, now)
                .then(redisTemplate.convertAndSend(STATUS_CHANNEL, username+":"+idStr+":CONNECTED"))
                .then();
    }

    public void scheduleRemoval(Long userId, String username) {
        log.info("Session will be terminated {} (in 30s)", username);
        
        // On crée une tâche différée
        Disposable removalTask = Mono.delay(Duration.ofSeconds(30))
						            .flatMap(d -> {
						                pendingRemovals.remove(userId);
						                log.info("Session ended for {}", username);
						                return removeOnlineUser(userId, username);
						            })
						            .subscribe();

        pendingRemovals.put(userId, removalTask);
    }

    public Mono<Void> removeOnlineUser(Long userId, String username) {
        String idStr = String.valueOf(userId);
        return redisTemplate.opsForZSet().remove(ONLINE_USERS_KEY, idStr)
                .then(redisTemplate.convertAndSend(STATUS_CHANNEL, username+":"+idStr+":DISCONNECTED"))
                .then();
    }
	
	public Mono<Long> purgeExpiredUsers(){
		
		double threshold = System.currentTimeMillis() - (180 * 1000);
		return redisTemplate.opsForZSet().removeRangeByScore(ONLINE_USERS_KEY, Range.closed(0.0,threshold));
	}
	
	public Mono<List<String>> getOnlineUsers() {
		
		return purgeExpiredUsers().then(redisTemplate.opsForZSet()
							.range(ONLINE_USERS_KEY,Range.unbounded()).collectList());
	}
	
	public Flux<String> getStatusDeltas(){
		
		return redisTemplate.listenTo(ChannelTopic.of(STATUS_CHANNEL))
							.map(ReactiveSubscription.Message::getMessage);
	}
}
