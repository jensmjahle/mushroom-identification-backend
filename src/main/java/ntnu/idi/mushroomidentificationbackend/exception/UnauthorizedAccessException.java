package ntnu.idi.mushroomidentificationbackend.exception;

public class UnauthorizedAccessException extends RuntimeException {
  
    public UnauthorizedAccessException(String message) {
      super(message);
    }
  
    public UnauthorizedAccessException(String message, Throwable cause) {
      super(message, cause);
    }
  
    public UnauthorizedAccessException() {
      super("Unauthorized access exception occurred.");
    }

}
