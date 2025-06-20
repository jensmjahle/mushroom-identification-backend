package ntnu.idi.mushroomidentificationbackend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Service class for handling statistics related operations.
 */
@Service
@AllArgsConstructor
public class StatsService {
  private final UserRequestRepository userRequestRepository;
  private final MushroomRepository mushroomRepository;
  private final StatisticsRepository statisticsRepository;


  /**
   * Retrieves combined statistics for the current month and historical data.
   * This method aggregates the following statistics:
   * - Total new requests for the current month
   * - Total completed requests for the current month
   * - Total new requests for all previous months
   * - Total completed requests for all previous months
   * * - Total FTR clicks (from the statistics table, including current month)
   * * Additionally, it calculates the number of requests created in the last 7 days.
   *
   * @return OverviewStatsDTO containing combined statistics
   */
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


  /**
   * Retrieves the total number of FTR clicks from the statistics repository.
   *
   * @return the total number of FTR clicks, or 0 if no clicks are recorded
   */
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

  /**
   * Retrieves mushroom category statistics for the current month and previous months.
   * This method combines the counts of mushrooms identified in the current month
   * with the historical data from the Statistics table.
   *
   * @return List of MushroomCategoryStatsDTO containing the counts for each mushroom status category
   */
  public List<MushroomCategoryStatsDTO> getMushroomCategoryStats() {
    LocalDate now = LocalDate.now();
    String currentMonthKey = getMonthKey(now);

    // Count for the current month from Mushroom table
    List<Object[]> currentMonthResults = mushroomRepository.countMushroomsByStatusCreatedBetween(
        java.sql.Date.valueOf(now.withDayOfMonth(1)),
        java.sql.Date.valueOf(now.plusMonths(1).withDayOfMonth(1).minusDays(1))
    );
    Map<MushroomStatus, Integer> currentCounts = currentMonthResults.stream()
        .collect(Collectors.toMap(
            r -> (MushroomStatus) r[0],
            r -> ((Long) r[1]).intValue()
        ));

    // Count for previous months from the Statistics table
    List<Statistics> previousStats = statisticsRepository.findAll().stream()
        .filter(s -> !s.getMonthYear().equals(currentMonthKey))
        .toList();

    Map<MushroomStatus, Integer> previousCounts = Map.of(
        MushroomStatus.PSILOCYBIN, previousStats.stream().mapToInt(s -> (int) s.getTotalPsilocybinIdentified()).sum(),
        MushroomStatus.NON_PSILOCYBIN, previousStats.stream().mapToInt(s -> (int) s.getTotalNonPsilocybinIdentified()).sum(),
        MushroomStatus.TOXIC, previousStats.stream().mapToInt(s -> (int) s.getTotalToxicIdentified()).sum(),
        MushroomStatus.UNKNOWN, previousStats.stream().mapToInt(s -> (int) s.getTotalUnknownIdentified()).sum(),
        MushroomStatus.UNIDENTIFIABLE, previousStats.stream().mapToInt(s -> (int) s.getTotalUnidentifiableIdentified()).sum()
    );

    // Combine current and previous
    return previousCounts.keySet().stream()
        .map(status -> {
          int current = currentCounts.getOrDefault(status, 0);
          int previous = previousCounts.getOrDefault(status, 0);
          return new MushroomCategoryStatsDTO(status, current + previous);
        })
        .toList();
  }

  /**
   * Retrieves the number of new user requests created in the specified month.
   * This method counts the requests created between the start of the month and the end of the month.
   *
   * @param date the date representing the month to retrieve statistics for
   * @return the number of new requests created in the specified month
   */
  public long getMonthlyNewRequests(LocalDate date) {
    LocalDateTime start = date.withDayOfMonth(1).atStartOfDay();
    LocalDateTime end = start.plusMonths(1).minusSeconds(1);
    return userRequestRepository.countByCreatedAtBetween(
        java.sql.Date.valueOf(start.toLocalDate()),
        java.sql.Date.valueOf(end.toLocalDate())
    );
  }

  /**
   * Retrieves the number of user requests with a specific status created in the specified month.
   * This method counts the requests with the given status created between the start of the month and the end of the month.
   *
   * @param status the status of the user requests to count
   * @param date the date representing the month to retrieve statistics for
   * @return the number of requests with the specified status created in the specified month
   */
  public long getMonthlyRequestsByStatus(UserRequestStatus status, LocalDate date) {
    LocalDateTime start = date.withDayOfMonth(1).atStartOfDay();
    LocalDateTime end = start.plusMonths(1).minusSeconds(1);
    return userRequestRepository.countByStatusAndCreatedAtBetween(
        status,
        java.sql.Date.valueOf(start.toLocalDate()),
        java.sql.Date.valueOf(end.toLocalDate())
    );
  }

