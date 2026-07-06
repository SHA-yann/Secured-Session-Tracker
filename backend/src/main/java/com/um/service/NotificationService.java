package com.um.service;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.dto.PresenceDTO;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.core.Disposable;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

	private final ReactiveStringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private static final String ONLINE_USERS_KEY = "presence:online-users";
	private static final String STATUS_CHANNEL ="user-status-changes";
	private Disposable redisSubscription;
	
	private final Map<Long, Disposable> pendingRemovals = new ConcurrentHashMap<>();
	private final Sinks.Many<PresenceDTO> bus = Sinks.many().multicast().onBackpressureBuffer(256, false);
	
	@PostConstruct
	public void init() {
		this.redisSubscription = this.redisTemplate.listenTo(ChannelTopic.of(STATUS_CHANNEL))
						.publishOn(Schedulers.boundedElastic())
						.map(msg -> {
							try {
								return objectMapper.readValue(msg.getMessage(), PresenceDTO.class);
							}catch (Exception e) {
								log.error("Erreur reading redis json",e);
								return null;
							}
						})
						.filter(Objects::nonNull)
						.doOnNext(event -> {
							bus.emitNext(event,(signalType, emitResult) -> {
								if(emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED)
									return true;
								log.warn("Failed to emit presence event to sink bus: {}", emitResult);
								return false;
							});
						})
						.onErrorResume(e ->{
							log.error("Redis unavailable! degrade mode activated");
							return Mono.empty();
						})
						.subscribe();
	}
	

	public Mono<Void> addOnlineUser(Long userId, String username, String connectionId) {
        // CRITIQUE : Si une suppression était prévue, on l'annule de suite
        Disposable pending = pendingRemovals.remove(userId);
        if (pending != null && !pending.isDisposed()) {
            pending.dispose();
            log.info("Removal canceled for session {} of user {}.",connectionId, username);
            // On met quand même à jour le score dans Redis pour le heartbeat
        }

        String redisValue = userId + ":" + username;
        String userSessionKey = "presence:session-Set-for-user-"+userId;
        
        return redisTemplate.opsForSet().add(userSessionKey, connectionId)
        		
    		.flatMap(isNewAdded -> {
    			
        		return redisTemplate.opsForSet().size(userSessionKey)
    				.flatMap(currentCount -> {
	        			double now = (double)System.currentTimeMillis();
	        			
	        			return redisTemplate.opsForZSet().add(ONLINE_USERS_KEY, redisValue, now)
        	                .then(Mono.defer(() -> {
        	                	if(currentCount == 1 && isNewAdded > 0) {
        	                		log.info("{} session(s) for {}",currentCount,username);
        	                		return publishStatus(userId, username, "CONNECTED");
        	                	}
        	                	log.info("{} session(s) for {}",currentCount,username);
        	                	return Mono.empty();
        	                }));
    				});
        		
        	})
    		.onErrorResume(e ->{
				log.error("Redis unavailable! degrade mode activated");
				return Mono.empty();
			})
    	    .then();

    }

    public void scheduleRemoval(Long userId, String username, String connectionId) {
        log.info("Session will be terminated for {} (in 15s)",username);
        
        Disposable previous = pendingRemovals.get(userId);
        if (previous != null && !previous.isDisposed()) {
            previous.dispose();
        }
        
        // On crée une tâche différée
        Disposable removalTask = Mono.delay(Duration.ofSeconds(15))
						            .flatMap(d -> {
						            	
						                pendingRemovals.remove(userId);
						                return removeOnlineUser(userId, username,connectionId);
						            })
						            .subscribe();

        pendingRemovals.put(userId, removalTask);
    }

    public Mono<Void> removeOnlineUser(Long userId, String username, String connectionId) {
    	String userSessionKey = "presence:session-Set-for-user-"+userId;
    	String redisValue = userId + ":" + username;
    	
    	Disposable pending = pendingRemovals.remove(userId);
        if (pending != null && !pending.isDisposed()) {
            pending.dispose();
            log.info("Logout instead of removal");
        }
    	
		return redisTemplate.opsForSet().remove(userSessionKey,connectionId)
			//.log("DEBUG_REMOV")
			.then(redisTemplate.opsForSet().size(userSessionKey))
    		.flatMap(currentCount ->{
    			if(currentCount <= 0) {
    				log.info("No active session remaining for {}", username);
    				return redisTemplate.opsForSet().delete(userSessionKey)
    						.then(redisTemplate.opsForZSet().remove(ONLINE_USERS_KEY, redisValue))
    						.then(publishStatus(userId, username,"DISCONNECTED"));
    			}
    			log.info("{} terminated a session but still gets {} active(s)",username,currentCount);
    			return Mono.empty();
    		})
    		.onErrorResume(e ->{
				log.error("Redis unavailable! degrade mode activated");
				return Mono.empty();
			})
    		.then();
	        	
    }
    
    public Mono<Void> instantRemove(Long userId, String username){
    	String redisValue = userId + ":" + username;
    	String userSessionKey = "presence:session-Set-for-user-"+userId;
    	return redisTemplate.delete(userSessionKey)
    			.then(redisTemplate.opsForZSet().remove(ONLINE_USERS_KEY, redisValue))
		        .then(publishStatus(userId, username,"DISCONNECTED"))
		        .onErrorResume(e ->{
					log.error("Redis unavailable! downgrade mode activated");
					return Mono.empty();
				});
    }
    
	private Mono<Void> publishStatus(Long userId, String username, String status){
		PresenceDTO event = new PresenceDTO(userId, username,status);
		String jsonPayload;
		try {
			jsonPayload = objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			return Mono.error(e);
		}
		return redisTemplate.convertAndSend(STATUS_CHANNEL, jsonPayload)
				.onErrorResume(e ->{
					log.error("Redis unavailable! degrade mode activated");
					return Mono.empty();
				})
				.then();
		
	}
    
    @Scheduled(fixedRate = 15000)
	public void autoPurge(){
		
		double threshold = (double) System.currentTimeMillis() - (60 * 1000);
		
		 redisTemplate.opsForZSet()
				.rangeByScore(ONLINE_USERS_KEY, Range.of(Range.Bound.inclusive(0.0), Range.Bound.inclusive(threshold))
				)
				.flatMap(expired -> {
					String[] parts = expired.split(":");
					Long id = Long.parseLong(parts[0]) ;
			    	String name = parts[1];
					log.info("{} is no longer active, all session(s) closed", name);
					return instantRemove(id,name);
				})
				.subscribeOn(Schedulers.boundedElastic())
				.onErrorResume(e ->{
					log.error("Redis unavailable! degrade mode activated");
					return Mono.empty();
				})
				.subscribe();
	}
	
	public Flux<String> getOnlineUsers() {
		
		return redisTemplate.opsForZSet()
							.range(ONLINE_USERS_KEY,Range.unbounded())
							.onErrorResume(e ->{
								log.error("Redis unavailable! degrade mode activated");
								return Mono.empty();
							});
	}
	
	public Mono<Boolean> updateUserRedisScore(String member, double score){
		
		return redisTemplate.opsForZSet().add(ONLINE_USERS_KEY, member, score)
							.onErrorResume(e ->{
								log.error("Redis unavailable! degrade mode activated");
								return Mono.empty();
							});
	}
	
	
	public Flux<PresenceDTO> getStatusDeltas(){
		
		return bus.asFlux();
	}
	
	@PreDestroy
	public void destroy() {
		if(this.redisSubscription != null && !this.redisSubscription.isDisposed())
			this.redisSubscription.dispose();
		
		pendingRemovals.values().forEach((Disposable::dispose));
		pendingRemovals.clear();
	}
}
