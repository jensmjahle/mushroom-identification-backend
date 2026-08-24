package ntnu.idi.mushroomidentificationbackend.handler;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import ntnu.idi.mushroomidentificationbackend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Global exception handler to manage all application exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

  /**
   * Builds a standardized response entity for exceptions.
   * This method creates a response entity with a specific HTTP status,
   * a message, and a type.
   *
   * @param status the HTTP status to return
   * @param message the error message to include in the response
   * @param type the type of error (e.g., DATABASE_ERROR, REQUEST_LOCKED, UNAUTHORIZED)
   * @return ResponseEntity containing the error details
   */
  private ResponseEntity<Map<String, String>> buildResponse(HttpStatus status, String message, String type) {
    return ResponseEntity.status(status).body(Map.of(
        "message", message,
        "type", type
    ));
  }

  /**
   * Handles any exception not covered by a more specific handler below. The exception detail is
   * logged server-side only: returning it to the client would leak internal implementation
   * details (stack traces, library/server info, SQL, etc.) that can help an attacker.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
    logger.log(Level.SEVERE, "Unhandled exception", e);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred.",
        "INTERNAL_SERVER_ERROR");
  }

  /**
   * Handles malformed request bodies (invalid JSON, wrong types, etc.), which previously fell
   * through to the generic 500 handler.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    return buildResponse(HttpStatus.BAD_REQUEST,
        "Malformed request body.",
        "MALFORMED_REQUEST");
  }

  /**
   * Handles requests sent with a Content-Type the endpoint does not support, which previously
   * fell through to the generic 500 handler instead of the correct 415 response.
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Map<String, String>> handleHttpMediaTypeNotSupportedException(
      HttpMediaTypeNotSupportedException e) {
    return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Unsupported content type.",
        "UNSUPPORTED_MEDIA_TYPE");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException e) {
    return buildResponse(HttpStatus.BAD_REQUEST,
        "Missing required parameter: " + e.getParameterName(),
        "MISSING_PARAMETER");
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<Map<String, String>> handleMissingServletRequestPartException(
      MissingServletRequestPartException e) {
    return buildResponse(HttpStatus.BAD_REQUEST,
        "Missing required part: " + e.getRequestPartName(),
        "MISSING_PARAMETER");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    return buildResponse(HttpStatus.BAD_REQUEST,
        "Invalid value for parameter: " + e.getName(),
        "INVALID_INPUT");
  }

  /**
   * Handles bean-validation failures on request bodies (@Valid @RequestBody) and on
   * form/multipart-bound objects (@Valid @ModelAttribute), enforcing server-side business
   * validation instead of accepting malformed/incomplete submissions.
   */
  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<Map<String, String>> handleValidationException(BindException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining("; "));
    return buildResponse(HttpStatus.BAD_REQUEST,
        message.isBlank() ? "Invalid request." : message,
        "INVALID_INPUT");
  }

  @ExceptionHandler(TooManyRequestsException.class)
  public ResponseEntity<Map<String, String>> handleTooManyRequestsException(TooManyRequestsException e) {
    return buildResponse(HttpStatus.TOO_MANY_REQUESTS,
        e.getMessage(),
        "TOO_MANY_REQUESTS");
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
        "This username is already taken.",
        "USERNAME_CONFLICT");
  }

  @ExceptionHandler(InvalidInputException.class)
  public ResponseEntity<Map<String, String>> handleInvalidInputException(InvalidInputException e) {
    return buildResponse(HttpStatus.BAD_REQUEST,
        e.getMessage(),
        "INVALID_INPUT");
  }
}
