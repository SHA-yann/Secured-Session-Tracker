package com.tm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tm.model.User;
import com.tm.repository.UserRepository;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	
	public UserService(UserRepository userRepository) {
		
    this.userRepository=userRepository;
	}

	@Transactional
	public User createUser(User u) {
		
    return userRepository.save(u);
	}
	
	public List<User> getAllUsers(){
		
    return userRepository.findAll();
	}

	public Optional<User> getUserById(long id) {
	
		return userRepository.findById(id);
	}

	public Optional<User> getUserByEmail(String email) {

		return userRepository.findByEmail(email);
	}

	public Optional<User> updateUser(long id, User update) {
		
		return userRepository.findById(id).map(found->{found.setUsername(update.getUsername());
				found.setPassword(update.getPassword());
				found.setEmail(update.getEmail());
				found.setRole(update.getRole());
				return userRepository.save(found);
			
		});
	}
	
	public Boolean deleteUser(Long id) {
		if(userRepository.existsById(id)) {
			userRepository.deleteById(id);
			return true;
		}
		return false;
	}

}
