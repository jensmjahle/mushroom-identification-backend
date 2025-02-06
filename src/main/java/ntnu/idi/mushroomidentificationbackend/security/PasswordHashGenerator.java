package ntnu.idi.mushroomidentificationbackend.security;

import java.util.Scanner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Class for generating a hashed password using BCryptPasswordEncoder.
 * This is used to generate hashed passwords for users in the database.
 * Should only be used for adding the first superuser to the database.
 */
public class PasswordHashGenerator {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    System.out.print("Enter password to hash: ");
    String rawPassword = scanner.nextLine();

    String hashedPassword = encoder.encode(rawPassword);
    System.out.println("Hashed Password: " + hashedPassword);

    scanner.close();
  }
}
