package ntnu.idi.mushroomidentificationbackend.handler;

import ntnu.idi.mushroomidentificationbackend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler to manage all application exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles general exceptions.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleGeneralException(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("An unexpected error occurred: " + e.getMessage());
  }

  /**
   * Handles DatabaseOperationException.
   */
  @ExceptionHandler(DatabaseOperationException.class)
  public ResponseEntity<String> handleDatabaseOperationException(DatabaseOperationException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Database error: " + e.getMessage());
  }

  /**
   * Handles UnauthorizedAccessException.
   */
  @ExceptionHandler(UnauthorizedAccessException.class)
  public ResponseEntity<String> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body("Unauthorized access: " + e.getMessage());
  }

  /**
   * Handles UserNotFoundException.
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body("User not found: " + e.getMessage());
  }

  /**
   * Handles RequestNotFoundException.
   */
  @ExceptionHandler(RequestNotFoundException.class)
  public ResponseEntity<String> handleRequestNotFoundException(RequestNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body("Identification request not found: " + e.getMessage());
  }

  /**
   * Handles ImageProcessingException.
   */
  @ExceptionHandler(ImageProcessingException.class)
  public ResponseEntity<String> handleImageProcessingException(ImageProcessingException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body("Error processing image: " + e.getMessage());
  }

  /**
   * Handles InvalidImageFormatException.
   */
  @ExceptionHandler(InvalidImageFormatException.class)
  public ResponseEntity<String> handleInvalidImageFormatException(InvalidImageFormatException e) {
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body("Invalid image format: " + e.getMessage());
  }

  /**
   * Handles ServiceUnavailableException.
   */
  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<String> handleServiceUnavailableException(ServiceUnavailableException e) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body("External service unavailable: " + e.getMessage());
  }
  
  /**
   * Handles InvalidTokenException.
   */
  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body("Invalid token: " + e.getMessage());
  }
  
  /**
   * Handles UsernameAlreadyExistsException.
   */
  @ExceptionHandler(UsernameAlreadyExistsException.class)
  public ResponseEntity<String> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body("Username already exists: " + e.getMessage());
  }
}
