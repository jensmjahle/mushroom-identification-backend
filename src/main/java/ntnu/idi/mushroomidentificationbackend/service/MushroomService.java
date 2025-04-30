package ntnu.idi.mushroomidentificationbackend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import ntnu.idi.mushroomidentificationbackend.dto.request.AddImagesToMushroomDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MushroomDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.ImageProcessingException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.mapper.MushroomMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Image;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.BasketBadgeType;
import ntnu.idi.mushroomidentificationbackend.repository.ImageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MushroomRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        imageUrls.add(jwtUtil.generateSignedImageUrl(userRequestId, mushroom.getMushroomId(),image.getImageUrl()));
      }
      mushroomDTOS.add(MushroomMapper.fromEntityToDto(mushroom, imageUrls));
    }
    
    return mushroomDTOS;
  }

  public List<BasketBadgeType> getBasketSummaryBadges(String userRequestId) {
    Optional<UserRequest> userRequestOpt = userRequestRepository.findByUserRequestId(userRequestId);
    if (userRequestOpt.isEmpty()) return List.of();

    List<Mushroom> mushrooms = mushroomRepository.findByUserRequest(userRequestOpt);
    List<BasketBadgeType> badges = new ArrayList<>();

    int total = mushrooms.size();
    int unprocessed = 0;
    int toxic = 0;
    int psilocybin = 0;
    int nonPsilocybin = 0;
    int unknown = 0;
    int unidentifiable = 0;

    for (Mushroom mushroom : mushrooms) {
      switch (mushroom.getMushroomStatus()) {
        case NOT_PROCESSED -> unprocessed++;
        case TOXIC -> toxic++;
        case PSILOCYBIN -> psilocybin++;
        case NON_PSILOCYBIN -> nonPsilocybin++;
        case UNKNOWN -> unknown++;
        case UNIDENTIFIABLE -> unidentifiable++;
      }
    }

    // Add badge based on presence
    if (toxic > 0) badges.add(BasketBadgeType.TOXIC_MUSHROOM_PRESENT);
    if (psilocybin > 0) badges.add(BasketBadgeType.PSYCHOACTIVE_MUSHROOM_PRESENT);
    if (nonPsilocybin > 0) badges.add(BasketBadgeType.NON_PSILOCYBIN_MUSHROOM_PRESENT);
    if (unknown > 0) badges.add(BasketBadgeType.UNKNOWN_MUSHROOM_PRESENT);
    if (unidentifiable > 0) badges.add(BasketBadgeType.UNIDENTIFIABLE_MUSHROOM_PRESENT);

    // Add absolute state badges
    if (total == 0 || unprocessed == total) {
      badges.add(BasketBadgeType.NO_MUSHROOMS_PROCESSED);
    } else if (unprocessed == 0) {
      badges.add(BasketBadgeType.ALL_MUSHROOMS_PROCESSED);
    }

    if (total > 0) {
      if (toxic == total) badges.add(BasketBadgeType.ALL_MUSHROOMS_ARE_TOXIC);
      if (psilocybin == total) badges.add(BasketBadgeType.ALL_MUSHROOMS_ARE_PSILOCYBIN);
      if (nonPsilocybin == total) badges.add(BasketBadgeType.ALL_MUSHROOMS_ARE_NON_PSILOCYBIN);
      if (unknown == total) badges.add(BasketBadgeType.ALL_MUSHROOMS_ARE_UNKNOWN);
      if (unidentifiable == total) badges.add(BasketBadgeType.ALL_MUSHROOMS_ARE_UNIDENTIFIABLE);
    }
    
    return badges;
  }

  /**
   * Adds images to a mushroom. 
   * Saves the images locally and adds the image URLs to the mushroom entity.
   *
   * @param userRequestId id for the user request that the mushroom is connected to
   * @param addImageToMushroomDTO images to be added to the mushroom
   */
  public void addImagesToMushroom(String userRequestId, AddImagesToMushroomDTO addImageToMushroomDTO) {
    Optional<UserRequest> userRequestOpt = userRequestRepository.findByUserRequestId(userRequestId);
    if (userRequestOpt.isEmpty()) {
      throw new RequestNotFoundException("User request not found.");
    }

    Optional<Mushroom> mushroomOpt = mushroomRepository.findById(addImageToMushroomDTO.getMushroomId());
    if (mushroomOpt.isEmpty()) {
      throw new DatabaseOperationException("Mushroom not found.");
    }

    Mushroom mushroom = mushroomOpt.get();

    if (!mushroom.getUserRequest().getUserRequestId().equals(userRequestId)) {
      throw new DatabaseOperationException("Mushroom does not belong to the specified user request.");
    }

    List<MultipartFile> images = addImageToMushroomDTO.getImages();
    List<Image> imageEntities = new ArrayList<>();

    for (MultipartFile image : images) {
      try {
        String imageUrl = ImageService.saveImage(image, userRequestId, mushroom.getMushroomId());
        Image imageEntity = new Image();
        imageEntity.setMushroom(mushroom);
        imageEntity.setImageUrl(imageUrl);
        imageEntities.add(imageEntity);
      } catch (IOException e) {
        throw new ImageProcessingException("Failed to save image: " + e.getMessage());
      }
    }

    imageRepository.saveAll(imageEntities);
    mushroom.setUpdatedAt(new Date());
    mushroomRepository.save(mushroom);
  }

}
