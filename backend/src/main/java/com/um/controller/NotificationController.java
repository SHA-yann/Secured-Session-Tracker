package com.um.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public Flux<ServerSentEvent<String>> streamNotifications() {
														
		Flux<ServerSentEvent<String>> deltas= presence.getStatusDeltas()
													.map(msg -> ServerSentEvent.builder(msg).event("presence-update").build());
		
		Flux<ServerSentEvent<String>> heartbeat = Flux.interval(Duration.ofSeconds(15))
															.map(tick -> ServerSentEvent.<String>builder()
																	.comment("keep-alive")
																	.build());
		
		return ReactiveSecurityContextHolder.getContext()
			    .map(SecurityContext::getAuthentication)
			    .flatMap(auth -> uServ.getUserByUsername(auth.getName()))
			    .flatMapMany(user -> {
			        final String name = user.getUsername();
			        final Long id = user.getId();

			        ServerSentEvent<String> selfConnect = ServerSentEvent.<String>builder(name+":"+id+":CONNECTED")
			        													.event("presence-update")
			        													.build();
			        // 1. On prépare la liste initiale
			        Flux<ServerSentEvent<String>> initialList = presence.getOnlineUsers()
			            .flatMapMany(Flux::fromIterable)
			            .filter(idStr -> !idStr.equals(String.valueOf(id)))
			            .map(idStr -> ServerSentEvent.<String>builder(idStr+":CONNECTED")
			        													.event("presence-update")
			        													.build());
			        	
			        // 2. On lance l'ajout Redis ET on fusionne TOUT
			        return presence.addOnlineUser(id, name)
			            .thenMany(Flux.concat(
			            		Flux.just(selfConnect),
			            		initialList,
			            		Flux.merge(deltas, heartbeat)
			            		)
			            	)// On utilise MERGE ici
			            .doOnCancel(() -> log.info("Cancelation: {} disconnected", name))
			            .doFinally(signalType -> {
			                log.info("{} terminated the session,(Signal: {})", name, signalType);
			                presence.scheduleRemoval(id, name);
			            })
			            .timeout(Duration.ofHours(1), Flux.empty())
			            .onErrorResume(e -> {
			                log.error("SSE flow error for {}: {}", name, e.getMessage());
			                return Flux.empty();
			            });
			    });

	}
	
	@GetMapping("/onlineList")
	public Mono<List<String>> getOnlineUsers(){
		
		return presence.getOnlineUsers();
	}
}
