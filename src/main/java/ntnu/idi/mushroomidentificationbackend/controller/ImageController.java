package ntnu.idi.mushroomidentificationbackend.controller;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.MessageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
public class ImageController {
  private final JWTUtil jwtUtil;
  private final Logger logger = Logger.getLogger(ImageController.class.getName());

  public ImageController(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @GetMapping
  public ResponseEntity<Resource> getImage(@RequestParam String token) {
    String internalPath = jwtUtil.validateSignedImageUrl(token);
    if (internalPath == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      Path file = Paths.get(internalPath).toAbsolutePath();
      Resource resource = new UrlResource(file.toUri());

      if (!resource.exists()) {
        return ResponseEntity.notFound().build();
      }

      // Detect a content type
      String contentType = Files.probeContentType(file);
      if (contentType == null) contentType = "application/octet-stream";

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .body(resource);

    } catch (IOException e) {
      logger.severe("Error reading file: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
    }
  }


}
