package TicketBookingSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.InputMismatchException; // Make sure this is imported

public class PlaneBooking {
    // Use CustomLinkedList for seats specific to this flight instance when booking
    private final CustomLinkedList<Seat> seats = new CustomLinkedList<>();
    // Passenger list might be less critical if bookings map holds all info
    // private final CustomLinkedList<Passenger> passengers = new CustomLinkedList<>(); // Can likely be removed if not used

    // Store loaded bookings specific to this flightId
    // Key: BookingID (e.g., "P1"), Value: Booking details
    private Map<String, Booking> bookings = new HashMap<>();

    private final String flightId;
    private final BookingSystem bookingSystem; // Reference to parent system

    // Removed startCity, destCity, routePrice instance variables

    /**
     * Constructor for PlaneBooking.
     * @param flightId The unique ID for this flight instance.
     * @param bookingSystem A reference to the main booking system for accessing shared resources like booking ID generation.
     */
    public PlaneBooking(String flightId, BookingSystem bookingSystem) {
        this.flightId = flightId;
        this.bookingSystem = bookingSystem;
        // Bookings are loaded by StorageManager via addBooking calls in BookingSystem constructor
    }

    /**
     * Initializes the seat layout for a specific class and base price for a new booking transaction.
     * Clears any existing seats in the list for this instance.
     * @param seatClass The class ("Economy", "Business", "First").
     * @param finalSeatPrice The calculated final price for a seat in this class for this transaction.
     */
    // Updated from previous version to accept finalSeatPrice directly
    private void initializeSeats(String seatClass, double finalSeatPrice) {
        seats.clear(); // Clear previous layout if any
        switch (seatClass.toLowerCase()) { // Use lowercase for robustness
            case "economy":
                // Example layout: 10 rows, 9 columns (A-I), grouped 3-3-3
                generateLayout(10, new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'}, finalSeatPrice, seatClass);
                break;
            case "business":
                // Example layout: 8 rows, 4 columns (A-D), grouped 1-2-1
                generateLayout(8, new char[]{'A', 'B', 'C', 'D'}, finalSeatPrice, seatClass);
                break;
            case "first":
                // Example layout: 5 rows, 2 columns (A-B), grouped 1-1
                generateLayout(5, new char[]{'A', 'B'}, finalSeatPrice, seatClass);
                break;
            default:
                System.out.println("\033[1;31mError: Invalid seat class '" + seatClass + "' provided for seat initialization.\033[0m");
                return; // Stop initialization if class is invalid
        }
        // Mark seats that are already booked (from the persistent bookings map)
        markExistingBookings(seatClass);
    }


    /**
     * Marks seats in the newly initialized layout as reserved based on existing bookings
     * for this flight and the specified seat class.
     * @param seatClass The class currently being booked.
     */
    // (Keep markExistingBookings as provided before)
    private void markExistingBookings(String seatClass) {
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
     * Helper to generate the seat nodes for the CustomLinkedList.
     */
    // (Keep generateLayout as provided before)
    private void generateLayout(int rows, char[] columns, double price, String seatClass) {
        for (int i = 1; i <= rows; i++) {
            for (char c : columns) {
                seats.add(new Seat(i, String.valueOf(c), seatClass, "Plane", price));
            }
        }
    }

    /**
     * Displays the current seating layout for the initialized class.
     */
    // (Keep displaySeats as provided before)
    public void displaySeats() {
        if (seats.isEmpty()) {
            System.out.println("\033[1;33mSeat layout not initialized or no seats available for this class.\033[0m");
            return;
        }
        String seatClass = seats.getHead().data.getSeatClass();
        System.out.println("\n\033[1;36mPlane Seat Layout - " + seatClass + " (Flight ID: " + flightId + ")\033[0m");
        int[] aisleAfterIndices;
        char lastCol = ' ';
        if (!seats.isEmpty()) {
            CustomLinkedList.Node<Seat> node = seats.getHead();
            while(node.next != null) node = node.next;
            lastCol = node.data.getColumn().charAt(0);
        }
        if (seatClass.equalsIgnoreCase("Economy")) aisleAfterIndices = new int[]{2, 5};
        else if (seatClass.equalsIgnoreCase("Business")) aisleAfterIndices = new int[]{0, 2};
        else if (seatClass.equalsIgnoreCase("First")) aisleAfterIndices = new int[]{0};
        else aisleAfterIndices = new int[]{};

        CustomLinkedList.Node<Seat> temp = seats.getHead();
        int currentRow = -1;
        StringBuilder rowDisplay = new StringBuilder();
        while (temp != null) {
            Seat seat = temp.data;
            if (seat.getRow() != currentRow) {
                if (currentRow != -1) System.out.println(rowDisplay);
                rowDisplay = new StringBuilder("\033[1mRow " + String.format("%2d", seat.getRow()) + ":\033[0m ");
                currentRow = seat.getRow();
            }
            rowDisplay.append(seat.toString()).append(" ");
            int colIndex = seat.getColumn().charAt(0) - 'A';
            for (int aisleIndex : aisleAfterIndices) {
                if (colIndex == aisleIndex) {
                    rowDisplay.append("  ");
                    break;
                }
            }
            temp = temp.next;
        }
        if (!rowDisplay.isEmpty()) System.out.println(rowDisplay);
        System.out.println("\n\033[1mSeat Legend:\033[0m");
        System.out.println(" \033[32mO(RowCol)\033[0m - Available");
        System.out.println(" \033[31mX(RowCol)\033[0m - Reserved");
    }


    /**
     * Handles the process of booking a specific seat for a user.
     * Includes validation for date, age, and gender input.
     *
     * @param sc Scanner for user input.
     * @param username The logged-in user performing the booking.
     * @param startCity The origin city.
     * @param destCity The destination city.
     * @param finalSeatPrice The price for one seat in the selected class.
     * @param seatClass The selected class ("Economy", "Business", "First").
     * @param travelDate The selected travel date string (pre-validated by BookingSystem).
     */
    // *** Updated book method with validation ***
    public void book(Scanner sc, String username, String startCity, String destCity, double finalSeatPrice, String seatClass, String travelDate) {

        // Initialize/Re-initialize seat layout for this specific booking attempt
        initializeSeats(seatClass, finalSeatPrice);

        // Check if initialization failed or resulted in no seats
        if (seats.isEmpty()) {
            System.out.println("\033[1;31mFailed to initialize seats for class: " + seatClass + ". Cannot proceed.\033[0m");
            return;
        }

        System.out.printf("\033[1mTicket Price (%s, per seat): Rs. \033[32m%.2f\033[0m\n", seatClass, finalSeatPrice);
        displaySeats(); // Show the available seats

        Seat selectedSeat = null;
        while (selectedSeat == null) {
            System.out.println("\n\033[1;33mExample: To book Row 2 Seat C, enter: 2 C\033[0m");
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
                    } else if (!potentialSeat.getSeatClass().equalsIgnoreCase(seatClass)) {
                        System.out.println("\033[1;31mError: Seat " + potentialSeat.getSeatId() + " is not in the selected class (" + seatClass + ").\033[0m");
                    }
                    else {
                        selectedSeat = potentialSeat; // Valid seat selected
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\033[1;31mInvalid input format. Please enter row number and column letter (e.g., 5 B).\033[0m");
                }
            } else {
                System.out.println("\033[1;31mInvalid input format. Please enter row and column (e.g., 2 C) or 'back'.\033[0m");
            }
        } // End while loop for seat selection

        // --- Get Passenger Details with Validation ---
        String name = "";
        System.out.printf("\033[1mSelected Seat:\033[0m %s, Price: Rs. \033[32m%.2f\033[0m\n", selectedSeat.getSeatId(), selectedSeat.getPrice());

        // Name (simple non-empty validation)
        while (name.isEmpty()) {
            System.out.print("\033[1mEnter passenger name: \033[0m");
            name = sc.nextLine().trim();
            if (name.isEmpty()) System.out.println("\033[1;31mPassenger name cannot be empty.\033[0m");
        }

        // Age (using Utils helper)
        int age = Utils.getValidAge(sc);

        // Gender (using Utils helper)
        String gender = Utils.getValidGender(sc);

        // --- Confirm and Finalize Booking ---
        Passenger passenger = new Passenger(name, age, gender, selectedSeat);
        // passengers.add(passenger); // Optional

        selectedSeat.reserve(); // Mark seat as reserved in the current view

        String bookingId = "P" + bookingSystem.getNextBookingId("P");
        String mapKey = bookingId.toUpperCase(); // Use uppercase ID for map key consistency

        // Create the booking record
        Booking newBooking = new Booking(username, startCity, destCity, selectedSeat.getPrice(), seatClass, selectedSeat, travelDate);
        bookings.put(mapKey, newBooking); // Add to the persistent map for this PlaneBooking instance

        // Print Confirmation
        System.out.println("\n----------------------------------------");
        System.out.println("\033[1;32mBooking Successful!\033[0m");
        System.out.println("\033[1mBooking ID:\033[0m " + bookingId);
        System.out.println("\033[1mFlight ID:\033[0m " + this.flightId);
        System.out.println("\033[1mPassenger:\033[0m " + name);
        System.out.println("\033[1mRoute:\033[0m " + startCity + " to " + destCity);
        System.out.println("\033[1mClass:\033[0m " + seatClass);
        System.out.println("\033[1mSeat:\033[0m " + selectedSeat.getSeatId());
        System.out.println("\033[1mTravel Date:\033[0m " + travelDate);
        System.out.printf("\033[1mTotal Paid: Rs. \033[32m%.2f\033[0m\n", selectedSeat.getPrice());
        System.out.println("----------------------------------------");

        // Display updated seat map
        displaySeats();
    }


    // (Keep displayUserBookings as updated before - returning boolean)
    public boolean displayUserBookings(String username) {
        boolean hasBookings = false;
        String header = String.format("\n\033[1;36m--- Plane Bookings for %s (Flight ID: %s) ---", username, this.flightId);
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
                        entry.getKey(), booking.getStartCity() + "->" + booking.getDestCity(),
                        booking.getSeatClass(), booking.getTravelDate(), booking.getPrice(),
                        booking.getSeat().getSeatId()));
            }
        }
        if (hasBookings) System.out.println(output);
        return hasBookings;
    }

    // (Keep cancelBooking as updated before - returning boolean)
    public boolean cancelBooking(String bookingId, String username) {
        String mapKey = bookingId.toUpperCase();
        Booking booking = bookings.get(mapKey);
        if (booking != null && booking.getUsername().equals(username)) {
            bookings.remove(mapKey);
            Seat seatToUnreserve = findSeat(booking.getSeat().getRow(), booking.getSeat().getColumn());
            if (seatToUnreserve != null && seatToUnreserve.isReserved()) {
                seatToUnreserve.unreserve();
                System.out.println("\033[0;90m(Seat " + seatToUnreserve.getSeatId() + " marked as available in current view)\033[0m");
            }
            return true;
        } else {
            return false;
        }
    }


    // (Keep addBooking methods as updated before)
    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat, String travelDate) {
        seat.reserve();
        Booking loadedBooking = new Booking(username, startCity, destCity, price, seatClass, seat, travelDate);
        bookings.put(bookingId.toUpperCase(), loadedBooking);
    }
    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat) {
        addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, "N/A");
    }

    // (Keep getBookings as updated before)
    public Map<String, Booking> getBookings() {
        return bookings;
    }

    // (Keep findSeat as updated before)
    private Seat findSeat(int row, String col) {
        CustomLinkedList.Node<Seat> temp = seats.getHead();
        while (temp != null) {
            Seat seat = temp.data;
            if (seat.getRow() == row && seat.getColumn().equalsIgnoreCase(col)) {
                return seat;
            }
            temp = temp.next;
        }
        return null;
    }

    // (Keep getFlightId as before)
    public String getFlightId() {
        return flightId;
    }

    // (Keep Booking inner class as updated before - 'static' not 'private static')
    static class Booking {
        private final String username;
        private final String startCity;
        private final String destCity;
        private final double price;
        private final String seatClass;
        private final Seat seat; // Store the specific seat object
        private final String travelDate;

        public Booking(String username, String startCity, String destCity, double price, String seatClass, Seat seat, String travelDate) {
            this.username = username;
            this.startCity = startCity;
            this.destCity = destCity;
            this.price = price;
            this.seatClass = seatClass;
            this.seat = seat; // The Seat object itself (includes row, col, status)
            this.travelDate = travelDate != null ? travelDate : "N/A"; // Handle null travelDate
        }

        // --- Getters ---
        public String getUsername() { return username; }
        public String getStartCity() { return startCity; }
        public String getDestCity() { return destCity; }
        public double getPrice() { return price; }
        public String getSeatClass() { return seatClass; }
        public Seat getSeat() { return seat; } // Returns the Seat object
        public String getTravelDate() { return travelDate; }
    }
}