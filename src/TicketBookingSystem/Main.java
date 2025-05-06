package TicketBookingSystem;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.Console;
import java.util.List;
import java.util.HashMap;

public class Main {
    // Define the admin username
    private static final String ADMIN_USERNAME = "admin";

    // Instantiate Managers once
    private static RouteDataManager routeDataManager = new RouteDataManager();
    private static UserManager userManager = new UserManager();
    private static BookingSystem bookingSystem = new BookingSystem();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // Managers are already instantiated above

        UserManager userManager = new UserManager();
        BookingSystem bookingSystem = new BookingSystem();

        // Add a default admin user if not present AFTER loading users
        if (!userManager.getUsersMap().containsKey(ADMIN_USERNAME)) {
            System.out.println(Utils.YELLOW + "Creating default admin user ('admin'/'admin')..." + Utils.RESET);
            // This call will now succeed because the check inside register was removed
            boolean created = userManager.register(ADMIN_USERNAME, "admin");
            if (created) {
                System.out.println(Utils.GREEN + "Default admin user created." + Utils.RESET);
                // StorageManager.saveUsers(userManager.getUsersMap()); // Save is already done inside register
            } else {
                // This might happen if hashing fails or another issue occurs in register
                System.out.println(Utils.RED + "Failed to create default admin user." + Utils.RESET);
            }
        }


        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Welcome to SkyRoute Booking System");
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Register" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Login" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " View Route Information" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "4." + Utils.RESET + Utils.CYAN + " Exit" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose an option: " + Utils.RESET);

