package ntnu.idi.mushroomidentificationbackend.exception;

public class RequestLockedException extends RuntimeException {
  public RequestLockedException(String message) {
    super(message);
  }
}
