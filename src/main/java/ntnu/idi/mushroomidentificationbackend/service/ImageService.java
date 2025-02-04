package ntnu.idi.mushroomidentificationbackend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
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
  /*
  public static byte[] removeMetadata(MultipartFile file) throws IOException {
    // Ensure PNG reader is registered
    ImageIO.scanForPlugins();
    
    // Read the image (this automatically removes metadata)
    BufferedImage image = ImageIO.read(file.getInputStream());

    if (image == null) {
      throw new IOException("Failed to read image: Unsupported format or corrupted file.");
    }
    
    // Save the image back to a byte array without the metadata
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, getFileExtension(file.getOriginalFilename()), outputStream);

    return outputStream.toByteArray();
  }
*/
  public static byte[] removeMetadata(MultipartFile file) throws IOException {
    // Register ImageIO plugins (ensures TwelveMonkeys is active)
    ImageIO.scanForPlugins();

    // Read image using ImageIO and validate
    try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file.getInputStream())) {
      if (imageInputStream == null) {
        throw new IOException("ImageInputStream is null, cannot read image.");
      }

      // Get a JPG-compatible reader
      Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
      if (!readers.hasNext()) {
        throw new IOException("No ImageReader found for the provided image format.");
      }

      ImageReader reader = readers.next();
      reader.setInput(imageInputStream);
      BufferedImage image = reader.read(0);

      if (image == null) {
        throw new IOException("Failed to read image: Unsupported format or corrupted file.");
      }

      // Save the image back to a byte array without metadata
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      boolean success = ImageIO.write(image, "jpg", outputStream);

      if (!success) {
        throw new IOException("ImageIO.write() failed: No suitable writer found for JPG.");
      }

      return outputStream.toByteArray();
    }
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
