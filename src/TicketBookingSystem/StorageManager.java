package TicketBookingSystem;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles saving and loading user and seat booking data for persistence across sessions.
 */
public class StorageManager {
    private static final String USER_FILE = "users.txt";
    private static final String BOOKINGS_FILE = "bookings.txt";

    /**
     * Saves user credentials to disk.
     */
    public static void saveUsers(HashMap<String, String> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (var entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
            System.out.println("\033[1;32mUser data saved successfully.\033[0m");
        } catch (IOException e) {
            System.out.println("\033[1;31mError saving users: " + e.getMessage() + "\033[0m");
        }
    }

    /**
     * Loads user credentials from disk.
     */
    public static HashMap<String, String> loadUsers() {
        HashMap<String, String> users = new HashMap<>();
        File file = new File(USER_FILE);
        if (!file.exists()) {
            System.out.println("\033[1;33mNo user data file found. Starting fresh.\033[0m");
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
            System.out.println("\033[1;32mUser data loaded successfully.\033[0m");
        } catch (IOException e) {
            System.out.println("\033[1;31mError loading users: " + e.getMessage() + "\033[0m");
        }
        return users;
    }

    /**
     * Saves all bookings to disk.
     */
    public static void saveBookings(List<PlaneBooking> planes, List<TrainBooking> trains, List<BusBooking> buses) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            // Save Plane bookings
            for (PlaneBooking plane : planes) {
                Map<String, Object[]> planeBookings = getBookingsMap(plane);
                for (var entry : planeBookings.entrySet()) {
                    Object[] booking = entry.getValue();
                    writer.write(String.format("%s:%s:%s:%s:%.2f:%s:%d:%s:%s:%s",
                            entry.getKey(), booking[0], booking[1], booking[2], booking[3], booking[4], booking[5], booking[6], plane.getFlightId(), booking[7]));
                    writer.newLine();
                }
            }

            // Save Train bookings
            for (TrainBooking train : trains) {
                Map<String, Object[]> trainBookings = getBookingsMap(train);
                for (var entry : trainBookings.entrySet()) {
                    Object[] booking = entry.getValue();
                    writer.write(String.format("%s:%s:%s:%s:%.2f:%s:%d:%s:%s:%s",
                            entry.getKey(), booking[0], booking[1], booking[2], booking[3], booking[4], booking[5], booking[6], train.getTrainId(), booking[7]));
                    writer.newLine();
                }
            }

            // Save Bus bookings
            for (BusBooking bus : buses) {
                Map<String, Object[]> busBookings = getBookingsMap(bus);
                for (var entry : busBookings.entrySet()) {
                    Object[] booking = entry.getValue();
                    writer.write(String.format("%s:%s:%s:%s:%.2f:%s:%d:%s:%s:%s",
                            entry.getKey(), booking[0], booking[1], booking[2], booking[3], booking[4], booking[5], booking[6], bus.getBusId(), booking[7]));
                    writer.newLine();
                }
            }

