package TicketBookingSystem;

import java.util.Scanner;

/**
 * Manages Seats for Booking
 */
public class SeatManager {
    private Seat[] seats;

    public SeatManager(int totalSeats) {
        seats = new Seat[totalSeats];
        for (int i = 0; i < totalSeats; i++) {
            seats[i] = new Seat();
        }
    }

    public void displaySeats() {
        System.out.println("\nAvailable Seats (O = Open, X = Reserved):\n");
        for (int i = 0; i < seats.length; i++) {
            if (i % 5 == 0) System.out.print("Row " + ((i / 5) + 1) + ": ");
            System.out.print(seats[i].isReserved() ? "X " : "O ");
            if ((i + 1) % 5 == 0) System.out.println();
        }
    }

    public boolean reserveSeat(int seatNumber) {
        if (seatNumber < 1 || seatNumber > seats.length) {
            System.out.println("❌ Invalid seat number!");
            return false;
        }
        if (seats[seatNumber - 1].isReserved()) {
            System.out.println("❌ Seat already reserved!");
            return false;
        }
        seats[seatNumber - 1].reserve();
        return true;
    }
}
