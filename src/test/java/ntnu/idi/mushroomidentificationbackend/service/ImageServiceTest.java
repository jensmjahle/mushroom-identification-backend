package ntnu.idi.mushroomidentificationbackend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import javax.imageio.ImageIO;
import ntnu.idi.mushroomidentificationbackend.exception.ImageProcessingException;
import ntnu.idi.mushroomidentificationbackend.exception.InvalidImageFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceTest {

  private static byte[] realJpegBytes() throws IOException {
    BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", out);
    return out.toByteArray();
  }

  @Test
  void saveImage_validImage_returnsFilename() throws IOException {
    MockMultipartFile file = new MockMultipartFile(
        "image",
        "image.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        realJpegBytes()
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
  void saveImage_realImageWithSpoofedContentType_isAcceptedByRealContent() throws IOException {
    // The client-supplied Content-Type is wrong/spoofed, but the actual bytes are a real JPEG.
    // Content sniffing should look past the declared Content-Type and accept it.
    MockMultipartFile file = new MockMultipartFile(
        "image",
        "image.jpg",
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        realJpegBytes()
    );

    String result = ImageService.saveImage(file, "user123", "mushroom123");
    assertNotNull(result);
  }

  @Test
  void saveImage_textFileDisguisedAsJpeg_throwsInvalidImageFormatException() {
    MockMultipartFile file = new MockMultipartFile(
        "image",
        "fake.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "still not an image".getBytes()
    );

    assertThrows(InvalidImageFormatException.class, () ->
        ImageService.saveImage(file, "user123", "mushroom123")
    );
  }

  @Test
  void saveImage_fileTooLarge_throwsImageProcessingException() {
    byte[] largeBytes = new byte[51 * 1024 * 1024]; // 51MB
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
