package ntnu.idi.mushroomidentificationbackend.service;

import java.time.Instant;
import java.time.ZoneId;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsServiceTest {

  private UserRequestRepository userRequestRepository;
  private MushroomRepository mushroomRepository;
  private StatisticsRepository statisticsRepository;
  private StatsService statsService;

  @BeforeEach
  void setUp() {
    userRequestRepository = mock(UserRequestRepository.class);
    mushroomRepository = mock(MushroomRepository.class);
    statisticsRepository = mock(StatisticsRepository.class);
    statsService = new StatsService(userRequestRepository, mushroomRepository, statisticsRepository);
  }

  @Test
  void getCombinedStatistics() {
    LocalDate now = LocalDate.now();
    java.sql.Date start = java.sql.Date.valueOf(now.withDayOfMonth(1));
    java.sql.Date end = java.sql.Date.valueOf(now.plusMonths(1).withDayOfMonth(1).minusDays(1));

    when(userRequestRepository.countByCreatedAtBetween(eq(start), eq(end))).thenReturn(10L);
    when(userRequestRepository.countByCreatedAtBetween(any(), any())).thenReturn(10L);
    when(userRequestRepository.countByStatusAndCreatedAtBetween(
        eq(UserRequestStatus.COMPLETED), eq(start), eq(end)
    )).thenReturn(10L);

    when(statisticsRepository.findAll()).thenReturn(Collections.emptyList());
    when(statisticsRepository.countTotalFtrClicks()).thenReturn(5L);

    OverviewStatsDTO dto = statsService.getCombinedStatistics();

    assertEquals(10L, dto.getTotalRequests());
    assertEquals(10L, dto.getTotalCompleted());
    assertEquals(10L, dto.getWeeklyRate());
    assertEquals(5L, dto.getFtrClicks());
  }

  @Test
  void getTotalFtrClicks_whenResultPresent() {
    when(statisticsRepository.countTotalFtrClicks()).thenReturn(15L);
    long result = statsService.getTotalFtrClicks();
    assertEquals(15L, result);
  }

  @Test
  void getTotalFtrClicks_whenResultIsNull() {
    when(statisticsRepository.countTotalFtrClicks()).thenReturn(null);
    long result = statsService.getTotalFtrClicks();
    assertEquals(0L, result);
  }

  @Test
  void getRequestsStatsRate() {
    LocalDate today = LocalDate.now();
    Instant instant = today.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    Date utilDate = Date.from(instant);

    UserRequest request = new UserRequest();
    request.setCreatedAt(utilDate);

    when(userRequestRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(request));

    RequestsStatsRateDTO result = statsService.getRequestsStatsRate(
        today.toString(),
        today.toString(),
        "DAY"
    );

    assertEquals("DAY", result.getTimeInterval());
    assertEquals(today.toString(), result.getFrom());
    assertEquals(today.toString(), result.getTo());

    List<RequestsStatsRateDTO.DataPoint> points = result.getPoints();
    assertEquals(1, points.size());
    assertEquals(today.toString(), points.get(0).getDate());
    assertEquals(1, points.get(0).getCount());
  }

  @Test
  void getMushroomCategoryStats() {
    List<Object[]> mockCounts = new ArrayList<>();
    mockCounts.add(new Object[]{MushroomStatus.PSILOCYBIN, 3L});

    when(mushroomRepository.countMushroomsByStatusCreatedBetween(any(), any())).thenReturn(mockCounts);

    Statistics stats = new Statistics();
    stats.setMonthYear("2024-04");
    stats.setTotalPsilocybinIdentified(5);
    stats.setTotalNonPsilocybinIdentified(2);
    stats.setTotalToxicIdentified(1);
    stats.setTotalUnknownIdentified(0);
    stats.setTotalUnidentifiableIdentified(0);

    when(statisticsRepository.findAll()).thenReturn(List.of(stats));

    List<MushroomCategoryStatsDTO> result = statsService.getMushroomCategoryStats();
    assertFalse(result.isEmpty());
    assertTrue(result.stream().anyMatch(dto ->
        dto.getStatus() == MushroomStatus.PSILOCYBIN && dto.getCount() == 8
    ));
  }

  @Test
  void getMonthlyNewRequests() {
    when(userRequestRepository.countByCreatedAtBetween(any(), any())).thenReturn(7L);
    assertEquals(7L, statsService.getMonthlyNewRequests(LocalDate.now()));
  }

  @Test
  void getMonthlyRequestsByStatus() {
    when(userRequestRepository.countByStatusAndCreatedAtBetween(any(), any(), any())).thenReturn(8L);
    assertEquals(8L, statsService.getMonthlyRequestsByStatus(UserRequestStatus.COMPLETED, LocalDate.now()));
  }

  @Test
  void getMonthlyPsilocybinIdentified() {
    when(mushroomRepository.countByStatusAndCreatedBetween(any(), any(), any())).thenReturn(2L);
    assertEquals(2L, statsService.getMonthlyPsilocybinIdentified(LocalDate.now()));
  }

  @Test
  void getMonthlyNonPsilocybinIdentified() {
    when(mushroomRepository.countByStatusAndCreatedBetween(any(), any(), any())).thenReturn(4L);
    assertEquals(4L, statsService.getMonthlyNonPsilocybinIdentified(LocalDate.now()));
  }

  @Test
  void getMonthlyToxicIdentified() {
    when(mushroomRepository.countByStatusAndCreatedBetween(any(), any(), any())).thenReturn(6L);
    assertEquals(6L, statsService.getMonthlyToxicIdentified(LocalDate.now()));
  }

  @Test
  void getMonthlyUnknownIdentified() {
    when(mushroomRepository.countByStatusAndCreatedBetween(any(), any(), any())).thenReturn(9L);
    assertEquals(9L, statsService.getMonthlyUnknownIdentified(LocalDate.now()));
  }

  @Test
  void getMonthlyUnidentifiableIdentified() {
    when(mushroomRepository.countByStatusAndCreatedBetween(any(), any(), any())).thenReturn(1L);
    assertEquals(1L, statsService.getMonthlyUnidentifiableIdentified(LocalDate.now()));
  }

  @Test
  void getFtrClicksForMonth() {
    Statistics stats = new Statistics();
    stats.setFtrClicks(12L);
    when(statisticsRepository.findById(any())).thenReturn(Optional.of(stats));
    assertEquals(12L, statsService.getFtrClicksForMonth(LocalDate.now()));
  }

  @Test
  void generateCsvForMonth() {
    UserRequest req = new UserRequest();
    req.setUserRequestId("abc123");
    req.setStatus(UserRequestStatus.COMPLETED);
    req.setUpdatedAt(new Date(System.currentTimeMillis()));

    when(userRequestRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(req));
    when(mushroomRepository.countByUserRequest(req)).thenReturn(2L);

    String csv = statsService.generateCsvForMonth(2025, 5);
    assertTrue(csv.contains("abc123"));
    assertTrue(csv.contains("COMPLETED"));
    assertTrue(csv.contains("2"));
  }

  @Test
  void generatePdfForMonth() {
    when(userRequestRepository.findByCreatedAtBetween(any(), any())).thenReturn(Collections.emptyList());
    try {
      byte[] pdf = statsService.generatePdfForMonth(2025, 5);
      assertNotNull(pdf);
      assertTrue(pdf.length > 0);
    } catch (Exception e) {
      fail("PDF generation should not throw exception");
    }
  }

  @Test
  void logRegistrationButtonPress() {
    Statistics stats = new Statistics();
    stats.setMonthYear(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
    stats.setFtrClicks(5L);

    when(statisticsRepository.findById(any())).thenReturn(Optional.of(stats));

    statsService.logRegistrationButtonPress();

    assertEquals(6L, stats.getFtrClicks());
    verify(statisticsRepository).save(stats);
  }
}
