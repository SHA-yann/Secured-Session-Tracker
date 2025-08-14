package com.tm.model;

import jakarta.persistence.Entity;

@Entity
public class User {

	private String name;
	private String email;
	
	public User(String name, String email) {
		this.name= name;
		this.email= email;
	}
	
	public String getName() {
		
		return name;
	}

	public String getEmail() {
		
		return email;
	}

	public Object getId() {
		// TODO Auto-generated method stub
		return null;
	}

}
