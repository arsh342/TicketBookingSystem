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
// Removed HashMap import - no longer needed after removing distance map

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
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu") // Use uuuu for year to avoid confusion with week-based year 'yyyy'
                    .withResolverStyle(ResolverStyle.STRICT); // Ensures dates like 31-02-2025 are rejected

    // Valid Gender Options (Checked case-insensitively)
    private static final Set<String> VALID_GENDERS = new HashSet<>(Arrays.asList(
            "MALE", "FEMALE", "OTHER", "M", "F", "O"
    ));

    // Base price per kilometer (Remains useful for calculating base fare)
    private static final double PLANE_PRICE_PER_KM = 5.0;
    private static final double TRAIN_PRICE_PER_KM = 1.0;
    private static final double BUS_PRICE_PER_KM = 0.5;


    // --- Utility Methods ---

    /**
     * Pauses execution until the user presses Enter.
     * @param sc Scanner instance.
     */
    public static void pause(Scanner sc) {
        System.out.print("\n" + YELLOW + "Press Enter to continue..." + RESET);
        sc.nextLine(); // Wait for Enter
    }

    /**
     * Clears the console screen using ANSI codes.
     * Note: May not work in all environments (like some IDE consoles).
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Validates if input matches a basic row/column format (e.g., "5 B").
     * @param input User input string.
     * @return true if format is potentially valid, false otherwise.
     */
    public static boolean isValidRowColumn(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        // Allows one or more digits, whitespace, one or more letters
        return input.trim().matches("\\d+\\s+[A-Za-z]+");
    }

    /**
     * Prints a styled banner with borders.
     * @param title The text for the banner.
     */
    public static void printBanner(String title) {
        int titleLength = title.length();
        // Ensure minimum length for aesthetics
        int borderLength = Math.max(titleLength + 6, 20);
        String border = CYAN_BOLD + "=".repeat(borderLength) + RESET;
        // Center title approximately
        int paddingTotal = borderLength - titleLength - 4; // 4 for "||  ||"
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;
        String titleLine = String.format("%s||%s%s%s%s%s||%s",
                CYAN_BOLD, " ".repeat(paddingLeft), WHITE_BOLD, title, RESET, " ".repeat(paddingRight), CYAN_BOLD);

        System.out.println("\n" + border);
        System.out.println(titleLine);
        System.out.println(border);
    }

    /**
     * Calculates base ticket price from distance and transport type.
     * Distance now comes from RouteDataManager.
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
        switch (transportType.toLowerCase()) { // Case-insensitive match
            case "plane": pricePerKm = PLANE_PRICE_PER_KM; break;
            case "train": pricePerKm = TRAIN_PRICE_PER_KM; break;
            case "bus": pricePerKm = BUS_PRICE_PER_KM; break;
            default:
                System.err.println(RED_BOLD + "Warning:" + RESET + RED + " calculatePrice called with unknown transport type: " + transportType + RESET);
                pricePerKm = 0.0; // Unknown type
        }
        return pricePerKm * distance;
    }

    /**
     * Prompts for and validates travel date (DD-MM-YYYY, future date).
     * Allows user to type 'back'.
     * @param sc Scanner instance.
     * @return Valid date string or null if user types 'back'.
     */
    public static String getValidTravelDate(Scanner sc) {
        LocalDate travelDate = null;
        LocalDate today = LocalDate.now();
        String inputDateStr = "";
        String prompt = WHITE_BOLD + "Enter travel date (" + YELLOW_BOLD + "DD-MM-YYYY" + WHITE_BOLD + ") or type '" + YELLOW_BOLD + "back" + WHITE_BOLD + "': " + RESET;

        while (travelDate == null) {
            System.out.print(prompt);
            inputDateStr = sc.nextLine().trim();
            if (inputDateStr.equalsIgnoreCase("back")) return null; // Allow cancellation

            try {
                travelDate = LocalDate.parse(inputDateStr, DATE_FORMATTER);
                if (travelDate.isBefore(today)) {
                    System.out.println(RED + "Travel date cannot be in the past. Please enter today or a future date." + RESET);
                    travelDate = null; // Reset to loop again
                }
            } catch (DateTimeParseException e) {
                System.out.println(RED + "Invalid date or format. Example: " + YELLOW + today.format(DATE_FORMATTER) + RESET);
            } catch (Exception e) {
                System.out.println(RED + "Error parsing date: " + e.getMessage() + RESET);
            }
        }
        return inputDateStr; // Return the validated original string
    }

    /**
     * Prompts for and validates passenger age (0-120).
     * @param sc Scanner instance.
     * @return Valid integer age.
     */
    public static int getValidAge(Scanner sc) {
        int age = -1;
        String prompt = WHITE_BOLD + "Enter age (" + YELLOW_BOLD + "0-120" + WHITE_BOLD + "): " + RESET;
        while (age < 0) {
            System.out.print(prompt);
            try {
                age = sc.nextInt();
                sc.nextLine(); // Consume newline
                if (age < 0 || age > 120) { // Validate range
                    System.out.println(RED + "Invalid age. Please enter an age between 0 and 120." + RESET);
                    age = -1; // Reset to loop
                }
            } catch (InputMismatchException e) {
                System.out.println(RED + "Invalid input. Please enter a number for age." + RESET);
                sc.nextLine(); // Consume invalid input
                age = -1; // Ensure loop continues
            } catch (Exception e) {
                System.out.println(RED + "Error reading age: " + e.getMessage() + RESET);
                age = -1; // Ensure loop continues
            }
        }
        return age;
    }

    /**
     * Prompts for and validates passenger gender against allowed options.
     * Returns standardized full gender name.
     * @param sc Scanner instance.
     * @return Validated, standardized gender string ("Male", "Female", "Other").
     */
    public static String getValidGender(Scanner sc) {
        String genderInput = "";
        String standardizedGender = "";
        boolean isValid = false;
        String prompt = WHITE_BOLD + "Enter gender (" + YELLOW_BOLD + "Male/Female/Other" + WHITE_BOLD + " or " + YELLOW_BOLD + "M/F/O" + WHITE_BOLD + "): " + RESET;

        while (!isValid) {
            System.out.print(prompt);
            genderInput = sc.nextLine().trim();
            String upperCaseInput = genderInput.toUpperCase();

            if (genderInput.isEmpty()) {
                System.out.println(RED + "Gender cannot be empty." + RESET);
            } else if (VALID_GENDERS.contains(upperCaseInput)) {
                isValid = true;
                // Standardize to full word
                switch(upperCaseInput) {
                    case "M": standardizedGender = "Male"; break;
                    case "F": standardizedGender = "Female"; break;
                    case "O": standardizedGender = "Other"; break;
                    default: standardizedGender = genderInput; // Assume full word was entered correctly
                }
            } else {
                System.out.println(RED + "Invalid input. Please use Male, Female, Other, M, F, or O." + RESET);
            }
        }
        return standardizedGender;
    }

} // End of Utils class