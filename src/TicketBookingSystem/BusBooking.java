package TicketBookingSystem;

import java.util.HashMap;
import java.util.InputMismatchException; // Import for input validation
import java.util.Map;
import java.util.Scanner;

public class BusBooking {
    // Define layout constants (can be adjusted)
    private static final int BUS_ROWS = 10;
    private static final char[] BUS_COLUMNS = {'A', 'B', 'C', 'D'}; // Example 2+2 layout

    // Seats list for the current booking transaction's display and selection
    private final CustomLinkedList<Seat> seats = new CustomLinkedList<>();
    // Passengers list (optional, could be removed if not used elsewhere)
    private final CustomLinkedList<Passenger> passengers = new CustomLinkedList<>();

    // Persistent storage of bookings for this specific busId
    private Map<String, Booking> bookings = new HashMap<>();

    private final String busId;
    private final BookingSystem bookingSystem; // Reference to the main system

    // Removed startCity, destCity, routePrice as instance variables

    /**
     * Constructor for BusBooking.
     * @param busId The unique ID for this bus instance.
     * @param bookingSystem Reference to the main booking system.
     */
    public BusBooking(String busId, BookingSystem bookingSystem) {
        this.busId = busId;
        this.bookingSystem = bookingSystem;
        // Bookings are loaded by StorageManager via addBooking calls
    }

    /**
     * Initializes the seat layout for a bus booking transaction.
     * Bus typically has only one class ("Standard").
     * @param seatClass Should always be "Standard" for buses in this model.
     * @param finalSeatPrice The calculated price for a standard bus seat.
     */
    private void initializeSeats(String seatClass, double finalSeatPrice) {
        // Basic validation for bus class
        if (!seatClass.equalsIgnoreCase("Standard")) {
            System.out.println("\033[1;31mWarning: Attempting to initialize bus seats with invalid class: " + seatClass + ". Using 'Standard'.\033[0m");
            seatClass = "Standard";
        }

        seats.clear(); // Clear previous layout

        for (int i = 1; i <= BUS_ROWS; i++) {
            for (char c : BUS_COLUMNS) {
                seats.add(new Seat(i, String.valueOf(c), seatClass, "Bus", finalSeatPrice));
            }
        }
        // Mark seats already booked
        markExistingBookings(seatClass);
    }

    /**
     * Marks seats in the newly initialized layout as reserved based on existing bookings
     * for this bus. Since buses usually have one class, no class filter is needed here.
     * @param seatClass The class currently being booked (should be "Standard").
     */
    private void markExistingBookings(String seatClass) {
        // Seat class check might be redundant for bus, but kept for consistency
        for (Booking booked : this.bookings.values()) {
            if (booked.getSeatClass().equalsIgnoreCase(seatClass)) {
                Seat seatInLayout = findSeat(booked.getSeat().getRow(), booked.getSeat().getColumn());
                if (seatInLayout != null && !seatInLayout.isReserved()) {
                    seatInLayout.reserve();
                }
            }
        }
    }

    /**
     * Displays the current bus seating layout.
     * Assumes a 2-2 configuration for display.
     */
    public void displaySeats() {
        if (seats.isEmpty()) {
            System.out.println("\033[1;33mSeat layout not initialized or no seats available.\033[0m");
            return;
        }

        String seatClass = seats.getHead().data.getSeatClass(); // Should be "Standard"
        System.out.println("\n\033[1;36mBus Seating Layout - " + seatClass + " (Bus ID: " + busId + ")\033[0m");
        System.out.println("\033[0;90m(Layout based on simplified 2-2 configuration view)\033[0m");

        int currentRow = -1;
        StringBuilder rowDisplay = new StringBuilder();
        CustomLinkedList.Node<Seat> temp = seats.getHead();

        while (temp != null) {
            Seat seat = temp.data;
            if (seat.getRow() != currentRow) {
                if (currentRow != -1) System.out.println(rowDisplay);
                rowDisplay = new StringBuilder("\033[1mRow " + String.format("%2d", seat.getRow()) + ":\033[0m ");
                currentRow = seat.getRow();
            }

            rowDisplay.append(seat.toString()).append(" "); // Append seat O(1A) / X(1A)

            // Assume aisle after column B for 2-2 display
            if (seat.getColumn().equalsIgnoreCase("B")) {
                rowDisplay.append("  "); // Aisle space
            }

            temp = temp.next;
        }
        if (!rowDisplay.isEmpty()) System.out.println(rowDisplay); // Print last row

        System.out.println("\n\033[1mSeat Legend:\033[0m");
        System.out.println(" \033[32mO(RowCol)\033[0m - Available");
        System.out.println(" \033[31mX(RowCol)\033[0m - Reserved");
    }

