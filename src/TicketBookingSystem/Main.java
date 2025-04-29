package TicketBookingSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserService userService = new UserService();
        System.out.println("=========== Welcome to the Ticket Booking System ===========");

        while (true) {
            System.out.println("\n1. Register\n2. Login\n3. Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> userService.register(sc);
                case 2 -> {
                    if (userService.login(sc)) {
                        BookingSystem.start(sc);
                    }
                }
                case 3 -> {
                    System.out.println("Thank you for using the system.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
}