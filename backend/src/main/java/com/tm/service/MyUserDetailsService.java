package com.tm.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tm.model.User;
import com.tm.repository.UserRepository;

import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class MyUserDetailsService implements UserDetailsService{

	private UserRepository userRepository;
	
	public MyUserDetailsService(UserRepository userRepository) {
		this.userRepository=userRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		
	Optional<User> user= userRepository.findByUsername(username);
		
		if(!user.isPresent())
			throw new UsernameNotFoundException("User not found");
		
		Collection<? extends GrantedAuthority> authorities = List.of( new SimpleGrantedAuthority("ROLE_"+user.get().getRole().name()));
		return new org.springframework.security.core.userdetails.User(
									user.get().getUsername(),
									user.get().getPassword(),
									authorities);
									
	}
}
