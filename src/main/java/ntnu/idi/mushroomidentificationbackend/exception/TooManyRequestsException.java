package ntnu.idi.mushroomidentificationbackend.exception;

public class TooManyRequestsException extends RuntimeException {

  public TooManyRequestsException(String message) {
    super(message);
  }

  public TooManyRequestsException(String message, Throwable cause) {
    super(message, cause);
  }

  public TooManyRequestsException() {
    super("Too many requests.");
  }

}
