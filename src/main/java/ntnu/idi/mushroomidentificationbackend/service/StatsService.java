package ntnu.idi.mushroomidentificationbackend.service;

import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.MushroomCategoryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.OverviewStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.RequestsStatsRateDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.SummaryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.repository.MushroomRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
  private final UserRequestRepository userRequestRepository;
  private final MushroomRepository mushroomRepository;
  

  public StatsService(UserRequestRepository userRequestRepository,
      MushroomRepository mushroomRepository) {
    this.userRequestRepository = userRequestRepository;
    this.mushroomRepository = mushroomRepository;
  }
  

  public RequestsStatsRateDTO getRequestsStatsRate(String from, String to, String interval) {
    
  }

  public SummaryStatsDTO getSummaryStats() {
  }

  public List<MushroomCategoryStatsDTO> getMushroomCategoryStats() {
  }

  public OverviewStatsDTO getOverviewStats() {
  }
}
