package TicketBookingSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BookingSystem {
    private List<PlaneBooking> planes;
    private List<TrainBooking> trains;
    private List<BusBooking> buses;
    private String loggedInUser;
    private int bookingIdCounter = 1;

    public BookingSystem() {
        planes = new ArrayList<>();
        trains = new ArrayList<>();
        buses = new ArrayList<>();

        // Initialize multiple planes, trains, and buses with unique IDs
        planes.add(new PlaneBooking("FL001", this));
        planes.add(new PlaneBooking("FL002", this));
        trains.add(new TrainBooking("TR001", this));
        trains.add(new TrainBooking("TR002", this));
        buses.add(new BusBooking("BS001", this));
        buses.add(new BusBooking("BS002", this));

        StorageManager.loadBookings(planes, trains, buses);
    }

    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
    }

    public int getNextBookingId(String prefix) {
        return bookingIdCounter++;
    }

    public void startBooking(Scanner sc) {
        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Booking Menu");
            System.out.println("\033[1;33m1.\033[0m Book a Ticket");
            System.out.println("\033[1;33m2.\033[0m View My Bookings");
            System.out.println("\033[1;33m3.\033[0m Cancel a Booking");
            System.out.println("\033[1;33m4.\033[0m Logout");
            System.out.print("\033[1mChoose an option: \033[0m");
            int choice = 0;
            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    bookTicket(sc);
                    StorageManager.saveBookings(planes, trains, buses);
                    break;
                case 2:
                    viewBookings(sc);
                    break;
                case 3:
                    cancelBooking(sc);
                    StorageManager.saveBookings(planes, trains, buses);
                    break;
                case 4:
                    System.out.println("\033[1;32mLogging out...\033[0m");
                    return;
                default:
                    System.out.println("\033[1;31mInvalid option. Try again.\033[0m");
            }
            Utils.pause(sc);
        }
    }

    private void bookTicket(Scanner sc) {
        Utils.printBanner("Select Mode of Transportation");
        System.out.println("\033[1;33m1.\033[0m Plane");
        System.out.println("\033[1;33m2.\033[0m Train");
        System.out.println("\033[1;33m3.\033[0m Bus");
        System.out.print("\033[1mEnter choice: \033[0m");
        int transportChoice = 0;
        try {
            transportChoice = sc.nextInt();
            sc.nextLine();
        } catch (Exception e) {
            System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
            sc.nextLine();
            return;
        }

        switch (transportChoice) {
            case 1:
                selectAndBookPlane(sc);
                break;
            case 2:
                selectAndBookTrain(sc);
                break;
            case 3:
                selectAndBookBus(sc);
                break;
            default:
                System.out.println("\033[1;31mInvalid option.\033[0m");
        }
    }

    private void selectAndBookPlane(Scanner sc) {
        Utils.printBanner("Select Plane");
        System.out.println("\033[1;36mAvailable Flights:\033[0m");
        for (int i = 0; i < planes.size(); i++) {
            System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Flight ID: " + planes.get(i).getFlightId());
        }
        System.out.print("\033[1mEnter flight number to book: \033[0m");
        int flightChoice = 0;
        try {
            flightChoice = sc.nextInt();
            sc.nextLine();
            if (flightChoice < 1 || flightChoice > planes.size()) {
                System.out.println("\033[1;31mInvalid flight selection.\033[0m");
                return;
            }
            planes.get(flightChoice - 1).book(sc, loggedInUser);
        } catch (Exception e) {
            System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
            sc.nextLine();
        }
    }

    private void selectAndBookTrain(Scanner sc) {
        Utils.printBanner("Select Train");
        System.out.println("\033[1;36mAvailable Trains:\033[0m");
        for (int i = 0; i < trains.size(); i++) {
            System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Train ID: " + trains.get(i).getTrainId());
        }
        System.out.print("\033[1mEnter train number to book: \033[0m");
        int trainChoice = 0;
        try {
            trainChoice = sc.nextInt();
            sc.nextLine();
            if (trainChoice < 1 || trainChoice > trains.size()) {
                System.out.println("\033[1;31mInvalid train selection.\033[0m");
                return;
            }
            trains.get(trainChoice - 1).book(sc, loggedInUser);
        } catch (Exception e) {
            System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
            sc.nextLine();
        }
    }

    private void selectAndBookBus(Scanner sc) {
        Utils.printBanner("Select Bus");
        System.out.println("\033[1;36mAvailable Buses:\033[0m");
        for (int i = 0; i < buses.size(); i++) {
            System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Bus ID: " + buses.get(i).getBusId());
        }
        System.out.print("\033[1mEnter bus number to book: \033[0m");
        int busChoice = 0;
        try {
            busChoice = sc.nextInt();
            sc.nextLine();
            if (busChoice < 1 || busChoice > buses.size()) {
                System.out.println("\033[1;31mInvalid bus selection.\033[0m");
                return;
            }
            buses.get(busChoice - 1).book(sc, loggedInUser);
        } catch (Exception e) {
            System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
            sc.nextLine();
        }
    }

    private void viewBookings(Scanner sc) {
        Utils.printBanner("Your Bookings");
        for (PlaneBooking plane : planes) {
            plane.displayUserBookings(loggedInUser);
        }
        for (TrainBooking train : trains) {
            train.displayUserBookings(loggedInUser);
        }
        for (BusBooking bus : buses) {
            bus.displayUserBookings(loggedInUser);
        }
        Utils.pause(sc);
    }

    private void cancelBooking(Scanner sc) {
        Utils.printBanner("Cancel Booking");
        System.out.print("\033[1mEnter Booking ID to cancel (e.g., P1, T1, B1): \033[0m");
        String bookingId = sc.nextLine();
        StorageManager.removeBooking(bookingId, planes, trains, buses);
        if (bookingId.startsWith("P")) {
            for (PlaneBooking plane : planes) {
                plane.cancelBooking(bookingId, loggedInUser);
            }
        } else if (bookingId.startsWith("T")) {
            for (TrainBooking train : trains) {
                train.cancelBooking(bookingId, loggedInUser);
            }
        } else if (bookingId.startsWith("B")) {
            for (BusBooking bus : buses) {
                bus.cancelBooking(bookingId, loggedInUser);
            }
        } else {
            System.out.println("\033[1;31mInvalid Booking ID format.\033[0m");
        }
    }
}