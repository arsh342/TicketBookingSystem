package TicketBookingSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
// Removed Random import
import java.util.Map;
import java.util.Scanner;
// Removed stream collectors import if no longer needed elsewhere

public class BookingSystem {
    private List<PlaneBooking> planes;
    private List<TrainBooking> trains;
    private List<BusBooking> buses;
    private String loggedInUser;
    private int bookingIdCounter = 1;

    // Add RouteDataManager instance
    private final RouteDataManager routeDataManager;

    public BookingSystem() {
        // Initialize RouteDataManager first
        routeDataManager = new RouteDataManager();

        planes = new ArrayList<>();
        trains = new ArrayList<>();
        buses = new ArrayList<>();

        // Add some default vehicles (These act as placeholders now)
        // Consider giving them more descriptive IDs if possible
        planes.add(new PlaneBooking("FL-A320-1", this)); // Example: Generic Airbus A320 #1
        planes.add(new PlaneBooking("FL-B737-1", this)); // Example: Generic Boeing 737 #1
        trains.add(new TrainBooking("TR-EXP-1", this)); // Example: Generic Express Train #1
        trains.add(new TrainBooking("TR-RAJ-1", this)); // Example: Generic Rajdhani-type Train #1
        buses.add(new BusBooking("BS-VOLVO-1", this)); // Example: Generic Volvo Bus #1
        buses.add(new BusBooking("BS-SLEEPER-1", this)); // Example: Generic Sleeper Bus #1

        // Load existing bookings from bookings.txt
        StorageManager.loadBookings(planes, trains, buses);

        // Update bookingIdCounter based on loaded bookings
        updateBookingIdCounter();
    }

    // Helper method to set the counter after loading bookings
    private void updateBookingIdCounter() {
        int maxId = 0;
        List<Map<String, ?>> allBookingMaps = new ArrayList<>();
        planes.forEach(p -> allBookingMaps.add(p.getBookings()));
        trains.forEach(t -> allBookingMaps.add(t.getBookings()));
        buses.forEach(b -> allBookingMaps.add(b.getBookings()));

        for (Map<String, ?> bookingMap : allBookingMaps) {
            for (String bookingId : bookingMap.keySet()) {
                try {
                    int idNum = Integer.parseInt(bookingId.substring(1));
                    if (idNum > maxId) {
                        maxId = idNum;
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) { /* ignore */ }
            }
        }
        this.bookingIdCounter = maxId + 1;
        System.out.println("\033[0;90mBooking ID counter initialized to: " + this.bookingIdCounter + "\033[0m");
    }


    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
    }

    public int getNextBookingId(String prefix) {
        return bookingIdCounter++;
    }

