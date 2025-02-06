package ntnu.idi.mushroomidentificationbackend.exception;

public class ServiceUnavailableException extends RuntimeException {
    
      public ServiceUnavailableException(String message) {
        super(message);
      }
    
      public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
      }
    
      public ServiceUnavailableException() {
        super("Service unavailable.");
      }
  

}
