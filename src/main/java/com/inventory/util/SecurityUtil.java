package com.inventory.util;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class SecurityUtil {

    
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

  
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        
        // 1. Hash the password the user just typed (e.g., "123")
        String newHash = hashPassword(plainPassword);
        
        // 2. Check if it matches the DB hash ("a665...")
        return newHash.equals(storedHash);
    }

    // Helper to turn the hash bytes into the long string you see in MySQL
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}