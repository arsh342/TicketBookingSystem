package TicketBookingSystem;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.Console;
import java.util.List;      // Required for List type used by getters
import java.util.HashMap;   // Required for HashMap type used by getUsersMap()

public class Main {
    // Instantiate Managers once
    // Assuming RouteDataManager constructor loads data automatically
    private static RouteDataManager routeDataManager = new RouteDataManager();
    private static UserManager userManager = new UserManager();
    private static BookingSystem bookingSystem = new BookingSystem(); // Assumes BookingSystem constructor uses its own RouteDataManager or doesn't need it passed

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // Managers are already instantiated above

        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Welcome to SkyRoute Booking System"); // Updated title
            // Styled Menu Options
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Register" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Login" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " View Route Information" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "4." + Utils.RESET + Utils.CYAN + " Exit" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose an option: " + Utils.RESET);

            int choice = 0;
            try {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume the invalid input
                Utils.pause(sc);
                continue;
            } catch (Exception e) {
                System.out.println(Utils.RED + "An unexpected error occurred: " + e.getMessage() + Utils.RESET);
                sc.nextLine(); // Consume potentially leftover input
                Utils.pause(sc);
                continue;
            }

            switch (choice) {
                case 1:
                    registerUser(sc, userManager);
                    break;
                case 2:
                    if (loginUser(sc, userManager, bookingSystem)) {
                        // User successfully logged in, start the booking process
                        bookingSystem.startBooking(sc);
                        // Control returns here after user logs out from booking menu
                    }
                    break;
                case 3:
                    showRouteInformationMenu(sc);
                    break;
                case 4: // Exit case
                    System.out.println(Utils.GREEN + "\nSaving data and exiting..." + Utils.RESET);
                    // Save Users (assuming getUsersMap exists in UserManager)
                    try { // Add try-catch around saving as HashMap could be null theoretically
                        HashMap<String, String> usersToSave = userManager.getUsersMap();
                        if (usersToSave != null) {
                            StorageManager.saveUsers(usersToSave);
                        } else {
                            System.err.println(Utils.RED + "Error: Could not retrieve users map for saving." + Utils.RESET);
                        }
                    } catch (Exception e) {
                        System.err.println(Utils.RED + "Error saving user data: " + e.getMessage() + Utils.RESET);
                    }

                    // Save Bookings (assuming getters exist in BookingSystem)
                    try {
                        List<PlaneBooking> planes = bookingSystem.getPlanes();
                        List<TrainBooking> trains = bookingSystem.getTrains();
                        List<BusBooking> buses = bookingSystem.getBuses();
                        if (planes != null && trains != null && buses != null) {
                            StorageManager.saveBookings(planes, trains, buses);
                        } else {
                            System.err.println(Utils.RED + "Error: Could not retrieve vehicle lists for saving bookings." + Utils.RESET);
                        }
                    } catch (Exception e) {
                        System.err.println(Utils.RED + "Error saving booking data: " + e.getMessage() + Utils.RESET);
                    }

                    System.out.println(Utils.GREEN_BOLD + "\nThank you for using SkyRoute Booking System. Goodbye!" + Utils.RESET);
                    sc.close(); // Close the scanner
                    return; // Exit the application
                default:
                    System.out.println(Utils.RED + "Invalid option. Please try again." + Utils.RESET);
                    Utils.pause(sc);
            }
        } // End while loop
    } // End main method

    /**
     * Handles user registration process with input validation and password masking.
     *
     * @param sc Scanner for input.
     * @param userManager UserManager instance.
     */
    private static void registerUser(Scanner sc, UserManager userManager) {
        Utils.printBanner("User Registration");
        String username = "";
        String password = "";
        Console console = System.console(); // Get console instance

        // Get Username
        while (username.isEmpty()) {
            System.out.print(Utils.WHITE_BOLD + "Enter username: " + Utils.RESET);
            username = sc.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println(Utils.RED + "Username cannot be empty." + Utils.RESET);
            }
        }

        // Get Password (masked if console available)
        while (password.isEmpty()) {
            String prompt = Utils.WHITE_BOLD + "Enter password: " + Utils.RESET;
            String fallbackPrompt = Utils.WHITE_BOLD + "Enter password (" + Utils.YELLOW + "masking not available" + Utils.WHITE_BOLD + "): " + Utils.RESET;

            if (console != null) {
                char[] passwordChars = console.readPassword(prompt); // Use prompt in readPassword
                if (passwordChars != null) {
                    password = new String(passwordChars);
                    java.util.Arrays.fill(passwordChars, ' '); // Clear array after use
                } else {
                    // This case is rare but possible if console fails during read
                    System.out.println(Utils.RED + "Error reading password from console. Please try again." + Utils.RESET);
                    continue; // Ask again
                }
            } else { // Fallback if console is not available
                System.out.print(fallbackPrompt);
                password = sc.nextLine().trim();
            }

            if (password.isEmpty()) {
                System.out.println(Utils.RED + "Password cannot be empty." + Utils.RESET);
            }
        }

        // Attempt registration
        if (userManager.register(username, password)) {
            System.out.println(Utils.GREEN_BOLD + "\nRegistration successful! You can now log in." + Utils.RESET);
        } else {
            // Check if it failed because username exists
            if (userManager.getUsersMap().containsKey(username)) { // Assuming getUsersMap exists
                System.out.println(Utils.RED + "\nUsername '" + username + "' already exists. Please try a different one or login." + Utils.RESET);
            } else {
                // Generic registration failure message (e.g., if validation inside register failed)
                System.out.println(Utils.RED + "\nRegistration failed. Please check input and try again." + Utils.RESET);
            }
        }
        Utils.pause(sc);
    }

    /**
     * Handles user login process with input validation and password masking.
     *
     * @param sc Scanner for input.
     * @param userManager UserManager instance.
     * @param bookingSystem BookingSystem instance to set logged-in user.
     * @return true if login is successful, false otherwise.
     */
    private static boolean loginUser(Scanner sc, UserManager userManager, BookingSystem bookingSystem) {
        Utils.printBanner("User Login");
        String username = "";
        String password = "";
        Console console = System.console(); // Get console instance

        // Get Username
        while (username.isEmpty()) {
            System.out.print(Utils.WHITE_BOLD + "Enter username: " + Utils.RESET);
            username = sc.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println(Utils.RED + "Username cannot be empty." + Utils.RESET);
            }
        }

        // Get Password (masked if console available)
        String prompt = Utils.WHITE_BOLD + "Enter password: " + Utils.RESET;
        String fallbackPrompt = Utils.WHITE_BOLD + "Enter password (" + Utils.YELLOW + "masking not available" + Utils.WHITE_BOLD + "): " + Utils.RESET;

        // Read password once (allow empty input, validation happens in login method)
        if (console != null) {
            char[] passwordChars = console.readPassword(prompt);
            if (passwordChars != null) {
                password = new String(passwordChars);
                java.util.Arrays.fill(passwordChars, ' '); // Clear array
            }
            // If passwordChars is null, password remains "" (empty string)
        } else { // Fallback
            System.out.print(fallbackPrompt);
            password = sc.nextLine().trim(); // Read the potentially empty password
        }

        // Attempt Login
        if (userManager.login(username, password)) {
            System.out.println(Utils.GREEN_BOLD + "\nLogin successful!" + Utils.RESET);
            bookingSystem.setLoggedInUser(username); // Set user in BookingSystem
            Utils.pause(sc);
            return true;
        } else {
            System.out.println(Utils.RED_BOLD + "\nInvalid username or password." + Utils.RESET);
            Utils.pause(sc);
            return false;
        }
    }

    /**
     * Displays the menu for viewing route information.
     *
     * @param sc Scanner for user input.
     */
    private static void showRouteInformationMenu(Scanner sc) {
        // Use RouteDataManager instance created at the start
        if (routeDataManager == null) {
            System.out.println(Utils.RED + "Error: Route Data Manager not initialized." + Utils.RESET);
            Utils.pause(sc);
            return;
        }

        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Route Information Menu");
            // Styled Menu Options
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " View All Airports" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " View All Bus Stations" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " View All Train Stations" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "4." + Utils.RESET + Utils.CYAN + " View Information by City" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back to Main Menu" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose an option: " + Utils.RESET);

            int choice = 0;
            try {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
                continue;
            } catch (Exception e) {
                System.out.println(Utils.RED + "An unexpected error occurred: " + e.getMessage() + Utils.RESET);
                sc.nextLine();
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
                    System.out.print(Utils.WHITE_BOLD + "Enter city name to view information: " + Utils.RESET);
                    String city = sc.nextLine().trim();
                    if (!city.isEmpty()) {
                        // displayInfoForCity includes its own pause
                        routeDataManager.displayInfoForCity(city, sc);
                    } else {
                        System.out.println(Utils.RED + "City name cannot be empty." + Utils.RESET);
                        Utils.pause(sc);
                    }
                    break;
                case 0:
                    return; // Go back to the main menu
                default:
                    System.out.println(Utils.RED + "Invalid option. Please try again." + Utils.RESET);
                    Utils.pause(sc);
            }
        }
    }

    // Removed incorrect getters from Main

} // End of Main class