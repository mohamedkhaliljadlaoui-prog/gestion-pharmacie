package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecurityUtil {
    
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algorithme SHA-256 non disponible");
            return password; // Fallback (à éviter en production)
        }
    }
    
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        String hashedInput = hashPassword(inputPassword);
        return hashedInput.equals(storedHash);
    }
}