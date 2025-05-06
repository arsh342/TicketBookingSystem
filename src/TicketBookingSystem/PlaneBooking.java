package TicketBookingSystem;

import java.util.ArrayList; // Required for sorting/displaying
import java.util.Collections;// Required for sorting/displaying
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List; // Required for sorting/displaying
import java.util.Map;
import java.util.Scanner;

/**
 * Manages bookings and seat layout for a specific plane instance/slot.
 */
public class PlaneBooking {
    private final CustomLinkedList<Seat> seats = new CustomLinkedList<>();
    private Map<String, Booking> bookings = new HashMap<>();
    private final String flightId;
    private final BookingSystem bookingSystem;

    public PlaneBooking(String flightId, BookingSystem bookingSystem) {
        this.flightId = flightId;
        this.bookingSystem = bookingSystem;
    }

    private void initializeSeats(String seatClass, double finalSeatPrice) {
        seats.clear();
        switch (seatClass.toLowerCase()) {
            case "economy": generateLayout(10, new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'}, finalSeatPrice, seatClass); break;
            case "business": generateLayout(8, new char[]{'A', 'B', 'C', 'D'}, finalSeatPrice, seatClass); break;
            case "first": generateLayout(5, new char[]{'A', 'B'}, finalSeatPrice, seatClass); break;
            default: System.out.println(Utils.RED + "Error: Invalid seat class '" + seatClass + "'." + Utils.RESET); return;
        }
        markExistingBookings(seatClass);
    }

    private void generateLayout(int rows, char[] columns, double price, String seatClass) {
        for (int i = 1; i <= rows; i++) for (char c : columns) seats.add(new Seat(i, String.valueOf(c), seatClass, "Plane", price));
    }

    private void markExistingBookings(String seatClass) {
        for (Booking booked : this.bookings.values()) {
            if (booked.getSeatClass().equalsIgnoreCase(seatClass)) {
                Seat seatInLayout = findSeat(booked.getSeat().getRow(), booked.getSeat().getColumn());
                if (seatInLayout != null && !seatInLayout.isReserved()) seatInLayout.reserve();
            }
        }
    }

    public void displaySeats() {
        if (seats.isEmpty()) { System.out.println(Utils.YELLOW + "No seats initialized." + Utils.RESET); return; }
        String seatClass = seats.getHead().data.getSeatClass();
        System.out.println("\n" + Utils.CYAN_BOLD + "Plane Seat Layout - " + seatClass + " (Aircraft ID: " + flightId + ")" + Utils.RESET);
        char firstCol = ' '; char lastCol = ' ';
        if (!seats.isEmpty()) { firstCol = seats.getHead().data.getColumn().charAt(0); lastCol = firstCol; CustomLinkedList.Node<Seat> node = seats.getHead(); while(node != null && node.data.getRow() == seats.getHead().data.getRow()) { lastCol = node.data.getColumn().charAt(0); node = node.next; } } else { return; }
        int[] aisleAfterIndices = new int[]{};
        if (seatClass.equalsIgnoreCase("Economy")) aisleAfterIndices = new int[]{2, 5}; else if (seatClass.equalsIgnoreCase("Business")) aisleAfterIndices = new int[]{0, 2}; else if (seatClass.equalsIgnoreCase("First")) aisleAfterIndices = new int[]{0};
        System.out.print("     ");
        for (char c = firstCol; c <= lastCol; c++) { System.out.print(Utils.YELLOW_BOLD + " " + c + " " + Utils.RESET); int colIndex = c - firstCol; for (int aisleIndex : aisleAfterIndices) if (colIndex == aisleIndex) { System.out.print("   "); break; } } System.out.println();
        CustomLinkedList.Node<Seat> temp = seats.getHead(); int currentRow = -1; StringBuilder rowDisplay = new StringBuilder();
        while (temp != null) { Seat seat = temp.data; if (seat.getRow() != currentRow) { if (currentRow != -1) System.out.println(rowDisplay); rowDisplay = new StringBuilder(Utils.YELLOW_BOLD + String.format("Row %2d:", seat.getRow()) + Utils.RESET); currentRow = seat.getRow(); } rowDisplay.append(" ").append(seat.toString()); int colIndex = seat.getColumn().charAt(0) - firstCol; for (int aisleIndex : aisleAfterIndices) if (colIndex == aisleIndex) { rowDisplay.append("  "); break; } temp = temp.next; } if (!rowDisplay.isEmpty()) System.out.println(rowDisplay);
        System.out.println("\n" + Utils.BLUE_BOLD + "Seat Legend:" + Utils.RESET); System.out.println(" " + Utils.GREEN + "O" + Utils.RESET + "(RowCol) - Available"); System.out.println(" " + Utils.RED + "X" + Utils.RESET + "(RowCol) - Reserved");
    }

    public void book(Scanner sc, String username, String startCity, String destCity, double finalSeatPrice, String seatClass, String travelDate, String selectedProvider) {
        initializeSeats(seatClass, finalSeatPrice);
        if (seats.isEmpty()) { System.out.println(Utils.RED + "Failed to initialize seats." + Utils.RESET); return; }
        System.out.printf("\n" + Utils.BLUE_BOLD + "Booking Service: " + Utils.CYAN + "%s" + Utils.RESET + "\n", selectedProvider); System.out.printf(Utils.BLUE_BOLD + "Price (" + Utils.CYAN + "%s" + Utils.BLUE_BOLD + "/seat): Rs. " + Utils.GREEN_BOLD + "%.2f" + Utils.RESET + "\n", seatClass, finalSeatPrice); displaySeats();
        Seat selectedSeat = null;
        while (selectedSeat == null) { /* ... Seat selection loop ... */
            System.out.println("\n" + Utils.YELLOW + "Example: 2 C" + Utils.RESET); System.out.print(Utils.WHITE_BOLD + "Enter row & seat (or 'back'): " + Utils.RESET); String inputLine = sc.nextLine().trim(); if (inputLine.equalsIgnoreCase("back")) { System.out.println(Utils.YELLOW + "Cancelled." + Utils.RESET); return; } String[] parts = inputLine.split("\\s+"); if (parts.length == 2) { try { int row = Integer.parseInt(parts[0]); String col = parts[1].toUpperCase(); Seat potSeat = findSeat(row, col); if (potSeat == null) System.out.println(Utils.RED + "Seat not found." + Utils.RESET); else if (potSeat.isReserved()) System.out.println(Utils.RED + "Seat reserved." + Utils.RESET); else if (!potSeat.getSeatClass().equalsIgnoreCase(seatClass)) System.out.println(Utils.RED + "Seat not in class." + Utils.RESET); else selectedSeat = potSeat; } catch (Exception e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); } } else System.out.println(Utils.RED + "Invalid format." + Utils.RESET);
        }
        System.out.printf(Utils.BLUE + "\nSelected Seat: " + Utils.MAGENTA_BOLD + "%s" + Utils.BLUE + ", Price: Rs. " + Utils.GREEN_BOLD + "%.2f" + Utils.RESET + "\n", selectedSeat.getSeatId(), selectedSeat.getPrice());
        String name = ""; while (name.isEmpty()) { /* ... Get Name ... */ System.out.print(Utils.WHITE_BOLD + "Passenger Name: " + Utils.RESET); name = sc.nextLine().trim(); if(name.isEmpty()) System.out.println(Utils.RED+"Cannot be empty."+Utils.RESET);}
        int age = Utils.getValidAge(sc); String gender = Utils.getValidGender(sc); String email = Utils.getValidEmail(sc); if (email == null) { System.out.println(Utils.YELLOW + "Booking cancelled." + Utils.RESET); return; }
        boolean paymentOk = Utils.simulatePayment(sc, selectedSeat.getPrice());
        if (paymentOk) { Passenger passenger = new Passenger(name, age, gender, email, selectedSeat); selectedSeat.reserve(); String bookingId = "P" + bookingSystem.getNextBookingId("P"); String mapKey = bookingId.toUpperCase(); Booking newBooking = new Booking(username, startCity, destCity, selectedSeat.getPrice(), seatClass, selectedSeat, travelDate, selectedProvider); bookings.put(mapKey, newBooking);
            System.out.println("\n" + Utils.GREEN_BOLD + "================ BOOKING CONFIRMED ================" + Utils.RESET);
            System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.YELLOW_BOLD + "%s\n" + Utils.RESET, "Booking ID", bookingId); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.CYAN + "%s\n" + Utils.RESET, "Service", selectedProvider); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.MAGENTA + "%s\n" + Utils.RESET, "Passenger", name); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.MAGENTA + "%d\n" + Utils.RESET, "Age", age); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.MAGENTA + "%s\n" + Utils.RESET, "Gender", gender); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.MAGENTA + "%s\n" + Utils.RESET, "Email", email); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.CYAN + "%s -> %s\n" + Utils.RESET, "Route", startCity, destCity); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.CYAN + "%s\n" + Utils.RESET, "Class", seatClass); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.YELLOW_BOLD + "%s\n" + Utils.RESET, "Seat", selectedSeat.getSeatId()); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.CYAN + "%s\n" + Utils.RESET, "Travel Date", travelDate); System.out.printf(Utils.BLUE_BOLD + "%-15s: " + Utils.GREEN_BOLD + "Rs. %.2f\n" + Utils.RESET, "Amount Paid", selectedSeat.getPrice()); System.out.println(Utils.GREEN_BOLD + "===============================================" + Utils.RESET); displaySeats();
        } else { System.out.println(Utils.RED_BOLD + "\nBooking Failed (Payment Cancelled/Failed)." + Utils.RESET); }
    }

    public boolean displayUserBookings(String username) {
        boolean hasBookings = false; String header = String.format("\n" + Utils.CYAN_BOLD + "--- Plane Bookings for " + Utils.YELLOW_BOLD + "%s" + Utils.CYAN_BOLD + " (Aircraft: " + Utils.YELLOW_BOLD + "%s" + Utils.CYAN_BOLD + ") ---" + Utils.RESET, username, this.flightId); String columns = String.format(Utils.BLUE_BOLD + "%-10s | %-20s | %-25s | %-11s | %-10s | %-8s | %s" + Utils.RESET, "Booking ID", "Route", "Service/Provider", "Travel Date", "Price", "Seat", "Class"); String separator = Utils.CYAN + "-----------+----------------------+---------------------------+-------------+------------+----------+---------------------" + Utils.RESET; StringBuilder output = new StringBuilder();
        List<Map.Entry<String, Booking>> sortedBookings = new ArrayList<>(bookings.entrySet()); sortedBookings.sort(Map.Entry.comparingByKey()); // Sort by Booking ID
        for (Map.Entry<String, Booking> entry : sortedBookings) { Booking booking = entry.getValue(); if (booking.getUsername().equals(username)) { if (!hasBookings) { output.append(header).append("\n").append(columns).append("\n").append(separator).append("\n"); hasBookings = true; } output.append(String.format(Utils.YELLOW_BOLD + "%-10s" + Utils.RESET + " | " + Utils.MAGENTA + "%-20s" + Utils.RESET + " | " + Utils.CYAN + "%-25.25s" + Utils.RESET + " | " + Utils.MAGENTA + "%-11s" + Utils.RESET + " | " + Utils.GREEN_BOLD + "Rs. %-7.2f" + Utils.RESET + " | " + Utils.YELLOW_BOLD + "%-8s" + Utils.RESET + " | " + Utils.MAGENTA+"%s"+Utils.RESET + "\n", entry.getKey(), booking.getStartCity() + "->" + booking.getDestCity(), booking.getProvider(), booking.getTravelDate(), booking.getPrice(), booking.getSeat().getSeatId(), booking.getSeatClass())); } } if (hasBookings) System.out.println(output); return hasBookings;
    }

    /** Cancels a booking if ID exists AND (username is null (admin) OR username matches). */
    public boolean cancelBooking(String bookingId, String username) {
        String mapKey = bookingId.toUpperCase();
        Booking booking = bookings.get(mapKey);
        // *** MODIFIED CONDITION FOR ADMIN ***
        if (booking != null && (username == null || booking.getUsername().equals(username))) {
            bookings.remove(mapKey);
            Seat seatToUnreserve = findSeat(booking.getSeat().getRow(), booking.getSeat().getColumn());
            if (seatToUnreserve != null && seatToUnreserve.isReserved()) { seatToUnreserve.unreserve(); System.out.println(Utils.GREY + "(Seat " + seatToUnreserve.getSeatId() + " marked available)" + Utils.RESET); }
            return true; // Success
        } else { return false; } // Not found or wrong user
    }

    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat, String travelDate, String provider) {
        seat.reserve(); Booking loadedBooking = new Booking(username, startCity, destCity, price, seatClass, seat, travelDate, provider); bookings.put(bookingId.toUpperCase(), loadedBooking);
    }

    public Map<String, Booking> getBookings() { return bookings; }

    private Seat findSeat(int row, String col) { CustomLinkedList.Node<Seat> temp = seats.getHead(); while (temp != null) { Seat seat = temp.data; if (seat.getRow() == row && seat.getColumn().equalsIgnoreCase(col)) return seat; temp = temp.next; } return null; }

    public String getFlightId() { return flightId; }

    /** Inner Booking class (package-private). Stores booking details including the provider. */
    static class Booking { // Package-private
        final String username; final String startCity; final String destCity; final double price; final String seatClass; final Seat seat; final String travelDate; final String provider;
        public Booking(String u, String s, String d, double p, String sc, Seat se, String td, String prov) { this.username=u; this.startCity=s; this.destCity=d; this.price=p; this.seatClass=sc; this.seat=se; this.travelDate=td!=null?td:"N/A"; this.provider=prov!=null?prov:"N/A"; }
        public String getUsername() { return username; } public String getStartCity() { return startCity; } public String getDestCity() { return destCity; } public double getPrice() { return price; } public String getSeatClass() { return seatClass; } public Seat getSeat() { return seat; } public String getTravelDate() { return travelDate; } public String getProvider() { return provider; }
    }
}