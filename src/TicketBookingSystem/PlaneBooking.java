package TicketBookingSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PlaneBooking {
    private final CustomLinkedList<Seat> seats = new CustomLinkedList<>();
    private final CustomLinkedList<Passenger> passengers = new CustomLinkedList<>();
    private String startCity;
    private String destCity;
    private double routePrice;
    private Map<String, Booking> bookings = new HashMap<>();
    private final String flightId;
    private final BookingSystem bookingSystem;

    public PlaneBooking(String flightId, BookingSystem bookingSystem) {
        this.flightId = flightId;
        this.bookingSystem = bookingSystem;
    }

    private void initializeSeats(String seatClass, double basePrice) {
        seats.clear();
        switch (seatClass) {
            case "Economy":
                generateLayout(10, new char[]{'A','B','C','D','E','F','G','H','I'}, basePrice * 1.0, seatClass);
                break;
            case "Business":
                generateLayout(8, new char[]{'A','B','C','D'}, basePrice * 2.0, seatClass);
                break;
            case "First":
                generateLayout(5, new char[]{'A','B'}, basePrice * 3.0, seatClass);
                break;
            default:
                System.out.println("\033[1;31mInvalid class selected.\033[0m");
        }
    }

    private void generateLayout(int rows, char[] columns, double price, String seatClass) {
        for (int i = 1; i <= rows; i++) {
            for (char c : columns) {
                seats.add(new Seat(i, String.valueOf(c), seatClass, "Plane", price));
            }
        }
    }

    public void displaySeats() {
        if (seats.getHead() == null) return;

        String seatClass = seats.getHead().data.getSeatClass();
        System.out.println("\n\033[1;36mPlane Seat Layout - " + seatClass + " (Flight ID: " + flightId + ")\033[0m");

        int[] groupSizes;
        if (seatClass.equals("Economy")) {
            groupSizes = new int[]{3, 3, 3};
        } else if (seatClass.equals("Business")) {
            groupSizes = new int[]{1, 2, 1};
        } else {
            groupSizes = new int[]{1, 1};
        }

        CustomLinkedList.Node<Seat> temp = seats.getHead();
        int currentRow = -1;
        StringBuilder rowDisplay = new StringBuilder();

        while (temp != null) {
            Seat seat = temp.data;
            if (seat.getRow() != currentRow) {
                if (currentRow != -1) System.out.println(rowDisplay);
                rowDisplay = new StringBuilder("\033[1mRow " + seat.getRow() + ":\033[0m ");
                currentRow = seat.getRow();
            }

            rowDisplay.append(seat.toString()).append(" ");
            int colIndex = seat.getColumn().charAt(0) - 'A';
            if (seatClass.equals("Economy") && (colIndex == 2 || colIndex == 5)) {
                rowDisplay.append("  ");
            } else if (seatClass.equals("Business") && (colIndex == 0 || colIndex == 2)) {
                rowDisplay.append("  ");
            } else if (seatClass.equals("First") && colIndex == 0) {
                rowDisplay.append("  ");
            }

            temp = temp.next;
        }
        if (!rowDisplay.isEmpty()) System.out.println(rowDisplay);

        System.out.println("\n\033[1mSeat Legend:\033[0m");
        System.out.println("\033[32mO\033[0m - Available");
        System.out.println("\033[31mX\033[0m - Reserved");
    }

    public void book(Scanner sc, String username) {
        bookSeat(sc, username);
        displaySeats();
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

        routePrice = Utils.calculatePrice("Plane", distance);
        System.out.println("\033[1mRoute: \033[0m" + startCity + " to " + destCity + " (\033[1;33m" + distance + " km\033[0m)");

        System.out.println("\n\033[1;36mChoose Seat Class:\033[0m");
        System.out.println("\033[1;33m1.\033[0m Economy (Base Price: Rs. " + routePrice + ")");
        System.out.println("\033[1;33m2.\033[0m Business (Base Price: Rs. " + (routePrice * 2) + ")");
        System.out.println("\033[1;33m3.\033[0m First (Base Price: Rs. " + (routePrice * 3) + ")");
        System.out.print("\033[1mEnter choice: \033[0m");
        int classChoice = sc.nextInt();
        sc.nextLine();

        String seatClass;
        switch (classChoice) {
            case 1:
                seatClass = "Economy";
                break;
            case 2:
                seatClass = "Business";
                break;
            case 3:
                seatClass = "First";
                break;
            default:
                System.out.println("\033[1;31mInvalid class selected.\033[0m");
                return;
        }

        initializeSeats(seatClass, routePrice);
        displaySeats();

        System.out.println("\n\033[1;33mExample: To book Row 2 Seat C, enter: 2 C\033[0m");
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

        String bookingId = "P" + bookingSystem.getNextBookingId("P");
        bookings.put(bookingId, new Booking(username, startCity, destCity, selectedSeat.getPrice(), seatClass, selectedSeat));
        System.out.println("\033[1;32mBooking successful! Booking ID: " + bookingId + ", Total payable: Rs. " + selectedSeat.getPrice() + "\033[0m");
    }

    public void displayUserBookings(String username) {
        System.out.println("\n\033[1;36m--- Plane Bookings for " + username + " (Flight ID: " + flightId + ") ---\033[0m");
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

    public String getFlightId() {
        return flightId;
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