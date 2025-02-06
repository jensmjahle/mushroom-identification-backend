package ntnu.idi.mushroomidentificationbackend;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class MushroomIdentificationBackendApplication {
  @Value("${spring.profiles.active}")
  private static String activeProfile;

  static {
    
    if (new File(".env").exists()) {
      System.out.println("Loading .env file");
      Dotenv dotenv = Dotenv.load(); // Load .env file
      System.setProperty("DB_URL", dotenv.get("DB_URL"));
      System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
      System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

    } else {
      System.out.println("No .env file found, using default values");
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(MushroomIdentificationBackendApplication.class, args);
  }
}
