package ntnu.idi.mushroomidentificationbackend.controller.admin;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.MushroomCategoryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.OverviewStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.RequestsStatsRateDTO;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;

import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Controller for handling statistics-related requests.
 * This controller provides endpoints for fetching
 * various statistics related to user requests,
 * mushroom categories, and overall system performance.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/stats")
public class StatsController {
  private final StatsService statsService;
  private final Logger logger = Logger.getLogger(StatsController.class.getName());

  /**
   * Fetches the rate of requests statistics
   * for a specified time range and interval.
   * 
   * @param from the start date of the time range in ISO format
   * @param to the end date of the time range in ISO format
   * @param interval the interval for aggregating statistics (e.g., DAY, WEEK, MONTH)
   * @return ResponseEntity containing the RequestsStatsRateDTO with the statistics
   */
  @GetMapping("/rate")
    public ResponseEntity<RequestsStatsRateDTO> getRequestsStatsRate(@RequestParam String from,
        @RequestParam String to,
        @RequestParam(defaultValue = "DAY") String interval) {
      logger.info("Fetching requests stats rate");
      return ResponseEntity.ok(statsService.getRequestsStatsRate(from, to, interval));
    }

  /**
   * Fetches statistics for mushroom categories.
   * This endpoint retrieves the distribution of mushroom categories
   * and their respective counts
   * within the system.
   * 
   * @return ResponseEntity containing a list of MushroomCategoryStatsDTO
   */
  @GetMapping("/categories")
    public ResponseEntity<List<MushroomCategoryStatsDTO>> getMushroomCategoryStats() {
      logger.info("Fetching mushroom category stats");
      return ResponseEntity.ok(statsService.getMushroomCategoryStats());
    }

  /**
   * Fetches an overview of system statistics.
   * 
   * @return ResponseEntity containing the OverviewStatsDTO
   */
    @GetMapping("/overview")
    public ResponseEntity<OverviewStatsDTO> getOverviewStats() {
      logger.info("Fetching overview stats");
      return ResponseEntity.ok(statsService.getCombinedStatistics());
    }

  /**
   * Exports statistics for a specific month in CSV format.
   * This endpoint generates a CSV file
   * containing statistics for the specified month
   * and year,
   * allowing for easy data analysis and reporting.
   *  
   * @param year the year for which statistics are to be exported as an integer (e.g., 2023) 
   * @param month the month for which statistics are to be exported as an integer (1-12)
   * @return ResponseEntity containing the CSV file as a ByteArrayResource
   */
    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam int year, @RequestParam int month) {
      LogHelper.info(logger, "Exporting CSV for year: {0}, month: {1}", year, month);
      String csv = statsService.generateCsvForMonth(year, month);
      ByteArrayResource resource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));

      String filename = String.format("requests_%d_%02d.csv", year, month);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.parseMediaType("text/csv"))
          .body(resource);
    }

  /**
   * Downloads statistics for a specific month in PDF format.
   * This endpoint generates a PDF file
   * containing statistics for the specified month
   * and year,
   * allowing for easy data sharing and reporting.
   * 
   * @param year the year for which statistics are to be exported as an integer (e.g., 2023)
   * @param month the month for which statistics are to be exported as an integer (1-12)
   * @return ResponseEntity containing the PDF file as a byte array
   */
  @GetMapping("/export/pdf")
  public ResponseEntity<byte[]> downloadStatisticsPdf(@RequestParam int year, @RequestParam int month) {
    try {
      byte[] pdfBytes = statsService.generatePdfForMonth(year, month);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(
          ContentDisposition.attachment().filename("statistics_" + year + "_" + month + ".pdf").build());
      return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

}
