package ntnu.idi.mushroomidentificationbackend;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.security.SecretsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(SecretsConfig.class)
public class MushroomIdentificationBackendApplication {
  private static final Logger logger = Logger.getLogger(MushroomIdentificationBackendApplication.class.getName());

  static {
    Dotenv dotenv = null;

    // Load .env file only if it exists (development only)
    if (new File(".env").exists()) {
      logger.info("Loading .env file for local development");
      dotenv = Dotenv.load();
    } else {
      logger.info(".env file not found — assuming real environment variables are set");
    }

    // Load environment variables with optional fallback to .env
    setEnvOrFallback("DB_URL", dotenv);
    setEnvOrFallback("DB_USERNAME", dotenv);
    setEnvOrFallback("DB_PASSWORD", dotenv);
    setEnvOrFallback("SECRET_KEY", dotenv);
    setEnvOrFallback("LOOKUP_SALT", dotenv);
    setEnvOrFallback("SPRING_PROFILES_ACTIVE", dotenv); // Optional: dev, prod, etc.
  }

  private static void setEnvOrFallback(String key, Dotenv dotenv) {
    String value = System.getenv(key);
    if (value == null && dotenv != null) {
      value = dotenv.get(key);
    }
    if (value != null && !value.isBlank()) {
      System.setProperty(key, value);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(MushroomIdentificationBackendApplication.class, args);
  }
}
