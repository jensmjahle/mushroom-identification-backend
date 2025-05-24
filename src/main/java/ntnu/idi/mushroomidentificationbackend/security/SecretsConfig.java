package ntnu.idi.mushroomidentificationbackend.security;

import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.secrets")
public class SecretsConfig {

  private String secretKey;
  private String lookupSalt;
  private final Logger logger = Logger.getLogger(SecretsConfig.class.getName());

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public void setLookupSalt(String lookupSalt) {
    this.lookupSalt = lookupSalt;
  }

  public String getSecretKey() {
    if (secretKey == null || secretKey.isBlank()) {
      LogHelper.warning(logger, "WARNING: SECRET_KEY not set. Using fallback.");
      return "fallback-secret-key-please-set-in-env-fallback-secret-key-please-set-in-env";
    }
    return secretKey;
  }

  public String getLookupSalt() {
    if (lookupSalt == null || lookupSalt.isBlank()) {
      LogHelper.warning(logger, "WARNING: LOOKUP_SALT not set. Using fallback.");
      return "fallback-lookup-salt";
    }
    return lookupSalt;
  }
}
