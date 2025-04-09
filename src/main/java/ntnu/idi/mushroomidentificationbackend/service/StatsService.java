package ntnu.idi.mushroomidentificationbackend.service;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.MushroomCategoryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.OverviewStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.RequestsStatsRateDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.SummaryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;
import ntnu.idi.mushroomidentificationbackend.repository.MushroomRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
    // Parse the input dates
    LocalDateTime fromDate = LocalDateTime.parse(from);
    LocalDateTime toDate = LocalDateTime.parse(to);

    // Fetch user requests within the date range
    List<UserRequest> userRequests = userRequestRepository.findByCreatedAtBetween(
        java.sql.Date.valueOf(fromDate.toLocalDate()),
        java.sql.Date.valueOf(toDate.toLocalDate())
    );

    // Group requests by the specified interval
    Map<String, Long> groupedRequests = userRequests.stream()
        .collect(Collectors.groupingBy(request -> {
          LocalDateTime createdAt = request.getCreatedAt().toInstant()
              .atZone(ZoneId.systemDefault())
              .toLocalDateTime();
          if ("DAY".equalsIgnoreCase(interval)) {
            return createdAt.toLocalDate().toString();
          } else if ("WEEK".equalsIgnoreCase(interval)) {
            return createdAt.getYear() + "-W" + createdAt.get(WeekFields.ISO.weekOfYear()); 
          } else if ("MONTH".equalsIgnoreCase(interval)) {
            return createdAt.getYear() + "-" + createdAt.getMonthValue(); 
          } else {
            throw new IllegalArgumentException("Invalid interval: " + interval);
          }
        }, Collectors.counting()));

    // Map the grouped data to DataPoint objects
    List<RequestsStatsRateDTO.DataPoint> points = groupedRequests.entrySet().stream()
        .map(entry -> new RequestsStatsRateDTO.DataPoint(entry.getKey(), entry.getValue().intValue()))
        .toList();
    
    return new RequestsStatsRateDTO(interval, from, to, points);
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
