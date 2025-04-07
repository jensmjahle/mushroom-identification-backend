package ntnu.idi.mushroomidentificationbackend.service;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.exception.ImageProcessingException;
import ntnu.idi.mushroomidentificationbackend.exception.InvalidImageFormatException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

  private static final String UPLOAD_DIR = "uploads/";
  private static final List<String> ALLOWED_MIME_TYPES = List.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB limit
  private static final Logger logger = Logger.getLogger(ImageService.class.getName());

  private ImageService() {
    
  }

  /**
   * Saves an image internally under the userRequestId  directory.
   */
  public static String saveImage(MultipartFile file, String userRequestId, String mushroomId ) throws IOException {
    logger.info("Saving image for user request: " + userRequestId );
    if (file == null || file.isEmpty()) {
      logger.info("Invalid file: The file is empty.");
      throw new ImageProcessingException("Invalid file: The file is empty.");
    }

    // **Validate file type**
    String mimeType = file.getContentType();  // Get MIME type from MultipartFile
    if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
      logger.info("Invalid file type. Only JPEG and PNG are allowed.");
      throw new InvalidImageFormatException("Invalid file type. Only JPEG and PNG are allowed.");
    }

    // **Check file size**
    if (file.getSize() > MAX_FILE_SIZE) {
      logger.info("File is too large. Max allowed size is 5MB.");
      throw new ImageProcessingException("File is too large. Max allowed size is 5MB.");
    }

    // **Sanitize userRequestId  to prevent path traversal**
    userRequestId  = userRequestId.replaceAll("[^a-zA-Z0-9_-]", "_");
    mushroomId = mushroomId.replaceAll("[^a-zA-Z0-9_-]", "_");

    // **Ensure upload directory exists**
    String requestUploadDir = UPLOAD_DIR + userRequestId  + "/" + mushroomId + "/";
    File directory = new File(requestUploadDir);
    if (!directory.exists() && !directory.mkdirs()) {
      logger.info("Failed to create upload directory.");
      throw new IOException("Failed to create upload directory.");
    }

    // **Generate unique filename**
    String fileExtension = getFileExtension(file.getOriginalFilename());
    String uniqueFilename = UUID.randomUUID() + "." + fileExtension;
    String filePath = requestUploadDir + uniqueFilename;
    logger.info("Saving image to: " + filePath);

    logger.info("Original filename: " + file.getOriginalFilename());
    logger.info("File size: " + file.getSize());
    logger.info("Content type: " + file.getContentType());

    Files.copy(file.getInputStream(), new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);

    return uniqueFilename;
  }

  private static String getFileExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      throw new IllegalArgumentException("Invalid filename: missing file extension.");
    }
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }
}
