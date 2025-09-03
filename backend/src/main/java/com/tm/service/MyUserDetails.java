package com.tm.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tm.model.User;
import com.tm.repository.UserRepository;

@Service
public class MyUserDetails implements UserDetailsService{

private final UserRepository userRepository;

	public MyUserDetails(UserRepository userRepository) {
		this.userRepository=userRepository;
	}

@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		
		Optional<User> user= userRepository.findByUsername(username);
		
		if(!user.isPresent())
			throw new UsernameNotFoundException("User not found");
		
		return org.springframework.security.core.userdetails.User.withUsername(user.get().getUsername())
									.password(user.get().getPassword())
									.roles(user.get().getRole().name())
									.build();
	}
}
