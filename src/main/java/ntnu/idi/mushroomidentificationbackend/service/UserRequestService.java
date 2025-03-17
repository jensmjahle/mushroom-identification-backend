package ntnu.idi.mushroomidentificationbackend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestWithMessagesDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestWithoutMessagesDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.mapper.UserRequestMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageType;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class UserRequestService {
    private final UserRequestRepository userRequestRepository;
    private final MessageRepository messageRepository;
    private final ImageService imageService;
    private final MessageService messageService;

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
            userRequest.setReferenceCode(generateReferenceCode());
            userRequest.setCreatedAt(new Date());
            userRequest.setUpdatedAt(new Date());
            userRequest.setStatus(UserRequestStatus.PENDING);
            UserRequest savedUserRequest = userRequestRepository.save(userRequest);
            
            // Create and save the text message
            Message messageText = new Message();
            messageText.setUserRequest(savedUserRequest);
            messageText.setCreatedAt(new Date());
            messageText.setContent(newUserRequestDTO.getText());
            messageText.setMessageType(MessageType.TEXT);
            messageText.setSenderType(MessageSenderType.USER);
            messageRepository.save(messageText);
            
            // Create and save the image messages
            List<Message> imageMessages = new ArrayList<>();
            if (newUserRequestDTO.getImages() != null) {
                for (MultipartFile image : newUserRequestDTO.getImages()) {
                    if (!image.isEmpty()) {
                     //   String savedFilePath = ImageService.saveImageLocally(image);
                        Message imageMessage = new Message();
                        imageMessage.setUserRequest(savedUserRequest);
                        imageMessage.setCreatedAt(new Date());
                       // imageMessage.setContent(savedFilePath);
                        imageMessage.setMessageType(MessageType.IMAGE);
                        imageMessage.setSenderType(MessageSenderType.USER);
                        imageMessages.add(imageMessage);
                    }
                }
                messageRepository.saveAll(imageMessages);
            }
            
            return savedUserRequest.getReferenceCode();
            
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
            if (userRequestRepository.findReferenceCodeByReferenceCode(referenceCode) == null) {
                return referenceCode;
            }
        }
    }

    /**
     * Retrieve a user request by reference code
     *
     * @param referenceCode the reference code of the user request
     * @return the user request answer DTO
     */
    public UserRequestWithMessagesDTO retrieveUserRequest(String referenceCode) {
        Optional<UserRequest> userRequestOpt = userRequestRepository.findByReferenceCode(referenceCode);
        if (userRequestOpt.isEmpty()) {
            throw new DatabaseOperationException("User request not found.");
        } else {
            UserRequest userRequest = userRequestOpt.get();
            List<Message> messages = messageService.getAllMessagesToUserRequest(userRequest);
            try {
                return UserRequestMapper.fromEntityToDto(userRequest, messages);
            } catch (Exception e) {
                throw new DatabaseOperationException("Failed to retrieve user request.");
            }
        }
    }
    
    public UserRequest getUserRequestByReferenceCode(String referenceCode) {
        Optional<UserRequest> userRequestOpt = userRequestRepository.findByReferenceCode(referenceCode);
        if (userRequestOpt.isEmpty()) {
            throw new RequestNotFoundException("User request not found.");
        } else {
            return userRequestOpt.get();
        }
    }
    
    public Page<UserRequestWithoutMessagesDTO> getPaginatedUserRequests(Pageable pageable) {
        return userRequestRepository.findAllByOrderByUpdatedAtDesc(pageable).map(UserRequestMapper::fromEntityToDto);
    }
}
