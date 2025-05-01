package TicketBookingSystem;

import java.util.HashMap;
import java.util.Scanner;

public class Utils {
    // Define cities and distances (in kilometers) as a simple lookup table
    private static final HashMap<String, HashMap<String, Integer>> distances = new HashMap<>();

    static {
        // Initialize distances between cities (symmetric)
        HashMap<String, Integer> delhi = new HashMap<>();
        delhi.put("Mumbai", 1200);
        delhi.put("Chennai", 2200);
        delhi.put("Kolkata", 1500);
        distances.put("Delhi", delhi);

        HashMap<String, Integer> mumbai = new HashMap<>();
        mumbai.put("Delhi", 1200);
        mumbai.put("Chennai", 1300);
        mumbai.put("Kolkata", 1900);
        distances.put("Mumbai", mumbai);

        HashMap<String, Integer> chennai = new HashMap<>();
        chennai.put("Delhi", 2200);
        chennai.put("Mumbai", 1300);
        chennai.put("Kolkata", 1700);
        distances.put("Chennai", chennai);

        HashMap<String, Integer> kolkata = new HashMap<>();
        kolkata.put("Delhi", 1500);
        kolkata.put("Mumbai", 1900);
        kolkata.put("Chennai", 1700);
        distances.put("Kolkata", kolkata);
    }

    // Base price per kilometer for each transport type
    private static final double PLANE_PRICE_PER_KM = 5.0;  // Rs. 5 per km
    private static final double TRAIN_PRICE_PER_KM = 1.0;  // Rs. 1 per km
    private static final double BUS_PRICE_PER_KM = 0.5;    // Rs. 0.5 per km

    // Available cities for user selection
    public static final String[] CITIES = {"Delhi", "Mumbai", "Chennai", "Kolkata"};

    public static void pause(Scanner sc) {
        System.out.println("\n\033[1;33mPress Enter to continue...\033[0m");
        sc.nextLine();
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static boolean isValidRowColumn(String input) {
        return input.matches("\\d+\\s+[A-Z]") || input.matches("\\d+\\s+\\d+");
    }

    public static void printBanner(String title) {
        System.out.println("\n\033[1;36m=== " + title + " ===\033[0m");
    }

    // Calculate distance between two cities
    public static int getDistance(String start, String dest) {
        start = start.substring(0, 1).toUpperCase() + start.substring(1).toLowerCase();
        dest = dest.substring(0, 1).toUpperCase() + dest.substring(1).toLowerCase();

        if (!distances.containsKey(start) || !distances.get(start).containsKey(dest)) {
            return -1; // Invalid route
        }
        return distances.get(start).get(dest);
    }

    // Calculate price based on transport type and distance
    public static double calculatePrice(String transportType, int distance) {
        double pricePerKm;
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
                pricePerKm = 0;
        }
        return pricePerKm * distance;
    }

    // Display available cities
    public static void displayCities() {
        System.out.println("\n\033[1;36mAvailable Cities:\033[0m");
        for (int i = 0; i < CITIES.length; i++) {
            System.out.println("\033[1;33m" + (i + 1) + ".\033[0m " + CITIES[i]);
        }
    }
}