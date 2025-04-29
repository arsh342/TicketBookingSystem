package TicketBookingSystem;

import java.util.Scanner;

public class Train {
    private boolean[][] seats;

    public Train() {
        seats = new boolean[6][5]; // 6 rows, 5 seats (3-2 layout)
    }

    public void book(Scanner sc) {
        System.out.print("Enter source: ");
        String source = sc.nextLine();
        System.out.print("Enter destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = sc.nextLine();
        System.out.print("Enter class (General/Sleeper): ");
        String cls = sc.nextLine();

        SeatMap.printTrain(seats);

        System.out.print("Example: To book Row 3 Seat 2, enter: 3 2\n");
        System.out.print("Enter number of passengers: ");
        int passengers = sc.nextInt();

        int price = cls.equalsIgnoreCase("Sleeper") ? 100 : 50;

        for (int i = 0; i < passengers; i++) {
            System.out.print("Enter seat (row seat): ");
            int row = sc.nextInt() - 1;
            int seat = sc.nextInt() - 1;

            if (row >= 0 && row < seats.length && seat >= 0 && seat < seats[0].length) {
                if (!seats[row][seat]) {
                    seats[row][seat] = true;
                    System.out.println("Seat booked successfully.");
                } else {
                    System.out.println("Seat already booked.");
                }
            } else {
                System.out.println("Invalid seat.");
            }
        }

        System.out.println("Total Price: $" + (price * passengers));
    }
}
