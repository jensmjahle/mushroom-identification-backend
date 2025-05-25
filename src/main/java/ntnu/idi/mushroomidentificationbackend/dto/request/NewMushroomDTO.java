package ntnu.idi.mushroomidentificationbackend.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object (DTO) for creating a new mushroom.
 * This DTO is used to encapsulate the list of images
 * associated with a new mushroom submission.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewMushroomDTO {
  private List<MultipartFile> images;
}
