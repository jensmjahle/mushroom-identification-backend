package ntnu.idi.mushroomidentificationbackend.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class UserRequestLookupHashGenerator {

  public static void main(String[] args) {
      try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String salt;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter reference code to hash: ");
        String referenceCode = scanner.nextLine();
        try {
          salt = System.getProperty("LOOKUP_SALT");
        } catch (NullPointerException e) {
         System.out.println("LOOKUP_SALT not found in environment variables. Please set the LOOKUP_SALT variable. The fallback salt will be used, which is not secure. Should only be used for development.");
          salt = "development-salt";
        }
        byte[] encodedHash = digest.digest((referenceCode + salt).getBytes());
        System.out.println("Hashed Reference Code: ");
        System.out.println(Base64.getEncoder().encodeToString(encodedHash));
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Error hashing reference code for lookup", e);
      }
    
  }
}
