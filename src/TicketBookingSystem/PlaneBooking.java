package TicketBookingSystem;

import java.util.Scanner;

public class PlaneBooking {
    private final boolean[][] economySeats = new boolean[10][9];
    private final boolean[][] businessSeats = new boolean[8][4];
    private final boolean[][] firstClassSeats = new boolean[5][2];

    public void book(Scanner sc) {
        System.out.print("Enter source: ");
        String source = sc.nextLine();
        System.out.print("Enter destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = sc.nextLine();
        System.out.print("Enter return date (YYYY-MM-DD or NA): ");
        String returnDate = sc.nextLine();
        System.out.print("Enter number of passengers: ");
        int passengers = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter class (Economy/Business/First): ");
        String seatClass = sc.nextLine();

        boolean[][] seats;
        switch (seatClass.toLowerCase()) {
            case "economy" -> seats = economySeats;
            case "business" -> seats = businessSeats;
            case "first" -> seats = firstClassSeats;
            default -> {
                System.out.println("Invalid class.");
                return;
            }
        }

        SeatMap.printPlane(seatClass, seats);
        for (int i = 0; i < passengers; i++) {
            System.out.println("Example: To book Row 2 Seat C, enter: 2 C");
            System.out.print("Enter seat (row and letter): ");
            int row = sc.nextInt() - 1;
            char col = sc.next().toUpperCase().charAt(0);
            int colIndex = col - 'A';
            if (row >= 0 && row < seats.length && colIndex >= 0 && colIndex < seats[0].length && !seats[row][colIndex]) {
                seats[row][colIndex] = true;
                System.out.println("Seat booked: Row " + (row + 1) + " Seat " + col);
            } else {
                System.out.println("Invalid or already booked seat.");
            }
        }
        System.out.println("Booking complete. Total cost: $" + passengers * 150);
    }
}