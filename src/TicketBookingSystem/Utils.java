package TicketBookingSystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern; // Required for email regex

/**
 * Utility class providing helper methods for console interaction,
 * validation, formatting, and calculations.
 */
public class Utils {

    // --- ANSI Color Constants ---
    public static final String RESET = "\033[0m"; // Text Reset

    // Regular Colors
    public static final String RED = "\033[0;31m";    // RED - Errors
    public static final String GREEN = "\033[0;32m";  // GREEN - Success
    public static final String YELLOW = "\033[0;33m"; // YELLOW - Warnings, prompts
    public static final String BLUE = "\033[0;34m";   // BLUE - Information labels
    public static final String MAGENTA = "\033[0;35m";// MAGENTA - Data emphasis
    public static final String CYAN = "\033[0;36m";   // CYAN - Headers, sections
    public static final String GREY = "\033[0;90m";   // GREY - Debug, less important info

    // Bold
    public static final String RED_BOLD = "\033[1;31m";   // RED - Critical errors
    public static final String GREEN_BOLD = "\033[1;32m"; // GREEN - Major success (Booking Confirmed)
    public static final String YELLOW_BOLD = "\033[1;33m";// YELLOW - Important prompts/choices
    public static final String BLUE_BOLD = "\033[1;34m";  // BLUE - Data labels (Booking ID:)
    public static final String MAGENTA_BOLD = "\033[1;35m"; // MAGENTA - Highlighted data
    public static final String CYAN_BOLD = "\033[1;36m";  // CYAN - Banners, Titles
    public static final String WHITE_BOLD = "\033[1;37m"; // WHITE - Standard prompts


    // --- Other Constants ---
    // Date Formatter for DD-MM-YYYY with strict validation
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu") // Use uuuu for year to avoid confusion with week-based year 'yyyy'
                    .withResolverStyle(ResolverStyle.STRICT); // Ensures dates like 31-02-2025 are rejected

    // Allowed gender inputs (checked case-insensitively)
    private static final Set<String> VALID_GENDERS = new HashSet<>(Arrays.asList(
            "MALE", "FEMALE", "OTHER", "M", "F", "O"
    ));

    // Base price per kilometer rates
    private static final double PLANE_PRICE_PER_KM = 5.0;
    private static final double TRAIN_PRICE_PER_KM = 1.0;
    private static final double BUS_PRICE_PER_KM = 0.5;

