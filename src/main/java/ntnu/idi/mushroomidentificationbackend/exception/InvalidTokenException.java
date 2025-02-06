package ntnu.idi.mushroomidentificationbackend.exception;

public class InvalidTokenException extends RuntimeException {
        
          public InvalidTokenException(String message) {
            super(message);
          }
        
          public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
          }
        
          public InvalidTokenException() {
            super("Invalid token.");
          }

}
