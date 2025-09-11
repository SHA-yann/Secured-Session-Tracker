package com.tm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tm.Exceptions.*;
import com.tm.model.Role;
import com.tm.model.User;
import com.tm.repository.UserRepository;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	public UserService(UserRepository userRepository) {
		this.userRepository=userRepository;
	}

	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public User createUser(User u) {
		u.setPassword(new BCryptPasswordEncoder(12).encode(u.getPassword()));
		u.setRole(Role.USER);
		
		if(userRepository.existsByUsername(u.getUsername()))
		throw new UserAlreadyExistsException("Username already taken"+u.getUsername());
		
		return userRepository.save(u);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	public List<User> getAllUsers(){
		return userRepository.findAll();
	}

	@PreAuthorize("hasRole('ADMIN') or #id==principal.id")
	public Optional<User> getUserById(long id) {
	
		return Optional.of(userRepository.findById(id)
				.orElseThrow(()-> new UserNotFoundException("User with id "+id+" not found")));
	}

	@PreAuthorize("hasRole('ADMIN') or #id==principal.id")
	public Optional<User> getUserByEmail(String email) {

		return Optional.of(userRepository.findByEmail(email)
				.orElseThrow(()-> new UserNotFoundException("User with email "+email+" not found")));
	}

	@Transactional
	@PreAuthorize("hasRole('ADMIN')or #id==principal.id")
	public Optional<User> updateUser(long id, User update) {
		
		return Optional.of(userRepository.findById(id).map(found->{found.setUsername(update.getUsername());
				found.setPassword(update.getPassword());
				found.setEmail(update.getEmail());
				
				boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
																	.getAuthorities()
																	.stream()
																	.anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN"));
				if(isAdmin && update.getRole() != null && update.getRole().name()!="")
				found.setRole(update.getRole());
				
				return userRepository.save(found);
		
		}).orElseThrow(()-> new UserNotFoundException("User with id "+id+" not found")));
	}
	
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public boolean deleteUser(Long id) {
		if(!userRepository.existsById(id)) {
			throw new UserNotFoundException("User with id "+id+" not found");
			
		}
		userRepository.deleteById(id);
		return true;
	}

	@PreAuthorize("hasRole('ADMIN')")
	public Optional<User> findByName(String username) {
		
		return userRepository.findByUsername(username);
	}

	public void wipeAll() {

		userRepository.deleteAll();;
	}	

}
