package TicketBookingSystem;

import java.util.Scanner;
// Removed java.util.HashMap import as the static distances map is gone.

public class Utils {

    // --- Price Constants (Still needed for calculatePrice) ---
    // Base price per kilometer for each transport type (adjust as needed)
    private static final double PLANE_PRICE_PER_KM = 5.0;  // Rs. 5 per km
    private static final double TRAIN_PRICE_PER_KM = 1.0;  // Rs. 1 per km
    private static final double BUS_PRICE_PER_KM = 0.5;    // Rs. 0.5 per km

    // --- Removed static distances map and initializer block ---
    // private static final HashMap<String, HashMap<String, Integer>> distances = new HashMap<>();
    // static { ... }

    // --- Removed static CITIES array ---
    // public static final String[] CITIES = {"Delhi", "Mumbai", "Chennai", "Kolkata"};


    /**
     * Pauses the execution and waits for the user to press Enter.
     * Clears the input buffer before waiting.
     * @param sc Scanner object to read input.
     */
    public static void pause(Scanner sc) {
        System.out.print("\n\033[1;33mPress Enter to continue...\033[0m");
        // No need for sc.nextLine() here if the previous input consumed the newline.
        // If issues arise, uncomment the next line. Be careful not to consume needed input.
        // if(sc.hasNextLine()) sc.nextLine(); // Consume any leftover newline
        sc.nextLine(); // Wait for Enter key press
    }

    /**
     * Clears the console screen using ANSI escape codes.
     * May not work on all terminals (e.g., some IDE consoles).
     */
    public static void clearScreen() {
        // Standard ANSI escape codes for clearing screen and moving cursor to top-left
        System.out.print("\033[H\033[2J");
        System.out.flush(); // Ensure the codes are sent to the terminal immediately
    }

    /**
     * Validates if the input string matches a pattern for row and column (e.g., "5 B" or "10 A").
     * Note: This might need adjustments based on actual seat formats (e.g., multi-letter columns).
     * @param input The user input string.
     * @return true if the input matches the expected pattern, false otherwise.
     */
    public static boolean isValidRowColumn(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        // Matches one or more digits, followed by whitespace, followed by one or more letters.
        // Allows for rows > 9 and potentially multi-letter columns like "AA".
        return input.trim().matches("\\d+\\s+[A-Za-z]+");
        // Original regex: kept for reference
        // return input.matches("\\d+\\s+[A-Z]") || input.matches("\\d+\\s+\\d+");
    }

    /**
     * Prints a formatted banner with the given title using ANSI colors.
     * @param title The title text to display in the banner.
     */
    public static void printBanner(String title) {
        // Example: === My Title === (in bold cyan)
        System.out.println("\n\033[1;36m=== " + title + " ===\033[0m");
    }

    // --- Removed getDistance method ---
    // public static int getDistance(String start, String dest) { ... }

    /**
     * Calculates the base price for a ticket based on transport type and distance.
     * Uses predefined price-per-kilometer constants.
     *
     * @param transportType The type of transport ("Plane", "Train", "Bus"). Case-insensitive.
     * @param distance The distance of the route in kilometers. Should be >= 0.
     * @return The calculated base price, or 0.0 if transport type is unknown or distance is negative.
     */
    public static double calculatePrice(String transportType, int distance) {
        // Return 0 if distance is invalid
        if (distance < 0) {
            System.err.println("\033[1;31mWarning: calculatePrice called with negative distance: " + distance + "\033[0m");
            return 0.0;
        }

        double pricePerKm;
        // Use toLowerCase() for case-insensitive matching
        switch (transportType.toLowerCase()) {
            case "plane":
                pricePerKm = PLANE_PRICE_PER_KM;
                break;
            case "train":
                pricePerKm = TRAIN_PRICE_PER_KM;
                break;
            case "bus":
                pricePerKm = BUS_PRICE_PER_KM;
                break;
            default:
                System.err.println("\033[1;31mWarning: calculatePrice called with unknown transport type: " + transportType + "\033[0m");
                pricePerKm = 0.0; // Unknown type, return 0 price
        }
        // Calculate and return the total base price
        return pricePerKm * distance;
    }

    // --- Removed displayCities method ---
    // public static void displayCities() { ... }

} // End of Utils class