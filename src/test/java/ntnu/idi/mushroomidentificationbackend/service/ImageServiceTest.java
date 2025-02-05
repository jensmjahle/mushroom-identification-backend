package ntnu.idi.mushroomidentificationbackend.service;

import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageServiceTest {

  @Test
  void testRemoveMetadata() throws IOException {
    // Load test JPEG image from resources folder
    InputStream imageStream = getClass().getClassLoader().getResourceAsStream("test-image.jpg");
    assertNotNull(imageStream, "Test image NOT FOUND in resources folder!");

    // Create mock MultipartFile (simulating an uploaded image)
    MockMultipartFile mockFile = new MockMultipartFile(
        "image", "test-image.jpg", "image/jpeg", imageStream
    );

    // Process image and remove metadata
    byte[] cleanedImage = ImageService.removeMetadata(mockFile);

    // Assertions to verify correctness
    assertNotNull(cleanedImage, "The cleaned image should not be null");
    assertTrue(cleanedImage.length > 0, "The cleaned image should not be empty");
    assertTrue(cleanedImage.length < mockFile.getSize(),
        "The cleaned image should be smaller than the original if metadata was removed");
  }

  @Test
  void testAvailableJPGReaders() {
    Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg");
    assertTrue(readers.hasNext(), "No JPEG reader found! TwelveMonkeys might not be loaded.");

    while (readers.hasNext()) {
      System.out.println("JPEG Reader Found: " + readers.next().getClass().getName());
    }
  }
}
