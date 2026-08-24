package ntnu.idi.mushroomidentificationbackend.service;

import jakarta.persistence.EntityNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.ChangeRequestStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewMushroomDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestLockedException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
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
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestSpecifications;
import ntnu.idi.mushroomidentificationbackend.security.ReferenceCodeGenerator;
import ntnu.idi.mushroomidentificationbackend.security.SecretsConfig;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service class for handling user requests.
 * This class provides methods to process new user requests, generate reference codes,
 * hash reference codes, and manage user request statuses.
 */
@Service
@AllArgsConstructor
public class UserRequestService {
    private final UserRequestRepository userRequestRepository;
    private final MessageRepository messageRepository;
    private final MushroomService mushroomService;
    private final AdminService adminService;
    private static final Logger logger = Logger.getLogger(UserRequestService.class.getName());
    private final MushroomRepository mushroomRepository;
    private final ImageRepository imageRepository;
    private final ReferenceCodeGenerator referenceCodeGenerator;
    private final SessionRegistry sessionRegistry;
    private final SecretsConfig secretsConfig;

    
    /**
     * Takes a new user request DTO and processes it, saving the user request and messages.
     *
     * @param newUserRequestDTO the new user request DTO
     * @return the reference code of the new user request
     */
    public String processNewUserRequest(NewUserRequestDTO newUserRequestDTO) {
        try {
            UserRequest userRequest = new UserRequest();
            userRequest.setCreatedAt(new Date());
            userRequest.setUpdatedAt(new Date());
            userRequest.setStatus(UserRequestStatus.NEW);
            
            String referenceCode = generateReferenceCode();
            userRequest.setPasswordHash(hashReferenceCode(referenceCode));
            userRequest.setLookUpKey(hashReferenceCodeForLookup(referenceCode));
            UserRequest savedUserRequest = userRequestRepository.save(userRequest);
            
            // Create and save the text message
            Message messageText = new Message();
            messageText.setUserRequest(savedUserRequest);
            messageText.setCreatedAt(new Date());
            messageText.setContent(newUserRequestDTO.getText());
            messageText.setSenderType(MessageSenderType.USER);
            messageRepository.save(messageText);
            
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
                            String imageUrl = ImageService.saveImage(image, savedUserRequest.getUserRequestId(), savedMushroom.getMushroomId());
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
            LogHelper.severe(logger, "Error processing new user request: {0}", e.getMessage());
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

    /**
     * Hash the reference code using BCrypt.
     *
     * @param referenceCode the reference code to hash
     * @return the hashed reference code
     */
    public static String hashReferenceCode(String referenceCode) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      return encoder.encode(referenceCode);
    }

    /**
     * Hash the reference code for lookup using SHA-256 and a salt.
     * This method is used to create a unique lookup key for the user request.
     *
     * @param referenceCode the reference code to hash
     * @return the hashed reference code
     */
    public String hashReferenceCodeForLookup(String referenceCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salt = secretsConfig.getLookupSalt();

            if (salt == null || salt.isBlank()) {
                logger.severe("LOOKUP_SALT is missing. Please provide it in the environment.");
                salt = "development-salt";
            }

            byte[] encodedHash = digest.digest((referenceCode + salt).getBytes());
            return Base64.getEncoder().encodeToString(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            LogHelper.severe(logger, "SHA-256 algorithm not found: {0}", e.getMessage());
            throw new EntityNotFoundException("SHA-256 algorithm not found. Please check your environment.");
        }
    }

    /**
     * Get a UserRequestDTO for a specific user request ID.
     *
     * @param userRequestId the ID of the user request to retrieve
     * @return the UserRequestDTO containing the user request details, badges, and mushroom count
     */
    public UserRequestDTO getUserRequestDTO(String userRequestId) {
        UserRequest userRequest = getUserRequest(userRequestId);
        long count = mushroomRepository.countByUserRequest(userRequest);
        List<BasketBadgeType> badges = mushroomService.getBasketSummaryBadges(userRequestId);
        Map<MushroomStatus, Long> statusCounts = mushroomService.getMushroomStatusCounts(userRequestId);
        return UserRequestMapper.fromEntityToDto(userRequest, badges, count, statusCounts);
    }

    /**
     * Get a filtered, sorted list of user requests for the admin request tables.
     *
     * @param status the status to filter by, or null to not filter by status
     * @param exclude if true, excludes requests with the given status instead of requiring it;
     *                ignored if status is null
     * @param from the start of the createdAt range to filter by (inclusive), or null to not filter by date
     * @param to the end of the createdAt range to filter by (inclusive), or null to not filter by date
     * @param pageable the pagination and sort information; an unpaged Pageable returns every match in one page
     * @return a page of UserRequestDTO objects matching the given filters
     */
    public Page<UserRequestDTO> getFilteredUserRequests(UserRequestStatus status, boolean exclude,
        Date from, Date to, Pageable pageable) {
        Specification<UserRequest> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(exclude
                ? UserRequestSpecifications.statusIsNot(status)
                : UserRequestSpecifications.statusIs(status));
        }
        if (from != null && to != null) {
            spec = spec.and(UserRequestSpecifications.createdBetween(from, to));
        }

        return userRequestRepository.findAll(spec, pageable)
            .map(req -> {
                long count = mushroomRepository.countByUserRequest(req);
                List<BasketBadgeType> badges = mushroomService.getBasketSummaryBadges(req.getUserRequestId());
                Map<MushroomStatus, Long> statusCounts = mushroomService.getMushroomStatusCounts(req.getUserRequestId());
                return UserRequestMapper.fromEntityToDto(req, badges, count, statusCounts);
            });
    }

