package ntnu.idi.mushroomidentificationbackend.controller.admin;

import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.response.AdminDTO;
import ntnu.idi.mushroomidentificationbackend.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminService adminService;
  private final Logger logger = Logger.getLogger(AdminController.class.getName());
  
  @GetMapping
  public ResponseEntity<Page<AdminDTO>> getAllAdminsPaginated(Pageable pageable) {
    logger.info(() -> String.format("Received request for all admins - page: %d, size: %d",
        pageable.getPageNumber(), pageable.getPageSize()));
    return ResponseEntity.ok(adminService.getAllAdminsPaginated(pageable));
  }
}
