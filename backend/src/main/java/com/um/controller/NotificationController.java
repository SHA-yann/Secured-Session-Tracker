package com.um.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.dto.PresenceDTO;
import com.um.service.NotificationService;
import com.um.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService presence;
	private final ObjectMapper objectMapper;
	
	@Autowired
	private UserService uServ;

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<Flux<ServerSentEvent<String>>> streamNotifications() {
		
		Flux<ServerSentEvent<String>> flux = ReactiveSecurityContextHolder.getContext()
		        .map(SecurityContext::getAuthentication)
		        .flatMapMany(auth -> {
		            // Ici, on extrait le nom IMMÉDIATEMENT pour ne plus dépendre du contexte
		            String username = auth.getName();
		            
		            return uServ.getUserByUsername(username)
		                .flatMapMany(user -> {
		                    log.info("presence starting for : {}", username);
		                    
		                    Flux<ServerSentEvent<String>> deltas = presence.getStatusDeltas()
	                                .map(event -> {
	                                	try {
	                                		return ServerSentEvent.builder(objectMapper.writeValueAsString(event))
	                                				.event("presence-update")
	                                				.build();
	                                	}catch(Exception e) {
	                                		return ServerSentEvent.<String>builder().comment("error")
	                                				.build();
	                                	}
	                                });
		                    
		                    Flux<ServerSentEvent<String>> list = presence.getOnlineUsers()
		                    								.flatMapMany(Flux::fromIterable)
		                    								.map(u -> { 
		                    									try {
		                    										String[] parts = u.split(":");
		                    										PresenceDTO initialEvent = new PresenceDTO(Long.parseLong(parts[0]),parts[1],"CONNECTED");
		                    										return ServerSentEvent.builder(objectMapper.writeValueAsString(initialEvent))
		                    												.event("presence-update")
		                    												.build();
		                    									}catch(Exception e) {
		                    										return ServerSentEvent.<String>builder().comment("error")
		                    												.build();
		                    									}
		                    								});
		                    
		                    // ON FORCE LA SÉQUENCE :
		                    // 1. ADD REDIS -> 2. GET LIST -> 3. MERGE DELTAS
		                    return presence.addOnlineUser(user.getId(), user.getUsername())
		                        .thenMany(list.mergeWith(deltas))
		                        .mergeWith(Flux.interval(Duration.ofSeconds(15))
		                                       .map(i -> ServerSentEvent.<String>builder().comment("keep-alive")
		                                    	.build()))
		                        .doFinally(signal -> {
		                        	presence.scheduleRemoval(user.getId(), user.getUsername());
		                        	log.info("Session Ended for {}", username);
		                        });
		                });
		        })
		        // TRÈS IMPORTANT : Si l'auth est vide, on log l'erreur
		        .switchIfEmpty(Flux.defer(() -> {
		            log.error("SSE connection attempts without valid authentication !");
		            return Flux.empty();
		        }));
		return ResponseEntity.ok()
				.header("X-Accel-Buffering","no")
				.header("Cache-Control","no-cache")
				.body(flux);
		}
	
	@GetMapping("/onlineList")
	public Mono<List<String>> getOnlineUsers(){
		
		return presence.getOnlineUsers();
	}
}
