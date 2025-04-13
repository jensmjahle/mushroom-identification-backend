package ntnu.idi.mushroomidentificationbackend.dto.response.statistics;

import java.util.List;
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
public class RequestsStatsRateDTO {
  
    private String timeInterval;
    private String from;
    private String to;
    private List<DataPoint> points;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class DataPoint {
      private String date;
      private int count;
    }
  }


