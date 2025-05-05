package TicketBookingSystem;

import java.util.HashMap;
import java.util.InputMismatchException; // Required import
import java.util.Map;
import java.util.Scanner;

public class PlaneBooking {
    // Seats list for the current booking transaction
    private final CustomLinkedList<Seat> seats = new CustomLinkedList<>();
    // Persistent storage of bookings for this flight (generic ID represents the physical plane/slot)
    private Map<String, Booking> bookings = new HashMap<>();

    private final String flightId; // The generic ID of this plane object (e.g., FL-A320-1)
    private final BookingSystem bookingSystem; // Reference to the main system

    /**
     * Constructor for PlaneBooking.
     * @param flightId The unique ID for this plane instance/slot.
     * @param bookingSystem Reference to the main booking system.
     */
    public PlaneBooking(String flightId, BookingSystem bookingSystem) {
        this.flightId = flightId;
        this.bookingSystem = bookingSystem;
        // Bookings loaded via StorageManager -> BookingSystem -> addBooking
    }

    /**
     * Initializes seat layout for a specific class and price.
     * @param seatClass "Economy", "Business", or "First".
     * @param finalSeatPrice The calculated price for one seat in this class.
     */
    private void initializeSeats(String seatClass, double finalSeatPrice) {
        seats.clear(); // Clear previous layout
        switch (seatClass.toLowerCase()) {
            case "economy":
                generateLayout(10, new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'}, finalSeatPrice, seatClass);
                break;
            case "business":
                generateLayout(8, new char[]{'A', 'B', 'C', 'D'}, finalSeatPrice, seatClass);
                break;
            case "first":
                generateLayout(5, new char[]{'A', 'B'}, finalSeatPrice, seatClass);
                break;
            default:
                System.out.println(Utils.RED + "Error: Invalid seat class '" + seatClass + "' for seat initialization." + Utils.RESET);
                return;
        }
        markExistingBookings(seatClass); // Mark already booked seats
    }

    /** Helper to generate seat nodes. */
    private void generateLayout(int rows, char[] columns, double price, String seatClass) {
        for (int i = 1; i <= rows; i++) {
            for (char c : columns) {
                seats.add(new Seat(i, String.valueOf(c), seatClass, "Plane", price));
            }
        }
    }

    /** Marks seats as reserved based on the loaded bookings map. */
    private void markExistingBookings(String seatClass) {
        for (Booking booked : this.bookings.values()) {
            // Check if the booking belongs to this plane object based on its generic ID
            // (This check might be implicit as the booking is already in this object's map)
            // AND check if it belongs to the class being displayed
            if (booked.getSeatClass().equalsIgnoreCase(seatClass)) {
                Seat seatInLayout = findSeat(booked.getSeat().getRow(), booked.getSeat().getColumn());
                if (seatInLayout != null && !seatInLayout.isReserved()) {
                    seatInLayout.reserve();
                }
            }
        }
    }


    /** Displays the current seat layout with colors and headers. */
    public void displaySeats() {
        if (seats.isEmpty()) {
            System.out.println(Utils.YELLOW + "Seat layout not initialized or no seats available." + Utils.RESET);
            return;
        }
        String seatClass = seats.getHead().data.getSeatClass();
        System.out.println("\n" + Utils.CYAN_BOLD + "Plane Seat Layout - " + seatClass + " (Aircraft ID: " + flightId + ")" + Utils.RESET);

        char firstCol = ' ';
        char lastCol = ' ';
        // Find first and last column dynamically
        if (!seats.isEmpty()) {
            firstCol = seats.getHead().data.getColumn().charAt(0);
            lastCol = firstCol;
            CustomLinkedList.Node<Seat> node = seats.getHead();
            while(node.next != null && node.data.getRow() == seats.getHead().data.getRow()) {
                lastCol = node.next.data.getColumn().charAt(0);
                node = node.next;
            }
        } else { return; } // Should not happen if !seats.isEmpty() check passed


        int[] aisleAfterIndices = new int[]{};
        if (seatClass.equalsIgnoreCase("Economy")) aisleAfterIndices = new int[]{2, 5}; // After C, F (0-based from firstCol)
        else if (seatClass.equalsIgnoreCase("Business")) aisleAfterIndices = new int[]{0, 2}; // After A, C
        else if (seatClass.equalsIgnoreCase("First")) aisleAfterIndices = new int[]{0};    // After A

        // Print Column Headers
        System.out.print("     "); // Space for "Row XX:"
        for (char c = firstCol; c <= lastCol; c++) {
            System.out.print(Utils.YELLOW_BOLD + " " + c + " " + Utils.RESET);
            int colIndex = c - firstCol; // Calculate 0-based index relative to firstCol
            for (int aisleIndex : aisleAfterIndices) {
                if (colIndex == aisleIndex) { System.out.print("   "); break; } // Aisle space in header
            }
        }
        System.out.println();

        // Print Seat Rows
        CustomLinkedList.Node<Seat> temp = seats.getHead();
        int currentRow = -1;
        StringBuilder rowDisplay = new StringBuilder();
        while (temp != null) {
            Seat seat = temp.data;
            if (seat.getRow() != currentRow) {
                if (currentRow != -1) System.out.println(rowDisplay);
                rowDisplay = new StringBuilder(Utils.YELLOW_BOLD + String.format("Row %2d:", seat.getRow()) + Utils.RESET);
                currentRow = seat.getRow();
            }
            rowDisplay.append(" ").append(seat.toString()); // Seat.toString() handles color

            int colIndex = seat.getColumn().charAt(0) - firstCol; // 0-based index relative to firstCol
            for (int aisleIndex : aisleAfterIndices) {
                if (colIndex == aisleIndex) { rowDisplay.append("  "); break; } // Aisle space in row
            }
            temp = temp.next;
        }
        if (!rowDisplay.isEmpty()) System.out.println(rowDisplay);

        // Print Legend
        System.out.println("\n" + Utils.BLUE_BOLD + "Seat Legend:" + Utils.RESET);
        System.out.println(" " + Utils.GREEN + "O" + Utils.RESET + "(RowCol) - Available");
        System.out.println(" " + Utils.RED + "X" + Utils.RESET + "(RowCol) - Reserved");
    }

    /**
     * Handles booking a specific seat for a user, storing the selected provider.
     * Includes input validation for passenger details.
     *
     * @param sc Scanner for user input.
     * @param username Logged-in username.
     * @param startCity Origin city.
     * @param destCity Destination city.
     * @param finalSeatPrice Price for the seat in the selected class.
     * @param seatClass Selected seat class.
     * @param travelDate Validated travel date string.
     * @param selectedProvider The specific airline/service selected by the user.
     */
    public void book(Scanner sc, String username, String startCity, String destCity, double finalSeatPrice, String seatClass, String travelDate, String selectedProvider) {
        initializeSeats(seatClass, finalSeatPrice);
        if (seats.isEmpty()) {
            System.out.println(Utils.RED + "Failed to initialize seats for class: " + seatClass + ". Cannot proceed." + Utils.RESET);
            return;
        }

        System.out.printf("\n" + Utils.BLUE_BOLD + "Booking for Service: " + Utils.CYAN + "%s" + Utils.RESET + "\n", selectedProvider);
        System.out.printf(Utils.BLUE_BOLD + "Ticket Price (" + Utils.CYAN + "%s" + Utils.BLUE_BOLD + ", per seat): Rs. " + Utils.GREEN_BOLD + "%.2f" + Utils.RESET + "\n", seatClass, finalSeatPrice);
        displaySeats();

        Seat selectedSeat = null;
        while (selectedSeat == null) {
            System.out.println("\n" + Utils.YELLOW + "Example: To book Row 2 Seat C, enter: 2 C" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Enter row and seat column (or type 'back'): " + Utils.RESET);
            String inputLine = sc.nextLine().trim();
            if (inputLine.equalsIgnoreCase("back")) { System.out.println(Utils.YELLOW + "Seat selection cancelled." + Utils.RESET); return; }

            String[] parts = inputLine.split("\\s+");
            if (parts.length == 2) {
                try {
                    int row = Integer.parseInt(parts[0]);
                    String col = parts[1].toUpperCase();
                    Seat potentialSeat = findSeat(row, col);
                    if (potentialSeat == null) { System.out.println(Utils.RED + "Seat " + row + col + " not found in this layout." + Utils.RESET); }
                    else if (potentialSeat.isReserved()) { System.out.println(Utils.RED + "Seat " + potentialSeat.getSeatId() + " is already reserved." + Utils.RESET); }
                    else if (!potentialSeat.getSeatClass().equalsIgnoreCase(seatClass)) { System.out.println(Utils.RED + "Error: Seat " + potentialSeat.getSeatId() + " not in selected class " + seatClass + "." + Utils.RESET); }
                    else { selectedSeat = potentialSeat; }
                } catch (NumberFormatException e) { System.out.println(Utils.RED + "Invalid row number format." + Utils.RESET); }
                catch (Exception e) { System.out.println(Utils.RED + "Invalid input: " + e.getMessage() + Utils.RESET); }
            } else { System.out.println(Utils.RED + "Invalid format. Please enter row and column (e.g., 2 C)." + Utils.RESET); }
        }

        // Get Passenger Details using Utils for validation
        System.out.printf(Utils.BLUE + "\nSelected Seat: " + Utils.MAGENTA_BOLD + "%s" + Utils.BLUE + ", Price: Rs. " + Utils.GREEN_BOLD + "%.2f" + Utils.RESET + "\n", selectedSeat.getSeatId(), selectedSeat.getPrice());
        String name = "";
        while (name.isEmpty()) {
            System.out.print(Utils.WHITE_BOLD + "Enter passenger name: " + Utils.RESET);
            name = sc.nextLine().trim();
            if (name.isEmpty()) System.out.println(Utils.RED + "Passenger name cannot be empty." + Utils.RESET);
        }
        int age = Utils.getValidAge(sc);
        String gender = Utils.getValidGender(sc); // Returns standardized gender (e.g., "Male")

        // Finalize Booking
        Passenger passenger = new Passenger(name, age, gender, selectedSeat);
        selectedSeat.reserve();
        String bookingId = "P" + bookingSystem.getNextBookingId("P");
        String mapKey = bookingId.toUpperCase();
        // Pass selectedProvider to the Booking constructor
        Booking newBooking = new Booking(username, startCity, destCity, selectedSeat.getPrice(), seatClass, selectedSeat, travelDate, selectedProvider);
        bookings.put(mapKey, newBooking); // Stored within this PlaneBooking object

        // Confirmation Message
        System.out.println("\n" + Utils.GREEN_BOLD + "========================================");
        System.out.println("       Booking Successful!          ");
        System.out.println("========================================" + Utils.RESET);
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.YELLOW_BOLD + "%s\n" + Utils.RESET, "Booking ID", bookingId);
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.CYAN + "%s\n" + Utils.RESET, "Service", selectedProvider); // Show selected provider
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.MAGENTA + "%s\n" + Utils.RESET, "Passenger", name);
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.MAGENTA + "%d\n" + Utils.RESET, "Age", age);
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.MAGENTA + "%s\n" + Utils.RESET, "Gender", gender);
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.CYAN + "%s -> %s\n" + Utils.RESET, "Route", startCity, destCity);
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.CYAN + "%s\n" + Utils.RESET, "Class", seatClass);
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.YELLOW_BOLD + "%s\n" + Utils.RESET, "Seat", selectedSeat.getSeatId());
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.CYAN + "%s\n" + Utils.RESET, "Travel Date", travelDate);
        System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.GREEN_BOLD + "Rs. %.2f\n" + Utils.RESET, "Total Paid", selectedSeat.getPrice());
        System.out.println(Utils.GREEN_BOLD + "========================================" + Utils.RESET);

        displaySeats(); // Show updated map
    }

    /** Displays user's bookings with formatting and colors, including provider. */
    public boolean displayUserBookings(String username) {
        boolean hasBookings = false;
        String header = String.format("\n" + Utils.CYAN_BOLD + "--- Plane Bookings for " + Utils.YELLOW_BOLD + "%s" + Utils.CYAN_BOLD + " (Associated Aircraft: " + Utils.YELLOW_BOLD + "%s" + Utils.CYAN_BOLD + ") ---" + Utils.RESET, username, this.flightId);
        // Added Provider column
        String columns = String.format(Utils.BLUE_BOLD + "%-10s | %-20s | %-25s | %-11s | %-10s | %-8s | %s" + Utils.RESET, "Booking ID", "Route", "Service/Provider", "Travel Date", "Price", "Seat", "Class");
        String separator = Utils.CYAN + "-----------+----------------------+---------------------------+-------------+------------+----------+---------------------" + Utils.RESET; // Adjust length
        StringBuilder output = new StringBuilder();

        for (Map.Entry<String, Booking> entry : bookings.entrySet()) {
            Booking booking = entry.getValue();
            if (booking.getUsername().equals(username)) {
                if (!hasBookings) { output.append(header).append("\n").append(columns).append("\n").append(separator).append("\n"); hasBookings = true; }
                // Display Provider
                output.append(String.format(Utils.YELLOW_BOLD + "%-10s" + Utils.RESET + " | " + Utils.MAGENTA + "%-20s" + Utils.RESET + " | " + Utils.CYAN + "%-25.25s" + Utils.RESET + " | " + Utils.MAGENTA + "%-11s" + Utils.RESET + " | " + Utils.GREEN_BOLD + "Rs. %-7.2f" + Utils.RESET + " | " + Utils.YELLOW_BOLD + "%-8s" + Utils.RESET + " | " + Utils.MAGENTA+"%s"+Utils.RESET + "\n",
                        entry.getKey(), booking.getStartCity() + "->" + booking.getDestCity(), booking.getProvider(), booking.getTravelDate(), booking.getPrice(), booking.getSeat().getSeatId(), booking.getSeatClass()));
            }
        }
        if (hasBookings) System.out.println(output);
        return hasBookings;
    }

    /** Cancels a booking, returns true on success. */
    public boolean cancelBooking(String bookingId, String username) {
        String mapKey = bookingId.toUpperCase();
        Booking booking = bookings.get(mapKey);
        if (booking != null && booking.getUsername().equals(username)) {
            bookings.remove(mapKey);
            // Attempt to unreserve seat in current layout for visual feedback
            Seat seatToUnreserve = findSeat(booking.getSeat().getRow(), booking.getSeat().getColumn());
            if (seatToUnreserve != null && seatToUnreserve.isReserved()) {
                seatToUnreserve.unreserve();
                System.out.println(Utils.GREY + "(Seat " + seatToUnreserve.getSeatId() + " marked available in current view)" + Utils.RESET);
            }
            return true; // Cancellation successful
        } else { return false; } // Booking not found or not authorized
    }

    /** Adds a booking (used by StorageManager), includes provider. */
    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat, String travelDate, String provider) {
        seat.reserve(); // Ensure loaded seat is reserved
        Booking loadedBooking = new Booking(username, startCity, destCity, price, seatClass, seat, travelDate, provider); // Include provider
        bookings.put(bookingId.toUpperCase(), loadedBooking); // Use uppercase key
    }

    /** Gets the map of bookings associated with this plane object. */
    public Map<String, Booking> getBookings() { return bookings; }

    /** Finds a seat in the current layout list. */
    private Seat findSeat(int row, String col) {
        CustomLinkedList.Node<Seat> temp = seats.getHead();
        while (temp != null) {
            Seat seat = temp.data;
            if (seat.getRow() == row && seat.getColumn().equalsIgnoreCase(col)) return seat;
            temp = temp.next;
        }
        return null; // Not found
    }

    /** Gets the generic flight/aircraft ID for this object. */
    public String getFlightId() { return flightId; }

    /** Inner Booking class (package-private). Stores booking details including the provider. */
    static class Booking { // Package-private
        private final String username;
        private final String startCity;
        private final String destCity;
        private final double price;
        private final String seatClass;
        private final Seat seat;
        private final String travelDate;
        private final String provider; // <-- ADDED

        public Booking(String u, String s, String d, double p, String sc, Seat se, String td, String prov) { // <-- ADDED PARAM
            this.username = u; this.startCity = s; this.destCity = d;
            this.price = p; this.seatClass = sc; this.seat = se;
            this.travelDate = td != null ? td : "N/A";
            this.provider = prov != null ? prov : "N/A"; // <-- STORED
        }
        // Getters...
        public String getUsername() { return username; }
        public String getStartCity() { return startCity; }
        public String getDestCity() { return destCity; }
        public double getPrice() { return price; }
        public String getSeatClass() { return seatClass; }
        public Seat getSeat() { return seat; }
        public String getTravelDate() { return travelDate; }
        public String getProvider() { return provider; } // <-- ADDED GETTER
    }
}