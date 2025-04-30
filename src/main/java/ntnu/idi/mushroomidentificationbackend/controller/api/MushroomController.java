package ntnu.idi.mushroomidentificationbackend.controller.api;

import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.request.AddImagesToMushroomDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MushroomDTO;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.MushroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mushrooms")
public class MushroomController {
  private final JWTUtil jwtUtil;
  private final MushroomService mushroomService;

  public MushroomController(JWTUtil jwtUtil, MushroomService mushroomService) {
    this.jwtUtil = jwtUtil;
    this.mushroomService = mushroomService;
  }
  
  
  
  
  @GetMapping("{userRequestId}")
  public List<MushroomDTO> getAllMushrooms(
      @PathVariable String userRequestId,
      @RequestHeader("Authorization") String token) {

    jwtUtil.validateChatroomToken(token, userRequestId);
    return mushroomService.getAllMushrooms(userRequestId);
  }
  
  @PostMapping("{userRequestId}/image")
  public String addImageToMushroom(
      @PathVariable String userRequestId,
      @RequestHeader("Authorization") String token, @RequestBody AddImagesToMushroomDTO addImagesToMushroomDTO) {
    jwtUtil.validateChatroomToken(token, userRequestId);
    mushroomService.addImageToMushroom(userRequestId, addImagesToMushroomDTO);
    return ResponseEntity.ok("Image added successfully").toString();
  }
  
}
