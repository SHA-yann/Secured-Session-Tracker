package com.tm.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tm.model.Role;
import com.tm.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);
	
	//Search by username()case-insensitive, partial match)
	Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
	
	//Filter by role
	Page<User> findByRole(Role role, Pageable pageable);

	boolean existsByUsername(String username);	

	
}
