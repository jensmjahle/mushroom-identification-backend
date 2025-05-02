package ntnu.idi.mushroomidentificationbackend.controller.admin;

import jakarta.validation.Valid;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangePasswordDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateProfileDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminService adminService;
  private final Logger logger = Logger.getLogger(AdminController.class.getName());
  private final JWTUtil jwtUtil;
  
  @GetMapping("/me")
  public ResponseEntity<AdminDTO> getAdmin(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
    String token = authHeader.replace("Bearer ", "").trim();
    String username = jwtUtil.extractUsername(token);
    logger.info(() -> String.format("Received request for admin details - username: %s", username));
    return ResponseEntity.ok(adminService.getAdminDTO(username));
  }
  
  @GetMapping
  public ResponseEntity<Page<AdminDTO>> getAllAdminsPaginated(Pageable pageable) {
    logger.info(() -> String.format("Received request for all admins - page: %d, size: %d",
        pageable.getPageNumber(), pageable.getPageSize()));
    return ResponseEntity.ok(adminService.getAllAdminsPaginated(pageable));
  }
  
  @PutMapping("/profile")
  public ResponseEntity<?> updateProfile(@Valid  @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,@RequestBody UpdateProfileDTO request) {
    String token = authHeader.replace("Bearer ", "").trim();
    String username = jwtUtil.extractUsername(token);
    adminService.updateProfile(request, username);
    return ResponseEntity.ok().build();
  }
  
  @PutMapping("/password")
  public ResponseEntity<?> changePassword(@Valid  @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestBody ChangePasswordDTO request) {
    String token = authHeader.replace("Bearer ", "").trim();
    String username = jwtUtil.extractUsername(token);
    adminService.changePassword(request, username);
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteAdmin(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
    String token = authHeader.replace("Bearer ", "").trim();
    String username = jwtUtil.extractUsername(token);
    adminService.deleteAdmin(username);
    return ResponseEntity.ok().build();
  }
}
