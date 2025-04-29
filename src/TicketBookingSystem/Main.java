package TicketBookingSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserManager userManager = new UserManager();
        BookingSystem bookingSystem = new BookingSystem();

        System.out.println("Welcome to the Online Ticket Booking System!");

        while (true) {
            System.out.println("\n1. Register\n2. Login\n3. Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    userManager.register(sc);
                    break;
                case 2:
                    if (userManager.login(sc)) {
                        bookingSystem.startBooking(sc);
                    }
                    break;
                case 3:
                    System.out.println("Thank you for using the booking system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
