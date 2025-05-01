package ntnu.idi.mushroomidentificationbackend.controller.admin;

import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateMushroomStatusDTO;
import ntnu.idi.mushroomidentificationbackend.service.MushroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/mushrooms")
public class AdminMushroomController {
  private final Logger logger = Logger.getLogger(AdminMushroomController.class.getName());
  private final MushroomService mushroomService;
  
  @PutMapping("/{userRequestId}/status")
  public String updateMushroomStatus(
      @PathVariable String userRequestId, UpdateMushroomStatusDTO updateMushroomStatusDTO) {
    logger.info(() -> String.format("Received request to update mushroom status for user request %s", userRequestId));
    mushroomService.updateMushroomStatus(userRequestId, updateMushroomStatusDTO);
    return ResponseEntity.ok("Mushroom status updated successfully").toString();
  }

}
