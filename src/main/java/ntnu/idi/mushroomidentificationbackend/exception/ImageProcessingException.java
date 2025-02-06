package ntnu.idi.mushroomidentificationbackend.exception;

public class ImageProcessingException extends RuntimeException {

  public ImageProcessingException(String message) {
    super(message);
  }

  public ImageProcessingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImageProcessingException() {
    super("Image processing exception occurred.");
  }

}