    /**
     * Get a user request by its ID.
     *
     * @param userRequestId the ID of the user request to retrieve
     * @return the UserRequest object if found
     */
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

    /**
     * Get the number of requests with a specific status.
     *
     * @param status the status of the user requests to count
     * @return the number of user requests with the specified status
     */
    public Long getNumberOfRequests(UserRequestStatus status) {
        try {
            return userRequestRepository.countByStatus(status);
        } catch (Exception e) {
            throw new DatabaseOperationException("Failed to retrieve number of requests.");
        }
     
    }

    /**
     * Update a user request after a message is sent.
     * If the sender is a user and the status is PENDING, set it to NEW.
     * If the request is already COMPLETED, it stays completed - instead this
     * tracks whether the user is waiting on a reply from the owning admin via
     * hasFollowUp, set on a user message and cleared on an admin reply.
     *
     * @param userRequestId the ID of the user request
     * @param senderType the type of sender (user or admin)
     */
  public void updateProjectAfterMessage(String userRequestId, MessageSenderType senderType) {
        UserRequest userRequest = getUserRequest(userRequestId);
        userRequest.setUpdatedAt(new Date());

        if (userRequest.getStatus() == UserRequestStatus.COMPLETED) {
            userRequest.setHasFollowUp(senderType == MessageSenderType.USER);
            userRequestRepository.save(userRequest);
            return;
        }

        // If the sender is a user and the status is PENDING, set it to NEW
        if (senderType == MessageSenderType.USER && userRequest.getStatus() == UserRequestStatus.PENDING) {
            userRequest.setStatus(UserRequestStatus.NEW);
        }
        userRequestRepository.save(userRequest);
  }

    /**
     * Get the next request from the queue. Fetches the first user request with status NEW, ordered by
     * updatedAt in ascending order.
     *
     * @return the next user request in the queue, or throws an exception if none found
     */
    public UserRequestDTO getNextRequestFromQueue() {
        Optional<UserRequest> userRequestOpt =
            userRequestRepository.findFirstByStatusAndAdminIsNullOrderByUpdatedAtAsc(UserRequestStatus.NEW);

        return userRequestOpt.map(userRequest -> {
            long count = mushroomRepository.countByUserRequest(userRequest);
            List<BasketBadgeType> badges = mushroomService.getBasketSummaryBadges(userRequest.getUserRequestId());
            Map<MushroomStatus, Long> statusCounts = mushroomService.getMushroomStatusCounts(userRequest.getUserRequestId());
            return UserRequestMapper.fromEntityToDto(userRequest, badges, count, statusCounts);
        }).orElse(null);
    }


    /**
     * Update the UpdatedAt variable for a user request with the given ID.
     *
     * @param userRequestId the ID of the user request to update
     */
    public void updateRequest(String userRequestId) {
        UserRequest userRequest = getUserRequest(userRequestId);
        userRequest.setUpdatedAt(new Date());
        userRequestRepository.save(userRequest);
    }

    /**
     * Try to lock a user request for processing by an admin.
     *
     * @param userRequestId the ID of the user request to lock
     * @param username the username of the admin trying to lock the request
     */
    public void tryLockRequest(String userRequestId, String username) {
        UserRequest userRequest = getUserRequest(userRequestId);
        Admin optAdmin = adminService.getAdmin(username);
        Admin lockedBy = userRequest.getAdmin();
        
        if (lockedBy != null && !lockedBy.getUsername().equals(username)) {
            throw new RequestLockedException("Request is already locked by another admin.");
        }
        userRequest.setAdmin(optAdmin);
        userRequest.setStatus(UserRequestStatus.IN_PROGRESS);
        sessionRegistry.promoteToRequestOwner(userRequestId, username);
        userRequestRepository.save(userRequest);
        
    }

    /**
     * Release the lock on a user request if it is locked by the specified admin.
     *
     * @param userRequestId the ID of the user request to release
     * @param username the username of the admin releasing the lock
     */
    public void releaseRequestIfLockedByAdmin(String userRequestId, String username) {
        if (!userRequestRepository.existsById(userRequestId)) {
            LogHelper.warning(logger, "Attempted to release lock for non-existent request: {0}", userRequestId);
            return;
        }
        UserRequest userRequest = getUserRequest(userRequestId);
        Admin lockedBy = userRequest.getAdmin();
        if (lockedBy != null && lockedBy.getUsername().equals(username)) {
            userRequest.setAdmin(null);
            if (userRequest.getStatus() == UserRequestStatus.IN_PROGRESS) {
                userRequest.setStatus(UserRequestStatus.NEW);
            }
            userRequestRepository.save(userRequest);
        }
    }

    /**
     * Forcibly releases a user request from whichever admin currently owns it and puts it
     * back into the queue, regardless of who owns it. Unlike {@link #releaseRequestIfLockedByAdmin},
     * this isn't gated on the caller being the current owner - it's a superuser-only escape hatch
     * for reclaiming a request whose assigned moderator has stopped responding.
     *
     * @param userRequestId the ID of the user request to release
     * @throws RequestNotFoundException if no such request exists
     */
    public void forceReleaseRequest(String userRequestId) {
        UserRequest userRequest = getUserRequest(userRequestId);
        userRequest.setAdmin(null);
        if (userRequest.getStatus() == UserRequestStatus.IN_PROGRESS) {
            userRequest.setStatus(UserRequestStatus.NEW);
        }
        userRequestRepository.save(userRequest);
    }
}
