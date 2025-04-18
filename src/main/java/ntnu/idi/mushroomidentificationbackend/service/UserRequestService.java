package ntnu.idi.mushroomidentificationbackend.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.controller.UserRequestController;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangeRequestStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewMushroomDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.mapper.UserRequestMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Image;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.ImageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MushroomRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@AllArgsConstructor
public class UserRequestService {
    private final UserRequestRepository userRequestRepository;
    private final MessageRepository messageRepository;
    private final ImageService imageService;
    private final MessageService messageService;
    private static final Logger logger = Logger.getLogger(UserRequestService.class.getName());
    private final MushroomRepository mushroomRepository;
    private final ImageRepository imageRepository;

    /**
     * Takes a new user request DTO and processes it, saving the user request and messages.
     *
     * @param newUserRequestDTO the new user request DTO
     * @return the reference code of the new user request
     */
    public String processNewUserRequest(NewUserRequestDTO newUserRequestDTO) {
        try {
            // Create and save a new user request
            UserRequest userRequest = new UserRequest();
            userRequest.setCreatedAt(new Date());
            userRequest.setUpdatedAt(new Date());
            userRequest.setStatus(UserRequestStatus.NEW);
            logger.info("User request created");
            
            String referenceCode = generateReferenceCode();
            logger.info("Generated reference code: " + referenceCode);
            userRequest.setPasswordHash(hashReferenceCode(referenceCode));
            userRequest.setLookUpKey(hashReferenceCodeForLookup(referenceCode));
            UserRequest savedUserRequest = userRequestRepository.save(userRequest);
            logger.info("User request saved with reference code: " + referenceCode);
            
            // Create and save the text message
            Message messageText = new Message();
            messageText.setUserRequest(savedUserRequest);
            messageText.setCreatedAt(new Date());
            messageText.setContent(newUserRequestDTO.getText());
            messageText.setSenderType(MessageSenderType.USER);
            logger.info("text message created" + messageText.getContent() + newUserRequestDTO.getText());
            messageRepository.save(messageText);
            
            
            // Create and save the mushrooms
            List<Message> imageMessages = new ArrayList<>();
            if (newUserRequestDTO.getMushrooms() != null) {
                //Loops through each mushroom
                for (NewMushroomDTO newMushroomDTO : newUserRequestDTO.getMushrooms()) {
                    Mushroom mushroom = new Mushroom();
                    mushroom.setUserRequest(userRequest);
                    mushroom.setCreatedAt(new Date());
                    mushroom.setUpdatedAt(new Date());
                    mushroom.setMushroomStatus(MushroomStatus.NOT_PROCESSED);
                    Mushroom savedMushroom = mushroomRepository.save(mushroom);
                    
                    List<Image> images = new ArrayList<>();
                    //Loops through each image in each mushroom
                    for (MultipartFile image: newMushroomDTO.getImages()) {
                        if (!image.isEmpty()) {
                            logger.info("Image received");
                            String imageUrl = ImageService.saveImage(image, savedUserRequest.getUserRequestId(), savedMushroom.getMushroomId());
                            logger.info("Image saved: " + imageUrl);
                            Image image1 = new Image();
                            image1.setMushroom(savedMushroom);
                            image1.setImageUrl(imageUrl);
                            images.add(image1);
                        }
                    }
                    imageRepository.saveAll(images);
                }
            }
            
             
            return referenceCode;
            
        } catch (Exception e) {
            throw new DatabaseOperationException("Failed to save user request.");
        }
        
    }

    /**
     * Generate a unique reference code for the user request
     * Loop until a unique reference code is generated
     *
     * @return a unique reference code
     */
    public String generateReferenceCode() {
        while (true) {
            String referenceCode = UUID.randomUUID().toString();
            String passwordHash = hashReferenceCode(referenceCode);
            if (userRequestRepository.findByPasswordHash(passwordHash).isEmpty()) {
                return referenceCode;
            }
        }
    }

    public static String hashReferenceCode(String referenceCode) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      return encoder.encode(referenceCode);
    }

    /**
     * Hash the reference code for lookup using SHA-256 and a salt.
     * @param referenceCode the reference code to hash
     * @return the hashed reference code
     */
    public static String hashReferenceCodeForLookup(String referenceCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salt;
          
                try {
                    salt = System.getProperty("LOOKUP_SALT");
                } catch (NullPointerException e) {
                    logger.severe("LOOKUP_SALT not found in environment variables. Please set the LOOKUP_SALT variable. The fallback salt will be used, which is not secure. Should only be used for development.");
                    salt = "development-salt";
                }
            byte[] encodedHash = digest.digest((referenceCode + salt).getBytes());
            return Base64.getEncoder().encodeToString(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing reference code for lookup", e);
        }
    }
    
  
    public UserRequestDTO getUserRequestDTO(String userRequestId) {
        Optional<UserRequest> userRequestOpt = userRequestRepository.findByUserRequestId(userRequestId);
        if (userRequestOpt.isEmpty()) {
            throw new DatabaseOperationException("User request not found.");
        } else {
            UserRequest userRequest = userRequestOpt.get();
            try {
                return UserRequestMapper.fromEntityToDto(userRequest);
            } catch (Exception e) {
                throw new DatabaseOperationException("Failed to retrieve user request.");
            }
        }
    }
    
    public UserRequest getUserRequestByReferenceCode(String referenceCode) {
        String passwordHash = hashReferenceCode(referenceCode);
        Optional<UserRequest> userRequestOpt = userRequestRepository.findByPasswordHash(passwordHash);
        if (userRequestOpt.isEmpty()) {
            throw new RequestNotFoundException("User request not found.");
        } else {
            return userRequestOpt.get();
        }
    }
    
    public Page<UserRequestDTO> getPaginatedUserRequests(Pageable pageable) {
        return userRequestRepository.findAllByOrderByUpdatedAtDesc(pageable).map(UserRequestMapper::fromEntityToDto);
    }
    
    public UserRequest getUserRequest(String userRequestId) {
        Optional<UserRequest> userRequestOpt = userRequestRepository.findByUserRequestId(userRequestId);
        if (userRequestOpt.isEmpty()) {
            throw new RequestNotFoundException("User request not found.");
        } else {
            return userRequestOpt.get();
        }
    }

    /**
     * Change the status of a user request
     *
     * @param changeRequestStatusDTO the DTO containing the user request ID and the new status
     */
    public void changeRequestStatus(ChangeRequestStatusDTO changeRequestStatusDTO) {
        UserRequest userRequest = getUserRequest(changeRequestStatusDTO.getUserRequestId());
        userRequest.setStatus(changeRequestStatusDTO.getNewStatus());
        userRequestRepository.save(userRequest);
    }

    public Long getNumberOfRequests(UserRequestStatus status) {
        try {
            return userRequestRepository.countByStatus(status);
        } catch (Exception e) {
            throw new DatabaseOperationException("Failed to retrieve number of requests.");
        }
     
    }
}
