package ntnu.idi.mushroomidentificationbackend.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangeRequestStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewMushroomDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.mapper.UserRequestMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.entity.Image;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.BasketBadgeType;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.ImageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MushroomRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import ntnu.idi.mushroomidentificationbackend.security.ReferenceCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    private final MushroomService mushroomService;
    private final AdminService adminService;
    private static final Logger logger = Logger.getLogger(UserRequestService.class.getName());
    private final MushroomRepository mushroomRepository;
    private final ImageRepository imageRepository;
    private final ReferenceCodeGenerator referenceCodeGenerator;
    

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
            String referenceCode = referenceCodeGenerator.generateCode();
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
        UserRequest userRequest = getUserRequest(userRequestId);
        long count = mushroomRepository.countByUserRequest(userRequest);
        List<BasketBadgeType> badges = mushroomService.getBasketSummaryBadges(userRequestId);
        return UserRequestMapper.fromEntityToDto(userRequest, badges, count);
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
        return userRequestRepository.findAllByOrderByUpdatedAtDesc(pageable)
            .map(req -> {
                long count = mushroomRepository.countByUserRequest(req);
                List<BasketBadgeType> badges = mushroomService.getBasketSummaryBadges(req.getUserRequestId());
                return UserRequestMapper.fromEntityToDto(req, badges, count);
            });
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

    /**
     * Update the status of a user request after a message is sent.
     * If the sender is a user and the status is PENDING, set it to NEW.
     * Otherwise, do nothing.
     *
     * @param userRequestId the ID of the user request
     * @param senderType the type of sender (user or admin)
     */
  public void updateProjectAfterMessage(String userRequestId, MessageSenderType senderType) {
        UserRequest userRequest = getUserRequest(userRequestId);
        
        if (userRequest.getStatus() == UserRequestStatus.COMPLETED) {
            throw new DatabaseOperationException("Cannot update a completed user request");
        }
        userRequest.setUpdatedAt(new Date());
        // If the sender is a user and the status is PENDING, set it to NEW
        if (senderType == MessageSenderType.USER && userRequest.getStatus() == UserRequestStatus.PENDING) {
            userRequest.setStatus(UserRequestStatus.NEW);
        } 
        userRequestRepository.save(userRequest);
  }

    public Page<UserRequestDTO> getPaginatedRequestsByStatus(UserRequestStatus status, Pageable pageable) {
        return userRequestRepository.findAllByStatus(status, pageable)
            .map(userRequest -> {
                long count = mushroomRepository.countByUserRequest(userRequest);
                List<BasketBadgeType> badges = mushroomService.getBasketSummaryBadges(userRequest.getUserRequestId());
                return UserRequestMapper.fromEntityToDto(userRequest, badges, count);
            });
    }

    public Page<UserRequestDTO> getPaginatedRequestsExcludingStatus(UserRequestStatus status, Pageable pageable) {
        return userRequestRepository.findAllByStatusNot(status, pageable)
            .map(userRequest -> {
                long count = mushroomRepository.countByUserRequest(userRequest);
                List<BasketBadgeType> badges = mushroomService.getBasketSummaryBadges(userRequest.getUserRequestId());
                return UserRequestMapper.fromEntityToDto(userRequest, badges, count);
            });
    }


    /**
     * Get the next request from the queue. Fetches the first user request with status NEW, ordered by
     * updatedAt in ascending order.
     *
     * @return the next user request in the queue, or throws an exception if none found
     */
    public ResponseEntity<Object> getNextRequestFromQueue() {
        Optional<UserRequest> userRequestOpt =
            userRequestRepository.findFirstByStatusAndAdminIsNullOrderByUpdatedAtAsc(UserRequestStatus.NEW);
        return userRequestOpt.<ResponseEntity<Object>>map(userRequest -> {
            long count = mushroomRepository.countByUserRequest(userRequest);
            List<BasketBadgeType> badges = mushroomService.getBasketSummaryBadges(userRequest.getUserRequestId());
            UserRequestDTO dto = UserRequestMapper.fromEntityToDto(userRequest, badges, count);
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.noContent().build());
    }


    public void updateRequest(String userRequestId) {
        UserRequest userRequest = getUserRequest(userRequestId);
        userRequest.setUpdatedAt(new Date());
        userRequestRepository.save(userRequest);
    }

    public void tryLockRequest(String userRequestId, String username) {
        UserRequest userRequest = getUserRequest(userRequestId);
        Admin optAdmin = adminService.getAdmin(username);
        Admin lockedBy = userRequest.getAdmin();
        
        if (lockedBy != null && !lockedBy.getUsername().equals(username)) {
            throw new DatabaseOperationException("Request is already locked by another admin.");
        }
        userRequest.setAdmin(optAdmin);
       // userRequest.setStatus(UserRequestStatus.IN_PROGRESS);
        logger.info("User request locked by admin: " + username);
        userRequestRepository.save(userRequest);
    }

    public void releaseRequestIfLockedByAdmin(String userRequestId) {
        UserRequest userRequest = getUserRequest(userRequestId);
        Admin lockedBy = userRequest.getAdmin();
        if (lockedBy != null) {
            userRequest.setAdmin(null);
            userRequestRepository.save(userRequest);
        }
    }
}
