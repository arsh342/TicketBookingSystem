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
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final Set<String> VALID_GENDERS = new HashSet<>(Arrays.asList(
            "MALE", "FEMALE", "OTHER", "M", "F", "O"
    ));
    private static final double PLANE_PRICE_PER_KM = 5.0;
    private static final double TRAIN_PRICE_PER_KM = 1.0;
    private static final double BUS_PRICE_PER_KM = 0.5;


    // --- Utility Methods ---

    public static void pause(Scanner sc) {
        System.out.print("\n" + YELLOW + "Press Enter to continue..." + RESET); // Yellow prompt
        sc.nextLine();
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // isValidRowColumn remains the same (validation logic)
    public static boolean isValidRowColumn(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        return input.trim().matches("\\d+\\s+[A-Za-z]+");
    }

    /**
     * Prints a more prominent banner with borders.
     * @param title The title text.
     */
    public static void printBanner(String title) {
        int titleLength = title.length();
        String border = CYAN_BOLD + "=".repeat(titleLength + 6) + RESET; // Dynamic border length
        System.out.println("\n" + border);
        System.out.println(CYAN_BOLD + "|| " + WHITE_BOLD + title + CYAN_BOLD + " ||" + RESET);
        System.out.println(border);
    }

    // calculatePrice remains the same (calculation logic)
    public static double calculatePrice(String transportType, int distance) {
        if (distance < 0) {
            System.err.println(RED_BOLD + "Warning:" + RESET + RED + " calculatePrice called with negative distance: " + distance + RESET);
            return 0.0;
        }
        double pricePerKm;
        switch (transportType.toLowerCase()) {
            case "plane": pricePerKm = PLANE_PRICE_PER_KM; break;
            case "train": pricePerKm = TRAIN_PRICE_PER_KM; break;
            case "bus": pricePerKm = BUS_PRICE_PER_KM; break;
            default:
                System.err.println(RED_BOLD + "Warning:" + RESET + RED + " calculatePrice called with unknown transport type: " + transportType + RESET);
                pricePerKm = 0.0;
        }
        return pricePerKm * distance;
    }

    // --- Input Validation Methods (using colors) ---

    public static String getValidTravelDate(Scanner sc) {
        LocalDate travelDate = null;
        LocalDate today = LocalDate.now();
        String inputDateStr = "";
        String prompt = WHITE_BOLD + "Enter travel date (" + YELLOW_BOLD + "DD-MM-YYYY" + WHITE_BOLD + ") or type '" + YELLOW_BOLD + "back" + WHITE_BOLD + "': " + RESET;

        while (travelDate == null) {
            System.out.print(prompt);
            inputDateStr = sc.nextLine().trim();
            if (inputDateStr.equalsIgnoreCase("back")) return null;
            try {
                travelDate = LocalDate.parse(inputDateStr, DATE_FORMATTER);
                if (travelDate.isBefore(today)) {
                    System.out.println(RED + "Travel date cannot be in the past. Please enter today or a future date." + RESET);
                    travelDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println(RED + "Invalid date or format. Example: " + YELLOW + today.format(DATE_FORMATTER) + RESET);
            } catch (Exception e) {
                System.out.println(RED + "Error parsing date: " + e.getMessage() + RESET);
            }
        }
        return inputDateStr;
    }

    public static int getValidAge(Scanner sc) {
        int age = -1;
        String prompt = WHITE_BOLD + "Enter age (" + YELLOW_BOLD + "0-120" + WHITE_BOLD + "): " + RESET;
        while (age < 0) {
            System.out.print(prompt);
            try {
                age = sc.nextInt();
                sc.nextLine();
                if (age < 0 || age > 120) {
                    System.out.println(RED + "Invalid age. Please enter an age between 0 and 120." + RESET);
                    age = -1;
                }
            } catch (InputMismatchException e) {
                System.out.println(RED + "Invalid input. Please enter a number for age." + RESET);
                sc.nextLine();
                age = -1;
            } catch (Exception e) {
                System.out.println(RED + "Error reading age: " + e.getMessage() + RESET);
                age = -1;
            }
        }
        return age;
    }

    public static String getValidGender(Scanner sc) {
        String gender = "";
        boolean isValid = false;
        String prompt = WHITE_BOLD + "Enter gender (" + YELLOW_BOLD + "Male/Female/Other" + WHITE_BOLD + " or " + YELLOW_BOLD + "M/F/O" + WHITE_BOLD + "): " + RESET;
        while (!isValid) {
            System.out.print(prompt);
            gender = sc.nextLine().trim();
            if (gender.isEmpty()) {
                System.out.println(RED + "Gender cannot be empty." + RESET);
            } else if (VALID_GENDERS.contains(gender.toUpperCase())) {
                isValid = true;
            } else {
                System.out.println(RED + "Invalid input. Please use Male, Female, Other, M, F, or O." + RESET);
            }
        }
        // Standardize output to full words for better display later
        switch(gender.toUpperCase()) {
            case "M": return "Male";
            case "F": return "Female";
            case "O": return "Other";
            default: return gender; // Return original valid input if already full word (e.g. "Male")
        }
    }

}