            int choice = 0;
            try { /* ... Input handling ... */
                choice = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input..." + Utils.RESET); sc.nextLine(); Utils.pause(sc); continue; }
            catch (Exception e) { System.out.println(Utils.RED + "Error: " + e.getMessage() + Utils.RESET); sc.nextLine(); Utils.pause(sc); continue; }


            switch (choice) {
                case 1:
                    registerUser(sc, userManager);
                    break;
                case 2:
                    // loginUser now returns the username on success, null on failure/back
                    String loggedInUsername = loginUser(sc, userManager);
                    if (loggedInUsername != null) {
                        // *** Check if Admin ***
                        if (loggedInUsername.equals(ADMIN_USERNAME)) {
                            // Create and show Admin Dashboard
                            AdminDashboard adminDashboard = new AdminDashboard(bookingSystem, userManager);
                            adminDashboard.showMenu(sc);
                            // Admin logs out implicitly when showMenu returns
                            System.out.println(Utils.GREEN + "\nAdmin session ended." + Utils.RESET);
                            Utils.pause(sc);
                        } else {
                            // Regular User Login
                            bookingSystem.setLoggedInUser(loggedInUsername);
                            bookingSystem.startBooking(sc); // Start regular booking session
                            // Control returns here after user selects logout from booking menu
                        }
                    }
                    break;
                case 3:
                    showRouteInformationMenu(sc);
                    break;
                case 4: // Exit case
                    System.out.println(Utils.GREEN + "\nSaving data and exiting..." + Utils.RESET);
                    // Save Users
                    try {
                        HashMap<String, String> usersToSave = userManager.getUsersMap();
                        if (usersToSave != null) StorageManager.saveUsers(usersToSave);
                    } catch (Exception e) { System.err.println(Utils.RED + "Error saving user data: " + e.getMessage() + Utils.RESET); }
                    // Save Bookings
                    try {
                        List<PlaneBooking> planes = bookingSystem.getPlanes();
                        List<TrainBooking> trains = bookingSystem.getTrains();
                        List<BusBooking> buses = bookingSystem.getBuses();
                        if (planes != null && trains != null && buses != null) StorageManager.saveBookings(planes, trains, buses);
                    } catch (Exception e) { System.err.println(Utils.RED + "Error saving booking data: " + e.getMessage() + Utils.RESET); }
                    System.out.println(Utils.GREEN_BOLD + "\nThank you for using SkyRoute Booking System. Goodbye!" + Utils.RESET);
                    sc.close();
                    return; // Exit application
                default:
                    System.out.println(Utils.RED + "Invalid option. Please try again." + Utils.RESET);
                    Utils.pause(sc);
            }
        } // End while loop
    } // End main method

    // registerUser remains largely the same
    private static void registerUser(Scanner sc, UserManager userManager) {
        // ... (keep implementation with password masking as before) ...
        Utils.printBanner("User Registration");
        String username = ""; String password = ""; Console console = System.console();
        while (username.isEmpty()) { /* ... get username ... */
            System.out.print(Utils.WHITE_BOLD + "Enter username: " + Utils.RESET);
            username = sc.nextLine().trim();
            if (username.isEmpty()) System.out.println(Utils.RED + "Username cannot be empty." + Utils.RESET);
            else if (username.equalsIgnoreCase(ADMIN_USERNAME)) { // Prevent registering as 'admin'
                System.out.println(Utils.RED + "Cannot register with the reserved admin username." + Utils.RESET);
                username = ""; // Force re-entry
            }
        }
        while (password.isEmpty()) { /* ... get password with masking ... */
            String prompt = Utils.WHITE_BOLD + "Enter password: " + Utils.RESET;
            String fallbackPrompt = Utils.WHITE_BOLD + "Enter password (" + Utils.YELLOW + "masking not available" + Utils.WHITE_BOLD + "): " + Utils.RESET;
            if (console != null) {
                char[] passwordChars = console.readPassword(prompt);
                if (passwordChars != null) { password = new String(passwordChars); java.util.Arrays.fill(passwordChars, ' '); }
                else { System.out.println(Utils.RED + "Error reading password..." + Utils.RESET); continue; }
            } else { System.out.print(fallbackPrompt); password = sc.nextLine().trim(); }
            if (password.isEmpty()) System.out.println(Utils.RED + "Password cannot be empty." + Utils.RESET);
        }
        if (userManager.register(username, password)) { System.out.println(Utils.GREEN_BOLD + "\nRegistration successful!" + Utils.RESET); }
        else { /* ... handle failure (username exists) ... */
            if (userManager.getUsersMap().containsKey(username)) { System.out.println(Utils.RED + "\nUsername '" + username + "' already exists." + Utils.RESET); }
            else { System.out.println(Utils.RED + "\nRegistration failed." + Utils.RESET); }
        }
        Utils.pause(sc);
    }

    // Modify loginUser to return username on success, null on failure
    /**
     * Handles user login process with password masking.
     * @return The username if login is successful, null otherwise.
     */
    private static String loginUser(Scanner sc, UserManager userManager) { // Removed BookingSystem param
        Utils.printBanner("User Login");
        String username = ""; String password = ""; Console console = System.console();
        while (username.isEmpty()) { /* ... get username ... */
            System.out.print(Utils.WHITE_BOLD + "Enter username: " + Utils.RESET);
            username = sc.nextLine().trim();
            if (username.isEmpty()) System.out.println(Utils.RED + "Username cannot be empty." + Utils.RESET);
        }
        // Get password
        String prompt = Utils.WHITE_BOLD + "Enter password: " + Utils.RESET;
        String fallbackPrompt = Utils.WHITE_BOLD + "Enter password (" + Utils.YELLOW + "masking not available" + Utils.WHITE_BOLD + "): " + Utils.RESET;
        if (console != null) {
            char[] passwordChars = console.readPassword(prompt);
            if (passwordChars != null) { password = new String(passwordChars); java.util.Arrays.fill(passwordChars, ' '); }
            // Allow empty password attempt
        } else { System.out.print(fallbackPrompt); password = sc.nextLine().trim(); }

        // Attempt Login
        if (userManager.login(username, password)) {
            System.out.println(Utils.GREEN_BOLD + "\nLogin successful!" + Utils.RESET);
            Utils.pause(sc);
            return username; // Return username on success
        } else {
            System.out.println(Utils.RED_BOLD + "\nInvalid username or password." + Utils.RESET);
            Utils.pause(sc);
            return null; // Return null on failure
        }
    }

    // showRouteInformationMenu remains the same
    private static void showRouteInformationMenu(Scanner sc) {
        // ... (implementation as before) ...
        if (routeDataManager == null) { /* ... handle error ... */ return; }
        while (true) {
            Utils.clearScreen(); Utils.printBanner("Route Information Menu");
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " View All Airports" + Utils.RESET);
            // ... other options ...
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back to Main Menu" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose an option: " + Utils.RESET);
            int choice = -1;
            try { /* ... get choice ... */ choice = sc.nextInt(); sc.nextLine(); } catch (Exception e) { /* ... handle ... */ continue; }
            switch (choice) { /* ... cases ... */
                case 1: Utils.clearScreen(); routeDataManager.displayAllAirports(); Utils.pause(sc); break;
                case 2: Utils.clearScreen(); routeDataManager.displayAllBusStations(); Utils.pause(sc); break;
                case 3: Utils.clearScreen(); routeDataManager.displayAllTrainStations(); Utils.pause(sc); break;
                case 4:
                    System.out.print(Utils.WHITE_BOLD + "Enter city name: " + Utils.RESET); String city = sc.nextLine().trim();
                    if (!city.isEmpty()) routeDataManager.displayInfoForCity(city, sc); // Pause inside
                    else { System.out.println(Utils.RED + "City name empty." + Utils.RESET); Utils.pause(sc); }
                    break;
                case 0: return;
                default: System.out.println(Utils.RED + "Invalid option." + Utils.RESET); Utils.pause(sc);
            }
        }
    }
}