package ntnu.idi.mushroomidentificationbackend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageType;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class UserRequestService {
    private final UserRequestRepository userRequestRepository;
    private final MessageRepository messageRepository;

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
                        String savedFilePath = ImageService.saveImageLocally(image);
                        Message imageMessage = new Message();
                        imageMessage.setUserRequest(savedUserRequest);
                        imageMessage.setCreatedAt(new Date());
                        imageMessage.setContent(savedFilePath);
                        imageMessage.setMessageType(MessageType.);
                        imageMessage.setSenderType(MessageSenderType.USER);
                        imageMessages.add(imageMessage);
                    }
                }
                messageRepository.saveAll(imageMessages);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
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

}
