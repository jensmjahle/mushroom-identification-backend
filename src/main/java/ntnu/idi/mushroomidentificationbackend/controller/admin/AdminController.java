package ntnu.idi.mushroomidentificationbackend.controller.admin;

import jakarta.validation.Valid;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangePasswordDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.CreateAdminDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateProfileDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.AdminService;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
  private final JWTUtil jwtUtil;
  private static final String BEARER = "Bearer ";
  private static final Logger logger = Logger.getLogger(AdminController.class.getName());

  @GetMapping("/me")
  public ResponseEntity<AdminDTO> getAdmin(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
    String token = authHeader.replace(BEARER, "").trim();
    String username = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Received request for admin details - username: {0}", username);
    return ResponseEntity.ok(adminService.getAdminDTO(username));
  }

  @GetMapping
  public ResponseEntity<Page<AdminDTO>> getAllAdminsPaginated(Pageable pageable) {
    LogHelper.info(logger, "Received request for all admins - page: {0}, size: {1}",
        pageable.getPageNumber(), pageable.getPageSize());
    return ResponseEntity.ok(adminService.getAllAdminsPaginated(pageable));
  }

  @PutMapping("/profile")
  public ResponseEntity<?> updateProfile(@Valid @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
      @RequestBody UpdateProfileDTO request) {
    String token = authHeader.replace(BEARER, "").trim();
    String username = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Profile update requested for admin: {0}", username);
    adminService.updateProfile(request, username);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/password")
  public ResponseEntity<?> changePassword(@Valid @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
      @RequestBody ChangePasswordDTO request) {
    String token = authHeader.replace(BEARER, "").trim();
    String username = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Password change requested for admin: {0}", username);
    adminService.changePassword(request, username);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteAdmin(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
    String token = authHeader.replace(BEARER, "").trim();
    String username = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Delete account requested for admin: {0}", username);
    adminService.deleteAdmin(username);
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("superuser/delete/{username}")
  public ResponseEntity<?> deleteAdmin(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathVariable String username) {
    String token = authHeader.replace(BEARER, "").trim();
    String adminUsername = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Deleting admin {0}, performed by: {1}",username, adminUsername);
    adminService.deleteAdminAsSuperuser(username, adminUsername);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/superuser/create")
  public ResponseEntity<?> createModerator(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
      @Valid @RequestBody CreateAdminDTO request
  ) {
    String token = authHeader.replace(BEARER, "").trim();
    String superuserUsername = jwtUtil.extractUsername(token);
    LogHelper.info(logger, "Superuser {0} is creating a new admin: {1}", superuserUsername, request.getUsername());
    adminService.createModerator(superuserUsername, request);
    return ResponseEntity.ok().build();
  }
}
