package ntnu.idi.mushroomidentificationbackend.service;

import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.MushroomCategoryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.OverviewStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.RequestsStatsRateDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.SummaryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;
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
    return null;
  }

  public SummaryStatsDTO getSummaryStats() {
    return null;
  }

  public List<MushroomCategoryStatsDTO> getMushroomCategoryStats() {
    List<Object[]> results = mushroomRepository.countMushroomsByStatus();
    return results.stream()
        .map(result -> new MushroomCategoryStatsDTO((MushroomStatus) result[0], ((Long) result[1]).intValue()))
        .toList();
  }

  public OverviewStatsDTO getOverviewStats() {
  return null;
  }
}
