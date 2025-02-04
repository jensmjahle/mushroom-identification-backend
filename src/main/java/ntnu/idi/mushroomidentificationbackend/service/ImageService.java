package ntnu.idi.mushroomidentificationbackend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
@Service
public class ImageService {

  /**
   * Remove metadata from an image.
   *
   * @param file The image file to remove metadata from
   * @return The image file without metadata
   * @throws IOException If an error occurs while reading the image
   */
  public static byte[] removeMetadata(MultipartFile file) throws IOException {
    // Read the image (this automatically removes metadata)
    BufferedImage image = ImageIO.read(file.getInputStream());

    // Save the image back to a byte array without the metadata
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, getFileExtension(file.getOriginalFilename()), outputStream);

    return outputStream.toByteArray();
  }

  /**
   * Get the file extension of a file.
   *
   * @param filename The name of the file
   * @return The file extension
   */
  private static String getFileExtension(String filename) {
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }
}
