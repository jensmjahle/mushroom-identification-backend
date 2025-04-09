package ntnu.idi.mushroomidentificationbackend.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OverviewStatsDTO {
    private int totalRequests;
    private int totalCompleted;
    private int weeklyRate;
    private int ftrClicks;
    
}
