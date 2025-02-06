package ntnu.idi.mushroomidentificationbackend.exception;

public class InvalidImageFormatException extends RuntimeException {
  
    public InvalidImageFormatException(String message) {
      super(message);
    }
  
    public InvalidImageFormatException(String message, Throwable cause) {
      super(message, cause);
    }
  
    public InvalidImageFormatException() {
      super("Invalid image format.");
    }

}