    // Basic Email Regex Pattern
    // Allows common characters, requires @, requires domain with ., and TLD of 2-7 letters
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );


    // --- Utility Methods ---

    /**
     * Pauses execution until the user presses Enter.
     * @param sc Scanner instance.
     */
    public static void pause(Scanner sc) {
        System.out.print("\n" + YELLOW + "Press Enter to continue..." + RESET);
        sc.nextLine(); // Wait for Enter key press
    }

    /**
     * Clears the console screen using ANSI escape codes.
     * Note: May not work in all environments (like some IDE consoles).
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Validates if input string matches a basic "Row Column" format (e.g., "5 B").
     * Allows multi-digit rows and multi-letter columns.
     * @param input User input string.
     * @return true if format is potentially valid, false otherwise.
     */
    public static boolean isValidRowColumn(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        // Matches: one or more digits (\d+), one or more spaces (\s+), one or more letters ([A-Za-z]+)
        return input.trim().matches("\\d+\\s+[A-Za-z]+");
    }

    /**
     * Prints a styled banner with the given title, using dynamic borders.
     * @param title The text for the banner.
     */
    public static void printBanner(String title) {
        int titleLength = title.length();
        // Set a minimum border length for visual consistency
        int borderLength = Math.max(titleLength + 6, 30); // Min length of 30
        String border = CYAN_BOLD + "=".repeat(borderLength) + RESET;
        // Calculate padding for approximate centering
        int paddingTotal = borderLength - titleLength - 4; // Account for "||  ||"
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;
        // Ensure padding is not negative if title is very long
        paddingLeft = Math.max(0, paddingLeft);
        paddingRight = Math.max(0, paddingRight);

        // Construct the title line with padding
        String titleLine = String.format("%s||%s%s%s%s%s||%s",
                CYAN_BOLD, " ".repeat(paddingLeft), WHITE_BOLD, title, RESET, " ".repeat(paddingRight), CYAN_BOLD);

        // Print the banner
        System.out.println("\n" + border);
        System.out.println(titleLine);
        System.out.println(border);
    }

    /**
     * Calculates base ticket price from distance and transport type.
     * Distance should be obtained from RouteDataManager.
     * @param transportType "Plane", "Train", or "Bus".
     * @param distance Distance in km (should be >= 0).
     * @return Calculated base price, or 0.0 if input is invalid.
     */
    public static double calculatePrice(String transportType, int distance) {
        if (distance < 0) {
            System.err.println(RED_BOLD + "Warning:" + RESET + RED + " calculatePrice called with negative distance: " + distance + RESET);
            return 0.0; // Invalid distance
        }
        double pricePerKm;
        switch (transportType.toLowerCase()) { // Use lowercase for case-insensitivity
            case "plane": pricePerKm = PLANE_PRICE_PER_KM; break;
            case "train": pricePerKm = TRAIN_PRICE_PER_KM; break;
            case "bus": pricePerKm = BUS_PRICE_PER_KM; break;
            default:
                System.err.println(RED_BOLD + "Warning:" + RESET + RED + " calculatePrice called with unknown transport type: " + transportType + RESET);
                pricePerKm = 0.0; // Unknown type results in 0 price
        }
        return pricePerKm * distance;
    }

    // --- Input Validation Helper Methods ---

    /**
     * Prompts the user for a travel date (DD-MM-YYYY) and validates it.
     * Ensures the date is syntactically valid and not in the past.
     * Allows the user to type 'back' to cancel.
     *
     * @param sc Scanner instance for reading input.
     * @return Valid future date string in DD-MM-YYYY format, or null if the user types 'back'.
     */
    public static String getValidTravelDate(Scanner sc) {
        LocalDate travelDate = null;
        LocalDate today = LocalDate.now(); // Get current date for comparison
        String inputDateStr = "";
        String prompt = WHITE_BOLD + "Enter travel date (" + YELLOW_BOLD + "DD-MM-YYYY" + WHITE_BOLD + ") or type '" + YELLOW_BOLD + "back" + WHITE_BOLD + "': " + RESET;

        while (travelDate == null) {
            System.out.print(prompt);
            inputDateStr = sc.nextLine().trim();

            if (inputDateStr.equalsIgnoreCase("back")) {
                return null; // Allow user to cancel/go back
            }

            try {
                // Attempt to parse the date using the strict formatter
                travelDate = LocalDate.parse(inputDateStr, DATE_FORMATTER);

                // Check if the parsed date is before today
                if (travelDate.isBefore(today)) {
                    System.out.println(RED + "Travel date cannot be in the past. Please enter today or a future date." + RESET);
                    travelDate = null; // Reset to null to continue the loop
                }
                // Optional: Add check for date too far in the future (e.g., > 1 year)
                // else if (travelDate.isAfter(today.plusYears(1))) {
                //    System.out.println(YELLOW + "Booking currently only available up to one year in advance." + RESET);
                //    travelDate = null;
                // }

            } catch (DateTimeParseException e) {
                // Handle invalid date format or impossible dates (like 31 Feb)
                System.out.println(RED + "Invalid date or format. Example: " + YELLOW + today.format(DATE_FORMATTER) + RESET);
                // travelDate remains null, loop continues
            } catch (Exception e) {
                // Catch any other unexpected errors during parsing
                System.out.println(RED + "Error parsing date: " + e.getMessage() + RESET);
                // travelDate remains null
            }
        }
        // Return the original validated input string (keeps user's format)
        return inputDateStr;
    }

    /**
     * Prompts the user for passenger age and validates it's within a reasonable range (0-120).
     * Handles non-numeric input.
     * @param sc Scanner instance for reading input.
     * @return Valid integer age.
     */
    public static int getValidAge(Scanner sc) {
        int age = -1; // Initialize age to an invalid value
        String prompt = WHITE_BOLD + "Enter age (" + YELLOW_BOLD + "0-120" + WHITE_BOLD + "): " + RESET;
        while (age < 0) { // Loop until age is 0 or greater
            System.out.print(prompt);
            try {
                age = sc.nextInt();
                sc.nextLine(); // Consume the rest of the line including newline

                // Validate the entered age range
                if (age < 0 || age > 120) { // Adjust upper limit if necessary
                    System.out.println(RED + "Invalid age. Please enter an age between 0 and 120." + RESET);
                    age = -1; // Reset age to continue loop
                }
                // If age is valid (0-120), the loop condition (age < 0) is false, and the loop exits.

            } catch (InputMismatchException e) {
                System.out.println(RED + "Invalid input. Please enter a number for age." + RESET);
                sc.nextLine(); // Consume the invalid non-numeric input
                age = -1; // Ensure loop continues
            } catch (Exception e) {
                System.out.println(RED + "Error reading age: " + e.getMessage() + RESET);
                age = -1; // Ensure loop continues
            }
        }
        return age; // Return the validated age
    }

    /**
     * Prompts the user for gender and validates it against allowed options (case-insensitive).
     * Returns the standardized full gender name ("Male", "Female", "Other").
     * @param sc Scanner instance for reading input.
     * @return Validated, standardized gender string.
     */
    public static String getValidGender(Scanner sc) {
        String genderInput = "";
        String standardizedGender = "";
        boolean isValid = false;
        String prompt = WHITE_BOLD + "Enter gender (" + YELLOW_BOLD + "Male/Female/Other" + WHITE_BOLD + " or " + YELLOW_BOLD + "M/F/O" + WHITE_BOLD + "): " + RESET;

        while (!isValid) {
            System.out.print(prompt);
            genderInput = sc.nextLine().trim(); // Read and trim input
            String upperCaseInput = genderInput.toUpperCase(); // For case-insensitive check

            if (genderInput.isEmpty()) {
                System.out.println(RED + "Gender cannot be empty." + RESET);
            } else if (VALID_GENDERS.contains(upperCaseInput)) {
                isValid = true; // Input is one of the allowed options
                // Standardize the output to full words
                switch(upperCaseInput) {
                    case "M": standardizedGender = "Male"; break;
                    case "F": standardizedGender = "Female"; break;
                    case "O": standardizedGender = "Other"; break;
                    // If user typed full word correctly (e.g., "Male"), use it directly after standardizing case
                    default: standardizedGender = genderInput.substring(0, 1).toUpperCase() + genderInput.substring(1).toLowerCase(); break;
                }
            } else {
                // Input didn't match any valid options
                System.out.println(RED + "Invalid input. Please use Male, Female, Other, M, F, or O." + RESET);
            }
        }
        return standardizedGender; // Return the standardized, validated gender
    }

    /**
     * Prompts the user for an email address and performs basic regex validation.
     * Allows the user to type 'back' to cancel.
     * @param sc Scanner instance.
     * @return A validated email string, or null if the user typed 'back'.
     */
    public static String getValidEmail(Scanner sc) {
        String email = "";
        boolean isValid = false;
        String prompt = WHITE_BOLD + "Enter passenger email (or type 'back'): " + RESET;

        while (!isValid) {
            System.out.print(prompt);
            email = sc.nextLine().trim();

            if (email.equalsIgnoreCase("back")) {
                return null; // Allow cancellation
            }

            if (email.isEmpty()) {
                System.out.println(RED + "Email cannot be empty." + RESET);
            } else if (EMAIL_PATTERN.matcher(email).matches()) {
                isValid = true; // Email matches the basic pattern
            } else {
                System.out.println(RED + "Invalid email format. Please enter a valid email address (e.g., user@example.com)." + RESET);
            }
        }
        return email; // Return the validated email string
    }

    /**
     * Simulates a payment process, prompting for method and dummy details.
     * Includes an artificial delay to mimic processing.
     *
     * @param sc Scanner instance.
     * @param amount The amount to be "paid".
     * @return true if payment simulation is "successful", false if cancelled by user or interrupted.
     */
    public static boolean simulatePayment(Scanner sc, double amount) {
        Utils.printBanner("Payment Simulation");
        System.out.printf(Utils.WHITE_BOLD + "Amount Due: " + Utils.GREEN_BOLD + "Rs. %.2f\n" + Utils.RESET, amount);
        System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Credit Card" + Utils.RESET);
        System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Debit Card" + Utils.RESET);
        System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " UPI" + Utils.RESET);
        System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Cancel Payment" + Utils.RESET);
        System.out.print(Utils.WHITE_BOLD + "Choose payment method: " + Utils.RESET);

        int choice = -1; // Initialize choice
        while (choice == -1) { // Loop until valid choice (0-3)
            try {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
                if (choice < 0 || choice > 3) { // Validate range
                    System.out.println(Utils.RED + "Invalid choice. Please select 0-3." + Utils.RESET);
                    System.out.print(Utils.WHITE_BOLD + "Choose payment method: " + Utils.RESET); // Re-prompt
                    choice = -1; // Reset to loop again
                }
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume invalid input
                System.out.print(Utils.WHITE_BOLD + "Choose payment method: " + Utils.RESET); // Re-prompt
                choice = -1; // Ensure loop continues
            } catch (Exception e){
                System.out.println(Utils.RED + "An error occurred: " + e.getMessage() + Utils.RESET);
                choice = -1; // Ensure loop continues
            }
        }

        // Handle payment method selection
        switch (choice) {
            case 1: // Credit Card
            case 2: // Debit Card
                // Prompt for dummy details - DO NOT STORE THESE
                System.out.print(Utils.WHITE_BOLD + "Enter dummy Card Number (e.g., 1111222233334444): " + Utils.RESET);
                sc.nextLine(); // Read dummy input
                System.out.print(Utils.WHITE_BOLD + "Enter dummy Expiry (e.g., 12/28): " + Utils.RESET);
                sc.nextLine(); // Read dummy input
                System.out.print(Utils.WHITE_BOLD + "Enter dummy CVV (e.g., 123): " + Utils.RESET);
                sc.nextLine(); // Read dummy input
                break;
            case 3: // UPI
                System.out.print(Utils.WHITE_BOLD + "Enter dummy UPI ID (e.g., user@bank): " + Utils.RESET);
                sc.nextLine(); // Read dummy input
                break;
            case 0: // Cancel
                System.out.println(Utils.YELLOW + "\nPayment cancelled by user." + Utils.RESET);
                return false; // Indicate cancellation
            default:
                // Should not be reachable due to validation loop, but good practice
                System.out.println(Utils.RED + "Unexpected payment method choice." + Utils.RESET);
                return false;
        }

        // Simulate processing delay
        System.out.print("\n" + Utils.CYAN + "Processing payment..." + Utils.RESET);
        try {
            for (int i = 0; i < 3; i++) {
                Thread.sleep(500); // Wait 0.5 seconds
                System.out.print(Utils.CYAN + "." + Utils.RESET);
            }
            System.out.println(); // Newline after dots
            Thread.sleep(300); // Short final pause
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Set interrupt flag
            System.out.println("\n" + Utils.RED + "Payment processing interrupted." + Utils.RESET);
            return false; // Indicate failure/interruption
        }

        // Simulate success
        System.out.println(Utils.GREEN_BOLD + "Payment Successful!" + Utils.RESET);
        return true; // Indicate success
    }

} // End of Utils class