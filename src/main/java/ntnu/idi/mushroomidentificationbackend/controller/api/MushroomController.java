package ntnu.idi.mushroomidentificationbackend.controller.api;

import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.request.AddImagesToMushroomDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MushroomDTO;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.MushroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling mushroom-related requests for authenticated public users.
 */
@RestController
@RequestMapping("/api/mushrooms")
public class MushroomController {
  private final JWTUtil jwtUtil;
  private final MushroomService mushroomService;
  private final WebSocketNotificationHandler webSocketNotificationHandler;

  public MushroomController(JWTUtil jwtUtil, MushroomService mushroomService,
      WebSocketNotificationHandler webSocketNotificationHandler) {
    this.jwtUtil = jwtUtil;
    this.mushroomService = mushroomService;
    this.webSocketNotificationHandler = webSocketNotificationHandler;
  }


  /**
   * Retrieves all mushrooms for a given user request ID.
   * This endpoint is used to fetch the list of mushrooms
   * associated with a specific user request.
   * 
   * @param userRequestId the ID of the user request for which mushrooms are to be retrieved
   * @param token the JWT token for authentication
   * @return List of MushroomDTO objects representing the mushrooms
   */
  @GetMapping("{userRequestId}")
  public List<MushroomDTO> getAllMushrooms(
      @PathVariable String userRequestId,
      @RequestHeader("Authorization") String token) {

    jwtUtil.validateChatroomToken(token, userRequestId);
    return mushroomService.getAllMushrooms(userRequestId);
  }

  /**
   * Adds images to a mushroom in the user's basket.
   * This endpoint allows users to upload images
   * to a specific mushroom identified by the user request ID.
   * 
   * @param userRequestId the ID of the user request to which the mushroom belongs
   * @param token the JWT token for authentication
   * @param addImagesToMushroomDTO the data transfer object containing the images to be added
   * @return ResponseEntity indicating the success of the operation
   */
  @PostMapping("{userRequestId}/image")
  public String addImageToMushroom(
      @PathVariable String userRequestId,
      @RequestHeader("Authorization") String token, @ModelAttribute AddImagesToMushroomDTO addImagesToMushroomDTO) {
    jwtUtil.validateChatroomToken(token, userRequestId);
    mushroomService.addImagesToMushroom(userRequestId, addImagesToMushroomDTO);
    webSocketNotificationHandler.sendRequestUpdateToObservers(userRequestId, WebsocketNotificationType.MUSHROOM_BASKET_UPDATED);
    return ResponseEntity.ok("Image added successfully").toString();
  }
  
}
