package com.um.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.Exceptions.*;
import com.um.dto.UpdateRequest;
import com.um.dto.UserRequest;
import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;
import com.um.repository.UserRepository;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service for user management.
 * Handles CRUD operations, search, and security access control.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    /**
     * Constructs a UserService with required dependencies.
     *
     * @param userRepository repository for accessing user entities
     * @param passwordEncoder encoder for hashing user passwords
     */
    public UserService(UserRepository userRepository,@Lazy PasswordEncoder passwordEncoder, 
    					RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.refreshTokenService = refreshTokenService;
    }

    /**
     * Creates a new user with hashed password and default USER role.
     *
     * @param user user to create
     * @return created user
     * @throws UserAlreadyExistsException if username is already taken
     */
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<User> createUser(UserRequest dto, String author) {
        
    	if (userRepository.existsByUsername(dto.username())) {
            return Mono.error( new UserAlreadyExistsException("Username " + dto.username() + " already taken"));
        }
    	
    	User user= new User(
    	dto.username(),
    	passwordEncoder.encode(dto.password()),
    	dto.email(),
    	dto.role(),
        dto.status());
        user.setCreatedBy(author);
        user.setUpdatedBy(author);

        return Mono.fromCallable(() -> userRepository.save(user))
        			.subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable pagination settings
     * @return page of users
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public Mono<Page<User>> getAllUsers(Pageable pageable) {
        
    	return  Mono.fromCallable(() -> userRepository.findAll(pageable))
    				.subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id user ID
     * @return Optional containing the user if found
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<User> getUserById(long id) {
    	
			return Mono.fromCallable(() -> {
						return userRepository.findById(id)
								.orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
				}).subscribeOn(Schedulers.boundedElastic());
	
    }
    
    public Mono<User> getUserByUsername(String name){
    	
    	return Mono.fromCallable(() -> {
			
    			return userRepository.findByUsername(name)
    						.orElseThrow(() -> new UserNotFoundException("User with name " +name+ " not found"));
    			}).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves a user by email.
     *
     * @param email user email
     * @return Optional containing the user if found
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<User> getUserByEmail(String email) {
    	
    	return Mono.fromCallable(() -> userRepository.findByEmail(email)
    				.orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"))
    				).subscribeOn(Schedulers.boundedElastic());
    	
    }

    /**
     * Updates a user’s information.
     * Admins can also update roles.
     *
     * @param id     user ID
     * @param update updated user data
     * @return Optional containing updated user
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<User> updateUser(Long id, UpdateRequest update, String author) {
        
    	return ReactiveSecurityContextHolder.getContext()
    			.map(SecurityContext::getAuthentication)
    			.flatMap(auth -> {
    				boolean isAdmin = auth.getAuthorities()
                            .stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    				
    				String currentUsername = auth.getName();

    				return Mono.fromCallable(() -> {
    					return userRepository.findById(id)
    			                .map(found -> {
    			                	if(!isAdmin && !found.getUsername().equals(currentUsername))
    			                		throw new AccessDeniedException("You are not Owner or Admin to modify these fields");
    			                	
    			                    found.setEmail(update.email());
    			                    found.setUpdatedBy(author);    			                            

    			                    if (!isAdmin)
    			                    	throw new AccessDeniedException("Only Admin can modify a role or a status");
    			                    else{
    			                        found.setRole(update.role());
    			                        found.setStatus(update.status());
    			                    }

    			                    return userRepository.save(found);
    			                }).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    				}).subscribeOn(Schedulers.boundedElastic());
    			                
    			});
   }

    /**
     * Deletes a user by ID.
     *
     * @param id user ID
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Mono<Void> disableUser(Long id) {
        
        return Mono.fromCallable(() -> {
					return userRepository.findById(id)
					.orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
					}).subscribeOn(Schedulers.boundedElastic())
					.flatMap(u -> {
						u.setStatus(Status.INACTIVE);
						refreshTokenService.revokeUserTokens(u.getId());
						return Mono.fromCallable(() -> {									
									userRepository.save(u);
									return u;
									})
									.subscribeOn(Schedulers.boundedElastic());
				    }).then();
        
    }

    /**
     * Searches users by username or role with pagination.
     *
     * @param username optional username filter
     * @param role     optional role filter
     * @param pageable pagination settings
     * @return page of users matching filters
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public Mono<Page<User>> searchUsers(String username, Role role, Pageable pageable) {
    	
    	return Mono.fromCallable(() -> {
    		if (username != null && !username.isBlank()) {
            return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        }
        else if (role != null) {
            return userRepository.findByRole(role, pageable);
        }
        else
        	return userRepository.findAll(pageable);
    	}).subscribeOn(Schedulers.boundedElastic());
        
    }

    /**
     * Deletes all users and flushes the repository.
     */
    @Transactional(readOnly = true)
    public void wipeAll() {
        userRepository.deleteAll();
        userRepository.flush();
    }
}
