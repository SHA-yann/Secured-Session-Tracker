package com.tm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tm.model.Role;
import com.tm.service.MyUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled=true)
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final MyUserDetailsService userDetailsService;
	
	public SecurityConfig(JwtAuthFilter jwtAuthFilter, MyUserDetailsService userDetailsService) {
		this.jwtAuthFilter=jwtAuthFilter;
		this.userDetailsService = userDetailsService;
	}
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/users/**").hasAnyRole(Role.ADMIN.name(), Role.USER.name())
                        .requestMatchers(HttpMethod.POST,"/users/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT,"/users/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE,"/users/**").hasRole(Role.ADMIN.name())
                		.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
	
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
		
		return config.getAuthenticationManager();
	}
	
	@Bean
	AuthenticationProvider authenticationProvider() {
		
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
		
		return provider;
	}
	
}
