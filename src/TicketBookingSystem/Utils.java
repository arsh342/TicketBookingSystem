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

public class Utils {

    // Price Constants
    private static final double PLANE_PRICE_PER_KM = 5.0;
    private static final double TRAIN_PRICE_PER_KM = 1.0;
    private static final double BUS_PRICE_PER_KM = 0.5;

    // Date Formatter (Strict parsing for dd-MM-yyyy)
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    // Valid Gender Options (Used for case-insensitive check)
    private static final Set<String> VALID_GENDERS = new HashSet<>(Arrays.asList(
            "MALE", "FEMALE", "OTHER", "M", "F", "O"
    ));

    // --- Standard Utilities (pause, clearScreen, printBanner, etc.) ---

    public static void pause(Scanner sc) {
        System.out.print("\n\033[1;33mPress Enter to continue...\033[0m");
        sc.nextLine();
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static boolean isValidRowColumn(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return input.trim().matches("\\d+\\s+[A-Za-z]+");
    }


    public static void printBanner(String title) {
        System.out.println("\n\033[1;36m=== " + title + " ===\033[0m");
    }

    public static double calculatePrice(String transportType, int distance) {
        if (distance < 0) {
            System.err.println("\033[1;31mWarning: calculatePrice called with negative distance: " + distance + "\033[0m");
            return 0.0;
        }
        double pricePerKm;
        switch (transportType.toLowerCase()) {
            case "plane": pricePerKm = PLANE_PRICE_PER_KM; break;
            case "train": pricePerKm = TRAIN_PRICE_PER_KM; break;
            case "bus": pricePerKm = BUS_PRICE_PER_KM; break;
            default:
                System.err.println("\033[1;31mWarning: calculatePrice called with unknown transport type: " + transportType + "\033[0m");
                pricePerKm = 0.0;
        }
        return pricePerKm * distance;
    }

    // --- NEW VALIDATION METHODS ---

    /**
     * Prompts the user for a travel date (DD-MM-YYYY) and validates it.
     * Ensures the date is valid and not in the past. Allows user to type 'back'.
     *
     * @param sc Scanner for input.
     * @return Valid future date string or null if user types 'back'.
     */
    public static String getValidTravelDate(Scanner sc) {
        LocalDate travelDate = null;
        LocalDate today = LocalDate.now();
        String inputDateStr = "";

        while (travelDate == null) {
            System.out.print("\033[1mEnter travel date (DD-MM-YYYY) or type 'back': \033[0m");
            inputDateStr = sc.nextLine().trim();

            if (inputDateStr.equalsIgnoreCase("back")) {
                return null; // Allow cancellation
            }

            try {
                travelDate = LocalDate.parse(inputDateStr, DATE_FORMATTER);
                if (travelDate.isBefore(today)) {
                    System.out.println("\033[1;31mTravel date cannot be in the past. Please enter today or a future date.\033[0m");
                    travelDate = null; // Loop again
                }
            } catch (DateTimeParseException e) {
                System.out.println("\033[1;31mInvalid date or format. Please use DD-MM-YYYY (e.g., " + today.format(DATE_FORMATTER) + ").\033[0m");
            } catch (Exception e) {
                System.out.println("\033[1;31mError parsing date: " + e.getMessage() + "\033[0m");
            }
        }
        return inputDateStr; // Return the validated input string
    }

    /**
     * Prompts the user for passenger age and validates it (0-120).
     *
     * @param sc Scanner for input.
     * @return Valid integer age.
     */
    public static int getValidAge(Scanner sc) {
        int age = -1;
        while (age < 0) {
            System.out.print("\033[1mEnter age (0-120): \033[0m");
            try {
                age = sc.nextInt();
                sc.nextLine(); // Consume newline

                if (age < 0 || age > 120) {
                    System.out.println("\033[1;31mInvalid age. Please enter an age between 0 and 120.\033[0m");
                    age = -1; // Reset to loop
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number for age.\033[0m");
                sc.nextLine(); // Consume invalid input
                age = -1;
            } catch (Exception e) {
                System.out.println("\033[1;31mError reading age: " + e.getMessage() + "\033[0m");
                age = -1;
            }
        }
        return age;
    }

    /**
     * Prompts the user for gender and validates against allowed options.
     *
     * @param sc Scanner for input.
     * @return Validated gender string.
     */
    public static String getValidGender(Scanner sc) {
        String gender = "";
        boolean isValid = false;
        while (!isValid) {
            System.out.print("\033[1mEnter gender (Male/Female/Other or M/F/O): \033[0m");
            gender = sc.nextLine().trim();

            if (gender.isEmpty()) {
                System.out.println("\033[1;31mGender cannot be empty.\033[0m");
            } else if (VALID_GENDERS.contains(gender.toUpperCase())) {
                isValid = true;
            } else {
                System.out.println("\033[1;31mInvalid input. Please use Male, Female, Other, M, F, or O.\033[0m");
            }
        }
        return gender; // Return validated input
    }

} // End of Utils class