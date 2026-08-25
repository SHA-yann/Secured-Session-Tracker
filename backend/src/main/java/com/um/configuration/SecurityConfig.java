package com.um.configuration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

import com.um.service.MyUserDetailsService;
import com.um.service.RateLimitingService;

/**
 * Configures Spring Security for the application.
 * Sets up authentication, authorization, JWT filter, and CORS.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final RateLimitingService rateLimitingService;
    private final JwtProvider jwtProvider;
	private final MyUserDetailsService userDetailsService;

    /**
     * Creates a new {@code SecurityConfig}.
     *
     * @param jwtAuthFilter the JWT authentication filter
     * @param userDetailsService the user details service (injected for future use)
     */
    public SecurityConfig( RateLimitingService rateLimitingService,
    						@Lazy MyUserDetailsService userDetailsService,
    						JwtProvider jwtProvider) {
        this.rateLimitingService = rateLimitingService;
		this.jwtProvider = jwtProvider;
		this.userDetailsService = userDetailsService;
    }

    /**
     * Defines the password encoder bean used for hashing user passwords.
     * 
     * Uses {@link BCryptPasswordEncoder} with a strength of 12,
     * which ensures strong one-way encryption for secure storage.
     *
     * @return a configured {@link PasswordEncoder} instance
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    /**
     * Builds the security filter chain.
     * <ul>
     *     <li>Enables CORS with default configuration</li>
     *     <li>Disables CSRF for stateless JWT authentication</li>
     *     <li>Defines public and role-based access rules</li>
     *     <li>Uses stateless session management</li>
     *     <li>Registers the JWT filter before the UsernamePasswordAuthenticationFilter</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} object to configure
     * @return 
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
     SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,CorsConfigurationSource corsSource) {
    	
    	JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtProvider, userDetailsService);
    	RateLimitingFilter rateLimitingFilter = new RateLimitingFilter(rateLimitingService);
    	
        http
            .cors(cors -> cors.configurationSource(corsSource))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(auth -> auth
                .pathMatchers("/notifications/**","/auth/**","/swagger-ui/**","/v3/api-docs/**","/webjars/**","/actuator/prometheus","/actuator/health/**","/actuator/info").permitAll()
                .pathMatchers(HttpMethod.POST, "/users").hasAuthority("ROLE_ADMIN")
                .pathMatchers(HttpMethod.GET, "/users","/users/**").hasAnyAuthority("ROLE_ADMIN","ROLE_USER")
                .pathMatchers(HttpMethod.PUT, "/users/**").hasAnyAuthority("ROLE_ADMIN","ROLE_USER")
                .pathMatchers(HttpMethod.DELETE, "/users/**").hasAuthority("ROLE_ADMIN")
                .anyExchange().authenticated()
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
            		.authenticationEntryPoint((exchange, e) ->{
            			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            			if(!exchange.getResponse().isCommitted()) { // Vérifie si la réponse n'a pas encore été engagée, sinon, erreur non blocante
            				exchange.getResponse().getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);// Supprime l'en-tête WWW-Authenticate pour éviter les prompts de navigateur
            			}
            			return exchange.getResponse().setComplete();
            		})
            )
            .addFilterBefore(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterAfter(rateLimitingFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http.build();
    }
    
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    WebFilter logFilter() {
    	return (exchange, chain) ->{
    		
    		return chain.filter(exchange)
    				.doFirst(() -> {
    					System.out.println(">>>Requete entrante:"+exchange.getRequest().getPath());
    					System.out.println("Headers:"+exchange.getRequest().getHeaders());
    				});
    		
    	};
    }
    
    /* @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // forcer l'écriture d'un Log de niveau ERROR avec la stack trace complète en déclarant un WebFilter de diagnostic dédié, placé tout au début de ta chaîne
    WebFilter debugStackTraceFilter() {
        Logger log = LoggerFactory.getLogger("DEBUG_STACK_TRACE");
        return (exchange, chain) -> chain.filter(exchange)
                .onErrorResume(ex -> {
                    // Force l'impression de la stack trace complète dans la console de l'IDE
                    log.error("=== CRASH CAPTURÉ AU SOMMET DU PIPELINE WEBFLUX ===", ex);
                    return Mono.error(ex); // Relance l'erreur pour ne pas casser le flux nominal
                });
    } */

    /**
     * Configures Cross-Origin Resource Sharing (CORS) to allow frontend applications.
     * <ul>
     *     <li>Permits requests from React and Angular dev servers</li>
     *     <li>Allows standard HTTP methods</li>
     *     <li>Accepts all headers and allows credentials</li>
     * </ul>
     *
     * @return the configured {@link CorsConfigurationSource}
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200","http://localhost"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","Cache-Control"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Exposes the {@link AuthenticationManager} bean for authentication handling.
     *
     * @param config the authentication configuration
     * @return the authentication manager
     * @throws Exception if creation fails
     */
    @Bean
    ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService, PasswordEncoder passWordEncoder) {
        
    	UserDetailsRepositoryReactiveAuthenticationManager authManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
    	authManager.setPasswordEncoder(passWordEncoder);
    	return authManager;
    }
}

