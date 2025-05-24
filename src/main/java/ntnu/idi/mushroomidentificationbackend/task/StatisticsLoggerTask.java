package ntnu.idi.mushroomidentificationbackend.task;

import java.time.LocalDate;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.model.entity.Statistics;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.StatisticsRepository;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that collects and stores statistics for the previous month.
 * This task runs at midnight on the first day of each month.
 */
@AllArgsConstructor
@Component
public class StatisticsLoggerTask {
  private final StatsService statsService;
  private final StatisticsRepository statisticsRepository;
  private static final Logger logger = Logger.getLogger(StatisticsLoggerTask.class.getName());

  /**
   * Scheduled task that runs at midnight on the first day of each month.
   * This task collects statistics for the previous month and stores them in the database in the statistics table.
   */
  @Scheduled(cron = "0 0 0 1 * ?")
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
    LogHelper.info(logger, "Stored statistics for month: {0}", monthKey);
  }

}
