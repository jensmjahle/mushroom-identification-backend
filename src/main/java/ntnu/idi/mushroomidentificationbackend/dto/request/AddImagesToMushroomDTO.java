package ntnu.idi.mushroomidentificationbackend.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AddImagesToMushroomDTO {
  private String mushroomId;
  private List<MultipartFile> images;

}
