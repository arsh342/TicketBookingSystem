package TicketBookingSystem;

import java.io.*;
import java.util.HashMap;

/**
 * Handles saving and loading user and seat booking data for persistence across sessions.
 */
public class StorageManager {
    private static final String USER_FILE = "users.txt";
    private static final String SEATS_FILE = "seats.txt";

    /**
     * Saves user credentials to disk.
     */
    public static void saveUsers(HashMap<String, String> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (var entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    /**
     * Loads user credentials from disk.
     */
    public static HashMap<String, String> loadUsers() {
        HashMap<String, String> users = new HashMap<>();
        File file = new File(USER_FILE);
        if (!file.exists()) return users;

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Saves booked seat IDs to disk.
     */
    public static void saveBookedSeats(HashMap<String, Boolean> bookedSeats) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SEATS_FILE))) {
            for (var entry : bookedSeats.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving seats: " + e.getMessage());
        }
    }

    /**
     * Loads booked seat IDs from disk.
     */
    public static HashMap<String, Boolean> loadBookedSeats() {
        HashMap<String, Boolean> bookedSeats = new HashMap<>();
        File file = new File(SEATS_FILE);
        if (!file.exists()) return bookedSeats;

        try (BufferedReader reader = new BufferedReader(new FileReader(SEATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    bookedSeats.put(parts[0], Boolean.parseBoolean(parts[1]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading seats: " + e.getMessage());
        }
        return bookedSeats;
    }
}
