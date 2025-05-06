package TicketBookingSystem;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminDashboard {

    private final BookingSystem bookingSystem;
    private final UserManager userManager;

    public AdminDashboard(BookingSystem bookingSystem, UserManager userManager) {
        this.bookingSystem = bookingSystem;
        this.userManager = userManager;
    }

    /**
     * Displays the main menu for the admin dashboard and handles actions.
     * @param sc Scanner for input.
     */
    public void showMenu(Scanner sc) {
        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Admin Dashboard");
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " View All Bookings" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " View All Users" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " Cancel Any Booking" + Utils.RESET);
            // Add more admin options here later (e.g., Manage Vehicles, Manage Routes)
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Logout Admin Session" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose an option: " + Utils.RESET);

            int choice = -1;
            try {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
                continue;
            } catch (Exception e) {
                System.out.println(Utils.RED + "An error occurred: " + e.getMessage() + Utils.RESET);
                sc.nextLine();
                Utils.pause(sc);
                continue;
            }

            switch (choice) {
                case 1:
                    viewAllBookings(sc);
                    break;
                case 2:
                    viewAllUsers(sc);
                    break;
                case 3:
                    cancelAnyBooking(sc);
                    // Save immediately after admin cancellation
                    StorageManager.saveBookings(bookingSystem.getPlanes(), bookingSystem.getTrains(), bookingSystem.getBuses());
                    break;
                case 0:
                    System.out.println(Utils.GREEN + "Logging out from admin dashboard..." + Utils.RESET);
                    return; // Exit admin menu
                default:
                    System.out.println(Utils.RED + "Invalid option. Please try again." + Utils.RESET);
                    Utils.pause(sc);
            }
        }
    }

    /**
     * Displays all bookings across all vehicles.
     * @param sc Scanner for pausing.
     */
    private void viewAllBookings(Scanner sc) {
        Utils.clearScreen();
        Utils.printBanner("View All Bookings");
        boolean foundAnyBookings = false;

        System.out.println(Utils.CYAN_BOLD + "\n--- All Plane Bookings ---" + Utils.RESET);
        for (PlaneBooking plane : bookingSystem.getPlanes()) {
            // Modify display method slightly or create an admin version if needed
            // For now, just iterate and print basic info for all bookings
            if (!plane.getBookings().isEmpty()) {
                foundAnyBookings = true;
                System.out.println(Utils.YELLOW_BOLD + "Aircraft ID: " + plane.getFlightId() + Utils.RESET);
                displayBookingMap(plane.getBookings());
            }
        }

        System.out.println(Utils.CYAN_BOLD + "\n--- All Train Bookings ---" + Utils.RESET);
        for (TrainBooking train : bookingSystem.getTrains()) {
            if (!train.getBookings().isEmpty()) {
                foundAnyBookings = true;
                System.out.println(Utils.YELLOW_BOLD + "Train ID: " + train.getTrainId() + Utils.RESET);
                displayBookingMap(train.getBookings());
            }
        }

        System.out.println(Utils.CYAN_BOLD + "\n--- All Bus Bookings ---" + Utils.RESET);
        for (BusBooking bus : bookingSystem.getBuses()) {
            if (!bus.getBookings().isEmpty()) {
                foundAnyBookings = true;
                System.out.println(Utils.YELLOW_BOLD + "Bus ID: " + bus.getBusId() + Utils.RESET);
                displayBookingMap(bus.getBookings());
            }
        }


        if (!foundAnyBookings) {
            System.out.println(Utils.YELLOW + "No bookings found in the system." + Utils.RESET);
        }
        Utils.pause(sc);
    }

    /**
     * Helper to display the content of a booking map.
     * @param bookings Map<String, ?> where value is expected to be a Booking object.
     */
    // Generic helper to display bookings from any type
    private void displayBookingMap(Map<String, ?> bookingsMap) {
        if (bookingsMap == null || bookingsMap.isEmpty()) return; // Nothing to display

        // Print header
        String columns = String.format(Utils.BLUE_BOLD + "%-10s | %-15s | %-20s | %-25s | %-11s | %-10s | %-8s | %s" + Utils.RESET,
                "Booking ID", "User", "Route", "Service/Provider", "Travel Date", "Price", "Seat", "Class");
        String separator = Utils.CYAN + "-----------+-----------------+----------------------+---------------------------+-------------+------------+----------+---------------------" + Utils.RESET;
        System.out.println(columns);
        System.out.println(separator);

        // Iterate and print details using reflection/casting carefully
        for (Map.Entry<String, ?> entry : bookingsMap.entrySet()) {
            String bookingId = entry.getKey();
            Object bookingObj = entry.getValue();
            try {
                // Use reflection carefully or common interface if possible
                // Assuming common getters exist via the static inner class structure
                String username = (String) bookingObj.getClass().getMethod("getUsername").invoke(bookingObj);
                String startCity = (String) bookingObj.getClass().getMethod("getStartCity").invoke(bookingObj);
                String destCity = (String) bookingObj.getClass().getMethod("getDestCity").invoke(bookingObj);
                String provider = (String) bookingObj.getClass().getMethod("getProvider").invoke(bookingObj);
                String travelDate = (String) bookingObj.getClass().getMethod("getTravelDate").invoke(bookingObj);
                double price = (Double) bookingObj.getClass().getMethod("getPrice").invoke(bookingObj);
                Seat seat = (Seat) bookingObj.getClass().getMethod("getSeat").invoke(bookingObj);
                String seatClass = (String) bookingObj.getClass().getMethod("getSeatClass").invoke(bookingObj);

                System.out.printf(Utils.YELLOW_BOLD + "%-10s" + Utils.RESET + " | " + Utils.MAGENTA + "%-15s" + Utils.RESET + " | " + Utils.MAGENTA + "%-20s" + Utils.RESET + " | " + Utils.CYAN + "%-25.25s" + Utils.RESET + " | " + Utils.MAGENTA + "%-11s" + Utils.RESET + " | " + Utils.GREEN_BOLD + "Rs. %-7.2f" + Utils.RESET + " | " + Utils.YELLOW_BOLD + "%-8s" + Utils.RESET + " | " + Utils.MAGENTA + "%s" + Utils.RESET + "\n",
                        bookingId, username, startCity + "->" + destCity, provider, travelDate, price, seat.getSeatId(), seatClass);

            } catch (Exception e) {
                // Fallback or error message if reflection fails
                System.err.println(Utils.RED + "Error displaying details for booking ID " + bookingId + ": " + e.getMessage() + Utils.RESET);
            }
        }
    }


    /**
     * Displays all registered users.
     * @param sc Scanner for pausing.
     */
    private void viewAllUsers(Scanner sc) {
        Utils.clearScreen();
        Utils.printBanner("View All Registered Users");
        // Call method in UserManager to display users
        userManager.displayAllUsers(); // Need to implement this in UserManager
        Utils.pause(sc);
    }

    /**
     * Allows the admin to cancel any booking by its ID.
     * @param sc Scanner for input.
     */
    private void cancelAnyBooking(Scanner sc) {
        Utils.clearScreen();
        Utils.printBanner("Cancel Any Booking (Admin)");
        System.out.print(Utils.WHITE_BOLD + "Enter Booking ID to cancel (e.g., P1, T1, B1): " + Utils.RESET);
        String bookingId = sc.nextLine().toUpperCase(); // Use uppercase for consistency
        boolean canceled = false;

        // Iterate through all vehicle types and their booking maps
        // Planes
        for (PlaneBooking plane : bookingSystem.getPlanes()) {
            // Check if this plane object holds the booking
            if (plane.getBookings().containsKey(bookingId)) {
                // Call cancelBooking, but ignore the username check (pass null or specific admin flag if implemented)
                // For simplicity, we call the existing method and ignore the boolean result's dependency on username match
                plane.cancelBooking(bookingId, null); // Pass null for username to potentially bypass check if method is adapted
                // OR call a dedicated adminCancel(bookingId) if created
                canceled = true; // Assume cancellation attempt was made
                System.out.println(Utils.GREEN + "Attempted cancellation for " + bookingId + " on Plane " + plane.getFlightId() + "." + Utils.RESET);
                break; // Exit loop once found
            }
        }
        // Trains
        if (!canceled) {
            for (TrainBooking train : bookingSystem.getTrains()) {
                if (train.getBookings().containsKey(bookingId)) {
                    train.cancelBooking(bookingId, null);
                    canceled = true;
                    System.out.println(Utils.GREEN + "Attempted cancellation for " + bookingId + " on Train " + train.getTrainId() + "." + Utils.RESET);
                    break;
                }
            }
        }
        // Buses
        if (!canceled) {
            for (BusBooking bus : bookingSystem.getBuses()) {
                if (bus.getBookings().containsKey(bookingId)) {
                    bus.cancelBooking(bookingId, null);
                    canceled = true;
                    System.out.println(Utils.GREEN + "Attempted cancellation for " + bookingId + " on Bus " + bus.getBusId() + "." + Utils.RESET);
                    break;
                }
            }
        }

        // Report outcome
        if (canceled) {
            System.out.println(Utils.GREEN_BOLD + "\nBooking " + bookingId + " should now be removed (if found)." + Utils.RESET);
            System.out.println(Utils.GREY + "(Changes saved automatically)" + Utils.RESET);
        } else {
            System.out.println(Utils.RED + "\nBooking ID " + bookingId + " not found in any vehicle's booking list." + Utils.RESET);
        }
        Utils.pause(sc);

        // Note: Modifying cancelBooking in Plane/Train/BusBooking to accept null username
        // and skip the check would make this more robust. E.g.:
        // if (booking != null && (username == null || booking.getUsername().equals(username))) { ... }
    }

}