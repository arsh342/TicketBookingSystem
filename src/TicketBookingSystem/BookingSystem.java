package TicketBookingSystem;

import java.util.Scanner;

public class BookingSystem {
    public static void start(Scanner sc) {
        while (true) {
            System.out.println("\nChoose mode of transportation:");
            System.out.println("1. Plane\n2. Train\n3. Bus\n4. Logout");
            System.out.print("Enter choice: ");
            int option = sc.nextInt();
            sc.nextLine();

            switch (option) {
                case 1 -> new PlaneBooking().book(sc);
                case 2 -> new TrainBooking().book(sc);
                case 3 -> new BusBooking().book(sc);
                case 4 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
}