    // startBooking method (no changes needed here)
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
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine();
                Utils.pause(sc);
                continue;
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred: " + e.getMessage() + "\033[0m");
                sc.nextLine();
                Utils.pause(sc);
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
                    StorageManager.saveBookings(planes, trains, buses); // Save after potential cancellation
                    break;
                case 4:
                    System.out.println("\033[1;32mLogging out...\033[0m");
                    return;
                default:
                    System.out.println("\033[1;31mInvalid option. Try again.\033[0m");
                    Utils.pause(sc);
            }
        }
    }

    // bookTicket method (no changes needed here)
    private void bookTicket(Scanner sc) {
        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Select Mode of Transportation");
            System.out.println("\033[1;33m1.\033[0m Plane");
            System.out.println("\033[1;33m2.\033[0m Train");
            System.out.println("\033[1;33m3.\033[0m Bus");
            System.out.println("\033[1;33m0.\033[0m Back to Main Menu");
            System.out.print("\033[1mEnter choice: \033[0m");
            int transportChoice = 0;
            try {
                transportChoice = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine();
                Utils.pause(sc);
                continue;
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred: " + e.getMessage() + "\033[0m");
                sc.nextLine();
                Utils.pause(sc);
                continue;
            }

            switch (transportChoice) {
                case 1: selectAndBookPlane(sc); return;
                case 2: selectAndBookTrain(sc); return;
                case 3: selectAndBookBus(sc); return;
                case 0: return;
                default:
                    System.out.println("\033[1;31mInvalid option.\033[0m");
                    Utils.pause(sc);
            }
        }
    }


    // selectValidRoute method (no changes needed here)
    private Object[] selectValidRoute(Scanner sc, String transportType) {
        Map<String, LocationInfo> locationData;
        String locationTypeName;

        switch (transportType) {
            case "Plane":
                locationData = routeDataManager.getAirportData();
                locationTypeName = "Airport";
                break;
            case "Train":
                locationData = routeDataManager.getTrainStationData();
                locationTypeName = "Train Station";
                break;
            case "Bus":
                locationData = routeDataManager.getBusStationData();
                locationTypeName = "Bus Station";
                break;
            default: return null; // Should not happen
        }

        if (locationData == null || locationData.isEmpty()) {
            System.out.println("\033[1;31mError: No " + locationTypeName + " data loaded.\033[0m");
            Utils.pause(sc);
            return null;
        }

        List<String> originCities = new ArrayList<>(locationData.keySet());
        Collections.sort(originCities);

        // --- Select Origin City ---
        int originChoice = -1;
        LocationInfo selectedOriginInfo = null;
        while (selectedOriginInfo == null) {
            Utils.clearScreen();
            Utils.printBanner("Select Origin " + locationTypeName);
            System.out.println("\033[1;33m0.\033[0m Back");
            for (int i = 0; i < originCities.size(); i++) {
                String cityName = locationData.get(originCities.get(i)).city();
                String primaryName = locationData.get(originCities.get(i)).primaryName();
                System.out.printf("\033[1;33m%d.\033[0m %s (%s)\n", i + 1, cityName, primaryName);
            }
            System.out.print("\033[1mChoose origin city number: \033[0m");

            try {
                originChoice = sc.nextInt();
                sc.nextLine();
                if (originChoice == 0) return null;
                if (originChoice >= 1 && originChoice <= originCities.size()) {
                    selectedOriginInfo = locationData.get(originCities.get(originChoice - 1));
                    if (selectedOriginInfo.routes() == null || selectedOriginInfo.routes().isEmpty()) {
                        System.out.println("\033[1;31mNo routes available from " + selectedOriginInfo.city() + ". Please choose a different origin.\033[0m");
                        selectedOriginInfo = null;
                        Utils.pause(sc);
                    }
                } else { System.out.println("\033[1;31mInvalid choice.\033[0m"); Utils.pause(sc); }
            } catch (InputMismatchException e) { System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m"); sc.nextLine(); Utils.pause(sc); }
            catch (Exception e) { System.out.println("\033[1;31mAn error occurred: " + e.getMessage() + "\033[0m"); sc.nextLine(); Utils.pause(sc); }
        }

        // --- Select Destination City ---
        Map<String, RouteDetail> availableRoutes = selectedOriginInfo.routes();
        List<String> destinationKeys = new ArrayList<>(availableRoutes.keySet()); // Keys are UPPERCASE city names
        Collections.sort(destinationKeys);

        int destChoice = -1;
        RouteDetail selectedRouteDetail = null;
        String selectedDestCityKey = null; // Store the chosen key

        while (selectedRouteDetail == null) {
            Utils.clearScreen();
            Utils.printBanner("Select Destination from " + selectedOriginInfo.city());
            System.out.println("\033[1;33m0.\033[0m Back to Origin Selection");
            int validRouteCount = 0;
            List<String> displayOrderDestKeys = new ArrayList<>();

            for (String destKey : destinationKeys) { // Iterate using sorted keys
                RouteDetail route = availableRoutes.get(destKey);
                // Check if route is valid for this transport mode (distance > -1 or not 'Not Available' ETA)
                boolean isAvailable = route.distance() > -1 || !route.eta().equalsIgnoreCase("Not Available");

                if (isAvailable) {
                    validRouteCount++;
                    displayOrderDestKeys.add(destKey); // Store the key in display order
                    String destCityName = route.destinationCity(); // Get display name from RouteDetail
                    String eta = route.eta();
                    int distance = route.distance();
                    System.out.printf("\033[1;33m%d.\033[0m %s (\033[1;33m%d km\033[0m, ETA: \033[1;32m%s\033[0m)\n",
                            validRouteCount, destCityName, distance, eta);
                }
            }

            if (validRouteCount == 0) {
                System.out.println("\033[1;31mNo valid destinations found from " + selectedOriginInfo.city() + " for " + transportType + "s.\033[0m");
                Utils.pause(sc);
                return null; // Go back if no valid destinations
            }

            System.out.print("\033[1mChoose destination city number: \033[0m");

            try {
                destChoice = sc.nextInt();
                sc.nextLine();
                if (destChoice == 0) return selectValidRoute(sc, transportType); // Recursive call to go back

                if (destChoice >= 1 && destChoice <= validRouteCount) {
                    selectedDestCityKey = displayOrderDestKeys.get(destChoice - 1); // Get the key from the displayed list index
                    selectedRouteDetail = availableRoutes.get(selectedDestCityKey); // Get the detail using the key
                } else { System.out.println("\033[1;31mInvalid choice.\033[0m"); Utils.pause(sc); }
            } catch (InputMismatchException e) { System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m"); sc.nextLine(); Utils.pause(sc); }
            catch (Exception e) { System.out.println("\033[1;31mAn error occurred: " + e.getMessage() + "\033[0m"); sc.nextLine(); Utils.pause(sc); }
        }

        // Return origin city name, destination city name, and the route details
        return new Object[]{selectedOriginInfo.city(), selectedRouteDetail.destinationCity(), selectedRouteDetail};
    }


    // --- Updated Booking Methods ---

    private void selectAndBookPlane(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Plane");

        if (selectedRoute == null) {
            System.out.println("\033[1;33mRoute selection cancelled.\033[0m");
            Utils.pause(sc);
            return;
        }

        String startCity = (String) selectedRoute[0];
        String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance();
        String eta = routeDetail.eta();
        List<String> providers = routeDetail.providers(); // Get providers for this route

        double basePrice = Utils.calculatePrice("Plane", distance);
        if (basePrice <= 0 && distance > 0) { // Allow 0 distance routes potentially? Check logic. If distance=0 price is 0.
            System.out.println("\033[1;31mError calculating price (Rs. " + basePrice + ") for the selected route ("+distance+" km).\033[0m");
            Utils.pause(sc);
            return;
        }

        Utils.printBanner("Selected Route");
        System.out.println("\033[1mFrom:\033[0m " + startCity);
        System.out.println("\033[1mTo:\033[0m " + destCity);
        System.out.println("\033[1mDistance:\033[0m \033[1;33m" + distance + " km\033[0m");
        System.out.println("\033[1mEstimated Travel Time:\033[0m \033[1;32m" + eta + "\033[0m");

        // --- *** ADDED: Display Providers *** ---
        if (providers != null && !providers.isEmpty()) {
            System.out.println("\033[1mAvailable Airlines/Services:\033[0m \033[0;35m" + String.join(", ", providers) + "\033[0m");
        } else {
            System.out.println("\033[1;33mNo specific provider information available for this route.\033[0m");
        }
        // --- *** END OF ADDED CODE *** ---

        // --- Continue with existing logic for date, class, plane selection ---
        // Use Utils.getValidTravelDate for date input
        String travelDate = Utils.getValidTravelDate(sc);
        if (travelDate == null) { // Handle 'back'
            System.out.println("\033[1;33mBooking cancelled.\033[0m");
            return;
        }


        // Select class
        double classMultiplier = 1.0;
        String seatClass = "";
        while (seatClass.isEmpty()) {
            System.out.println("\n\033[1;36mChoose Seat Class:\033[0m");
            System.out.printf("\033[1;33m1.\033[0m Economy (Base Price: Rs. %.2f)\n", basePrice * 1.0);
            System.out.printf("\033[1;33m2.\033[0m Business (Base Price: Rs. %.2f)\n", basePrice * 2.0);
            System.out.printf("\033[1;33m3.\033[0m First (Base Price: Rs. %.2f)\n", basePrice * 3.0);
            System.out.println("\033[1;33m0.\033[0m Back");
            System.out.print("\033[1mEnter choice: \033[0m");
            try {
                int classChoice = sc.nextInt();
                sc.nextLine();
                switch (classChoice) {
                    case 1: seatClass = "Economy"; classMultiplier = 1.0; break;
                    case 2: seatClass = "Business"; classMultiplier = 2.0; break;
                    case 3: seatClass = "First"; classMultiplier = 3.0; break;
                    case 0: return; // Go back
                    default: System.out.println("\033[1;31mInvalid class choice.\033[0m"); Utils.pause(sc); continue;
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); Utils.pause(sc);
            }
        }
        double finalRoutePrice = basePrice * classMultiplier;


        // Select Plane (Presenting generic vehicle IDs after showing providers)
        if (planes.isEmpty()) {
            System.out.println("\033[1;31mSorry, no planes are currently configured in the system.\033[0m");
            Utils.pause(sc);
            return;
        }

        PlaneBooking selectedPlane = null;
        while(selectedPlane == null) {
            Utils.printBanner("Select Specific Plane/Aircraft"); // Changed title slightly
            System.out.println("\033[1;36m(The actual airline service is listed above)\033[0m"); // Remind user
            System.out.println("\033[1;33m0.\033[0m Back");
            for (int i = 0; i < planes.size(); i++) {
                System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Aircraft ID: " + planes.get(i).getFlightId()); // Changed label
            }
            System.out.print("\033[1mEnter aircraft number to book on: \033[0m"); // Changed prompt
            try {
                int flightChoice = sc.nextInt();
                sc.nextLine();
                if (flightChoice == 0) return;
                if (flightChoice >= 1 && flightChoice <= planes.size()) {
                    selectedPlane = planes.get(flightChoice - 1);
                } else {
                    System.out.println("\033[1;31mInvalid aircraft selection.\033[0m");
                    Utils.pause(sc);
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); Utils.pause(sc);
            }
        }

        // Call book method (which now includes passenger validation)
        selectedPlane.book(sc, loggedInUser, startCity, destCity, finalRoutePrice, seatClass, travelDate);
        Utils.pause(sc); // Pause after booking attempt

    }

    // --- selectAndBookTrain ---
    private void selectAndBookTrain(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Train");

        if (selectedRoute == null) { /* ... handle cancellation ... */ return; }

        String startCity = (String) selectedRoute[0];
        String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance();
        String eta = routeDetail.eta();
        List<String> providers = routeDetail.providers(); // Get providers

        double basePrice = Utils.calculatePrice("Train", distance);
        if (basePrice <= 0 && distance > 0) { /* ... handle price error ... */ return; }

        Utils.printBanner("Selected Route");
        System.out.println("\033[1mFrom:\033[0m " + startCity);
        System.out.println("\033[1mTo:\033[0m " + destCity);
        System.out.println("\033[1mDistance:\033[0m \033[1;33m" + distance + " km\033[0m");
        System.out.println("\033[1mEstimated Travel Time:\033[0m \033[1;32m" + eta + "\033[0m");

        // --- *** ADDED: Display Providers *** ---
        if (providers != null && !providers.isEmpty()) {
            System.out.println("\033[1mAvailable Trains/Services:\033[0m \033[0;35m" + String.join(", ", providers) + "\033[0m");
        } else {
            System.out.println("\033[1;33mNo specific provider information available for this route.\033[0m");
        }
        // --- *** END OF ADDED CODE *** ---

        // Use Utils.getValidTravelDate
        String travelDate = Utils.getValidTravelDate(sc);
        if (travelDate == null) { return; } // Handle 'back'

        // Select class
        double classMultiplier = 1.0;
        String seatClass = "";
        while (seatClass.isEmpty()) {
            System.out.println("\n\033[1;36mChoose Class:\033[0m");
            // (Display train classes and prices as before)
            System.out.printf("\033[1;33m1.\033[0m AC First Class (1A) (Rs. %.2f)\n", basePrice * 2.5);
            System.out.printf("\033[1;33m2.\033[0m Second AC (2A) (Rs. %.2f)\n", basePrice * 2.0);
            System.out.printf("\033[1;33m3.\033[0m Third AC (3A) (Rs. %.2f)\n", basePrice * 1.5);
            System.out.printf("\033[1;33m4.\033[0m Sleeper Class (SL) (Rs. %.2f)\n", basePrice * 1.0);
            System.out.printf("\033[1;33m5.\033[0m Chair Car (CC) (Rs. %.2f)\n", basePrice * 0.8);
            System.out.printf("\033[1;33m6.\033[0m Second Seater (2S) (Rs. %.2f)\n", basePrice * 0.5);
            System.out.println("\033[1;33m0.\033[0m Back");
            System.out.print("\033[1mEnter choice: \033[0m");

            try {
                int classChoice = sc.nextInt();
                sc.nextLine();
                switch (classChoice) {
                    case 1: seatClass = "AC First Class (1A)"; classMultiplier = 2.5; break;
                    case 2: seatClass = "Second AC (2A)"; classMultiplier = 2.0; break;
                    case 3: seatClass = "Third AC (3A)"; classMultiplier = 1.5; break;
                    case 4: seatClass = "Sleeper Class (SL)"; classMultiplier = 1.0; break;
                    case 5: seatClass = "Chair Car (CC)"; classMultiplier = 0.8; break;
                    case 6: seatClass = "Second Seater (2S)"; classMultiplier = 0.5; break;
                    case 0: return; // Go back
                    default: System.out.println("\033[1;31mInvalid class choice.\033[0m"); Utils.pause(sc); continue;
                }
            } catch (InputMismatchException e) { /* ... handle error ... */ }
        }
        double finalRoutePrice = basePrice * classMultiplier;

        // Select Train (Presenting generic IDs after showing providers)
        if (trains.isEmpty()) { /* ... handle no trains ... */ return; }
        TrainBooking selectedTrain = null;
        while(selectedTrain == null) {
            Utils.printBanner("Select Specific Train");
            System.out.println("\033[1;36m(The actual train service is listed above)\033[0m");
            System.out.println("\033[1;33m0.\033[0m Back");
            for (int i = 0; i < trains.size(); i++) {
                System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Train ID: " + trains.get(i).getTrainId());
            }
            System.out.print("\033[1mEnter train number to book on: \033[0m");
            try {
                int trainChoice = sc.nextInt();
                sc.nextLine();
                if (trainChoice == 0) return;
                if (trainChoice >= 1 && trainChoice <= trains.size()) {
                    selectedTrain = trains.get(trainChoice - 1);
                } else { /* ... handle invalid choice ... */ }
            } catch (InputMismatchException e) { /* ... handle error ... */ }
        }

        // Call book method (which now includes passenger validation)
        selectedTrain.book(sc, loggedInUser, startCity, destCity, finalRoutePrice, seatClass, travelDate);
        Utils.pause(sc);
    }

    // --- selectAndBookBus ---
    private void selectAndBookBus(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Bus");

        if (selectedRoute == null) { /* ... handle cancellation ... */ return; }

        String startCity = (String) selectedRoute[0];
        String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance();
        String eta = routeDetail.eta();
        List<String> providers = routeDetail.providers(); // Get providers

        double routePrice = Utils.calculatePrice("Bus", distance); // Bus has only one class usually
        if (routePrice <= 0 && distance > 0) { /* ... handle price error ... */ return; }

        Utils.printBanner("Selected Route");
        System.out.println("\033[1mFrom:\033[0m " + startCity);
        System.out.println("\033[1mTo:\033[0m " + destCity);
        System.out.println("\033[1mDistance:\033[0m \033[1;33m" + distance + " km\033[0m");
        System.out.println("\033[1mEstimated Travel Time:\033[0m \033[1;32m" + eta + "\033[0m");

        // --- *** ADDED: Display Providers *** ---
        if (providers != null && !providers.isEmpty()) {
            System.out.println("\033[1mAvailable Bus Operators:\033[0m \033[0;35m" + String.join(", ", providers) + "\033[0m");
        } else {
            System.out.println("\033[1;33mNo specific provider information available for this route.\033[0m");
        }
        // --- *** END OF ADDED CODE *** ---

        // Use Utils.getValidTravelDate
        String travelDate = Utils.getValidTravelDate(sc);
        if (travelDate == null) { return; } // Handle 'back'

        // Bus usually has only 'Standard' class
        String seatClass = "Standard";
        System.out.println("\n\033[1;36mClass:\033[0m Standard");
        System.out.printf("\033[1mBase Price:\033[0m Rs. %.2f\n", routePrice);


        // Select Bus (Presenting generic IDs after showing providers)
        if (buses.isEmpty()) { /* ... handle no buses ... */ return; }
        BusBooking selectedBus = null;
        while (selectedBus == null) {
            Utils.printBanner("Select Specific Bus");
            System.out.println("\033[1;36m(The actual bus operator/service is listed above)\033[0m");
            System.out.println("\033[1;33m0.\033[0m Back");
            for (int i = 0; i < buses.size(); i++) {
                System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Bus ID: " + buses.get(i).getBusId());
            }
            System.out.print("\033[1mEnter bus number to book on: \033[0m");
            try {
                int busChoice = sc.nextInt();
                sc.nextLine();
                if (busChoice == 0) return;
                if (busChoice >= 1 && busChoice <= buses.size()) {
                    selectedBus = buses.get(busChoice - 1);
                } else { /* ... handle invalid choice ... */ }
            } catch (InputMismatchException e) { /* ... handle error ... */ }
        }

        // Call book method (which now includes passenger validation)
        selectedBus.book(sc, loggedInUser, startCity, destCity, routePrice, seatClass, travelDate);
        Utils.pause(sc);
    }


    // viewBookings method (no changes needed here)
    private void viewBookings(Scanner sc) {
        Utils.clearScreen();
        Utils.printBanner("Your Bookings");
        boolean foundBookings = false;
        for (PlaneBooking plane : planes) {
            if (plane.displayUserBookings(loggedInUser)) foundBookings = true;
        }
        for (TrainBooking train : trains) {
            if (train.displayUserBookings(loggedInUser)) foundBookings = true;
        }
        for (BusBooking bus : buses) {
            if (bus.displayUserBookings(loggedInUser)) foundBookings = true;
        }
        if (!foundBookings) {
            System.out.println("\033[1;33mYou have no active bookings.\033[0m");
        }
        Utils.pause(sc);
    }


    // cancelBooking method (no changes needed here, relies on boolean returns)
    private void cancelBooking(Scanner sc) {
        Utils.clearScreen();
        Utils.printBanner("Cancel Booking");
        System.out.print("\033[1mEnter Booking ID to cancel (e.g., P1, T1, B1): \033[0m");
        String bookingId = sc.nextLine().toUpperCase();
        boolean canceled = false;

        // *** REMINDER: StorageManager.removeBooking() was removed ***

        if (bookingId.startsWith("P")) {
            for (PlaneBooking plane : planes) {
                if (plane.cancelBooking(bookingId, loggedInUser)) { canceled = true; break; }
            }
        } else if (bookingId.startsWith("T")) {
            for (TrainBooking train : trains) {
                if (train.cancelBooking(bookingId, loggedInUser)) { canceled = true; break; }
            }
        } else if (bookingId.startsWith("B")) {
            for (BusBooking bus : buses) {
                if (bus.cancelBooking(bookingId, loggedInUser)) { canceled = true; break; }
            }
        } else {
            System.out.println("\033[1;31mInvalid Booking ID format (must start with P, T, or B).\033[0m");
            Utils.pause(sc);
            return;
        }

        if (canceled) {
            System.out.println("\033[1;32mBooking " + bookingId + " cancelled successfully in memory.\033[0m");
            System.out.println("\033[0;90m(Changes will be saved automatically on logout/exit)\033[0m");
        } else {
            System.out.println("\033[1;31mBooking ID " + bookingId + " not found or you are not authorized to cancel it.\033[0m");
        }
        Utils.pause(sc);
    }

} // End of BookingSystem class