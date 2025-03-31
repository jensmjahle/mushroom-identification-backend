package ntnu.idi.mushroomidentificationbackend.mapper;

import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.response.MushroomDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;

public class MushroomMapper {
  private MushroomMapper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
  
  public static MushroomDTO fromEntityToDto(Mushroom mushroom, List<String> images) {
    MushroomDTO mushroomDTO = new MushroomDTO();
    mushroomDTO.setMushroomId(mushroom.getMushroomId());
    mushroomDTO.setCreatedAt(mushroom.getCreatedAt());
    mushroomDTO.setUpdatedAt(mushroom.getUpdatedAt());
    mushroomDTO.setMushroomStatus(mushroom.getMushroomStatus());
    mushroomDTO.setImageUrls(images);
    return mushroomDTO;
  }
}
