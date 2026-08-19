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

	  @ExceptionHandler(EmailAlreadyRegisteredException.class)
	  @ResponseStatus(HttpStatus.CONFLICT)
	    public <T> ApiResponse<T> handleUserAlreadyExists(
	    		EmailAlreadyRegisteredException ex) {

	        return ApiResponse.failure(ex.getErrorCode().getCode(),ex.getMessage(), null);
	                
	    }
	  @ExceptionHandler(MobileAlreadyRegisteredException.class)
	  @ResponseStatus(HttpStatus.CONFLICT)
	    public <T> ApiResponse<T> handleUserAlreadyExists(
	    		MobileAlreadyRegisteredException ex) {

	        return ApiResponse.failure(ex.getErrorCode().getCode(),ex.getMessage(), null);
	                
	    }
	  
	  @ExceptionHandler(MethodArgumentNotValidException.class)
	  @ResponseStatus(HttpStatus.BAD_REQUEST)
	  public ApiResponse<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
		  Map<String, String> errors = new LinkedHashMap<>();

		    for (FieldError fieldError : ex.getFieldErrors()) {
		        errors.put(
		            fieldError.getField(),
		            fieldError.getDefaultMessage()
		        );
		    }
		  return ApiResponse.failure(ErrorCode.VALIDATION_FAILED.getCode(),"Validation failed", errors);
	  }
}
