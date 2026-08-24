package ntnu.idi.mushroomidentificationbackend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import ntnu.idi.mushroomidentificationbackend.exception.ImageProcessingException;
import ntnu.idi.mushroomidentificationbackend.exception.InvalidImageFormatException;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service class for handling image uploads and management.
 */
@Service
public class ImageService {

  private static final String UPLOAD_DIR = "uploads/";
  private static final List<String> ALLOWED_MIME_TYPES = List.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);
  private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB limit
  private static final Logger logger = Logger.getLogger(ImageService.class.getName());
  private static final String REGEX_SANITIZE = "[^a-zA-Z0-9_-]";
  private static final Tika TIKA = new Tika();

  private ImageService() {
    
  }

  /**
   * Saves an image file to the server.
   * This method validates the file type and size, sanitizes the userRequestId and mushroomId to prevent path traversal attacks,
   * creates the necessary directory structure, and saves the file with a unique name.
   * The image is saved at the path `uploads/{userRequestId}/{mushroomId}/{uniqueFilename}`.
   *
   * @param file the image file to save
   * @param userRequestId the ID of the user request, used to create a unique directory
   * @param mushroomId the ID of the mushroom, used to create a unique subdirectory
   * @return the unique filename of the saved image
   * @throws IOException if an I/O error occurs during file saving
   */
  public static String saveImage(MultipartFile file, String userRequestId, String mushroomId ) throws IOException {
    LogHelper.info(logger, "Saving image for user request: {0} and mushroom: {1}", userRequestId, mushroomId);
    if (file == null || file.isEmpty()) {
      LogHelper.info(logger, "Invalid file: The file is empty.");
      throw new ImageProcessingException("Invalid file: The file is empty.");
    }

    // **Check file size**
    if (file.getSize() > MAX_FILE_SIZE) {
      LogHelper.info(logger, "File is too large. Max allowed size is {0}MB and the file was {1}MB", MAX_FILE_SIZE / (1024 * 1024), file.getSize() / (1024 * 1024));
      throw new ImageProcessingException("File is too large. Max allowed size is 50MB.");
    }

    byte[] content = file.getBytes();

    // **Validate the actual file content, not just the client-supplied filename/Content-Type.**
    // Detects the real MIME type from the file's magic bytes (signature) so a renamed or
    // relabelled non-image file cannot masquerade as a JPEG/PNG.
    String detectedMimeType = TIKA.detect(content);
    if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
      LogHelper.info(logger, "Invalid file type: {0}. Only JPEG and PNG are allowed.", detectedMimeType);
      throw new InvalidImageFormatException("Invalid file type. Only JPEG and PNG are allowed.");
    }

    // **Confirm the content actually decodes as an image**, rejecting truncated or corrupt
    // files that merely happen to have a valid image signature.
    BufferedImage decoded;
    try {
      decoded = ImageIO.read(new ByteArrayInputStream(content));
    } catch (IOException e) {
      decoded = null;
    }
    if (decoded == null) {
      LogHelper.info(logger, "File could not be decoded as a valid image.");
      throw new InvalidImageFormatException("File is not a valid image.");
    }

    // **Sanitize userRequestId  to prevent path traversal**
    userRequestId  = userRequestId.replaceAll(REGEX_SANITIZE, "_");
    mushroomId = mushroomId.replaceAll(REGEX_SANITIZE, "_");

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
    LogHelper.info(logger, "Saving image {0} for request {1} in directory {2}",
        uniqueFilename, userRequestId, requestUploadDir);
    Files.write(new File(filePath).toPath(), content);

    return uniqueFilename;
  }

  /**
   * Extracts the file extension from the given filename.
   *
   * @param filename the name of the file
   * @return the file extension in lowercase
   */
  private static String getFileExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      throw new IllegalArgumentException("Invalid filename: missing file extension.");
    }
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }

  /**
   * Deletes all images for the given userRequestId (and all subfolders).
   *
   * @param userRequestId the ID whose directory should be removed
   * @throws IOException if an I/O error occurs during deletion
   */
  public static void deleteImagesForRequest(String userRequestId) throws IOException {
    String safeId = userRequestId.replaceAll(REGEX_SANITIZE, "_");
    Path dir = Paths.get(UPLOAD_DIR, safeId);

    if (Files.exists(dir)) {
      // Walk the file tree, delete files before directories
      Files.walk(dir)
          .sorted(Comparator.reverseOrder())
          .forEach(path -> {
            try {
              Files.delete(path);
              LogHelper.info(logger, "Deleted: {0}", path);
            } catch (IOException e) {
              LogHelper.warning(logger, "Failed to delete {0}: {1}", path, e.getMessage());
            }
          });
      LogHelper.info(logger, "All images for request {0} have been removed.", safeId);
    } else {
      LogHelper.warning(logger, "No image directory to delete for request {0}", safeId);
    }
  }
}
