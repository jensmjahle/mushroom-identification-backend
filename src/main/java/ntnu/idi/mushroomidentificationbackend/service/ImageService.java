package ntnu.idi.mushroomidentificationbackend.service;

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


  /**
   * Saves an image internally under the referenceCode directory.
   */
  public String saveImage(MultipartFile file, String referenceCode) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new ImageProcessingException("Invalid file: The file is empty.");
    }

    // **Validate file type**
    String mimeType = file.getContentType();  // Get MIME type from MultipartFile
    if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
      throw new InvalidImageFormatException("Invalid file type. Only JPEG and PNG are allowed.");
    }

    // **Check file size**
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new ImageProcessingException("File is too large. Max allowed size is 5MB.");
    }

    // **Sanitize referenceCode to prevent path traversal**
    referenceCode = referenceCode.replaceAll("[^a-zA-Z0-9_-]", "_");


    // **Ensure upload directory exists**
    String requestUploadDir = UPLOAD_DIR + referenceCode + "/";
    File directory = new File(requestUploadDir);
    if (!directory.exists() && !directory.mkdirs()) {
      throw new IOException("Failed to create upload directory.");
    }

    // **Generate unique filename**
    String fileExtension = getFileExtension(file.getOriginalFilename());
    String uniqueFilename = UUID.randomUUID() + "." + fileExtension;
    String filePath = requestUploadDir + uniqueFilename;


    // **Save file**
    file.transferTo(new File(filePath));

    return uniqueFilename;
  }

  private String getFileExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      throw new IllegalArgumentException("Invalid filename: missing file extension.");
    }
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }
}
