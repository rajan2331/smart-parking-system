package com.parking.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.parking.common.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler  {

	  @ExceptionHandler(UserAlreadyExistsException.class)
	  @ResponseStatus(HttpStatus.CONFLICT)
	    public <T> ApiResponse<T> handleUserAlreadyExists(
	            UserAlreadyExistsException ex) {

	        return ApiResponse.failure(ex.getMessage(), null);
	                
	    }
	  
	  @ExceptionHandler(MethodArgumentNotValidException.class)
	  public ApiResponse<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
		  Map<String, String> errors = new LinkedHashMap<>();

		    for (FieldError fieldError : ex.getFieldErrors()) {
		        errors.put(
		            fieldError.getField(),
		            fieldError.getDefaultMessage()
		        );
		    }
		  return ApiResponse.failure("Validation failed", errors);
	  }
}
