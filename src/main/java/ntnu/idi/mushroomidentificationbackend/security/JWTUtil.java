package ntnu.idi.mushroomidentificationbackend.security;



import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import org.springframework.core.env.Environment;

@Component
public class JWTUtil {

  private static final long EXPIRATION_TIME = 86400000; // 1 day
  private final Key key;
  private static final Logger logger = Logger.getLogger(JWTUtil.class.getName());
  
  public JWTUtil(Environment environment) {  
    String activeProfile = environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default";
    logger.info("Active profile: " + activeProfile);
    String secretKey;
    if (activeProfile.equals("dev")) {
      secretKey = "developmentKey-very-secret-key-extra-secret-key";
    } else {
      try {
        secretKey = System.getProperty("SECRET_KEY");
      } catch (NullPointerException e) {
        throw new IllegalStateException(
            "SECRET_KEY not found in environment variables. Please set the SECRET_KEY variable. Or run the application in development mode.");
      }
    }
    key = Keys.hmacShaKeyFor(secretKey.getBytes());
  }
  
  /**
   * Generates a JWT token for an admin user.
   */
  public String generateToken(String username, String role) {
    return Jwts.builder()
        .setSubject(username)
        .claim("role", role)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Extracts the username from the JWT token.
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the role from the JWT token.
   */
  public String extractRole(String token) {
    return extractAllClaims(token).get("role", String.class);
  }

  /**
   * Extracts claims using a resolver function.
   */
  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Parses the JWT token and extracts claims.
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Validates the token.
   */
  public boolean isTokenValid(String token) {
    try {
      return !isTokenExpired(token);
    } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Checks if the token has expired.
   */
  private boolean isTokenExpired(String token) {
    return extractAllClaims(token).getExpiration().before(new Date());
  }
}
