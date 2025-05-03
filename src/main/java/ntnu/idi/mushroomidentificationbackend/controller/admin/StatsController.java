package ntnu.idi.mushroomidentificationbackend.controller.admin;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.MushroomCategoryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.OverviewStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.RequestsStatsRateDTO;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/stats")
public class StatsController {
  private final StatsService statsService;
  private final Logger logger = Logger.getLogger(StatsController.class.getName());
  
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
    
    @GetMapping("/export")
    public ResponseEntity<Resource> exportCsv(@RequestParam int year, @RequestParam int month) {
      logger.info("Exporting CSV for year " + year + ", month " + month);
      String csv = statsService.generateCsvForMonth(year, month);
      ByteArrayResource resource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));

      String filename = String.format("requests_%d_%02d.csv", year, month);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.parseMediaType("text/csv"))
          .body(resource);
    }
}
