package TicketBookingSystem;

import java.util.Scanner;

public class TrainBooking {
    private final boolean[][] trainSeats = new boolean[6][5];

    public void book(Scanner sc) {
        System.out.print("Enter source: ");
        String source = sc.nextLine();
        System.out.print("Enter destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = sc.nextLine();
        System.out.print("Enter class (Sleeper/AC): ");
        String seatClass = sc.nextLine();

        SeatMap.printTrain(trainSeats);
        System.out.println("Example: To book Row 3 Seat 2, enter: 3 2");
        System.out.print("Enter seat: ");
        int row = sc.nextInt() - 1;
        int col = sc.nextInt() - 1;

        if (row >= 0 && row < trainSeats.length && col >= 0 && col < trainSeats[0].length && !trainSeats[row][col]) {
            trainSeats[row][col] = true;
            System.out.println("Seat booked: Row " + (row + 1) + " Seat " + (col + 1));
            System.out.println("Booking complete. Total cost: $75");
        } else {
            System.out.println("Invalid or already booked seat.");
        }
    }
}