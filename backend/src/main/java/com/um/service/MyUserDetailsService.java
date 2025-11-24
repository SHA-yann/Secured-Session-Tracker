package com.um.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.um.model.User;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user information from the database for authentication.
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserService userService;
    /**
     * Constructor injecting UserService dependency.
     *
     * @param userService service to access user data
     */
    public MyUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Loads a user by username for Spring Security authentication.
     *
     * @param username the username identifying the user
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userService.findByName(username);

        if (!user.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new org.springframework.security.core.userdetails.User(
        		user.get().getUsername(),
                user.get().getPassword(),
                user.get().getAuthorities()
        );
    }
    
}
