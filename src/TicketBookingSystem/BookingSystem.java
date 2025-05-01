package TicketBookingSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
        while (true) {
            Utils.printBanner("Select Mode of Transportation");
            System.out.println("\033[1;33m0.\033[0m Back");
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
                continue;
            }

            if (transportChoice == 0) return;

            switch (transportChoice) {
                case 1:
                    selectAndBookPlane(sc);
                    return;
                case 2:
                    selectAndBookTrain(sc);
                    return;
                case 3:
                    selectAndBookBus(sc);
                    return;
                default:
                    System.out.println("\033[1;31mInvalid option.\033[0m");
            }
            Utils.pause(sc);
        }
    }

    private void selectAndBookPlane(Scanner sc) {
        while (true) {
            // Select route with free text input
            String[] route = selectRoute(sc);
            if (route == null) return; // User chose to go back

            String startCity = route[0];
            String destCity = route[1];
            int distance = Utils.getDistance(startCity, destCity);
            if (distance == -1) {
                System.out.println("\033[1;31mRoute between " + startCity + " and " + destCity + " not available.\033[0m");
                Utils.pause(sc);
                continue;
            }
            double routePrice = Utils.calculatePrice("Plane", distance);
            System.out.println("\033[1mRoute: \033[0m" + startCity + " to " + destCity + " (\033[1;33m" + distance + " km\033[0m)");
            String eta = calculateETA(distance, "Plane");
            System.out.println("\033[1mEstimated Time of Arrival: \033[32m" + eta + "\033[0m");

            // Select class
            System.out.println("\n\033[1;36mChoose Seat Class:\033[0m");
            System.out.println("\033[1;33m0.\033[0m Back");
            System.out.println("\033[1;33m1.\033[0m Economy (Base Price: Rs. " + routePrice + ")");
            System.out.println("\033[1;33m2.\033[0m Business (Base Price: Rs. " + (routePrice * 2) + ")");
            System.out.println("\033[1;33m3.\033[0m First (Base Price: Rs. " + (routePrice * 3) + ")");
            System.out.print("\033[1mEnter choice: \033[0m");
            int classChoice = sc.nextInt();
            sc.nextLine();

            if (classChoice == 0) return;

            String seatClass;
            switch (classChoice) {
                case 1:
                    seatClass = "Economy";
                    break;
                case 2:
                    seatClass = "Business";
                    break;
                case 3:
                    seatClass = "First";
                    break;
                default:
                    System.out.println("\033[1;31mInvalid class selected.\033[0m");
                    continue;
            }

            // Filter available planes for this route (for simplicity, assume all planes can serve all routes)
            List<PlaneBooking> availablePlanes = planes.stream()
                    .filter(plane -> true) // In a real system, filter based on route availability
                    .collect(Collectors.toList());

            if (availablePlanes.isEmpty()) {
                System.out.println("\033[1;31mNo planes available for this route and class.\033[0m");
                Utils.pause(sc);
                continue;
            }

            // Show number of available planes
            System.out.println("\033[1;36mNumber of Available Planes: " + availablePlanes.size() + "\033[0m");

            Utils.printBanner("Select Plane");
            System.out.println("\033[1;36mAvailable Flights:\033[0m");
            System.out.println("\033[1;33m0.\033[0m Back");
            for (int i = 0; i < availablePlanes.size(); i++) {
                System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Flight ID: " + availablePlanes.get(i).getFlightId());
            }
            System.out.print("\033[1mEnter flight number to book: \033[0m");
            int flightChoice = 0;
            try {
                flightChoice = sc.nextInt();
                sc.nextLine();
                if (flightChoice == 0) continue;
                if (flightChoice < 1 || flightChoice > availablePlanes.size()) {
                    System.out.println("\033[1;31mInvalid flight selection.\033[0m");
                    continue;
                }
                availablePlanes.get(flightChoice - 1).book(sc, loggedInUser, startCity, destCity, routePrice, seatClass);
                return;
            } catch (Exception e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine();
            }
            Utils.pause(sc);
        }
    }

    private void selectAndBookTrain(Scanner sc) {
        while (true) {
            // Select route with free text input
            String[] route = selectRoute(sc);
            if (route == null) return;

            String startCity = route[0];
            String destCity = route[1];
            int distance = Utils.getDistance(startCity, destCity);
            if (distance == -1) {
                System.out.println("\033[1;31mRoute between " + startCity + " and " + destCity + " not available.\033[0m");
                Utils.pause(sc);
                continue;
            }
            double routePrice = Utils.calculatePrice("Train", distance);
            System.out.println("\033[1mRoute: \033[0m" + startCity + " to " + destCity + " (\033[1;33m" + distance + " km\033[0m)");
            String eta = calculateETA(distance, "Train");
            System.out.println("\033[1mEstimated Time of Arrival: \033[32m" + eta + "\033[0m");

            // Select class
            System.out.println("\n\033[1;36mChoose Class:\033[0m");
            System.out.println("\033[1;33m0.\033[0m Back");
            System.out.println("\033[1;33m1.\033[0m AC First Class (1A) - Rs. " + (routePrice * 2.5));
            System.out.println("\033[1;33m2.\033[0m Second AC (2A) - Rs. " + (routePrice * 2.0));
            System.out.println("\033[1;33m3.\033[0m Third AC (3A) - Rs. " + (routePrice * 1.5));
            System.out.println("\033[1;33m4.\033[0m Sleeper Class (SL) - Rs. " + routePrice);
            System.out.println("\033[1;33m5.\033[0m Chair Car (CC) - Rs. " + (routePrice * 0.8));
            System.out.println("\033[1;33m6.\033[0m Second Seater (2S) - Rs. " + (routePrice * 0.5));
            System.out.print("\033[1mEnter choice: \033[0m");
            int classChoice = sc.nextInt();
            sc.nextLine();

            if (classChoice == 0) return;

            String seatClass;
            switch (classChoice) {
                case 1:
                    seatClass = "AC First Class (1A)";
                    break;
                case 2:
                    seatClass = "Second AC (2A)";
                    break;
                case 3:
                    seatClass = "Third AC (3A)";
                    break;
                case 4:
                    seatClass = "Sleeper Class (SL)";
                    break;
                case 5:
                    seatClass = "Chair Car (CC)";
                    break;
                case 6:
                    seatClass = "Second Seater (2S)";
                    break;
                default:
                    System.out.println("\033[1;31mInvalid class selected.\033[0m");
                    continue;
            }

            // Filter available trains for this route (for simplicity, assume all trains can serve all routes)
            List<TrainBooking> availableTrains = trains.stream()
                    .filter(train -> true)
                    .collect(Collectors.toList());

            if (availableTrains.isEmpty()) {
                System.out.println("\033[1;31mNo trains available for this route and class.\033[0m");
                Utils.pause(sc);
                continue;
            }

            // Show number of available trains
            System.out.println("\033[1;36mNumber of Available Trains: " + availableTrains.size() + "\033[0m");

            Utils.printBanner("Select Train");
            System.out.println("\033[1;36mAvailable Trains:\033[0m");
            System.out.println("\033[1;33m0.\033[0m Back");
            for (int i = 0; i < availableTrains.size(); i++) {
                System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Train ID: " + availableTrains.get(i).getTrainId());
            }
            System.out.print("\033[1mEnter train number to book: \033[0m");
            int trainChoice = 0;
            try {
                trainChoice = sc.nextInt();
                sc.nextLine();
                if (trainChoice == 0) continue;
                if (trainChoice < 1 || trainChoice > availableTrains.size()) {
                    System.out.println("\033[1;31mInvalid train selection.\033[0m");
                    continue;
                }
                availableTrains.get(trainChoice - 1).book(sc, loggedInUser, startCity, destCity, routePrice, seatClass);
                return;
            } catch (Exception e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine();
            }
            Utils.pause(sc);
        }
    }

    private void selectAndBookBus(Scanner sc) {
        while (true) {
            // Select route with free text input
            String[] route = selectRoute(sc);
            if (route == null) return;

            String startCity = route[0];
            String destCity = route[1];
            int distance = Utils.getDistance(startCity, destCity);
            if (distance == -1) {
                System.out.println("\033[1;31mRoute between " + startCity + " and " + destCity + " not available.\033[0m");
                Utils.pause(sc);
                continue;
            }
            double routePrice = Utils.calculatePrice("Bus", distance);
            System.out.println("\033[1mRoute: \033[0m" + startCity + " to " + destCity + " (\033[1;33m" + distance + " km\033[0m)");
            String eta = calculateETA(distance, "Bus");
            System.out.println("\033[1mEstimated Time of Arrival: \033[32m" + eta + "\033[0m");

            // Select class (only Standard for bus)
            System.out.println("\n\033[1;36mChoose Class:\033[0m");
            System.out.println("\033[1;33m0.\033[0m Back");
            System.out.println("\033[1;33m1.\033[0m Standard (Base Price: Rs. " + routePrice + ")");
            System.out.print("\033[1mEnter choice: \033[0m");
            int classChoice = sc.nextInt();
            sc.nextLine();

            if (classChoice == 0) return;
            if (classChoice != 1) {
                System.out.println("\033[1;31mInvalid class selected.\033[0m");
                continue;
            }

            String seatClass = "Standard";

            // Filter available buses for this route (for simplicity, assume all buses can serve all routes)
            List<BusBooking> availableBuses = buses.stream()
                    .filter(bus -> true)
                    .collect(Collectors.toList());

            if (availableBuses.isEmpty()) {
                System.out.println("\033[1;31mNo buses available for this route and class.\033[0m");
                Utils.pause(sc);
                continue;
            }

            // Show number of available buses
            System.out.println("\033[1;36mNumber of Available Buses: " + availableBuses.size() + "\033[0m");

            Utils.printBanner("Select Bus");
            System.out.println("\033[1;36mAvailable Buses:\033[0m");
            System.out.println("\033[1;33m0.\033[0m Back");
            for (int i = 0; i < availableBuses.size(); i++) {
                System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Bus ID: " + availableBuses.get(i).getBusId());
            }
            System.out.print("\033[1mEnter bus number to book: \033[0m");
            int busChoice = 0;
            try {
                busChoice = sc.nextInt();
                sc.nextLine();
                if (busChoice == 0) continue;
                if (busChoice < 1 || busChoice > availableBuses.size()) {
                    System.out.println("\033[1;31mInvalid bus selection.\033[0m");
                    continue;
                }
                availableBuses.get(busChoice - 1).book(sc, loggedInUser, startCity, destCity, routePrice, seatClass);
                return;
            } catch (Exception e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine();
            }
            Utils.pause(sc);
        }
    }

    private String[] selectRoute(Scanner sc) {
        System.out.println("\033[1;36mEnter Source and Destination Cities\033[0m");
        System.out.print("\033[1mEnter starting city: \033[0m");
        String startCity = sc.nextLine().trim();
        if (startCity.isEmpty()) {
            System.out.println("\033[1;31mStarting city cannot be empty.\033[0m");
            return null;
        }

        System.out.print("\033[1mEnter destination city: \033[0m");
        String destCity = sc.nextLine().trim();
        if (destCity.isEmpty()) {
            System.out.println("\033[1;31mDestination city cannot be empty.\033[0m");
            return null;
        }

        if (startCity.equalsIgnoreCase(destCity)) {
            System.out.println("\033[1;31mStarting and destination cities cannot be the same.\033[0m");
            return null;
        }

        return new String[]{startCity, destCity};
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
        boolean canceled = false;
        if (bookingId.startsWith("P")) {
            for (PlaneBooking plane : planes) {
                if (plane.getBookings().containsKey(bookingId)) {
                    plane.cancelBooking(bookingId, loggedInUser);
                    canceled = true;
                    break;
                }
            }
        } else if (bookingId.startsWith("T")) {
            for (TrainBooking train : trains) {
                if (train.getBookings().containsKey(bookingId)) {
                    train.cancelBooking(bookingId, loggedInUser);
                    canceled = true;
                    break;
                }
            }
        } else if (bookingId.startsWith("B")) {
            for (BusBooking bus : buses) {
                if (bus.getBookings().containsKey(bookingId)) {
                    bus.cancelBooking(bookingId, loggedInUser);
                    canceled = true;
                    break;
                }
            }
        } else {
            System.out.println("\033[1;31mInvalid Booking ID format.\033[0m");
            return;
        }
        if (!canceled) {
            System.out.println("\033[1;31mBooking ID not found or unauthorized.\033[0m");
        }
    }

    public static String calculateETA(int distance, String transportType) {
        double speed; // km/h
        switch (transportType) {
            case "Plane":
                speed = 800.0;
                break;
            case "Train":
                speed = 100.0;
                break;
            case "Bus":
                speed = 60.0;
                break;
            default:
                return "Unknown";
        }
        double hours = distance / speed;
        int wholeHours = (int) hours;
        int minutes = (int) ((hours - wholeHours) * 60);
        return wholeHours + "h " + minutes + "m";
    }
}