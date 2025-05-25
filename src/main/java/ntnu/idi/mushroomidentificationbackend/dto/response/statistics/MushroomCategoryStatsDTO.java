package ntnu.idi.mushroomidentificationbackend.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;

/**
 * Data Transfer Object (DTO) for Mushroom Category Statistics.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class MushroomCategoryStatsDTO {

    private MushroomStatus status;
    private int count;
}
