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

import lombok.AllArgsConstructor;

@Service
//@AllArgsConstructor
public class MyUserDetailsService implements UserDetailsService{

	private UserService userService;
	
	public MyUserDetailsService(UserService userService) {
		this.userService=userService;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		
	Optional<User> user= userService.findByName(username);
		
		if(!user.isPresent())
			throw new UsernameNotFoundException("User not found");
		
		Collection<? extends GrantedAuthority> authorities = List.of( new SimpleGrantedAuthority("ROLE_"+user.get().getRole().name()));
		return new org.springframework.security.core.userdetails.User(
									user.get().getUsername(),
									user.get().getPassword(),
									authorities);
									
	}
}
