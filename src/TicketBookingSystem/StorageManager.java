package TicketBookingSystem;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles saving and loading user credentials and booking data to/from files
 * for persistence across application sessions. Uses updated format with Provider.
 */
public class StorageManager {
    private static final String USER_FILE = "users.txt";
    private static final String BOOKINGS_FILE = "bookings.txt";
    private static final String DELIMITER = ":"; // File delimiter

    /** Saves user credentials. */
    public static void saveUsers(HashMap<String, String> users) {
        // Use try-with-resources for automatic closing
        System.out.println(Utils.GREY + "Saving user data..." + Utils.RESET);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            if (users != null) { // Add null check
                for (Map.Entry<String, String> entry : users.entrySet()) {
                    writer.write(entry.getKey() + DELIMITER + entry.getValue());
                    writer.newLine();
                }
            }
            System.out.println(Utils.GREEN + "User data saved successfully to " + USER_FILE + "." + Utils.RESET);
        } catch (IOException e) {
            System.err.println(Utils.RED_BOLD + "Error saving user data to " + USER_FILE + ": " + Utils.RESET + Utils.RED + e.getMessage() + Utils.RESET);
        } catch (NullPointerException e) {
            System.err.println(Utils.RED_BOLD + "Error: User map provided for saving was null." + Utils.RESET);
        }
    }

    /** Loads user credentials. */
    public static HashMap<String, String> loadUsers() {
        HashMap<String, String> users = new HashMap<>();
        File file = new File(USER_FILE);
        if (!file.exists()) {
            System.out.println(Utils.YELLOW + "User data file (" + USER_FILE + ") not found. Starting fresh." + Utils.RESET);
            return users;
        }

        System.out.println(Utils.GREY + "Loading user data from " + USER_FILE + "..." + Utils.RESET);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line; int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(DELIMITER, 2); // Split only on first delimiter
                if (parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
                    users.put(parts[0], parts[1]);
                } else { System.err.println(Utils.YELLOW_BOLD + "Warning:" + Utils.YELLOW + " Skipping malformed line #" + lineNumber + " in " + USER_FILE + Utils.RESET); }
            }
            System.out.println(Utils.GREEN + "User data loaded successfully ("+users.size()+" users)." + Utils.RESET);
        } catch (IOException e) { System.err.println(Utils.RED_BOLD + "Error loading user data: " + Utils.RESET + Utils.RED + e.getMessage() + Utils.RESET); }
        return users;
    }

    /**
     * Saves all bookings using the updated format including the provider.
     * Format: BookingID:Username:Start:Dest:Price:SeatClass:Row:Col:VehicleID:Date:Provider
     */
    public static void saveBookings(List<PlaneBooking> planes, List<TrainBooking> trains, List<BusBooking> buses) {
        System.out.println(Utils.GREY + "Saving bookings data..." + Utils.RESET);
        int bookingsSaved = 0;
        // Use try-with-resources
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            // Save Plane bookings from all plane manager objects
            if (planes != null) {
                for (PlaneBooking plane : planes) {
                    String vehicleId = plane.getFlightId(); // This is the manager ID (e.g., PLANE-MANAGER-1)
                    for (Map.Entry<String, ?> entry : plane.getBookings().entrySet()) {
                        String bookingId = entry.getKey().toUpperCase();
                        try {
                            PlaneBooking.Booking booking = (PlaneBooking.Booking) entry.getValue(); // Cast to inner class
                            Seat seat = booking.getSeat();
                            // Join using the delimiter, including the provider
                            String line = String.join(DELIMITER,
                                    bookingId, booking.getUsername(), booking.getStartCity(), booking.getDestCity(),
                                    String.format("%.2f", booking.getPrice()), booking.getSeatClass(),
                                    String.valueOf(seat.getRow()), seat.getColumn(), vehicleId, booking.getTravelDate(),
                                    booking.getProvider() // Save the actual provider
                            );
                            writer.write(line); writer.newLine(); bookingsSaved++;
                        } catch (ClassCastException | NullPointerException e) { System.err.println(Utils.RED+"Error processing plane booking "+bookingId+": "+e.getMessage()+Utils.RESET); }
                    }
                }
            }
            // Save Train bookings (similar logic)
            if (trains != null) {
                for (TrainBooking train : trains) {
                    String vehicleId = train.getTrainId();
                    for (Map.Entry<String, ?> entry : train.getBookings().entrySet()) {
                        String bookingId = entry.getKey().toUpperCase();
                        try {
                            TrainBooking.Booking booking = (TrainBooking.Booking) entry.getValue();
                            Seat seat = booking.getSeat();
                            String line = String.join(DELIMITER,
                                    bookingId, booking.getUsername(), booking.getStartCity(), booking.getDestCity(),
                                    String.format("%.2f", booking.getPrice()), booking.getSeatClass(),
                                    String.valueOf(seat.getRow()), seat.getColumn(), vehicleId, booking.getTravelDate(),
                                    booking.getProvider());
                            writer.write(line); writer.newLine(); bookingsSaved++;
                        } catch (ClassCastException | NullPointerException e) { System.err.println(Utils.RED+"Error processing train booking "+bookingId+": "+e.getMessage()+Utils.RESET); }
                    }
                }
            }
            // Save Bus bookings (similar logic)
            if (buses != null) {
                for (BusBooking bus : buses) {
                    String vehicleId = bus.getBusId();
                    for (Map.Entry<String, ?> entry : bus.getBookings().entrySet()) {
                        String bookingId = entry.getKey().toUpperCase();
                        try {
                            BusBooking.Booking booking = (BusBooking.Booking) entry.getValue();
                            Seat seat = booking.getSeat();
                            String line = String.join(DELIMITER,
                                    bookingId, booking.getUsername(), booking.getStartCity(), booking.getDestCity(),
                                    String.format("%.2f", booking.getPrice()), booking.getSeatClass(), // Should be Standard
                                    String.valueOf(seat.getRow()), seat.getColumn(), vehicleId, booking.getTravelDate(),
                                    booking.getProvider());
                            writer.write(line); writer.newLine(); bookingsSaved++;
                        } catch (ClassCastException | NullPointerException e) { System.err.println(Utils.RED+"Error processing bus booking "+bookingId+": "+e.getMessage()+Utils.RESET); }
                    }
                }
            }
            System.out.println(Utils.GREEN + "Bookings data saved successfully ("+bookingsSaved+" bookings)." + Utils.RESET);
        } catch (IOException e) { System.err.println(Utils.RED_BOLD + "Error saving bookings: " + Utils.RESET + Utils.RED + e.getMessage() + Utils.RESET); }
        catch (Exception e) { System.err.println(Utils.RED_BOLD + "Unexpected error during booking save: " + e.getMessage() + Utils.RESET); e.printStackTrace();}
    }

    /**
     * Loads bookings using the updated format including the provider.
     * Format: BookingID:Username:Start:Dest:Price:SeatClass:Row:Col:VehicleID:Date:Provider
     */
    public static void loadBookings(List<PlaneBooking> planes, List<TrainBooking> trains, List<BusBooking> buses) {
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) { System.out.println(Utils.YELLOW + "Bookings file (" + BOOKINGS_FILE + ") not found. Starting fresh." + Utils.RESET); return; }

        System.out.println(Utils.GREY + "Loading bookings data from " + BOOKINGS_FILE + "..." + Utils.RESET);
        int lineNumber = 0; int loadedCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(DELIMITER);
                // Expecting 11 parts now
                if (parts.length != 11) {
                    System.err.println(Utils.YELLOW_BOLD + "Warning:" + Utils.YELLOW + " Skipping malformed line #" + lineNumber + " (Expected 11 parts, found " + parts.length + ")" + Utils.RESET);
                    continue;
                }

                try {
                    // Parse all parts
                    String bookingId = parts[0].toUpperCase(); String username = parts[1];
                    String startCity = parts[2]; String destCity = parts[3];
                    double price = Double.parseDouble(parts[4]); String seatClass = parts[5];
                    int row = Integer.parseInt(parts[6]); String col = parts[7];
                    String vehicleId = parts[8]; // ID of the *manager* object
                    String travelDate = parts[9]; String provider = parts[10]; // The actual service provider

                    String transportType = "";
                    if (bookingId.startsWith("P")) transportType = "Plane";
                    else if (bookingId.startsWith("T")) transportType = "Train";
                    else if (bookingId.startsWith("B")) transportType = "Bus";
                    else { System.err.println(Utils.YELLOW + "Warning: Unknown booking ID prefix on line #" + lineNumber + Utils.RESET); continue; }

                    // Create Seat object (price here might be the final price paid)
                    Seat seat = new Seat(row, col, seatClass, transportType, price);

                    // Find the correct manager object by its ID and add the booking
                    boolean bookingAdded = false;
                    if (transportType.equals("Plane")) {
                        for (PlaneBooking plane : planes) {
                            if (plane.getFlightId().equals(vehicleId)) {
                                plane.addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, travelDate, provider);
                                bookingAdded = true; break;
                            }
                        }
                    } else if (transportType.equals("Train")) {
                        for (TrainBooking train : trains) {
                            if (train.getTrainId().equals(vehicleId)) {
                                train.addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, travelDate, provider);
                                bookingAdded = true; break;
                            }
                        }
                    } else if (transportType.equals("Bus")) {
                        for (BusBooking bus : buses) {
                            if (bus.getBusId().equals(vehicleId)) {
                                bus.addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, travelDate, provider);
                                bookingAdded = true; break;
                            }
                        }
                    }

                    if (bookingAdded) loadedCount++;
                    else { System.err.println(Utils.YELLOW + "Warning: No matching manager vehicle found for ID '" + vehicleId + "' on line #" + lineNumber + "." + Utils.RESET); }

                } catch (NumberFormatException e) { System.err.println(Utils.YELLOW + "Warning: Skipping line #" + lineNumber + " due to number format error: " + e.getMessage() + Utils.RESET); }
                catch (Exception e) { System.err.println(Utils.RED + "Error processing line #" + lineNumber + ": " + e.getMessage() + Utils.RESET); e.printStackTrace(); }
            } // End while
            System.out.println(Utils.GREEN + "Bookings data loaded successfully (" + loadedCount + " bookings)." + Utils.RESET);
        } catch (IOException e) { System.err.println(Utils.RED_BOLD + "Error loading bookings: " + Utils.RESET + Utils.RED + e.getMessage() + Utils.RESET); }
    }

} // End of StorageManager class