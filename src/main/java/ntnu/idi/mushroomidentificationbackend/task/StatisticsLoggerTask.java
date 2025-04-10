package ntnu.idi.mushroomidentificationbackend.task;

import java.time.LocalDate;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.model.entity.Statistics;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.StatisticsRepository;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class StatisticsLoggerTask {
  private final StatsService statsService;
  private final StatisticsRepository statisticsRepository;
  private final Logger logger = Logger.getLogger(StatisticsLoggerTask.class.getName());

  @Scheduled(cron = "0 0 0 1 * ?") // Runs at midnight on the 1st of every month
  public void storePreviousMonthStatistics() {
    LocalDate now = LocalDate.now();
    LocalDate previousMonth = now.minusMonths(1);
    String monthKey = previousMonth.getYear() + "-" + String.format("%02d", previousMonth.getMonthValue());

    long newRequests = statsService.getMonthlyNewRequests(previousMonth);
    long completedRequests = statsService.getMonthlyRequestsByStatus(UserRequestStatus.COMPLETED, previousMonth);
    long psilocybin = statsService.getMonthlyPsilocybinIdentified(previousMonth);
    long nonPsilocybin = statsService.getMonthlyNonPsilocybinIdentified(previousMonth);
    long toxic = statsService.getMonthlyToxicIdentified(previousMonth);
    long unknown = statsService.getMonthlyUnknownIdentified(previousMonth);
    long unidentifiable = statsService.getMonthlyUnidentifiableIdentified(previousMonth);
    long existingFtrClicks = statisticsRepository.findById(monthKey)
        .map(Statistics::getFtrClicks)
        .orElse(0L);

    Statistics stats = new Statistics(
        monthKey,
        newRequests,
        completedRequests,
        existingFtrClicks, 
        psilocybin,
        nonPsilocybin,
        toxic,
        unknown,
        unidentifiable
    );

    statisticsRepository.save(stats);
    logger.info("Stored statistics for month: " + monthKey);
  }

}
