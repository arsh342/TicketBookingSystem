package TicketBookingSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TrainBooking {
    private final CustomLinkedList<Seat> seats = new CustomLinkedList<>();
    private final CustomLinkedList<Passenger> passengers = new CustomLinkedList<>();
    private String startCity;
    private String destCity;
    private double routePrice;
    private Map<String, Booking> bookings = new HashMap<>();
    private final String trainId;
    private final BookingSystem bookingSystem;

    public TrainBooking(String trainId, BookingSystem bookingSystem) {
        this.trainId = trainId;
        this.bookingSystem = bookingSystem;
    }

    private void initTrainSeats(String seatClass, double basePrice) {
        seats.clear();
        int rows = 6;
        String[] columns = {"A", "B", "C", "D", "E"};

        for (int r = 1; r <= rows; r++) {
            for (String c : columns) {
                double priceMultiplier = switch (seatClass) {
                    case "AC First Class (1A)" -> 2.5;
                    case "Second AC (2A)" -> 2.0;
                    case "Third AC (3A)" -> 1.5;
                    case "Sleeper Class (SL)" -> 1.0;
                    case "Chair Car (CC)" -> 0.8;
                    case "Second Seater (2S)" -> 0.5;
                    default -> 1.0;
                };
                seats.add(new Seat(r, c, seatClass, "Train", basePrice * priceMultiplier));
            }
        }
    }

    public void displaySeats() {
        if (seats.getHead() == null) return;

        System.out.println("\n\033[1;36mTrain Seat Layout (3-2 Configuration) (Train ID: " + trainId + ")\033[0m");
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
            if (seat.getColumn().equals("C")) rowDisplay.append("  ");
            temp = temp.next;
        }
        if (!rowDisplay.isEmpty()) System.out.println(rowDisplay);

        System.out.println("\n\033[1mSeat Legend:\033[0m");
        System.out.println("\033[32mO\033[0m - Available");
        System.out.println("\033[31mX\033[0m - Reserved");
    }

    public void book(Scanner sc, String username, String startCity, String destCity, double routePrice, String seatClass, String travelDate) {
        this.startCity = startCity;
        this.destCity = destCity;
        this.routePrice = routePrice;

        initTrainSeats(seatClass, routePrice);
        System.out.println("\033[1mTicket Price (per seat): Rs. \033[32m" +
                (routePrice * (seatClass.equals("AC First Class (1A)") ? 2.5 :
                        seatClass.equals("Second AC (2A)") ? 2.0 :
                                seatClass.equals("Third AC (3A)") ? 1.5 :
                                        seatClass.equals("Sleeper Class (SL)") ? 1.0 :
                                                seatClass.equals("Chair Car (CC)") ? 0.8 : 0.5)) + "\033[0m");
        displaySeats();

        System.out.println("\n\033[1;33mExample: To book Row 3 Seat B, enter: 3 B\033[0m");
        System.out.print("\033[1mEnter row number and seat column: \033[0m");
        int row = sc.nextInt();
        String col = sc.next().toUpperCase();
        sc.nextLine();

        Seat seat = findSeat(row, col);
        if (seat == null) {
            System.out.println("\033[1;31mSeat not found.\033[0m");
            return;
        }
        if (seat.isReserved()) {
            System.out.println("\033[1;31mSeat already reserved.\033[0m");
            return;
        }

        seat.reserve();
        System.out.println("\033[1;32mSeat " + row + col + " booked.\033[0m");

        System.out.print("\033[1mEnter passenger name: \033[0m");
        String name = sc.nextLine();
        System.out.print("\033[1mEnter age: \033[0m");
        int age = sc.nextInt();
        sc.nextLine();
        System.out.print("\033[1mEnter gender: \033[0m");
        String gender = sc.nextLine();

        Passenger passenger = new Passenger(name, age, gender, seat);
        passengers.add(passenger);

        String bookingId = "T" + bookingSystem.getNextBookingId("T");
        bookings.put(bookingId, new Booking(username, startCity, destCity, seat.getPrice(), seatClass, seat, travelDate));
        System.out.println("\033[1;32mBooking successful! Booking ID: " + bookingId + ", Total payable: Rs. " + seat.getPrice() + "\033[0m");
        displaySeats();
    }

    public void displayUserBookings(String username) {
        System.out.println("\n\033[1;36m--- Train Bookings for " + username + " (Train ID: " + trainId + ") ---\033[0m");
        boolean hasBookings = false;
        for (Map.Entry<String, Booking> entry : bookings.entrySet()) {
            if (entry.getValue().getUsername().equals(username)) {
                hasBookings = true;
                Booking booking = entry.getValue();
                System.out.println("\033[1;34mBooking ID: \033[0m" + entry.getKey() +
                        " \033[1m| Route: \033[0m" + booking.getStartCity() + " to " + booking.getDestCity() +
                        " \033[1m| Class: \033[0m" + booking.getSeatClass() +
                        " \033[1m| Travel Date: \033[0m" + booking.getTravelDate() +
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

    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat, String travelDate) {
        bookings.put(bookingId, new Booking(username, startCity, destCity, price, seatClass, seat, travelDate));
    }

    public void addBooking(String bookingId, String username, String startCity, String destCity, double price, String seatClass, Seat seat) {
        bookings.put(bookingId, new Booking(username, startCity, destCity, price, seatClass, seat, "N/A"));
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

    public String getTrainId() {
        return trainId;
    }

    private static class Booking {
        private final String username;
        private final String startCity;
        private final String destCity;
        private final double price;
        private final String seatClass;
        private final Seat seat;
        private final String travelDate;

        public Booking(String username, String startCity, String destCity, double price, String seatClass, Seat seat, String travelDate) {
            this.username = username;
            this.startCity = startCity;
            this.destCity = destCity;
            this.price = price;
            this.seatClass = seatClass;
            this.seat = seat;
            this.travelDate = travelDate;
        }

        public String getUsername() { return username; }
        public String getStartCity() { return startCity; }
        public String getDestCity() { return destCity; }
        public double getPrice() { return price; }
        public String getSeatClass() { return seatClass; }
        public Seat getSeat() { return seat; }
        public String getTravelDate() { return travelDate; }
    }
}