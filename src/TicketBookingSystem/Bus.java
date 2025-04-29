package TicketBookingSystem;

import java.util.Scanner;

public class Bus {
    private boolean[] seats;

    public Bus() {
        seats = new boolean[40]; // 10 rows, 4 seats (2-2 layout)
    }

    public void book(Scanner sc) {
        System.out.print("Enter source: ");
        String source = sc.nextLine();
        System.out.print("Enter destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = sc.nextLine();

        SeatMap.printBus(seats);

        System.out.print("Example: To book Seat 10, enter: 10\n");
        System.out.print("Enter number of passengers: ");
        int passengers = sc.nextInt();

        for (int i = 0; i < passengers; i++) {
            System.out.print("Enter seat number (1-40): ");
            int seatNo = sc.nextInt();
            if (seatNo >= 1 && seatNo <= 40) {
                if (!seats[seatNo - 1]) {
                    seats[seatNo - 1] = true;
                    System.out.println("Seat booked successfully.");
                } else {
                    System.out.println("Seat already booked.");
                }
            } else {
                System.out.println("Invalid seat number.");
            }
        }

        int price = 30;
        System.out.println("Total Price: $" + (price * passengers));
    }
}
