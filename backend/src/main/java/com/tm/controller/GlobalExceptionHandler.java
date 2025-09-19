package com.tm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tm.Exceptions.UserAlreadyExistsException;
import com.tm.Exceptions.UserNotFoundException;

import lombok.Getter;
import lombok.Setter;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException message) {
		ErrorResponse error= new ErrorResponse(HttpStatus.NOT_FOUND.value(),message.getMessage());
		return new ResponseEntity<>(error,HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException message){
		
		ErrorResponse error= new ErrorResponse(HttpStatus.CONFLICT.value(),message.getMessage());
		return new ResponseEntity<>(error,HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception message){
		
		ErrorResponse error= new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Une erreur est survenue");
		return new ResponseEntity<>(error,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@Getter @Setter
	public static class ErrorResponse {

		private int status;
		private String message;
		
		public ErrorResponse(int status, String message) {
			this.status=status;
			this.message=message;
		}
	}
}