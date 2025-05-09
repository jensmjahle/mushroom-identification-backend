package ntnu.idi.mushroomidentificationbackend.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app.secrets")
public class SecretsConfig {

  private String secretKey;
  private String lookupSalt;

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public void setLookupSalt(String lookupSalt) {
    this.lookupSalt = lookupSalt;
  }

  @PostConstruct
  public void init() {
    if (secretKey == null || secretKey.isBlank()) {
      System.err.println("WARNING: SECRET_KEY not found in env. Using fallback for development.");
      this.secretKey = "fallback-development-secret-key-use-real-one-in-prod";
    }

    if (lookupSalt == null || lookupSalt.isBlank()) {
      System.err.println("WARNING: LOOKUP_SALT not found in env. Using fallback for development.");
      this.lookupSalt = "fallback-development-salt";
    }
  }
}
