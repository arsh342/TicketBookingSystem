package TicketBookingSystem;

import java.util.Scanner;

public class BookingSystem {
    private Plane plane;
    private Train train;
    private Bus bus;

    public BookingSystem() {
        plane = new Plane();
        train = new Train();
        bus = new Bus();
    }

    public void startBooking(Scanner sc) {
        System.out.println("\nSelect Mode of Transportation:");
        System.out.println("1. Plane\n2. Train\n3. Bus");
        System.out.print("Enter choice: ");
        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                plane.book(sc);
                break;
            case 2:
                train.book(sc);
                break;
            case 3:
                bus.book(sc);
                break;
            default:
                System.out.println("Invalid option.");
        }
    }
}