    /**
     * Handles the process of booking a specific seat for a user on this bus.
     *
     * @param sc Scanner for user input.
     * @param username The logged-in user.
     * @param startCity The origin city.
     * @param destCity The destination city.
     * @param finalSeatPrice The price for one standard bus seat.
     * @param seatClass Should be "Standard".
     * @param travelDate The selected travel date string.
     */
    public void book(Scanner sc, String username, String startCity, String destCity, double finalSeatPrice, String seatClass, String travelDate) {

        // Initialize seat layout (using "Standard" class)
        initializeSeats("Standard", finalSeatPrice);

        if (seats.isEmpty()) {
            System.out.println("\033[1;31mFailed to initialize bus seats. Cannot proceed.\033[0m");
            return;
        }


        System.out.printf("\033[1mTicket Price (%s, per seat): Rs. \033[32m%.2f\033[0m\n", seatClass, finalSeatPrice);
        displaySeats(); // Show available seats

        Seat selectedSeat = null;
        while (selectedSeat == null) {
            System.out.println("\n\033[1;33mExample: To book Row 5 Column B, enter: 5 B\033[0m");
            System.out.print("\033[1mEnter row number and seat column (or type 'back'): \033[0m");

            String inputLine = sc.nextLine().trim();
            if (inputLine.equalsIgnoreCase("back")) {
                System.out.println("\033[1;33mSeat selection cancelled.\033[0m");
                return; // Exit booking process
            }


            String[] parts = inputLine.split("\\s+");
            if (parts.length == 2) {
                try {
                    int row = Integer.parseInt(parts[0]);
                    String col = parts[1].toUpperCase();
                    Seat potentialSeat = findSeat(row, col);

                    if (potentialSeat == null) {
                        System.out.println("\033[1;31mSeat " + row + col + " not found in this layout. Check available seats.\033[0m");
                    } else if (potentialSeat.isReserved()) {
                        System.out.println("\033[1;31mSeat " + potentialSeat.getSeatId() + " is already reserved. Please choose another.\033[0m");
                    } else {
                        selectedSeat = potentialSeat; // Seat is valid
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\033[1;31mInvalid input format. Please enter row number and column letter (e.g., 5 B).\033[0m");
                }
            } else {
                System.out.println("\033[1;31mInvalid input format. Please enter row and column (e.g., 5 B) or 'back'.\033[0m");
            }
        } // End while loop for seat selection

        // --- Get Passenger Details ---
        String name = "";
        int age = -1;
        String gender = "";

        System.out.printf("\033[1mSelected Seat:\033[0m %s, Price: Rs. \033[32m%.2f\033[0m\n", selectedSeat.getSeatId(), selectedSeat.getPrice());

        while (name.isEmpty()) {
            System.out.print("\033[1mEnter passenger name: \033[0m");
            name = sc.nextLine().trim();
            if (name.isEmpty()) System.out.println("\033[1;31mPassenger name cannot be empty.\033[0m");
        }

        while (age < 0) {
            System.out.print("\033[1mEnter age: \033[0m");
            try {
                age = sc.nextInt();
                sc.nextLine(); // Consume newline
                if (age < 0) {
                    System.out.println("\033[1;31mAge cannot be negative.\033[0m");
                    age = -1;
                } else if (age > 120) {
                    System.out.println("\033[1;33mAge seems high, please verify.\033[0m");
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number for age.\033[0m");
                sc.nextLine(); // Consume invalid input
            }
        }

        while (gender.isEmpty()) {
            System.out.print("\033[1mEnter gender: \033[0m");
            gender = sc.nextLine().trim();
            if (gender.isEmpty()) System.out.println("\033[1;31mGender cannot be empty.\033[0m");
        }


        // --- Confirm and Finalize Booking ---
        Passenger passenger = new Passenger(name, age, gender, selectedSeat);
        // passengers.add(passenger); // Optional

        selectedSeat.reserve(); // Mark seat as reserved

        String bookingId = "B" + bookingSystem.getNextBookingId("B");
        String mapKey = bookingId.toUpperCase(); // Consistent key

        // Use "Standard" for seatClass consistently
        Booking newBooking = new Booking(username, startCity, destCity, selectedSeat.getPrice(), "Standard", selectedSeat, travelDate);
        bookings.put(mapKey, newBooking); // Add to persistent map

        System.out.println("\n----------------------------------------");
        System.out.println("\033[1;32mBooking Successful!\033[0m");
        System.out.println("\033[1mBooking ID:\033[0m " + bookingId);
        System.out.println("\033[1mBus ID:\033[0m " + this.busId);
        System.out.println("\033[1mPassenger:\033[0m " + name);
        System.out.println("\033[1mRoute:\033[0m " + startCity + " to " + destCity);
        System.out.println("\033[1mClass:\033[0m Standard");
        System.out.println("\033[1mSeat:\033[0m " + selectedSeat.getSeatId());
        System.out.println("\033[1mTravel Date:\033[0m " + travelDate);
        System.out.printf("\033[1mTotal Paid: Rs. \033[32m%.2f\033[0m\n", selectedSeat.getPrice());
        System.out.println("----------------------------------------");

        displaySeats(); // Show updated map
    }

    /**
     * Displays bookings made by a specific user on this bus.
     *
     * @param username The user whose bookings are to be displayed.
     * @return true if any bookings were found and displayed, false otherwise.
     */
    public boolean displayUserBookings(String username) {
        boolean hasBookings = false;
        String header = String.format("\n\033[1;36m--- Bus Bookings for %s (Bus ID: %s) ---", username, this.busId);
        // Adjusted column width for Class ("Standard")
        String columns = String.format("\033[1;34m%-10s | %-20s | %-10s | %-11s | %-10s | %s\033[0m",
                "Booking ID", "Route", "Class", "Travel Date", "Price", "Seat");
        String separator = "-----------+----------------------+------------+-------------+------------+------";

        StringBuilder output = new StringBuilder();

        for (Map.Entry<String, Booking> entry : bookings.entrySet()) {
            Booking booking = entry.getValue();
            if (booking.getUsername().equals(username)) {
                if (!hasBookings) {
                    output.append(header).append("\n");
                    output.append(columns).append("\n");
                    output.append(separator).append("\n");
                    hasBookings = true;
                }
                output.append(String.format("%-10s | %-20s | %-10s | %-11s | Rs. %-7.2f | %s\n",
                        entry.getKey(),
                        booking.getStartCity() + "->" + booking.getDestCity(),
                        booking.getSeatClass(), // Will be "Standard"
                        booking.getTravelDate(),
                        booking.getPrice(),
                        booking.getSeat().getSeatId()));
            }
        }

        if (hasBookings) {
            System.out.println(output);
        }
        return hasBookings;
    }

    /**
     * Cancels a booking if the ID exists and belongs to the specified user.
     *
     * @param bookingId The ID of the booking to cancel (case-insensitive).
     * @param username The user attempting the cancellation.
     * @return true if the booking was found and removed, false otherwise.
     */
    public boolean cancelBooking(String bookingId, String username) {
        String mapKey = bookingId.toUpperCase();
        Booking booking = bookings.get(mapKey);

        if (booking != null && booking.getUsername().equals(username)) {
            bookings.remove(mapKey); // Remove from persistent map

            // Attempt to unreserve seat in current layout view
            Seat seatToUnreserve = findSeat(booking.getSeat().getRow(), booking.getSeat().getColumn());
            if (seatToUnreserve != null && seatToUnreserve.isReserved()) {
                seatToUnreserve.unreserve();
                System.out.println("\033[0;90m(Seat " + seatToUnreserve.getSeatId() + " marked as available in current view)\033[0m");
            }
            // Confirmation message handled by BookingSystem
            return true; // Success
        } else {
            return false; // Failure (not found or unauthorized)
        }
    }

    /**
     * Adds a booking record, typically used when loading from storage.
     * Ensures the seat associated with the booking is marked as reserved.
     */
    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat, String travelDate) {
        seat.reserve(); // Ensure seat status is correct
        // Ensure seatClass is "Standard" when adding bus booking
        Booking loadedBooking = new Booking(username, startCity, destCity, price, "Standard", seat, travelDate);
        bookings.put(bookingId.toUpperCase(), loadedBooking); // Use uppercase key
    }

    // Overload for compatibility
    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat) {
        // Pass "Standard" as class, ignore provided seatClass for bus
        addBooking(bookingId, username, startCity, destCity, price, "Standard", seat, "N/A");
    }

    /**
     * Returns the map of bookings for this bus. Used by StorageManager.
     */
    public Map<String, Booking> getBookings() {
        return bookings;
    }

    /**
     * Finds a seat in the current 'seats' list based on row and column.
     *
     * @param row Row number.
     * @param col Column letter (case-insensitive).
     * @return The Seat object if found, null otherwise.
     */
    private Seat findSeat(int row, String col) {
        CustomLinkedList.Node<Seat> temp = seats.getHead();
        while (temp != null) {
            Seat seat = temp.data;
            if (seat.getRow() == row && seat.getColumn().equalsIgnoreCase(col)) {
                return seat;
            }
            temp = temp.next;
        }
        return null; // Not found
    }

    /**
     * Gets the bus ID.
     */
    public String getBusId() {
        return busId;
    }

    /**
     * Inner static class representing the details of a single booking record.
     */
    static class Booking {
        private final String username;
        private final String startCity;
        private final String destCity;
        private final double price;
        private final String seatClass; // Should be "Standard" for Bus
        private final Seat seat; // Reference to the specific Seat
        private final String travelDate;

        public Booking(String username, String startCity, String destCity, double price, String seatClass, Seat seat, String travelDate) {
            this.username = username;
            this.startCity = startCity;
            this.destCity = destCity;
            this.price = price;
            this.seatClass = "Standard"; // Enforce "Standard" for bus bookings
            this.seat = seat;
            this.travelDate = travelDate != null ? travelDate : "N/A";
        }

        // --- Getters ---
        public String getUsername() { return username; }
        public String getStartCity() { return startCity; }
        public String getDestCity() { return destCity; }
        public double getPrice() { return price; }
        public String getSeatClass() { return seatClass; }
        public Seat getSeat() { return seat; }
        public String getTravelDate() { return travelDate; }
    }
}