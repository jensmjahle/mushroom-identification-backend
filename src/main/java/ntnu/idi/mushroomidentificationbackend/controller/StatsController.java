package ntnu.idi.mushroomidentificationbackend.controller;

import java.util.List;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.MushroomCategoryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.OverviewStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.RequestsStatsRateDTO;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
public class StatsController {
  private final StatsService statsService;
  private final Logger logger = Logger.getLogger(StatsController.class.getName());

  public StatsController(StatsService statsService) {
    this.statsService = statsService;
  }

  @GetMapping("/rate")
    public ResponseEntity<RequestsStatsRateDTO> getRequestsStatsRate(@RequestParam String from,
        @RequestParam String to,
        @RequestParam(defaultValue = "DAY") String interval) {
      logger.info("Fetching requests stats rate");
      return ResponseEntity.ok(statsService.getRequestsStatsRate(from, to, interval));
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<MushroomCategoryStatsDTO>> getMushroomCategoryStats() {
      logger.info("Fetching mushroom category stats");
      return ResponseEntity.ok(statsService.getMushroomCategoryStats());
    }

    @GetMapping("/overview")
    public ResponseEntity<OverviewStatsDTO> getOverviewStats() {
      logger.info("Fetching overview stats");
      return ResponseEntity.ok(statsService.getCombinedStatistics());
    }
  

}
