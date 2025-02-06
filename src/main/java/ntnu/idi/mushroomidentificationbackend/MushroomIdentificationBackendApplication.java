package ntnu.idi.mushroomidentificationbackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MushroomIdentificationBackendApplication {
  static {
    if (!"dev".equals(System.getProperty("SPRING_PROFILES_ACTIVE"))) {
      Dotenv dotenv = Dotenv.load(); // Load .env file
      System.setProperty("DB_URL", dotenv.get("DB_URL"));
      System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
      System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(MushroomIdentificationBackendApplication.class, args);
  }

}
