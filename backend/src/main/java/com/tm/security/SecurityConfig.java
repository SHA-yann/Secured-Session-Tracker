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

/**
 * Spring Security configuration class.
 * Configures authentication, authorization, JWT filter, and password encoding.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final MyUserDetailsService userDetailsService;

    /**
     * Constructor injecting JWT filter and user details service.
     *
     * @param jwtAuthFilter filter to authenticate JWT tokens
     * @param userDetailsService service to load user details
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, MyUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures the security filter chain.
     * - Disables CSRF (for stateless JWT authentication)
     * - Defines endpoint access rules
     * - Configures stateless session management
     * - Adds JWT authentication filter
     *
     * @param http HttpSecurity configuration object
     * @return configured SecurityFilterChain
     * @throws Exception in case of configuration errors
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/users").permitAll()
            .requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole(Role.ADMIN.name(), Role.USER.name())
            .requestMatchers(HttpMethod.PUT, "/users/**").hasRole(Role.ADMIN.name())
            .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole(Role.ADMIN.name())
            .anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the AuthenticationManager bean.
     *
     * @param config AuthenticationConfiguration injected by Spring
     * @return AuthenticationManager
     * @throws Exception if manager cannot be built
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the AuthenticationProvider using DaoAuthenticationProvider.
     * - Sets the custom UserDetailsService
     * - Uses BCryptPasswordEncoder for password hashing
     *
     * @return configured AuthenticationProvider
     */
    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return provider;
    }
}
