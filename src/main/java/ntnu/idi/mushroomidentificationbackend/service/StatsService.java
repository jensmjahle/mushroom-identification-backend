package ntnu.idi.mushroomidentificationbackend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.MushroomCategoryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.OverviewStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.RequestsStatsRateDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Statistics;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.MushroomRepository;
import ntnu.idi.mushroomidentificationbackend.repository.StatisticsRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.springframework.stereotype.Service;
import java.time.ZoneId;


@Service
public class StatsService {
  private final UserRequestRepository userRequestRepository;
  private final MushroomRepository mushroomRepository;
  private final StatisticsRepository statisticsRepository;
  

  public StatsService(UserRequestRepository userRequestRepository,
      MushroomRepository mushroomRepository, StatisticsRepository statisticsRepository) {
    this.userRequestRepository = userRequestRepository;
    this.mushroomRepository = mushroomRepository;
    this.statisticsRepository = statisticsRepository;
  }

  public OverviewStatsDTO getCombinedStatistics() {
    // Retrieve statistics for the current month
    long currentMonthNewRequests = getCurrentMonthNewRequests();
    long currentMonthCompletedRequests = getCurrentMonthRequestsByStatus(UserRequestStatus.COMPLETED);
    
    // Retrieve statistics for all months previously recorded
    List<Statistics> allMonthlyStats = statisticsRepository.findAll();
    long cumulativeNewRequests = allMonthlyStats.stream()
        .mapToLong(Statistics::getTotalNewRequests)
        .sum();
    long cumulativeCompletedRequests = allMonthlyStats.stream()
        .mapToLong(Statistics::getTotalRequestsCompleted)
        .sum();
   
    long totalNewRequests = cumulativeNewRequests + currentMonthNewRequests;
    long totalCompletedRequests = cumulativeCompletedRequests + currentMonthCompletedRequests;
    long requestsInLastWeek = userRequestRepository.countByCreatedAtBetween(java.sql.Date.valueOf(LocalDate.now().minusDays(7)), java.sql.Date.valueOf(LocalDate.now()));
    long totalFtrClicks = getTotalFtrClicks();

    // Create and return the combined statistics DTO
    return new OverviewStatsDTO(totalNewRequests, totalCompletedRequests, requestsInLastWeek, totalFtrClicks);
  }
  public long getCurrentMonthNewRequests() {
    LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

    return userRequestRepository.countByCreatedAtBetween(
        java.sql.Date.valueOf(startOfMonth.toLocalDate()),
        java.sql.Date.valueOf(endOfMonth.toLocalDate())
    );
  }
  public long getCurrentMonthRequestsByStatus(UserRequestStatus status) {
    LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

    return userRequestRepository.countByStatusAndCreatedAtBetween(
        status,
        java.sql.Date.valueOf(startOfMonth.toLocalDate()),
        java.sql.Date.valueOf(endOfMonth.toLocalDate())
    );
  }
  public long getTotalFtrClicks() {
    if (statisticsRepository.countTotalFtrClicks() == null) {
      return 0;
    }
    return statisticsRepository.countTotalFtrClicks();
  }
  public RequestsStatsRateDTO getRequestsStatsRate(String from, String to, String interval) {
    LocalDateTime fromDate = LocalDate.parse(from).atStartOfDay();
    LocalDateTime toDate = LocalDate.parse(to).atStartOfDay();
    
    List<UserRequest> userRequests = userRequestRepository.findByCreatedAtBetween(
        java.sql.Date.valueOf(fromDate.toLocalDate()),
        java.sql.Date.valueOf(toDate.toLocalDate())
    );
    
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
  
  
  public List<MushroomCategoryStatsDTO> getMushroomCategoryStats() {
    List<Object[]> results = mushroomRepository.countMushroomsByStatus();
    return results.stream()
        .map(result -> new MushroomCategoryStatsDTO((MushroomStatus) result[0], ((Long) result[1]).intValue()))
        .toList();
  }

  public OverviewStatsDTO getOverviewStats() {
    long totalRequests = userRequestRepository.count();
    long totalCompleted = userRequestRepository.countByStatus(UserRequestStatus.COMPLETED);
    
   
    
    
  return null;
  }
}