  /**
   * Retrieves the number of mushrooms identified in the specified month with a specific status.
   * This method checks if the month is the current month and retrieves the count from the Mushroom repository.
   *
   * @param date the date representing the month to retrieve statistics for
   * @param status the status of the mushrooms to count
   * @return the number of mushrooms identified with the specified status in the specified month
   */
  private long getMonthlyIdentifiedCount(LocalDate date, MushroomStatus status) {
    boolean isCurrentMonth = getMonthKey(date).equals(getMonthKey(LocalDate.now()));

    if (isCurrentMonth) {
      return mushroomRepository.countByStatusAndCreatedBetween(
          status,
          java.sql.Date.valueOf(date.withDayOfMonth(1)),
          java.sql.Date.valueOf(date.plusMonths(1).withDayOfMonth(1).minusDays(1))
      );
    }

    return statisticsRepository.findById(getMonthKey(date))
        .map(stat -> switch (status) {
          case PSILOCYBIN -> stat.getTotalPsilocybinIdentified();
          case NON_PSILOCYBIN -> stat.getTotalNonPsilocybinIdentified();
          case TOXIC -> stat.getTotalToxicIdentified();
          case UNKNOWN -> stat.getTotalUnknownIdentified();
          case UNIDENTIFIABLE -> stat.getTotalUnidentifiableIdentified();
          default -> 0L;
        })
        .orElse(0L);
  }

  /**
   * Retrieves the number of psilocybin mushrooms identified in the specified month.
   *
   * @param date the date representing the month to retrieve statistics for
   * @return the number of psilocybin mushrooms identified in the specified month
   */
  public long getMonthlyPsilocybinIdentified(LocalDate date) {
    return getMonthlyIdentifiedCount(date, MushroomStatus.PSILOCYBIN);
  }

  /**
   * Retrieves the number of non-psilocybin mushrooms identified in the specified month.
   *
   * @param date the date representing the month to retrieve statistics for
   * @return the number of non-psilocybin mushrooms identified in the specified month
   */
  public long getMonthlyNonPsilocybinIdentified(LocalDate date) {
    return getMonthlyIdentifiedCount(date, MushroomStatus.NON_PSILOCYBIN);
  }

  /**
   * Retrieves the number of toxic mushrooms identified in the specified month.
   *
   * @param date the date representing the month to retrieve statistics for
   * @return the number of toxic mushrooms identified in the specified month
   */
  public long getMonthlyToxicIdentified(LocalDate date) {
    return getMonthlyIdentifiedCount(date, MushroomStatus.TOXIC);
  }

  /**
   * Retrieves the number of unknown mushrooms identified in the specified month.
   *
   * @param date the date representing the month to retrieve statistics for
   * @return the number of unknown mushrooms identified in the specified month
   */
  public long getMonthlyUnknownIdentified(LocalDate date) {
    return getMonthlyIdentifiedCount(date, MushroomStatus.UNKNOWN);
  }

  /**
   * Retrieves the number of unidentifiable mushrooms identified in the specified month.
   *
   * @param date the date representing the month to retrieve statistics for
   * @return the number of unidentifiable mushrooms identified in the specified month
   */
  public long getMonthlyUnidentifiableIdentified(LocalDate date) {
    return getMonthlyIdentifiedCount(date, MushroomStatus.UNIDENTIFIABLE);
  }

  /**
   * Generates a key for the month in the format "YYYY-MM".
   *
   * @param date the date representing the month
   * @return the month key in the format "YYYY-MM"
   */
  private String getMonthKey(LocalDate date) {
    return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
  }

  /**
   * Retrieves the number of FTR clicks for the specified month.
   *
   * @param date the date representing the month to retrieve statistics for
   * @return the number of FTR clicks for the specified month, or 0 if no clicks are recorded
   */
  public long getFtrClicksForMonth(LocalDate date) {
    String monthKey = getMonthKey(date);
    return statisticsRepository.findById(monthKey)
        .map(Statistics::getFtrClicks)
        .orElse(0L);
  }

  /**
   *  Generates a CSV report for the user requests created in the specified month.
   * 
   * @param year the year of the month to generate the report for
   * @param month the month to generate the report for (1-12)
   * @return a CSV string containing the user requests data
   */
  public String generateCsvForMonth(int year, int month) {
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.plusMonths(1); 
  
    List<UserRequest> requests = userRequestRepository.findByCreatedAtBetween(
        java.sql.Date.valueOf(start),
        java.sql.Date.valueOf(end)
    );
  
    StringBuilder csv = new StringBuilder();
    csv.append("Request ID,Status,Updated At,Mushroom Count\n");
  
    for (UserRequest request : requests) {
      String requestId = request.getUserRequestId();
      String status = request.getStatus() != null ? request.getStatus().toString() : "";
      String updatedAt = request.getUpdatedAt() != null
          ? request.getUpdatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
          : "";
  
      int mushroomCount = (int) mushroomRepository.countByUserRequest(request);
  
      csv.append(String.format("%s,%s,%s,%d\n", requestId, status, updatedAt, mushroomCount));
    }
  
    return csv.toString();
  }

