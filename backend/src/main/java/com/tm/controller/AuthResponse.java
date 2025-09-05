package com.tm.controller;

import com.tm.model.RefreshToken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class AuthResponse {

	private String accessToken;
	private RefreshToken refreshToken;
	
}
