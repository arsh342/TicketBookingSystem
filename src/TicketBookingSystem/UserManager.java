package TicketBookingSystem;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager {
    private HashMap<String, String> users = new HashMap<>();

    public UserManager() {
        users = StorageManager.loadUsers();
    }

    public boolean register(String username, String password) {
        // Basic validation
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.out.println(Utils.RED + "Username and password cannot be empty for registration." + Utils.RESET);
            return false;
        }

        // *** REMOVE THIS CHECK - No longer needed here ***
        // if (username.equalsIgnoreCase("admin")) {
        //     System.out.println(Utils.RED + "Cannot register with the reserved admin username." + Utils.RESET);
        //     return false;
        // }

        // Check if username already exists (case-sensitive)
        if (users.containsKey(username)) {
            // Message handled by caller (Main.java)
            return false;
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            System.err.println(Utils.RED_BOLD + "Critical Error: Password hashing failed. Cannot register user." + Utils.RESET);
            return false;
        }

        users.put(username, hashedPassword);
        StorageManager.saveUsers(users); // Save immediately
        return true;
    }

    // login, hashPassword, getUsersMap, displayAllUsers methods remain the same...
    public boolean login(String username, String password) {
        if (username == null || password == null || !users.containsKey(username)) { return false; }
        String storedHash = users.get(username); String inputHash = hashPassword(password);
        if (inputHash == null || inputHash.isEmpty()) { System.err.println(Utils.RED + "Login failed: hashing error." + Utils.RESET); return false; }
        return java.util.Objects.equals(storedHash, inputHash);
    }

    private String hashPassword(String password) {
        if (password == null) { System.err.println(Utils.RED_BOLD + "Error: Hash null password." + Utils.RESET); return ""; }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8)); StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) { String hex = Integer.toHexString(0xff & b); if (hex.length() == 1) hexString.append('0'); hexString.append(hex); }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) { System.err.println(Utils.RED_BOLD + "CRITICAL ERROR: SHA-256 missing! " + e.getMessage() + Utils.RESET); return ""; }
    }

    public HashMap<String, String> getUsersMap() { return users; }

    public void displayAllUsers() {
        if (users == null || users.isEmpty()) { System.out.println(Utils.YELLOW + "No registered users." + Utils.RESET); return; }
        System.out.println(Utils.CYAN_BOLD + "\n--- Registered Users ---" + Utils.RESET); int count = 0; List<String> sortedUsernames = new ArrayList<>(users.keySet()); Collections.sort(sortedUsernames);
        for (String username : sortedUsernames) { count++; System.out.printf(Utils.YELLOW_BOLD + "%d. " + Utils.MAGENTA + "%s\n" + Utils.RESET, count, username); }
        System.out.println(Utils.CYAN + "------------------------" + Utils.RESET); System.out.println(Utils.BLUE_BOLD + "Total Users: " + Utils.MAGENTA_BOLD + count + Utils.RESET);
    }
}