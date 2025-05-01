package TicketBookingSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BusBooking {
    private static final int ROWS = 10;
    private final CustomLinkedList<Seat> seats = new CustomLinkedList<>();
    private final CustomLinkedList<Passenger> passengers = new CustomLinkedList<>();
    private String startCity;
    private String destCity;
    private double routePrice;
    private Map<String, Booking> bookings = new HashMap<>();
    private final String busId;
    private final BookingSystem bookingSystem;

    public BusBooking(String busId, BookingSystem bookingSystem) {
        this.busId = busId;
        this.bookingSystem = bookingSystem;
    }

    private void initializeSeats(double basePrice) {
        seats.clear();
        char[] columns = {'A', 'B', 'C', 'D'};
        for (int i = 1; i <= ROWS; i++) {
            for (char c : columns) {
                seats.add(new Seat(i, String.valueOf(c), "Standard", "Bus", basePrice));
            }
        }
    }

    public void displaySeats() {
        System.out.println("\n\033[1;36mBus Seating Layout (2-2 Configuration) (Bus ID: " + busId + ")\033[0m");
        int currentRow = -1;
        StringBuilder rowDisplay = new StringBuilder();

        CustomLinkedList.Node<Seat> temp = seats.getHead();
        while (temp != null) {
            Seat seat = temp.data;
            if (seat.getRow() != currentRow) {
                if (currentRow != -1) System.out.println(rowDisplay);
                rowDisplay = new StringBuilder("\033[1mRow " + seat.getRow() + ":\033[0m ");
                currentRow = seat.getRow();
            }
            rowDisplay.append(seat.toString()).append(" ");
            if (seat.getColumn().equals("B")) rowDisplay.append("  ");
            temp = temp.next;
        }
        if (!rowDisplay.isEmpty()) System.out.println(rowDisplay);

        System.out.println("\n\033[1mSeat Legend:\033[0m");
        System.out.println("\033[32mO\033[0m - Available");
        System.out.println("\033[31mX\033[0m - Reserved");
    }

    public void book(Scanner sc, String username) {
        bookSeat(sc, username);
    }

    private void bookSeat(Scanner sc, String username) {
        Utils.displayCities();
        System.out.print("\033[1mEnter starting city number: \033[0m");
        int startChoice = sc.nextInt();
        sc.nextLine();
        System.out.print("\033[1mEnter destination city number: \033[0m");
        int destChoice = sc.nextInt();
        sc.nextLine();

        if (startChoice < 1 || startChoice > Utils.CITIES.length || destChoice < 1 || destChoice > Utils.CITIES.length) {
            System.out.println("\033[1;31mInvalid city selection.\033[0m");
            return;
        }

        startCity = Utils.CITIES[startChoice - 1];
        destCity = Utils.CITIES[destChoice - 1];
        if (startCity.equals(destCity)) {
            System.out.println("\033[1;31mStarting and destination cities cannot be the same.\033[0m");
            return;
        }

        int distance = Utils.getDistance(startCity, destCity);
        if (distance == -1) {
            System.out.println("\033[1;31mRoute not available.\033[0m");
            return;
        }

        routePrice = Utils.calculatePrice("Bus", distance);
        System.out.println("\033[1mRoute: \033[0m" + startCity + " to " + destCity + " (\033[1;33m" + distance + " km\033[0m)");
        System.out.println("\033[1mBase Price: Rs. \033[32m" + routePrice + "\033[0m");

        initializeSeats(routePrice);
        displaySeats();

        System.out.println("\n\033[1;33mExample: To book Seat Row 5 Column B, enter: 5 B\033[0m");
        System.out.print("\033[1mEnter row number and seat column: \033[0m");
        int row = sc.nextInt();
        String col = sc.next().toUpperCase();
        sc.nextLine();

        Seat selectedSeat = findSeat(row, col);

        if (selectedSeat == null) {
            System.out.println("\033[1;31mSeat not found.\033[0m");
            return;
        }
        if (selectedSeat.isReserved()) {
            System.out.println("\033[1;31mSeat already reserved.\033[0m");
            return;
        }

        System.out.println("\033[1mSeat Price: Rs. \033[32m" + selectedSeat.getPrice() + "\033[0m");
        System.out.print("\033[1mEnter passenger name: \033[0m");
        String name = sc.nextLine();
        System.out.print("\033[1mEnter age: \033[0m");
        int age = sc.nextInt();
        sc.nextLine();
        System.out.print("\033[1mEnter gender: \033[0m");
        String gender = sc.nextLine();

        Passenger passenger = new Passenger(name, age, gender, selectedSeat);
        passengers.add(passenger);
        selectedSeat.reserve();

        String bookingId = "B" + bookingSystem.getNextBookingId("B");
        bookings.put(bookingId, new Booking(username, startCity, destCity, selectedSeat.getPrice(), "Standard", selectedSeat));
        System.out.println("\033[1;32mBooking successful! Booking ID: " + bookingId + ", Total payable: Rs. " + selectedSeat.getPrice() + "\033[0m");
    }

    public void displayUserBookings(String username) {
        System.out.println("\n\033[1;36m--- Bus Bookings for " + username + " (Bus ID: " + busId + ") ---\033[0m");
        boolean hasBookings = false;
        for (Map.Entry<String, Booking> entry : bookings.entrySet()) {
            if (entry.getValue().getUsername().equals(username)) {
                hasBookings = true;
                Booking booking = entry.getValue();
                System.out.println("\033[1;34mBooking ID: \033[0m" + entry.getKey() +
                        " \033[1m| Route: \033[0m" + booking.getStartCity() + " to " + booking.getDestCity() +
                        " \033[1m| Class: \033[0m" + booking.getSeatClass() +
                        " \033[1m| Price: Rs. \033[32m" + booking.getPrice() + "\033[0m" +
                        " \033[1m| Seat: \033[0m" + booking.getSeat().toString());
            }
        }
        if (!hasBookings) {
            System.out.println("\033[1;33mNo bookings found.\033[0m");
        }
    }

    public void cancelBooking(String bookingId, String username) {
        if (bookings.containsKey(bookingId) && bookings.get(bookingId).getUsername().equals(username)) {
            Booking booking = bookings.remove(bookingId);
            booking.getSeat().unreserve();
            System.out.println("\033[1;32mBooking " + bookingId + " canceled successfully.\033[0m");
        } else {
            System.out.println("\033[1;31mBooking ID not found or unauthorized.\033[0m");
        }
    }

    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat) {
        bookings.put(bookingId, new Booking(username, startCity, destCity, price, seatClass, seat));
    }

    public Map<String, Booking> getBookings() {
        return bookings;
    }

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

    public String getBusId() {
        return busId;
    }

    private static class Booking {
        private final String username;
        private final String startCity;
        private final String destCity;
        private final double price;
        private final String seatClass;
        private final Seat seat;

        public Booking(String username, String startCity, String destCity, double price, String seatClass, Seat seat) {
            this.username = username;
            this.startCity = startCity;
            this.destCity = destCity;
            this.price = price;
            this.seatClass = seatClass;
            this.seat = seat;
        }

        public String getUsername() { return username; }
        public String getStartCity() { return startCity; }
        public String getDestCity() { return destCity; }
        public double getPrice() { return price; }
        public String getSeatClass() { return seatClass; }
        public Seat getSeat() { return seat; }
    }
}