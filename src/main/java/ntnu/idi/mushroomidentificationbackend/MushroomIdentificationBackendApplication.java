package ntnu.idi.mushroomidentificationbackend;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.util.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MushroomIdentificationBackendApplication {
  private static final Logger logger = Logger.getLogger(MushroomIdentificationBackendApplication.class.getName());

  static {
    
    if (new File(".env").exists()) {
      logger.info("Loading .env file");
      Dotenv dotenv = Dotenv.load(); // Load .env file
      System.setProperty("DB_URL", dotenv.get("DB_URL"));
      System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
      System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
      System.setProperty("SECRET_KEY", dotenv.get("SECRET_KEY"));
    } else {
      logger.info(".env file not found");
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(MushroomIdentificationBackendApplication.class, args);
  }
}
