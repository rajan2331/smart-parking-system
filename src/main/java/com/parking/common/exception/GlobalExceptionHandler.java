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
	    public <T> ApiResponse<T> handleEmailAlreadyExists(
	    		EmailAlreadyRegisteredException ex) {

	        return ApiResponse.failure(ex.getErrorCode().getCode(),ex.getMessage(), null);
	                
	    }
	  @ExceptionHandler(MobileAlreadyRegisteredException.class)
	  @ResponseStatus(HttpStatus.CONFLICT)
	    public <T> ApiResponse<T> handleMobileAlreadyExists(
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
	  
	  @ExceptionHandler(UserNotFoundException.class)
	  @ResponseStatus(HttpStatus.NOT_FOUND)
	    public <T> ApiResponse<T> handleUserNotFound(
	    		UserNotFoundException ex) {

	        return ApiResponse.failure(ex.getErrorCode().getCode(),ex.getMessage(), null);
	                
	    }
	  
	  @ExceptionHandler(VehicleAlreadyRegisteredException.class)
	  @ResponseStatus(HttpStatus.NOT_FOUND)
	    public <T> ApiResponse<T> handleVehicleALreadyExists(
	    		VehicleAlreadyRegisteredException ex) {

	        return ApiResponse.failure(ex.getErrorCode().getCode(),ex.getMessage(), null);
	                
	    }
	  @ExceptionHandler(ParkingSpotAlreadyRegisteredException.class)
	  @ResponseStatus(HttpStatus.CONFLICT)
	  public ApiResponse<Void> handleParkingSpotAlreadyRegistered(
	          ParkingSpotAlreadyRegisteredException ex) {

	      return ApiResponse.failure(
	              ErrorCode.PARKING_SPOT_ALREADY_REGISTERED.getCode(),
	              ex.getMessage(),null
	      );
	  }
	  
	  @ExceptionHandler(VehicleNotFoundException.class)
	  @ResponseStatus(HttpStatus.NOT_FOUND)
	  public ApiResponse<Void> handleVehicleNotFound(
	          VehicleNotFoundException ex) {

	      return ApiResponse.failure(
	              ErrorCode.VEHICLE_NOT_FOUND.getCode(),
	              ex.getMessage(),null
	      );
	  }
	  
	  @ExceptionHandler(ParkingSpotNotAvailableException.class)
	  @ResponseStatus(HttpStatus.NOT_FOUND)
	  public ApiResponse<Void> handleParkingSpotNotFound(
			  ParkingSpotNotAvailableException ex) {

	      return ApiResponse.failure(
	              ErrorCode.PARKING_SPOT_NOT_AVAILABLE.getCode(),
	              ex.getMessage()
	   ,null   );
	  }
	  @ExceptionHandler(VehicleAlreadyParkedException.class)
	  @ResponseStatus(HttpStatus.CONFLICT)
	  public ApiResponse<Void> handleVehicleAlreadyParked(
	          VehicleAlreadyParkedException ex) {

	      return ApiResponse.failure(
	              ErrorCode.VEHICLE_ALREADY_PARKED.getCode(),
	              ex.getMessage(),null
	      );
	  }
	  
	  @ExceptionHandler(ParkingSessionAlreadyCompletedException.class)
	  @ResponseStatus(HttpStatus.CONFLICT)
	  public ApiResponse<Void> handleParkingSessionAlreadyCompleted(
	          ParkingSessionAlreadyCompletedException ex) {

	      return ApiResponse.failure(
	              ErrorCode.PARKING_SESSION_ALREADY_COMPLETED.getCode(),
	              ex.getMessage(),null
	      );
	  }
	  
	  @ExceptionHandler(ParkingSessionNotFoundException.class)
	  @ResponseStatus(HttpStatus.NOT_FOUND)
	  public ApiResponse<Void> handleParkingSessionNotFound(
	          ParkingSessionNotFoundException ex) {

	      return ApiResponse.failure(
	              ErrorCode.PARKING_SESSION_NOT_FOUND.getCode(),
	              ex.getMessage(),null
	      );
	  }
}
