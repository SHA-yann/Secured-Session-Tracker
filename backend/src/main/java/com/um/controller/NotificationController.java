package com.um.controller;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
	
	@Autowired
	private UserService uServ;

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<Flux<ServerSentEvent<PresenceDTO>>> streamNotifications() {
		
		Flux<ServerSentEvent<PresenceDTO>> flux = ReactiveSecurityContextHolder.getContext()
		        .map(SecurityContext::getAuthentication)
		        // TRÈS IMPORTANT : Si l'auth est vide, on renvoie une erreur 401 pour éviter que le flux ne reste ouvert sans authentification
		        .switchIfEmpty(Mono.error( new ResponseStatusException(HttpStatus.UNAUTHORIZED,"SSE connection attempts without valid authentication !")))
		        .flatMapMany(auth -> {
		            // Ici, on extrait le nom IMMÉDIATEMENT pour ne plus dépendre du contexte
		            String username = auth.getName();
		            
		            return uServ.getUserByUsername(username)
		            		.flatMapMany(user -> {
			                    log.info("presence starting for : {}", username);
			                    
			                    Flux<ServerSentEvent<PresenceDTO>> deltas = presence.getStatusDeltas()
		                                .map(event -> ServerSentEvent.builder(event).event("presence-update")
		                                		.build());
			                
			                    
			                    Flux<ServerSentEvent<PresenceDTO>> heartbeat = Flux.interval(Duration.ofSeconds(20))
	                                    .flatMap(i -> {
		                                	double now = (double)System.currentTimeMillis();
		                                	String redisValue = user.getId()+":"+user.getUsername();
	                                    	
	                                    	return presence.updateUserRedisScore(redisValue, now)
	                                    					.then(Mono.just(ServerSentEvent.<PresenceDTO>builder().comment("keep-alive").build()));
		            					});
			                    
			                    // ON FORCE LA SÉQUENCE :
			                    // 1. ADD REDIS -> 2. GET LIST -> 3. MERGE DELTAS
			                    return presence.addOnlineUser(user.getId(), user.getUsername())
			                        .thenMany(Flux.defer(() -> {
			                        	Flux<ServerSentEvent<PresenceDTO>> list = getOnlineList()
                								.map(u -> {
                									return ServerSentEvent.builder(u).event("online-users").build();
                								});
			                        	
			                        	return Flux.concat(list,Flux.merge(deltas,heartbeat));
			                        }))
			                        .doFinally(signal -> {
			                        	presence.scheduleRemoval(user.getId(), user.getUsername());
			                        	log.info("Session Ended for {}", username);
			                        });
		            		});
		        });
		        
		        
		return ResponseEntity.ok()
				.header("X-Accel-Buffering","no")
				.header("Cache-Control","no-cache")
				.body(flux);
		}
	
	@GetMapping("/onlineList")
	public Flux<PresenceDTO> getOnlineList(){
		
		return presence.getOnlineUsers()
				.map(u -> {
			       String[] parts = u.split(":");
			       return new PresenceDTO(Long.parseLong(parts[0]),parts[1],"CONNECTED");
				});
				
	}
}
