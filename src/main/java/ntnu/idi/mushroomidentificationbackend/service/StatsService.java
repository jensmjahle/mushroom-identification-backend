package ntnu.idi.mushroomidentificationbackend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class StatsService {
  private final UserRequestRepository userRequestRepository;
  private final MushroomRepository mushroomRepository;
  private final StatisticsRepository statisticsRepository;



  public OverviewStatsDTO getCombinedStatistics() {
    // Current month stats, only for the current month
    LocalDate now = LocalDate.now();
    long currentMonthNewRequests = getMonthlyNewRequests(now);
    long currentMonthCompletedRequests = getMonthlyRequestsByStatus(UserRequestStatus.COMPLETED, now);

    // Historical stats, only for previous months, retrieved from the statistics table
    List<Statistics> allMonthlyStats = statisticsRepository.findAll();
    long previousNewRequests = allMonthlyStats.stream()
        .filter(stat -> !stat.getMonthYear().equals(getMonthKey(now)))
        .mapToLong(Statistics::getTotalNewRequests)
        .sum();
    long previousCompletedRequests = allMonthlyStats.stream()
        .filter(stat -> !stat.getMonthYear().equals(getMonthKey(now)))
        .mapToLong(Statistics::getTotalRequestsCompleted)
        .sum();
    long totalFtrClicks = getTotalFtrClicks(); // FTR clicks are only stored in the statistics table,
    // this includes the current month

    // Combine the stats
    long totalNewRequests = previousNewRequests + currentMonthNewRequests;
    long totalCompletedRequests = previousCompletedRequests + currentMonthCompletedRequests;

    //  Last 7 days count
    long requestsLastWeek = userRequestRepository.countByCreatedAtBetween(
        java.sql.Date.valueOf(LocalDate.now().minusDays(7)),
        java.sql.Date.valueOf(LocalDate.now())
    );
    
    return new OverviewStatsDTO(totalNewRequests, totalCompletedRequests, requestsLastWeek, totalFtrClicks);
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
  
  
  public long getMonthlyNewRequests(LocalDate date) {
    LocalDateTime start = date.withDayOfMonth(1).atStartOfDay();
    LocalDateTime end = start.plusMonths(1).minusSeconds(1);
    return userRequestRepository.countByCreatedAtBetween(
        java.sql.Date.valueOf(start.toLocalDate()),
        java.sql.Date.valueOf(end.toLocalDate())
    );
  }

  public long getMonthlyRequestsByStatus(UserRequestStatus status, LocalDate date) {
    LocalDateTime start = date.withDayOfMonth(1).atStartOfDay();
    LocalDateTime end = start.plusMonths(1).minusSeconds(1);
    return userRequestRepository.countByStatusAndCreatedAtBetween(
        status,
        java.sql.Date.valueOf(start.toLocalDate()),
        java.sql.Date.valueOf(end.toLocalDate())
    );
  }
  
  private String getMonthKey(LocalDate date) {
    return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
  }

}
