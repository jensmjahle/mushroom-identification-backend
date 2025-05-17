package ntnu.idi.mushroomidentificationbackend.service;

import java.lang.reflect.InvocationTargetException;
import ntnu.idi.mushroomidentificationbackend.exception.ImageProcessingException;
import ntnu.idi.mushroomidentificationbackend.exception.InvalidImageFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceTest {

  @Test
  void saveImage_validImage_returnsFilename() throws IOException {
    MockMultipartFile file = new MockMultipartFile(
        "image",
        "image.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        new byte[1024]
    );

    String result = ImageService.saveImage(file, "user123", "mushroom123");
    assertNotNull(result);
    assertTrue(result.endsWith(".jpg"));
  }

  @Test
  void saveImage_emptyFile_throwsImageProcessingException() {
    MockMultipartFile file = new MockMultipartFile("image", new byte[0]);
    assertThrows(ImageProcessingException.class, () ->
        ImageService.saveImage(file, "user123", "mushroom123")
    );
  }

  @Test
  void saveImage_invalidMimeType_throwsInvalidImageFormatException() {
    MockMultipartFile file = new MockMultipartFile(
        "image",
        "image.gif",
        MediaType.IMAGE_GIF_VALUE,
        new byte[1024]
    );

    assertThrows(InvalidImageFormatException.class, () ->
        ImageService.saveImage(file, "user123", "mushroom123")
    );
  }

  @Test
  void saveImage_fileTooLarge_throwsImageProcessingException() {
    byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6MB
    MockMultipartFile file = new MockMultipartFile(
        "image",
        "large.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        largeBytes
    );

    assertThrows(ImageProcessingException.class, () ->
        ImageService.saveImage(file, "user123", "mushroom123")
    );
  }

  @Test
  void getFileExtension_invalidFilename_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () ->
        ImageServiceTest.invokeGetFileExtension("invalid_filename")
    );
  }

  static String invokeGetFileExtension(String filename) throws Exception {
    var method = ImageService.class.getDeclaredMethod("getFileExtension", String.class);
    method.setAccessible(true);
    try {
      return (String) method.invoke(null, filename);
    } catch (InvocationTargetException e) {
      throw (Exception) e.getCause();
    }
  }

}
