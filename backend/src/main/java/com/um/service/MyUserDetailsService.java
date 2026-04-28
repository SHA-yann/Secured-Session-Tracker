package com.um.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.Exceptions.UserNotFoundException;
import com.um.repository.UserRepository;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user information from the database for authentication.
 */
@Service
public class MyUserDetailsService implements ReactiveUserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepo;
    /**
     * Constructor injecting UserService dependency.
     *
     * @param userService service to access user data
     */
    public MyUserDetailsService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Loads a user by username for Spring Security authentication.
     *
     * @param username the username identifying the user
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    @Transactional
    public Mono<UserDetails> findByUsername(String username) {

        return Mono.fromCallable(() -> userRepo.findByUsername(username)
        										.map(user -> User.builder()
			        										.username(user.getUsername())
			        										.password(user.getPassword())
			        										.authorities(user.getAuthorities())
			        										.build())
			        							.orElseThrow(() -> new UserNotFoundException("No such user"))
        										).subscribeOn(Schedulers.boundedElastic());
    }
    
}
