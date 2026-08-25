package com.um.service;

import org.springframework.http.ResponseCookie;

public record AuthResult(String token, ResponseCookie cookie) {
	
}
