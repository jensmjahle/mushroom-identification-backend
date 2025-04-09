package ntnu.idi.mushroomidentificationbackend.controller;

import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.MushroomCategoryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.OverviewStatsDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.RequestsStatsRateDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.statistics.SummaryStatsDTO;
import ntnu.idi.mushroomidentificationbackend.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/admin/stats")
public class StatsController {
  private StatsService statsService;
 
    @GetMapping("/rate")
    public RequestsStatsRateDTO getRequestsStatsRate(@RequestParam String from,
        @RequestParam String to,
        @RequestParam(defaultValue = "DAY") String interval) {
      return statsService.getRequestsStatsRate(from, to, interval);
    }

    @GetMapping("/summary")
    public SummaryStatsDTO getSummaryStats() {
      return statsService.getSummaryStats();
    }
    

    @GetMapping("/categories")
    public List<MushroomCategoryStatsDTO> getMushroomCategoryStats() {
      return statsService.getMushroomCategoryStats();
    }

    @GetMapping("/overview")
    public OverviewStatsDTO getOverviewStats() {
      return statsService.getOverviewStats();
    }
  

}
