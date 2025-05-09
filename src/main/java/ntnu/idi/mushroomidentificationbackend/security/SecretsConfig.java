package ntnu.idi.mushroomidentificationbackend.security;

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
    if (secretKey == null || secretKey.isBlank()) {
      System.err.println("[WARN] SECRET_KEY not found — falling back to development default.");
      this.secretKey = "development-fallback-secret-key-super-duper-key";
    } else {
      this.secretKey = secretKey;
    }
  }

  public void setLookupSalt(String lookupSalt) {
    if (lookupSalt == null || lookupSalt.isBlank()) {
      System.err.println("[WARN] LOOKUP_SALT not found — falling back to development salt.");
      this.lookupSalt = "development-fallback-lookup-salt-please-change";
    } else {
      this.lookupSalt = lookupSalt;
    }
  }
}
