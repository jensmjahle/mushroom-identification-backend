package ntnu.idi.mushroomidentificationbackend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ntnu.idi.mushroomidentificationbackend.dto.response.MushroomDTO;
import ntnu.idi.mushroomidentificationbackend.mapper.MushroomMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Image;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.repository.ImageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MushroomRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.stereotype.Service;

@Service
public class MushroomService {
  private final MushroomRepository mushroomRepository;
  private final UserRequestRepository userRequestRepository;
  private final ImageRepository imageRepository;
  private final JWTUtil jwtUtil;

  public MushroomService(MushroomRepository mushroomRepository,
      UserRequestRepository userRequestRepository, ImageRepository imageRepository, JWTUtil jwtUtil) {
    this.mushroomRepository = mushroomRepository;
    this.userRequestRepository = userRequestRepository;
    this.imageRepository = imageRepository;
    this.jwtUtil = jwtUtil;
  }

  /**
   * Gets all the mushrooms
   * connected to a user by retrieving all mushrooms and images from the database.
   * It also generates a signed image url for each image.
   *
   * @param userRequestId id for the user
   * @return a list of MushroomDTOs 
   */
  public List<MushroomDTO> getAllMushrooms(String userRequestId) {
    Optional<UserRequest> userRequest = userRequestRepository.findByUserRequestId(userRequestId);
     List<Mushroom> mushrooms = mushroomRepository.findByUserRequest(userRequest);
     List<MushroomDTO> mushroomDTOS = new ArrayList<>();
     
     // Gets all images connected to a mushroom
    for (Mushroom mushroom : mushrooms) {
      List<Image> images = imageRepository.findAllByMushroom(mushroom);
      List<String> imageUrls = new ArrayList<>();
      
      // Generates a signed url for each image
      for (Image image: images) {
        imageUrls.add(jwtUtil.generateSignedImageUrl(userRequestId, image.getImageUrl()));
      }
      mushroomDTOS.add(MushroomMapper.fromEntityToDto(mushroom, imageUrls));
      imageUrls.clear();
    }
    
    return mushroomDTOS;
  }
}
