package ntnu.idi.mushroomidentificationbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
  
  @PostMapping("/send-message")
  public ResponseEntity<String> sendMessage() {
    return ResponseEntity.ok("Message sent");
  }
}
