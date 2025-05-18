package ntnu.idi.mushroomidentificationbackend.controller.admin;

import java.nio.charset.StandardCharsets;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.MushroomCategoryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.OverviewStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.RequestsStatsRateDTO;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.io.ByteArrayResource;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StatsController.class)
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    StatsController.class,
    StatsControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
public class StatsControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public StatsService statsService() { return mock(StatsService.class); }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private StatsService statsService;

  @Test
  void getRequestsStatsRate_returnsOk() throws Exception {
    when(statsService.getRequestsStatsRate(anyString(), anyString(), anyString()))
        .thenReturn(new RequestsStatsRateDTO());

    mockMvc.perform(get("/api/admin/stats/rate")
            .param("from", "2024-01-01")
            .param("to", "2024-01-31")
            .param("interval", "DAY"))
        .andExpect(status().isOk());
  }

  @Test
  void getMushroomCategoryStats_returnsOk() throws Exception {
    when(statsService.getMushroomCategoryStats())
        .thenReturn(Collections.singletonList(new MushroomCategoryStatsDTO()));

    mockMvc.perform(get("/api/admin/stats/categories"))
        .andExpect(status().isOk());
  }

  @Test
  void getOverviewStats_returnsOk() throws Exception {
    when(statsService.getCombinedStatistics()).thenReturn(new OverviewStatsDTO());

    mockMvc.perform(get("/api/admin/stats/overview"))
        .andExpect(status().isOk());
  }

  @Test
  void exportCsv_returnsOk() throws Exception {
    when(statsService.generateCsvForMonth(anyInt(), anyInt())).thenReturn("some,data");

    mockMvc.perform(get("/api/admin/stats/export/csv")
            .param("year", "2024")
            .param("month", "5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/csv"));
  }

  @Test
  void exportPdf_returnsOk() throws Exception {
    when(statsService.generatePdfForMonth(anyInt(), anyInt()))
        .thenReturn("fake-pdf-content".getBytes(StandardCharsets.UTF_8));

    mockMvc.perform(get("/api/admin/stats/export/pdf")
            .param("year", "2024")
            .param("month", "4"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF));
  }


  @Test
  void exportPdf_fails_returns500() throws Exception {
    when(statsService.generatePdfForMonth(anyInt(), anyInt())).thenThrow(new RuntimeException("error"));

    mockMvc.perform(get("/api/admin/stats/export/pdf")
            .param("year", "2024")
            .param("month", "5"))
        .andExpect(status().isInternalServerError());
  }
} 
