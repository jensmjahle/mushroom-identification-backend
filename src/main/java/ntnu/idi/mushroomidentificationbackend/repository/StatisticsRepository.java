package ntnu.idi.mushroomidentificationbackend.repository;

import ntnu.idi.mushroomidentificationbackend.model.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, String> {
  @Query("SELECT SUM(s.ftrClicks) FROM Statistics s")
  long countTotalFtrClicks();
}
