package ntnu.idi.mushroomidentificationbackend.security;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tracks JWTs that have been revoked (e.g. via logout) before their natural expiration.
 * Since the application uses stateless JWTs, this denylist is what makes a token issued
 * before logout stop being accepted afterward.
 */
@Component
public class TokenBlocklistService {

  private final ConcurrentMap<String, Instant> revokedTokenExpirations = new ConcurrentHashMap<>();

  /**
   * Marks a token as revoked until its own expiration time. After that point it would be
   * rejected as expired anyway, so there is no need to keep tracking it.
   *
   * @param tokenId the unique ID (jti claim) of the token to revoke
   * @param expiration the original expiration time of the token
   */
  public void revoke(String tokenId, Date expiration) {
    if (tokenId == null) {
      return;
    }
    revokedTokenExpirations.put(tokenId, expiration.toInstant());
  }

  /**
   * Checks whether the given token ID has been revoked.
   *
   * @param tokenId the unique ID (jti claim) of the token to check
   * @return true if the token has been revoked and has not yet naturally expired
   */
  public boolean isRevoked(String tokenId) {
    return tokenId != null && revokedTokenExpirations.containsKey(tokenId);
  }

  /**
   * Periodically purges entries for tokens that have since expired naturally, since they no
   * longer need to be tracked in the denylist.
   */
  @Scheduled(fixedRate = 60 * 60 * 1000) // every hour
  public void purgeExpiredEntries() {
    Instant now = Instant.now();
    revokedTokenExpirations.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
  }
}