            System.out.println("\033[1;32mBookings saved successfully.\033[0m");
        } catch (IOException e) {
            System.out.println("\033[1;31mError saving bookings: " + e.getMessage() + "\033[0m");
        }
    }

    /**
     * Loads bookings from disk and populates the booking classes.
     */
    public static void loadBookings(List<PlaneBooking> planes, List<TrainBooking> trains, List<BusBooking> buses) {
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) {
            System.out.println("\033[1;33mNo bookings file found. Starting fresh.\033[0m");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                // Handle both old format (9 parts) and new format (10 parts with travelDate)
                if (parts.length != 9 && parts.length != 10) continue; // Skip malformed entries

                String bookingId = parts[0];
                String username = parts[1];
                String startCity = parts[2];
                String destCity = parts[3];
                double price = Double.parseDouble(parts[4]);
                String seatClass = parts[5];
                int row = Integer.parseInt(parts[6]);
                String col = parts[7];
                String vehicleId = parts[8];
                String travelDate = (parts.length == 10) ? parts[9] : "N/A"; // Default to "N/A" for old format

                String transportType = bookingId.startsWith("P") ? "Plane" : bookingId.startsWith("T") ? "Train" : "Bus";
                Seat seat = new Seat(row, col, seatClass, transportType, price);
                seat.reserve(); // Mark as reserved since this is a booked seat

                if (bookingId.startsWith("P")) {
                    PlaneBooking targetPlane = planes.stream()
                            .filter(plane -> plane.getFlightId().equals(vehicleId))
                            .findFirst()
                            .orElse(null);
                    if (targetPlane != null) {
                        targetPlane.addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, travelDate);
                    }
                } else if (bookingId.startsWith("T")) {
                    TrainBooking targetTrain = trains.stream()
                            .filter(train -> train.getTrainId().equals(vehicleId))
                            .findFirst()
                            .orElse(null);
                    if (targetTrain != null) {
                        targetTrain.addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, travelDate);
                    }
                } else if (bookingId.startsWith("B")) {
                    BusBooking targetBus = buses.stream()
                            .filter(bus -> bus.getBusId().equals(vehicleId))
                            .findFirst()
                            .orElse(null);
                    if (targetBus != null) {
                        targetBus.addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, travelDate);
                    }
                }
            }
            System.out.println("\033[1;32mBookings loaded successfully.\033[0m");
        } catch (IOException | NumberFormatException e) {
            System.out.println("\033[1;31mError loading bookings: " + e.getMessage() + "\033[0m");
        }
    }

    /**
     * Removes a booking from the file and updates the bookings.
     */
    public static void removeBooking(String bookingId, List<PlaneBooking> planes, List<TrainBooking> trains, List<BusBooking> buses) {
        Map<String, Object[]> allBookings = new HashMap<>();

        // Collect bookings from Planes
        for (PlaneBooking plane : planes) {
            allBookings.putAll(getBookingsMap(plane, plane.getFlightId()));
        }
        // Collect bookings from Trains
        for (TrainBooking train : trains) {
            allBookings.putAll(getBookingsMap(train, train.getTrainId()));
        }
        // Collect bookings from Buses
        for (BusBooking bus : buses) {
            allBookings.putAll(getBookingsMap(bus, bus.getBusId()));
        }

        // Remove the specified booking
        allBookings.remove(bookingId);

        // Rewrite the bookings file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            for (var entry : allBookings.entrySet()) {
                Object[] booking = entry.getValue();
                writer.write(String.format("%s:%s:%s:%s:%.2f:%s:%d:%s:%s:%s",
                        entry.getKey(), booking[0], booking[1], booking[2], booking[3], booking[4], booking[5], booking[6], booking[7], booking[8]));
                writer.newLine();
            }
            System.out.println("\033[1;32mBooking removed and file updated successfully.\033[0m");
        } catch (IOException e) {
            System.out.println("\033[1;31mError removing booking: " + e.getMessage() + "\033[0m");
        }
    }

    /**
     * Helper method to extract bookings from a booking class.
     */
    private static Map<String, Object[]> getBookingsMap(Object bookingClass, String vehicleId) {
        Map<String, Object[]> bookingMap = new HashMap<>();
        Map<String, ?> bookings;

        if (bookingClass instanceof PlaneBooking) {
            bookings = ((PlaneBooking) bookingClass).getBookings();
        } else if (bookingClass instanceof TrainBooking) {
            bookings = ((TrainBooking) bookingClass).getBookings();
        } else if (bookingClass instanceof BusBooking) {
            bookings = ((BusBooking) bookingClass).getBookings();
        } else {
            return bookingMap;
        }

        for (var entry : bookings.entrySet()) {
            String bookingId = entry.getKey();
            Object booking = entry.getValue();
            String username = (String) invokeMethod(booking, "getUsername");
            String startCity = (String) invokeMethod(booking, "getStartCity");
            String destCity = (String) invokeMethod(booking, "getDestCity");
            double price = (double) invokeMethod(booking, "getPrice");
            String seatClass = (String) invokeMethod(booking, "getSeatClass");
            Seat seat = (Seat) invokeMethod(booking, "getSeat");
            String travelDate = (String) invokeMethod(booking, "getTravelDate");

            bookingMap.put(bookingId, new Object[]{username, startCity, destCity, price, seatClass, seat.getRow(), seat.getColumn(), vehicleId, travelDate});
        }
        return bookingMap;
    }

    /**
     * Helper method to extract bookings without vehicle ID.
     */
    private static Map<String, Object[]> getBookingsMap(Object bookingClass) {
        String vehicleId = "";
        if (bookingClass instanceof PlaneBooking) {
            vehicleId = ((PlaneBooking) bookingClass).getFlightId();
        } else if (bookingClass instanceof TrainBooking) {
            vehicleId = ((TrainBooking) bookingClass).getTrainId();
        } else if (bookingClass instanceof BusBooking) {
            vehicleId = ((BusBooking) bookingClass).getBusId();
        }
        return getBookingsMap(bookingClass, vehicleId);
    }

    /**
     * Helper method to invoke a method via reflection (since Booking is a private inner class).
     */
    private static Object invokeMethod(Object obj, String methodName) {
        try {
            return obj.getClass().getDeclaredMethod(methodName).invoke(obj);
        } catch (Exception e) {
            System.out.println("\033[1;31mError invoking method " + methodName + ": " + e.getMessage() + "\033[0m");
            return null;
        }
    }
}