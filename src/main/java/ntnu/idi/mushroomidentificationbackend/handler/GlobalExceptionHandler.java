package ntnu.idi.mushroomidentificationbackend.handler;

import ntnu.idi.mushroomidentificationbackend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Global exception handler to manage all application exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private ResponseEntity<Map<String, String>> buildResponse(HttpStatus status, String message, String type) {
    return ResponseEntity.status(status).body(Map.of(
        "message", message,
        "type", type
    ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred: " + e.getMessage(),
        "INTERNAL_SERVER_ERROR");
  }

  @ExceptionHandler(DatabaseOperationException.class)
  public ResponseEntity<Map<String, String>> handleDatabaseOperationException(DatabaseOperationException e) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        "Database error: " + e.getMessage(),
        "DATABASE_ERROR");
  }

  @ExceptionHandler(RequestLockedException.class)
  public ResponseEntity<Map<String, String>> handleRequestLockedException(RequestLockedException e) {
    return buildResponse(HttpStatus.CONFLICT,
        e.getMessage(),
        "REQUEST_LOCKED");
  }

  @ExceptionHandler(UnauthorizedAccessException.class)
  public ResponseEntity<Map<String, String>> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
    return buildResponse(HttpStatus.UNAUTHORIZED,
        e.getMessage(),
        "UNAUTHORIZED");
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<Map<String, String>> handleInvalidTokenException(InvalidTokenException e) {
    return buildResponse(HttpStatus.UNAUTHORIZED,
        e.getMessage(),
        "INVALID_TOKEN");
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException e) {
    return buildResponse(HttpStatus.NOT_FOUND,
        "User not found: " + e.getMessage(),
        "USER_NOT_FOUND");
  }

  @ExceptionHandler(RequestNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleRequestNotFoundException(RequestNotFoundException e) {
    return buildResponse(HttpStatus.NOT_FOUND,
        "Identification request not found: " + e.getMessage(),
        "REQUEST_NOT_FOUND");
  }

  @ExceptionHandler(ImageProcessingException.class)
  public ResponseEntity<Map<String, String>> handleImageProcessingException(ImageProcessingException e) {
    return buildResponse(HttpStatus.BAD_REQUEST,
        "Error processing image: " + e.getMessage(),
        "IMAGE_PROCESSING_ERROR");
  }

  @ExceptionHandler(InvalidImageFormatException.class)
  public ResponseEntity<Map<String, String>> handleInvalidImageFormatException(InvalidImageFormatException e) {
    return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        e.getMessage(),
        "INVALID_IMAGE_FORMAT");
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<Map<String, String>> handleServiceUnavailableException(ServiceUnavailableException e) {
    return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,
        e.getMessage(),
        "SERVICE_UNAVAILABLE");
  }

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  public ResponseEntity<Map<String, String>> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e) {
    return buildResponse(HttpStatus.CONFLICT,
        "Obs! This username is already taken",
        "USERNAME_CONFLICT");
  }

  @ExceptionHandler(InvalidInputException.class)
  public ResponseEntity<Map<String, String>> handleInvalidInputException(InvalidInputException e) {
    return buildResponse(HttpStatus.BAD_REQUEST,
        e.getMessage(),
        "INVALID_INPUT");
  }
}
