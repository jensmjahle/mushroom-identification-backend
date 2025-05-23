package ntnu.idi.mushroomidentificationbackend.security;



import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import org.springframework.core.env.Environment;

@Component
public class JWTUtil {

  private static final long EXPIRATION_TIME = 86400000; // 1 day
  private static final long IMAGE_URL_EXPIRATION = 86400000; // 1 day
  private final Key key;
  private static final Logger logger = Logger.getLogger(JWTUtil.class.getName());

  public JWTUtil(SecretsConfig secretsConfig) {
    String secretKey = secretsConfig.getSecretKey();
    if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
      logger.severe("SECRET_KEY is too short or missing. Using fallback key.");
      secretKey = "defaultSecretKey-super-duper-key-secrets-yes-defaultSecretKey-super-duper-key-secrets-yes";
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
   * Generates a Signed JWT URL for secure image access.
   */
  public String generateSignedImageUrl(String userRequestId, String mushroomId, String filename) {
    return Jwts.builder()
        .setSubject(filename)
        .claim("userRequestId", userRequestId)
        .claim("mushroomId", mushroomId)
        .setExpiration(new Date(System.currentTimeMillis() + IMAGE_URL_EXPIRATION))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Validates a Signed JWT URL and returns the internal file path.
   */
  public String validateSignedImageUrl(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token)
          .getBody();

      String userRequestId = claims.get("userRequestId", String.class);
      String mushroomId = claims.get("mushroomId", String.class);
      String filename = claims.getSubject();
      return "uploads/" + userRequestId + "/" + mushroomId + "/" + filename;
    } catch (Exception e) {
      logger.warning("Invalid or expired signed image URL: " + e.getMessage());
      return null;
    }
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

  public void validateChatroomToken(String bearer, String userRequestId) {
    String token = bearer.replace("Bearer ", "");
    String userRequestIdFromToken = extractUsername(token);
    String role = extractRole(token);
    
    if (!role.equals("SUPERUSER") && !role.equals("MODERATOR") && !userRequestIdFromToken.equals(userRequestId)) {
        throw new UnauthorizedAccessException("Unauthorized: You cannot send messages to this chat.");
      }
  }
}
