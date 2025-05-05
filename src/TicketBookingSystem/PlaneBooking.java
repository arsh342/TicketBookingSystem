package TicketBookingSystem;

import java.util.HashMap;
import java.util.InputMismatchException; // Import for input validation
import java.util.Map;
import java.util.Scanner;

public class PlaneBooking {
    // Use CustomLinkedList for seats specific to this flight instance when booking
    private final CustomLinkedList<Seat> seats = new CustomLinkedList<>();
    // Passenger list might be less critical if bookings map holds all info
    private final CustomLinkedList<Passenger> passengers = new CustomLinkedList<>();

    // Store loaded bookings specific to this flightId
    // Key: BookingID (e.g., "P1"), Value: Booking details
    private Map<String, Booking> bookings = new HashMap<>();

    private final String flightId;
    private final BookingSystem bookingSystem; // Reference to parent system

    // Removed startCity, destCity, routePrice as instance variables,
    // as they are specific to a booking transaction, not the PlaneBooking object itself.

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
        }
        // Mark seats that are already booked (from the persistent bookings map)
        markExistingBookings(seatClass);
    }

    /**
     * Marks seats in the newly initialized layout as reserved based on existing bookings
     * for this flight and the specified seat class.
     * @param seatClass The class currently being booked.
     */
    private void markExistingBookings(String seatClass) {
        for (Booking booked : this.bookings.values()) {
            // Only mark if the booked seat belongs to the currently viewed class
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
    public void displaySeats() {
        // Check if seats list is empty or head is null
        if (seats.getHead() == null) {
            System.out.println("\033[1;33mSeat layout not initialized or no seats available for this class.\033[0m");
            return;
        }

        // Determine class from the first seat (assumes all seats in the list are of the same class)
        String seatClass = seats.getHead().data.getSeatClass();
        System.out.println("\n\033[1;36mPlane Seat Layout - " + seatClass + " (Flight ID: " + flightId + ")\033[0m");

        // Define aisle spacing based on class (example)
        int[] aisleAfterIndices; // Column indices *after* which an aisle appears
        char lastCol = ' ';
        if (!seats.isEmpty()) { // Get last column dynamically
            CustomLinkedList.Node<Seat> temp = seats.getHead();
            while(temp.next != null) temp = temp.next;
            lastCol = temp.data.getColumn().charAt(0);
        }


        if (seatClass.equalsIgnoreCase("Economy")) {
            aisleAfterIndices = new int[]{2, 5}; // After C and F (0-based index)
        } else if (seatClass.equalsIgnoreCase("Business")) {
            aisleAfterIndices = new int[]{0, 2}; // After A and C
        } else if (seatClass.equalsIgnoreCase("First")) {
            aisleAfterIndices = new int[]{0};    // After A
        } else {
            aisleAfterIndices = new int[]{}; // No aisles for unknown layout
        }

        CustomLinkedList.Node<Seat> temp = seats.getHead();
        int currentRow = -1;
        StringBuilder rowDisplay = new StringBuilder();

        while (temp != null) {
            Seat seat = temp.data;
            // Start new row line
            if (seat.getRow() != currentRow) {
                if (currentRow != -1) System.out.println(rowDisplay); // Print previous row
                rowDisplay = new StringBuilder("\033[1mRow " + String.format("%2d", seat.getRow()) + ":\033[0m "); // Format row number
                currentRow = seat.getRow();
            }

            // Append seat representation (e.g., O(5B))
            rowDisplay.append(seat.toString()).append(" ");

            // Check for adding aisle spacing
            int colIndex = seat.getColumn().charAt(0) - 'A'; // 0-based index
            for (int aisleIndex : aisleAfterIndices) {
                if (colIndex == aisleIndex) {
                    rowDisplay.append("  "); // Add extra space for aisle
                    break;
                }
            }

            temp = temp.next;
        }
        // Print the last row
        if (!rowDisplay.isEmpty()) System.out.println(rowDisplay);

        System.out.println("\n\033[1mSeat Legend:\033[0m");
        System.out.println(" \033[32mO(RowCol)\033[0m - Available");
        System.out.println(" \033[31mX(RowCol)\033[0m - Reserved");
    }

    /**
     * Handles the process of booking a specific seat for a user.
     * Assumes route, class, and price have been determined by BookingSystem.
     *
     * @param sc Scanner for user input.
     * @param username The logged-in user performing the booking.
     * @param startCity The origin city.
     * @param destCity The destination city.
     * @param finalSeatPrice The price for one seat in the selected class.
     * @param seatClass The selected class ("Economy", "Business", "First").
     * @param travelDate The selected travel date string.
     */
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

            // Read the whole line to handle 'back' input easily
            String inputLine = sc.nextLine().trim();
            if (inputLine.equalsIgnoreCase("back")) {
                System.out.println("\033[1;33mSeat selection cancelled.\033[0m");
                return; // Exit booking process
            }

            // Try to parse row and column
            String[] parts = inputLine.split("\\s+"); // Split by whitespace
            if (parts.length == 2) {
                try {
                    int row = Integer.parseInt(parts[0]);
                    String col = parts[1].toUpperCase(); // Ensure column is uppercase

                    Seat potentialSeat = findSeat(row, col);

                    if (potentialSeat == null) {
                        System.out.println("\033[1;31mSeat " + row + col + " not found in this layout. Check available seats.\033[0m");
                    } else if (potentialSeat.isReserved()) {
                        System.out.println("\033[1;31mSeat " + potentialSeat.getSeatId() + " is already reserved. Please choose another.\033[0m");
                    } else if (!potentialSeat.getSeatClass().equalsIgnoreCase(seatClass)) {
                        // This check should ideally be redundant if initializeSeats works correctly
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
                    age = -1; // Reset to loop again
                } else if (age > 120) { // Basic sanity check
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
            // Optional: Validate gender input (e.g., Male/Female/Other)
        }


        // --- Confirm and Finalize Booking ---
        Passenger passenger = new Passenger(name, age, gender, selectedSeat);
        // passengers.add(passenger); // Adding to this list might be redundant if bookings map is primary store

        selectedSeat.reserve(); // Mark seat as reserved in the current view

        String bookingId = "P" + bookingSystem.getNextBookingId("P");
        // Use uppercase ID for map key consistency
        String mapKey = bookingId.toUpperCase();

        // Create the booking record
        Booking newBooking = new Booking(username, startCity, destCity, selectedSeat.getPrice(), seatClass, selectedSeat, travelDate);
        // Add to the persistent map for this PlaneBooking instance
        bookings.put(mapKey, newBooking);

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

    /**
     * Displays bookings made by a specific user on this flight.
     * Improved formatting using printf.
     *
     * @param username The user whose bookings are to be displayed.
     * @return true if any bookings were found and displayed for the user, false otherwise.
     */
    public boolean displayUserBookings(String username) {
        boolean hasBookings = false;
        // Prepare header string
        String header = String.format("\n\033[1;36m--- Plane Bookings for %s (Flight ID: %s) ---", username, this.flightId);
        String columns = String.format("\033[1;34m%-10s | %-20s | %-10s | %-11s | %-10s | %s\033[0m",
                "Booking ID", "Route", "Class", "Travel Date", "Price", "Seat");
        String separator = "-----------+----------------------+------------+-------------+------------+------";

        StringBuilder output = new StringBuilder();

        // Iterate through bookings map
        for (Map.Entry<String, Booking> entry : bookings.entrySet()) {
            Booking booking = entry.getValue();
            // Check if the booking belongs to the specified user
            if (booking.getUsername().equals(username)) {
                // Print header only once when the first booking is found
                if (!hasBookings) {
                    output.append(header).append("\n");
                    output.append(columns).append("\n");
                    output.append(separator).append("\n");
                    hasBookings = true;
                }
                // Append formatted booking details
                output.append(String.format("%-10s | %-20s | %-10s | %-11s | Rs. %-7.2f | %s\n",
                        entry.getKey(), // Booking ID
                        booking.getStartCity() + "->" + booking.getDestCity(), // Route
                        booking.getSeatClass(), // Class
                        booking.getTravelDate(), // Travel Date
                        booking.getPrice(), // Price
                        booking.getSeat().getSeatId())); // Seat ID (e.g., "5B")
            }
        }

        // Print the collected output if any bookings were found
        if (hasBookings) {
            System.out.println(output);
        }
        // Indicate whether bookings were found for this user on this flight
        return hasBookings;
    }


    /**
     * Cancels a booking if the ID exists and belongs to the specified user.
     * Attempts to unreserve the seat in the *current* seat list if available.
     * Note: Unreserving the seat here only affects the current booking session's display.
     * The primary action is removing the booking from the map, which persists.
     *
     * @param bookingId The ID of the booking to cancel (case-insensitive).
     * @param username The user attempting the cancellation.
     * @return true if the booking was found and removed, false otherwise.
     */
    public boolean cancelBooking(String bookingId, String username) {
        // Use uppercase for lookup consistency
        String mapKey = bookingId.toUpperCase();
        Booking booking = bookings.get(mapKey);

        // Check if booking exists and belongs to the user
        if (booking != null && booking.getUsername().equals(username)) {
            // Remove the booking from the map
            bookings.remove(mapKey);

            // Attempt to find and unreserve the corresponding seat in the *current* seat layout
            // This is mainly for visual feedback if the user cancels right after booking.
            // It might not find the seat if the layout wasn't initialized for the same class recently.
            Seat seatToUnreserve = findSeat(booking.getSeat().getRow(), booking.getSeat().getColumn());
            if (seatToUnreserve != null && seatToUnreserve.isReserved()) {
                seatToUnreserve.unreserve();
                System.out.println("\033[0;90m(Seat " + seatToUnreserve.getSeatId() + " marked as available in current view)\033[0m");
            } else {
                // It's okay if the seat isn't found in the current list,
                // the booking is still removed from the persistent map.
            }
            // Confirmation message is handled by the caller (BookingSystem)
            return true; // Indicate cancellation success
        } else {
            // Booking not found or doesn't belong to the user
            return false; // Indicate cancellation failure
        }
    }

    /**
     * Adds a booking record, typically used when loading from storage.
     * Ensures the seat associated with the booking is marked as reserved.
     * @param bookingId Booking ID.
     * @param username User who made the booking.
     * @param startCity Origin city.
     * @param destCity Destination city.
     * @param price Price paid.
     * @param seatClass Seat class.
     * @param seat The Seat object.
     * @param travelDate Travel date string.
     */
    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat, String travelDate) {
        // Ensure the seat object reflects its booked status
        seat.reserve();
        Booking loadedBooking = new Booking(username, startCity, destCity, price, seatClass, seat, travelDate);
        // Use uppercase for map key consistency
        bookings.put(bookingId.toUpperCase(), loadedBooking);
    }

    // Overload for compatibility with older storage format (if needed)
    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat) {
        addBooking(bookingId, username, startCity, destCity, price, seatClass, seat, "N/A"); // Default travel date
    }

    /**
     * Returns the map of bookings for this flight.
     * Primarily used by StorageManager.
     * @return The map of bookings.
     */
    public Map<String, Booking> getBookings() {
        return bookings;
    }

    /**
     * Finds a seat in the current 'seats' list based on row and column.
     * Used during the booking process and potentially during cancellation.
     *
     * @param row Row number.
     * @param col Column letter (case-insensitive).
     * @return The Seat object if found, null otherwise.
     */
    private Seat findSeat(int row, String col) {
        CustomLinkedList.Node<Seat> temp = seats.getHead();
        while (temp != null) {
            Seat seat = temp.data;
            // Check row and case-insensitive column match
            if (seat.getRow() == row && seat.getColumn().equalsIgnoreCase(col)) {
                return seat;
            }
            temp = temp.next;
        }
        return null; // Seat not found in the current list
    }

    /**
     * Gets the flight ID.
     * @return The flight ID string.
     */
    public String getFlightId() {
        return flightId;
    }

    /**
     * Inner class representing the details of a single booking record.
     * Made static as it doesn't need access to PlaneBooking instance variables directly.
     */
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

        // --- Getters for Booking details ---
        public String getUsername() { return username; }
        public String getStartCity() { return startCity; }
        public String getDestCity() { return destCity; }
        public double getPrice() { return price; }
        public String getSeatClass() { return seatClass; }
        public Seat getSeat() { return seat; } // Returns the Seat object
        public String getTravelDate() { return travelDate; }
    }
}