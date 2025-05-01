package TicketBookingSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserManager userManager = new UserManager();
        BookingSystem bookingSystem = new BookingSystem();

        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Welcome to Ticket Booking System");
            System.out.println("\033[1;33m1.\033[0m Register");
            System.out.println("\033[1;33m2.\033[0m Login");
            System.out.println("\033[1;33m3.\033[0m Exit");
            System.out.print("\033[1mChoose an option: \033[0m");

            int choice = 0;
            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine();
                Utils.pause(sc);
                continue;
            }

            switch (choice) {
                case 1:
                    registerUser(sc, userManager);
                    break;
                case 2:
                    if (loginUser(sc, userManager, bookingSystem)) {
                        bookingSystem.startBooking(sc);
                    }
                    break;
                case 3:
                    System.out.println("\033[1;32mThank you for using the Ticket Booking System. Goodbye!\033[0m");
                    sc.close();
                    return;
                default:
                    System.out.println("\033[1;31mInvalid option. Please try again.\033[0m");
                    Utils.pause(sc);
            }
        }
    }

    private static void registerUser(Scanner sc, UserManager userManager) {
        Utils.printBanner("Register");
        System.out.print("\033[1mEnter username: \033[0m");
        String username = sc.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("\033[1;31mUsername cannot be empty.\033[0m");
            Utils.pause(sc);
            return;
        }
        System.out.print("\033[1mEnter password: \033[0m");
        String password = sc.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println("\033[1;31mPassword cannot be empty.\033[0m");
            Utils.pause(sc);
            return;
        }

        if (userManager.register(username, password)) {
            System.out.println("\033[1;32mRegistration successful! You can now log in.\033[0m");
        } else {
            System.out.println("\033[1;31mUsername already exists. Try a different one.\033[0m");
        }
        Utils.pause(sc);
    }

    private static boolean loginUser(Scanner sc, UserManager userManager, BookingSystem bookingSystem) {
        Utils.printBanner("Login");
        System.out.print("\033[1mEnter username: \033[0m");
        String username = sc.nextLine().trim();
        System.out.print("\033[1mEnter password: \033[0m");
        String password = sc.nextLine().trim();

        if (userManager.login(username, password)) {
            System.out.println("\033[1;32mLogin successful!\033[0m");
            bookingSystem.setLoggedInUser(username);
            return true;
        } else {
            System.out.println("\033[1;31mInvalid username or password.\033[0m");
            Utils.pause(sc);
            return false;
        }
    }
}