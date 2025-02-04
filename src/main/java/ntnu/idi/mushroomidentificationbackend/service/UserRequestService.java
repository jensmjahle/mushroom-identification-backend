package ntnu.idi.mushroomidentificationbackend.service;

import java.util.UUID;
import ntnu.idi.mushroomidentificationbackend.model.dto.NewUserRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class UserRequestService {
    public String processNewUserRequest(NewUserRequestDTO newUserRequestDTO) {
        return UUID.randomUUID().toString();
    }

}
