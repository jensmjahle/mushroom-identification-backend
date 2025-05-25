package ntnu.idi.mushroomidentificationbackend.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object (DTO) for adding images to a mushroom.
 * This DTO is used to encapsulate the mushroom ID and the list of images
 * to be added to the mushroom's record in the system.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AddImagesToMushroomDTO {
  private String mushroomId;
  private List<MultipartFile> images;

}
