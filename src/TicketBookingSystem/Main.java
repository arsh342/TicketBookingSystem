package TicketBookingSystem;

import java.util.InputMismatchException; // Import for exception handling
import java.util.Scanner;

public class Main {
    // Instantiate RouteDataManager once when the application starts
    private static RouteDataManager routeDataManager = new RouteDataManager();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserManager userManager = new UserManager();
        // Pass the RouteDataManager instance to BookingSystem if needed,
        // but currently BookingSystem creates its own instance.
        BookingSystem bookingSystem = new BookingSystem();

        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Welcome to Ticket Booking System");
            System.out.println("\033[1;33m1.\033[0m Register");
            System.out.println("\033[1;33m2.\033[0m Login");
            System.out.println("\033[1;33m3.\033[0m View Route Information"); // New Option
            System.out.println("\033[1;33m4.\033[0m Exit");                  // Renumbered Exit
            System.out.print("\033[1mChoose an option: \033[0m");

            int choice = -1; // Default to invalid choice
            try {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) { // Catch non-integer input
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume the invalid input
                Utils.pause(sc);
                continue; // Loop back to main menu
            } catch (Exception e) { // Catch other potential exceptions
                System.out.println("\033[1;31mAn unexpected error occurred: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline if present
                Utils.pause(sc);
                continue; // Loop back to main menu
            }


            switch (choice) {
                case 1:
                    registerUser(sc, userManager);
                    break;
                case 2:
                    if (loginUser(sc, userManager, bookingSystem)) {
                        // Pass RouteDataManager if BookingSystem needs it later (currently not needed)
                        bookingSystem.startBooking(sc); // Enter the booking sub-menu
                    }
                    break;
                case 3:
                    showRouteInformationMenu(sc); // Call the new menu method
                    break;
                case 4: // Updated Exit option number
                    System.out.println("\033[1;32mThank you for using the Ticket Booking System. Goodbye!\033[0m");
                    // Ensure data is saved before exiting (BookingSystem handles saves internally now)
                    sc.close(); // Close the scanner
                    return; // Exit the application
                default:
                    System.out.println("\033[1;31mInvalid option. Please try again.\033[0m");
                    Utils.pause(sc);
            }
        } // End of while loop
    }

    // --- Register User Method ---
    private static void registerUser(Scanner sc, UserManager userManager) {
        Utils.clearScreen();
        Utils.printBanner("Register");
        String username = "";
        while (username.isEmpty()) {
            System.out.print("\033[1mEnter username: \033[0m");
            username = sc.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("\033[1;31mUsername cannot be empty.\033[0m");
            }
        }

        String password = "";
        while (password.isEmpty()) {
            System.out.print("\033[1mEnter password: \033[0m");
            password = sc.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("\033[1;31mPassword cannot be empty.\033[0m");
            }
            // Add password complexity check if needed
        }


        if (userManager.register(username, password)) {
            System.out.println("\033[1;32mRegistration successful! You can now log in.\033[0m");
        } else {
            System.out.println("\033[1;31mUsername '" + username + "' already exists. Try a different one.\033[0m");
        }
        Utils.pause(sc);
    }

    // --- Login User Method ---
    private static boolean loginUser(Scanner sc, UserManager userManager, BookingSystem bookingSystem) {
        Utils.clearScreen();
        Utils.printBanner("Login");
        System.out.print("\033[1mEnter username: \033[0m");
        String username = sc.nextLine().trim();
        System.out.print("\033[1mEnter password: \033[0m");
        String password = sc.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("\033[1;31mUsername and password cannot be empty.\033[0m");
            Utils.pause(sc);
            return false;
        }

        if (userManager.login(username, password)) {
            System.out.println("\033[1;32mLogin successful! Welcome, " + username + "!\033[0m");
            bookingSystem.setLoggedInUser(username); // Set user in BookingSystem
            Utils.pause(sc); // Pause briefly after successful login
            return true;
        } else {
            System.out.println("\033[1;31mInvalid username or password.\033[0m");
            Utils.pause(sc);
            return false;
        }
    }

    /**
     * Displays the menu for viewing route information using the static RouteDataManager.
     * @param sc Scanner for user input.
     */
    private static void showRouteInformationMenu(Scanner sc) {
        if (routeDataManager == null) {
            System.out.println("\033[1;31mError: Route Data Manager is not initialized. Cannot show information.\033[0m");
            Utils.pause(sc);
            return;
        }

        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Route Information Menu");
            System.out.println("\033[1;33m1.\033[0m View All Airports");
            System.out.println("\033[1;33m2.\033[0m View All Bus Stations");
            System.out.println("\033[1;33m3.\033[0m View All Train Stations");
            System.out.println("\033[1;33m4.\033[0m View Information by City");
            System.out.println("\033[1;33m0.\033[0m Back to Main Menu");
            System.out.print("\033[1mChoose an option: \033[0m");

            int choice = -1;
            try {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume the invalid input
                Utils.pause(sc);
                continue;
            } catch (Exception e) {
                System.out.println("\033[1;31mAn unexpected error occurred: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline if present
                Utils.pause(sc);
                continue;
            }


            switch (choice) {
                case 1:
                    Utils.clearScreen();
                    routeDataManager.displayAllAirports();
                    Utils.pause(sc);
                    break;
                case 2:
                    Utils.clearScreen();
                    routeDataManager.displayAllBusStations();
                    Utils.pause(sc);
                    break;
                case 3:
                    Utils.clearScreen();
                    routeDataManager.displayAllTrainStations();
                    Utils.pause(sc);
                    break;
                case 4:
                    System.out.print("\033[1mEnter city name to view information: \033[0m");
                    String city = sc.nextLine().trim();
                    if (!city.isEmpty()) {
                        routeDataManager.displayInfoForCity(city, sc);
                        // The pause is handled within displayInfoForCity
                    } else {
                        System.out.println("\033[1;31mCity name cannot be empty.\033[0m");
                        Utils.pause(sc);
                    }
                    break;
                case 0:
                    return; // Go back to the main menu
                default:
                    System.out.println("\033[1;31mInvalid option. Please try again.\033[0m");
                    Utils.pause(sc);
            }
        }
    }
}