  /**
   * Generates a PDF report for the user requests created in the specified month.
   *
   * @param year the year of the month to generate the report for
   * @param month the month to generate the report for (1-12)
   * @return a byte array containing the PDF report
   * @throws Exception if an error occurs during PDF generation
   */
  public byte[] generatePdfForMonth(int year, int month) throws Exception {
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.plusMonths(1);
    String monthKey = getMonthKey(start);

    Statistics stats = statisticsRepository.findById(monthKey).orElse(null);

    // If not stored yet, get dynamic data
    boolean useLiveData = stats == null;
    long newRequests = useLiveData ? getMonthlyNewRequests(start) : stats.getTotalNewRequests();
    long completedRequests = useLiveData ? getMonthlyRequestsByStatus(UserRequestStatus.COMPLETED, start) : stats.getTotalRequestsCompleted();
    long psilocybin = useLiveData ? getMonthlyPsilocybinIdentified(start) : stats.getTotalPsilocybinIdentified();
    long nonPsilocybin = useLiveData ? getMonthlyNonPsilocybinIdentified(start) : stats.getTotalNonPsilocybinIdentified();
    long toxic = useLiveData ? getMonthlyToxicIdentified(start) : stats.getTotalToxicIdentified();
    long unknown = useLiveData ? getMonthlyUnknownIdentified(start) : stats.getTotalUnknownIdentified();
    long unidentifiable = useLiveData ? getMonthlyUnidentifiableIdentified(start) : stats.getTotalUnidentifiableIdentified();
    long ftrClicks = useLiveData ? getFtrClicksForMonth(start) : stats.getFtrClicks();

    List<UserRequest> requests = userRequestRepository.findByCreatedAtBetween(
        java.sql.Date.valueOf(start), java.sql.Date.valueOf(end)
    );

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document doc = new Document();
    PdfWriter.getInstance(doc, baos);
    doc.open();

    // Add logo
    InputStream logoStream = getClass().getClassLoader().getResourceAsStream("static/logo-horizontal.png");
    if (logoStream != null) {
      Image logo = Image.getInstance(logoStream.readAllBytes());
      logo.scaleToFit(100, 100);
      logo.setAlignment(Image.ALIGN_CENTER);
      doc.add(logo);
    }

    // Add Title and Dates
    String monthName = start.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    Paragraph title = new Paragraph("Monthly Report - " + monthName + " " + year, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
    title.setAlignment(Element.ALIGN_CENTER);
    doc.add(title);

    Paragraph exportDate = new Paragraph("Export Date: " + LocalDate.now().toString(), FontFactory.getFont(FontFactory.HELVETICA, 10));
    exportDate.setAlignment(Element.ALIGN_RIGHT);
    doc.add(exportDate);

    doc.add(Chunk.NEWLINE);

    // Summary Table
    PdfPTable summary = new PdfPTable(2);
    summary.setWidthPercentage(100);
    summary.addCell("New Requests");
    summary.addCell(String.valueOf(newRequests));
    summary.addCell("Completed Requests");
    summary.addCell(String.valueOf(completedRequests));
    summary.addCell("FTR Clicks");
    summary.addCell(String.valueOf(ftrClicks));
    summary.addCell("Psilocybin Identified");
    summary.addCell(String.valueOf(psilocybin));
    summary.addCell("Non-Psilocybin Identified");
    summary.addCell(String.valueOf(nonPsilocybin));
    summary.addCell("Toxic Identified");
    summary.addCell(String.valueOf(toxic));
    summary.addCell("Unknown Identified");
    summary.addCell(String.valueOf(unknown));
    summary.addCell("Unidentifiable Identified");
    summary.addCell(String.valueOf(unidentifiable));
    doc.add(summary);

    doc.add(Chunk.NEWLINE);

    // Requests Table
    PdfPTable table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.addCell("Request ID");
    table.addCell("Status");
    table.addCell("Updated At");
    table.addCell("Mushroom Count");

    for (UserRequest request : requests) {
      String updatedAt = request.getUpdatedAt() != null
          ? request.getUpdatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
          : "";

      int mushroomCount = (int) mushroomRepository.countByUserRequest(request);
      table.addCell(request.getUserRequestId());
      table.addCell(request.getStatus() != null ? request.getStatus().toString() : "");
      table.addCell(updatedAt);
      table.addCell(String.valueOf(mushroomCount));
    }

    doc.add(table);
    doc.close();

    return baos.toByteArray();
  }

  /**
   * Logs a registration button press by updating the statistics for the current month.
   */
  public void logRegistrationButtonPress() {
    String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    Optional<Statistics> optionalStats = statisticsRepository.findById(currentMonthYear);

    Statistics stats;
    if (optionalStats.isPresent()) {
      stats = optionalStats.get();
      stats.setFtrClicks(stats.getFtrClicks() + 1);
    } else {
      stats = new Statistics();
      stats.setMonthYear(currentMonthYear);
      stats.setFtrClicks(1);
    }
    statisticsRepository.save(stats);
  }
}
