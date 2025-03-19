package ntnu.idi.mushroomidentificationbackend.service;

import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageServiceTest {

  @Test
  void testAvailableJPGReaders() {
    Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg");
    assertTrue(readers.hasNext(), "No JPEG reader found! TwelveMonkeys might not be loaded.");

    while (readers.hasNext()) {
      System.out.println("JPEG Reader Found: " + readers.next().getClass().getName());
    }
  }
}
