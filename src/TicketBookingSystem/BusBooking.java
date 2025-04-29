package TicketBookingSystem;

import java.util.Scanner;

public class BusBooking {
    private final boolean[][] busSeats = new boolean[10][4];

    public void book(Scanner sc) {
        System.out.print("Enter source: ");
        String source = sc.nextLine();
        System.out.print("Enter destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = sc.nextLine();

        SeatMap.printBus(busSeats);
        System.out.println("Example: To book Seat 10, enter: 10");
        System.out.print("Enter seat number: ");
        int seatNumber = sc.nextInt();

        int row = (seatNumber - 1) / 4;
        int col = (seatNumber - 1) % 4;

        if (row >= 0 && row < busSeats.length && col >= 0 && col < 4 && !busSeats[row][col]) {
            busSeats[row][col] = true;
            System.out.println("Seat booked: " + seatNumber);
            System.out.println("Booking complete. Total cost: $50");
        } else {
            System.out.println("Invalid or already booked seat.");
        }
    }
}