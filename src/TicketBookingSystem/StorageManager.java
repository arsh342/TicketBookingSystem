package TicketBookingSystem;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles saving and loading user credentials and booking data to/from files
 * for persistence across application sessions.
 */
public class StorageManager {
    // Constants for filenames
    private static final String USER_FILE = "users.txt";
    private static final String BOOKINGS_FILE = "bookings.txt";
    // Define the delimiter used in the bookings file
    private static final String DELIMITER = ":";

    /**
     * Saves user credentials (username:hashed_password) to the user file.
     * Uses try-with-resources for safe file handling.
     *
     * @param users HashMap containing username-password pairs.
     */
    public static void saveUsers(HashMap<String, String> users) {
        System.out.println("\033[0;90mSaving user data...\033[0m"); // Debug message
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                // Simple format: username:hashedPassword
                writer.write(entry.getKey() + DELIMITER + entry.getValue());
                writer.newLine();
            }
            System.out.println("\033[1;32mUser data saved successfully to " + USER_FILE + ".\033[0m");
        } catch (IOException e) {
            System.err.println("\033[1;31mError saving user data to " + USER_FILE + ": " + e.getMessage() + "\033[0m");
        }
    }

    /**
     * Loads user credentials from the user file.
     * Handles file not found scenarios gracefully.
     * Uses try-with-resources.
     *
     * @return HashMap containing loaded username-password pairs.
     */
    public static HashMap<String, String> loadUsers() {
        HashMap<String, String> users = new HashMap<>();
        File file = new File(USER_FILE);

        if (!file.exists()) {
            System.out.println("\033[1;33mUser data file (" + USER_FILE + ") not found. Starting with no users.\033[0m");
            return users; // Return empty map
        }

        System.out.println("\033[0;90mLoading user data from " + USER_FILE + "...\033[0m");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.startsWith("#")) continue; // Skip empty/comment lines

                String[] parts = line.split(DELIMITER, 2); // Split only on the first delimiter
                if (parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
                    users.put(parts[0], parts[1]); // username, hashedPassword
                } else {
                    System.err.println("\033[1;33mWarning: Skipping malformed line #" + lineNumber + " in " + USER_FILE + ": " + line + "\033[0m");
                }
            }
            System.out.println("\033[1;32mUser data loaded successfully.\033[0m");
        } catch (IOException e) {
            System.err.println("\033[1;31mError loading user data from " + USER_FILE + ": " + e.getMessage() + "\033[0m");
        }
        return users;
    }

    /**
     * Saves all current bookings from Plane, Train, and Bus objects to the bookings file.
     * Uses a consistent format:
     * BookingID:Username:StartCity:DestCity:Price:SeatClass:SeatRow:SeatCol:VehicleID:TravelDate
     *
     * @param planes List of PlaneBooking objects.
     * @param trains List of TrainBooking objects.
     * @param buses List of BusBooking objects.
     */
    public static void saveBookings(List<PlaneBooking> planes, List<TrainBooking> trains, List<BusBooking> buses) {
        System.out.println("\033[0;90mSaving bookings data...\033[0m"); // Debug message
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            // Save Plane bookings
            for (PlaneBooking plane : planes) {
                String vehicleId = plane.getFlightId();
                // Directly iterate through the bookings map using the public getter
                for (Map.Entry<String, ?> entry : plane.getBookings().entrySet()) {
                    String bookingId = entry.getKey().toUpperCase(); // Save uppercase ID
                    // Cast the value to the correct inner Booking type (we know it's static now)
                    Object bookingObj = entry.getValue();
                    // Access details using getters (Casting needed if Booking class is not directly accessible - but it should be via package access)
                    try {
                        // Assuming Booking class is accessible within the package
                        PlaneBooking.Booking booking = (PlaneBooking.Booking) bookingObj; // Cast needed if generic Map type is used
                        Seat seat = booking.getSeat();
                        String line = String.join(DELIMITER,
                                bookingId,
                                booking.getUsername(),
                                booking.getStartCity(),
                                booking.getDestCity(),
                                String.format("%.2f", booking.getPrice()), // Format price
                                booking.getSeatClass(),
                                String.valueOf(seat.getRow()),
                                seat.getColumn(),
                                vehicleId,
                                booking.getTravelDate()
                        );
                        writer.write(line);
                        writer.newLine();
                    } catch (ClassCastException e) {
                        System.err.println("\033[1;31mError casting booking object during save for Plane ID " + vehicleId + ". Booking ID: " + bookingId + "\033[0m");
                    }
                }
            }

            // Save Train bookings (similar logic)
            for (TrainBooking train : trains) {
                String vehicleId = train.getTrainId();
                for (Map.Entry<String, ?> entry : train.getBookings().entrySet()) {
                    String bookingId = entry.getKey().toUpperCase();
                    Object bookingObj = entry.getValue();
                    try {
                        TrainBooking.Booking booking = (TrainBooking.Booking) bookingObj;
                        Seat seat = booking.getSeat();
                        String line = String.join(DELIMITER,
                                bookingId, booking.getUsername(), booking.getStartCity(), booking.getDestCity(),
                                String.format("%.2f", booking.getPrice()), booking.getSeatClass(),
                                String.valueOf(seat.getRow()), seat.getColumn(), vehicleId, booking.getTravelDate()
                        );
                        writer.write(line);
                        writer.newLine();
                    } catch (ClassCastException e) {
                        System.err.println("\033[1;31mError casting booking object during save for Train ID " + vehicleId + ". Booking ID: " + bookingId + "\033[0m");
                    }
                }
            }

            // Save Bus bookings (similar logic)
            for (BusBooking bus : buses) {
                String vehicleId = bus.getBusId();
                for (Map.Entry<String, ?> entry : bus.getBookings().entrySet()) {
                    String bookingId = entry.getKey().toUpperCase();
                    Object bookingObj = entry.getValue();
                    try {
                        BusBooking.Booking booking = (BusBooking.Booking) bookingObj;
                        Seat seat = booking.getSeat();
                        String line = String.join(DELIMITER,
                                bookingId, booking.getUsername(), booking.getStartCity(), booking.getDestCity(),
                                String.format("%.2f", booking.getPrice()), booking.getSeatClass(), // Should be "Standard"
                                String.valueOf(seat.getRow()), seat.getColumn(), vehicleId, booking.getTravelDate()
                        );
                        writer.write(line);
                        writer.newLine();
                    } catch (ClassCastException e) {
                        System.err.println("\033[1;31mError casting booking object during save for Bus ID " + vehicleId + ". Booking ID: " + bookingId + "\033[0m");
                    }
                }
            }

            System.out.println("\033[1;32mBookings data saved successfully to " + BOOKINGS_FILE + ".\033[0m");
        } catch (IOException e) {
            System.err.println("\033[1;31mError saving bookings data to " + BOOKINGS_FILE + ": " + e.getMessage() + "\033[0m");
        } catch (Exception e) {
            // Catch unexpected errors during saving
            System.err.println("\033[1;31mAn unexpected error occurred during booking save: " + e.getMessage() + "\033[0m");
            e.printStackTrace();
        }
    }

    /**
     * Loads bookings from the bookings file and adds them to the appropriate
     * Plane, Train, or Bus objects.
     * Format expected: BookingID:Username:StartCity:DestCity:Price:SeatClass:SeatRow:SeatCol:VehicleID:TravelDate
     *
     * @param planes List of PlaneBooking objects to populate.
     * @param trains List of TrainBooking objects to populate.
     * @param buses List of BusBooking objects to populate.
     */
    public static void loadBookings(List<PlaneBooking> planes, List<TrainBooking> trains, List<BusBooking> buses) {
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) {
            System.out.println("\033[1;33mBookings file (" + BOOKINGS_FILE + ") not found. Starting with no existing bookings.\033[0m");
            return;
        }

        System.out.println("\033[0;90mLoading bookings data from " + BOOKINGS_FILE + "...\033[0m");
        int lineNumber = 0;
        int loadedCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(DELIMITER);
                // Expecting 10 parts for the new format
                if (parts.length != 10) {
                    System.err.println("\033[1;33mWarning: Skipping malformed line #" + lineNumber + " in " + BOOKINGS_FILE + " (Expected 10 parts, found " + parts.length + "): " + line + "\033[0m");
                    continue; // Skip malformed entries
                }

                try {
                    // Parse data from parts
                    String bookingId = parts[0].toUpperCase(); // Load uppercase ID
                    String username = parts[1];
                    String startCity = parts[2];
                    String destCity = parts[3];
                    double price = Double.parseDouble(parts[4]);
                    String seatClass = parts[5];
                    int row = Integer.parseInt(parts[6]);
                    String col = parts[7]; // Seat column is already a string
                    String vehicleId = parts[8];
                    String travelDate = parts[9]; // Travel date is the last part

                    // Determine transport type from Booking ID prefix
                    String transportType = "";
                    if (bookingId.startsWith("P")) transportType = "Plane";
                    else if (bookingId.startsWith("T")) transportType = "Train";
                    else if (bookingId.startsWith("B")) transportType = "Bus";
                    else {
                        System.err.println("\033[1;33mWarning: Skipping line #" + lineNumber + " due to unknown booking ID prefix: " + bookingId + "\033[0m");
                        continue;
                    }

                    // Create the Seat object - its status will be set by addBooking
                    Seat seat = new Seat(row, col, seatClass, transportType, price);

                    // Find the correct vehicle and add the booking
                    boolean bookingAdded = false;
                    if (transportType.equals("Plane")) {
                        for (PlaneBooking plane : planes) {
                            if (plane.getFlightId().equals(vehicleId)) {
                                plane.addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, travelDate);
                                bookingAdded = true;
                                break;
                            }
                        }
                    } else if (transportType.equals("Train")) {
                        for (TrainBooking train : trains) {
                            if (train.getTrainId().equals(vehicleId)) {
                                train.addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, travelDate);
                                bookingAdded = true;
                                break;
                            }
                        }
                    } else if (transportType.equals("Bus")) {
                        // For bus, ensure seatClass loaded is "Standard" conceptually
                        for (BusBooking bus : buses) {
                            if (bus.getBusId().equals(vehicleId)) {
                                // BusBooking's addBooking should handle setting class to "Standard"
                                bus.addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, travelDate);
                                bookingAdded = true;
                                break;
                            }
                        }
                    }

                    if (bookingAdded) {
                        loadedCount++;
                    } else {
                        System.err.println("\033[1;33mWarning: Skipping line #" + lineNumber + ". Could not find matching vehicle ID '" + vehicleId + "' for booking ID '" + bookingId + "'.\033[0m");
                    }

                } catch (NumberFormatException e) {
                    System.err.println("\033[1;33mWarning: Skipping line #" + lineNumber + " due to number format error in " + BOOKINGS_FILE + ": " + e.getMessage() + "\033[0m");
                } catch (Exception e) { // Catch any other unexpected errors during parsing/loading
                    System.err.println("\033[1;31mError processing line #" + lineNumber + " in " + BOOKINGS_FILE + ": " + e.getMessage() + "\033[0m");
                    e.printStackTrace(); // Print stack trace for debugging help
                }
            } // End while loop
            System.out.println("\033[1;32mBookings data loaded successfully (" + loadedCount + " bookings).\033[0m");
        } catch (IOException e) {
            System.err.println("\033[1;31mError loading bookings data from " + BOOKINGS_FILE + ": " + e.getMessage() + "\033[0m");
        }
    }

    // --- REMOVED METHODS ---
    // removeBooking(String bookingId, ...) - Removed, cancellation handled in memory
    // getBookingsMap(...) - Removed, no longer needed due to public getters
    // invokeMethod(...) - Removed, no reflection needed

} // End of StorageManager class