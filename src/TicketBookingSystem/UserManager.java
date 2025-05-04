package TicketBookingSystem;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class UserManager {
    private HashMap<String, String> users = new HashMap<>();

    public UserManager() {
        users = StorageManager.loadUsers(); // Load existing users at startup
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        String hashedPassword = hashPassword(password);
        users.put(username, hashedPassword);
        StorageManager.saveUsers(users); // Save users after registration
        return true;
    }

    public boolean login(String username, String password) {
        if (!users.containsKey(username)) {
            return false;
        }
        String hashedPassword = hashPassword(password);
        return users.get(username).equals(hashedPassword);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage());
        }
    }
}