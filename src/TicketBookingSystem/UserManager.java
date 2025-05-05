package TicketBookingSystem;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap; // Ensure this import exists

public class UserManager {
    // Keep users map private
    private HashMap<String, String> users = new HashMap<>();

    public UserManager() {
        users = StorageManager.loadUsers(); // Load existing users at startup
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            System.out.println("\033[1;31mUsername '" + username + "' already exists.\033[0m");
            return false; // Username already exists
        }
        // Prevent registration with empty username or password
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.out.println("\033[1;31mUsername and password cannot be empty.\033[0m");
            return false;
        }
        String hashedPassword = hashPassword(password);
        // Check if hashing failed
        if (hashedPassword.isEmpty()) {
            System.out.println("\033[1;31mPassword hashing failed. Cannot register user.\033[0m");
            return false;
        }

        users.put(username, hashedPassword);
        StorageManager.saveUsers(users); // Save users immediately after registration
        return true;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null || !users.containsKey(username)) {
            return false; // User doesn't exist or input is null
        }
        String hashedPassword = hashPassword(password);
        // Check if hashing failed
        if (hashedPassword.isEmpty()) {
            return false;
        }
        // Use null-safe equals comparison, although stored password should not be null
        return java.util.Objects.equals(users.get(username), hashedPassword);
    }

    private String hashPassword(String password) {
        // Consider adding a salt for better security in a real application
        if (password == null) { // Handle null password input gracefully
            System.err.println("\033[1;31mError: Attempted to hash a null password.\033[0m");
            return ""; // Return empty string to indicate failure
        }
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
            // Log error more permanently in a real app
            System.err.println("\033[1;31mCritical Error: SHA-256 algorithm not found. " + e.getMessage() + "\033[0m");
            return ""; // Indicate hashing failure
        }
    }

    // --- GETTER METHOD FOR SAVING ---
    /**
     * Returns the internal map of users.
     * Used by StorageManager to save user data.
     * @return HashMap<String, String> containing usernames and hashed passwords.
     */
    public HashMap<String, String> getUsersMap() {
        // Return a copy to prevent external modification (optional but safer)
        // return new HashMap<>(users);
        return users; // Returning direct reference for simplicity here
    }

} // End of UserManager class