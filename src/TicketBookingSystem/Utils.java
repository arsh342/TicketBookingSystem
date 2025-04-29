package TicketBookingSystem;

import java.util.Scanner;

public class Utils {

    public static void pause(Scanner sc) {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }

    public static void clearScreen() {
        for (int i = 0; i < 50; i++) System.out.println();
    }

    public static boolean isValidRowColumn(String input) {
        return input.matches("\\d+\\s+[A-Z]") || input.matches("\\d+\\s+\\d+");
    }

    public static void printBanner(String title) {
        System.out.println("=== " + title + " ===");
    }
}
