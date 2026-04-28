package com.um.controller;

import java.security.Principal;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService presence;
	private String name;

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<String>> streamNotifications() {
														
		Flux<ServerSentEvent<String>> deltas= presence.getStatusDeltas()
													.map(msg -> ServerSentEvent.builder(msg).event("presence-update").build());
		
		Flux<ServerSentEvent<String>> heartbeat = Flux.interval(Duration.ofSeconds(20))
															.map(tick -> ServerSentEvent.<String>builder()
																	.comment("keep-alive")
																	.build());
		
		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.map(Principal::getName)
				.flatMapMany(temp -> {
					this.name=temp;
					return presence.addOnlineUser(name)
							.thenMany(Flux.merge(deltas,heartbeat));
				})
				.doOnCancel(() -> log.info("Cancelation received: client {} closed the connection", name))
					.doFinally(signalType -> {
						log.info("End of SSE flow(Signal:{})",signalType);
						presence.removeOnlineUser(name).subscribe();
								}
					)
					.timeout(Duration.ofHours(2), Flux.empty())
					.onErrorResume(e ->{ log.error("SSE flow Error for {}:Message {}",name,e.getMessage());
										 return Flux.empty();
										}
					);
	}
}
