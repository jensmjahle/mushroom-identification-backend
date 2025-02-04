package ntnu.idi.mushroomidentificationbackend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.UUID;
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
  public static String getFileExtension(String filename) {
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }

  public static String saveImageLocally(MultipartFile file) throws IOException {
    // Define local storage directory (ensure this folder exists or create it dynamically)
    String uploadDir = "uploads/";
    File directory = new File(uploadDir);
    if (!directory.exists()) {
      directory.mkdirs(); // Create directory if it doesn't exist
    }

    // Create a unique filename to prevent overwriting
    String uniqueFilename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
    String filePath = uploadDir + uniqueFilename;

    // Save the file
    Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

    return filePath; // Return the saved file path
  }

}
