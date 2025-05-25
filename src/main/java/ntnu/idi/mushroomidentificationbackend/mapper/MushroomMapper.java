package ntnu.idi.mushroomidentificationbackend.mapper;

import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.response.MushroomDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Mushroom;

/**
 * Utility class for mapping Mushroom entities to MushroomDTOs.
 */
public class MushroomMapper {
  private MushroomMapper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Maps a Mushroom entity to a MushroomDTO.
   * This method converts the Mushroom entity to a DTO
   * which can be used in the response layer
   * to avoid exposing the entity directly.
   *
   * @param mushroom Mushroom entity to be converted to DTO.
   * @param images List of image URLs associated with the mushroom.
   * @return MushroomDTO containing the mushroom's details and image URLs.
   */
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
