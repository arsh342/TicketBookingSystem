package TicketBookingSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
// Removed imports no longer used

public class BookingSystem {
    private List<PlaneBooking> planes;
    private List<TrainBooking> trains;
    private List<BusBooking> buses;
    private String loggedInUser;
    private int bookingIdCounter = 1;

    private final RouteDataManager routeDataManager;

    public BookingSystem() {
        routeDataManager = new RouteDataManager(); // Load route data

        // Initialize vehicle lists (these act as booking managers/templates)
        planes = new ArrayList<>();
        trains = new ArrayList<>();
        buses = new ArrayList<>();

        // Add at least one manager object per type
        // IDs can be generic as they represent the booking manager, not the specific service
        planes.add(new PlaneBooking("PLANE-MANAGER-1", this));
        trains.add(new TrainBooking("TRAIN-MANAGER-1", this));
        buses.add(new BusBooking("BUS-MANAGER-1", this));
        // Add more if you need separate booking maps for different physical vehicles of the same type
        // planes.add(new PlaneBooking("PLANE-MANAGER-2", this));

        StorageManager.loadBookings(planes, trains, buses); // Load existing bookings into managers
        updateBookingIdCounter(); // Set counter based on loaded bookings
    }

    /** Sets the booking ID counter based on max loaded ID. */
    private void updateBookingIdCounter() {
        int maxId = 0;
        List<Map<String, ?>> allBookingMaps = new ArrayList<>();
        // Correctly get bookings from ALL manager objects if multiple exist
        planes.forEach(p -> allBookingMaps.add(p.getBookings()));
        trains.forEach(t -> allBookingMaps.add(t.getBookings()));
        buses.forEach(b -> allBookingMaps.add(b.getBookings()));

        for (Map<String, ?> bookingMap : allBookingMaps) {
            for (String bookingId : bookingMap.keySet()) {
                try {
                    int idNum = Integer.parseInt(bookingId.substring(1));
                    if (idNum > maxId) maxId = idNum;
                } catch (NumberFormatException | IndexOutOfBoundsException e) { /* ignore parse errors */ }
            }
        }
        this.bookingIdCounter = maxId + 1;
        System.out.println(Utils.GREY + "Booking ID counter initialized to: " + this.bookingIdCounter + Utils.RESET);
    }

    /** Sets the currently logged-in user. */
    public void setLoggedInUser(String username) { this.loggedInUser = username; }

    /** Gets the next booking ID number. */
    public int getNextBookingId(String prefix) { return bookingIdCounter++; } // Prefix not used here, just returns number

    // --- Getters for vehicle lists (used for saving) ---
    public List<PlaneBooking> getPlanes() { return this.planes; }
    public List<TrainBooking> getTrains() { return this.trains; }
    public List<BusBooking> getBuses() { return this.buses; }

    /** Starts the main booking menu loop. */
    public void startBooking(Scanner sc) {
        while (true) {
            Utils.clearScreen();
            Utils.printBanner(loggedInUser + "'s Booking Menu");
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Book a Ticket" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " View My Bookings" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " Cancel a Booking" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "4." + Utils.RESET + Utils.CYAN + " Logout" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose an option: " + Utils.RESET);

            int choice = -1; // Initialize choice
            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) { /* ... handle error ... */ Utils.pause(sc); continue; }
            catch (Exception e) { /* ... handle error ... */ Utils.pause(sc); continue; }

            switch (choice) {
                case 1: bookTicket(sc); break; // Save happens after returning if needed
                case 2: viewBookings(sc); break;
                case 3:
                    cancelBooking(sc);
                    StorageManager.saveBookings(planes, trains, buses); // Save immediately after cancel attempt
                    break;
                case 4: System.out.println(Utils.GREEN + "\nLogging out..." + Utils.RESET); return; // Exit loop
                default: System.out.println(Utils.RED + "Invalid option." + Utils.RESET); Utils.pause(sc);
            }
        }
    }

    /** Shows transport type selection menu. */
    private void bookTicket(Scanner sc) {
        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Select Mode of Transportation");
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Plane" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Train" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " Bus" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back to Booking Menu" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Enter choice: " + Utils.RESET);

            int transportChoice = -1;
            try {
                transportChoice = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) { /* ... handle error ... */ Utils.pause(sc); continue; }
            catch (Exception e) { /* ... handle error ... */ Utils.pause(sc); continue; }

            switch (transportChoice) {
                case 1: selectAndBookPlane(sc); return; // Return after attempt
                case 2: selectAndBookTrain(sc); return;
                case 3: selectAndBookBus(sc); return;
                case 0: return; // Go back
                default: System.out.println(Utils.RED + "Invalid option." + Utils.RESET); Utils.pause(sc);
            }
        }
    }

    /** Handles selection of origin and destination using RouteDataManager. */
    private Object[] selectValidRoute(Scanner sc, String transportType) {
        Map<String, LocationInfo> locationData; String locationTypeName;
        switch (transportType) {
            case "Plane": locationData = routeDataManager.getAirportData(); locationTypeName = "Airport"; break;
            case "Train": locationData = routeDataManager.getTrainStationData(); locationTypeName = "Train Station"; break;
            case "Bus": locationData = routeDataManager.getBusStationData(); locationTypeName = "Bus Station"; break;
            default: return null;
        }
        if (locationData == null || locationData.isEmpty()) { System.out.println(Utils.RED + "Error: No " + locationTypeName + " data." + Utils.RESET); Utils.pause(sc); return null; }

        List<String> originCityKeys = new ArrayList<>(locationData.keySet()); Collections.sort(originCityKeys);
        LocationInfo selectedOriginInfo = null;
        while (selectedOriginInfo == null) { // Origin Selection Loop
            Utils.clearScreen(); Utils.printBanner("Select Origin " + locationTypeName);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            for (int i = 0; i < originCityKeys.size(); i++) {
                LocationInfo info = locationData.get(originCityKeys.get(i));
                System.out.printf(Utils.YELLOW_BOLD + "%d." + Utils.RESET + Utils.CYAN + " %s (%s)" + Utils.RESET + "\n", i + 1, info.city(), info.primaryName());
            }
            System.out.print(Utils.WHITE_BOLD + "Choose origin city number: " + Utils.RESET);
            try {
                int originChoice = sc.nextInt(); sc.nextLine();
                if (originChoice == 0) return null;
                if (originChoice >= 1 && originChoice <= originCityKeys.size()) {
                    selectedOriginInfo = locationData.get(originCityKeys.get(originChoice - 1));
                    if (selectedOriginInfo.routes() == null || selectedOriginInfo.routes().isEmpty()) {
                        System.out.println(Utils.YELLOW + "No routes from " + selectedOriginInfo.city() + "." + Utils.RESET); selectedOriginInfo = null; Utils.pause(sc);
                    }
                } else { System.out.println(Utils.RED + "Invalid choice." + Utils.RESET); Utils.pause(sc); }
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
            catch (Exception e) { System.out.println(Utils.RED + "Error: " + e.getMessage() + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
        }

        Map<String, RouteDetail> availableRoutes = selectedOriginInfo.routes();
        List<String> destinationKeys = new ArrayList<>(availableRoutes.keySet()); Collections.sort(destinationKeys);
        RouteDetail selectedRouteDetail = null;
        while (selectedRouteDetail == null) { // Destination Selection Loop
            Utils.clearScreen(); Utils.printBanner("Select Destination from " + selectedOriginInfo.city());
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back to Origin Selection" + Utils.RESET);
            int validRouteCount = 0; List<String> displayOrderDestKeys = new ArrayList<>();
            for (String destKey : destinationKeys) {
                RouteDetail route = availableRoutes.get(destKey);
                boolean isAvailable = route.distance() > -1 || !route.eta().equalsIgnoreCase("Not Available");
                if (isAvailable) {
                    validRouteCount++; displayOrderDestKeys.add(destKey);
                    System.out.printf(Utils.YELLOW_BOLD + "%d." + Utils.RESET + Utils.CYAN + " %s (" + Utils.BLUE_BOLD + "Dist:" + Utils.MAGENTA_BOLD + " %d km" + Utils.CYAN + ", ETA: " + Utils.GREEN_BOLD + "%s" + Utils.CYAN + ")" + Utils.RESET + "\n",
                            validRouteCount, route.destinationCity(), route.distance(), route.eta());
                }
            }
            if (validRouteCount == 0) { System.out.println(Utils.YELLOW + "No valid destinations found." + Utils.RESET); Utils.pause(sc); return selectValidRoute(sc, transportType); } // Go back to origin
            System.out.print(Utils.WHITE_BOLD + "Choose destination city number: " + Utils.RESET);
            try {
                int destChoice = sc.nextInt(); sc.nextLine();
                if (destChoice == 0) return selectValidRoute(sc, transportType); // Go back to origin
                if (destChoice >= 1 && destChoice <= validRouteCount) {
                    String selectedDestCityKey = displayOrderDestKeys.get(destChoice - 1);
                    selectedRouteDetail = availableRoutes.get(selectedDestCityKey);
                } else { System.out.println(Utils.RED + "Invalid choice." + Utils.RESET); Utils.pause(sc); }
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
            catch (Exception e) { System.out.println(Utils.RED + "Error: " + e.getMessage() + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
        }
        return new Object[]{selectedOriginInfo.city(), selectedRouteDetail.destinationCity(), selectedRouteDetail};
    }

    /** Handles Plane booking flow: route -> date -> class -> provider -> book. */
    private void selectAndBookPlane(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Plane");
        if (selectedRoute == null) { System.out.println(Utils.YELLOW + "Route selection cancelled." + Utils.RESET); return; }

        String startCity = (String) selectedRoute[0]; String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance(); String eta = routeDetail.eta(); List<String> providers = routeDetail.providers();
        double basePrice = Utils.calculatePrice("Plane", distance);
        if (basePrice < 0 || (basePrice == 0 && distance > 0)) { System.out.println(Utils.RED + "Price calculation error." + Utils.RESET); Utils.pause(sc); return; }

        // Display Route Info
        Utils.printBanner("Selected Route: " + startCity + " -> " + destCity);
        System.out.println(Utils.BLUE_BOLD + "Distance:           " + Utils.MAGENTA_BOLD + distance + " km" + Utils.RESET);
        System.out.println(Utils.BLUE_BOLD + "Est. Travel Time:   " + Utils.GREEN_BOLD + eta + Utils.RESET); System.out.println();

        // Get Travel Date
        String travelDate = Utils.getValidTravelDate(sc);
        if (travelDate == null) { System.out.println(Utils.YELLOW + "\nBooking cancelled." + Utils.RESET); Utils.pause(sc); return; }

        // Select Class
        double classMultiplier = 1.0; String seatClass = "";
        while (seatClass.isEmpty()) {
            System.out.println("\n" + Utils.CYAN_BOLD + "Choose Seat Class:" + Utils.RESET);
            System.out.printf(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Economy (Base: Rs. %.2f)" + Utils.RESET + "\n", basePrice * 1.0);
            System.out.printf(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Business (Base: Rs. %.2f)" + Utils.RESET + "\n", basePrice * 2.0);
            System.out.printf(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " First (Base: Rs. %.2f)" + Utils.RESET + "\n", basePrice * 3.0);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Enter choice: " + Utils.RESET);
            try { /* ... Get choice, set seatClass & multiplier, handle 0/default/error ... */
                int classChoice=sc.nextInt(); sc.nextLine();
                switch(classChoice){case 1:seatClass="Economy";classMultiplier=1.0;break; case 2:seatClass="Business";classMultiplier=2.0;break; case 3:seatClass="First";classMultiplier=3.0;break; case 0:System.out.println(Utils.YELLOW+"Booking cancelled."+Utils.RESET);Utils.pause(sc);return; default:System.out.println(Utils.RED+"Invalid choice."+Utils.RESET);Utils.pause(sc);}
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
        }
        double finalRoutePrice = basePrice * classMultiplier;

        // Select Provider
        String selectedProvider = "N/A"; // Default if none listed or selected
        if (providers != null && !providers.isEmpty()) {
            Utils.printBanner("Select Airline/Service");
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            for (int i = 0; i < providers.size(); i++) System.out.println(Utils.YELLOW_BOLD + (i + 1) + "." + Utils.RESET + Utils.CYAN + " " + providers.get(i) + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose service number: " + Utils.RESET);
            selectedProvider = null; // Reset for selection loop
            while (selectedProvider == null) {
                try {
                    int providerChoice = sc.nextInt(); sc.nextLine();
                    if (providerChoice == 0) { System.out.println(Utils.YELLOW+"Booking cancelled."+Utils.RESET); Utils.pause(sc); return; }
                    if (providerChoice >= 1 && providerChoice <= providers.size()) selectedProvider = providers.get(providerChoice - 1);
                    else { System.out.println(Utils.RED + "Invalid choice." + Utils.RESET); System.out.print(Utils.WHITE_BOLD + "Choose service number: " + Utils.RESET); } // Re-prompt
                } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); sc.nextLine(); System.out.print(Utils.WHITE_BOLD + "Choose service number: " + Utils.RESET); } // Re-prompt
            }
        } else { System.out.println(Utils.YELLOW + "No specific airlines listed, proceeding..." + Utils.RESET); }

        // Use the first PlaneBooking object as the manager/template
        if (planes.isEmpty()) { System.out.println(Utils.RED + "No planes configured." + Utils.RESET); Utils.pause(sc); return; }
        PlaneBooking bookingManagerPlane = planes.get(0);

        // Call book, passing the selected provider
        bookingManagerPlane.book(sc, loggedInUser, startCity, destCity, finalRoutePrice, seatClass, travelDate, selectedProvider);
        Utils.pause(sc); // Pause after the booking process
    }

    /** Handles Train booking flow. */
    private void selectAndBookTrain(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Train");
        if (selectedRoute == null) { System.out.println(Utils.YELLOW + "Route selection cancelled." + Utils.RESET); return; }

        String startCity = (String) selectedRoute[0]; String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance(); String eta = routeDetail.eta(); List<String> providers = routeDetail.providers();
        double basePrice = Utils.calculatePrice("Train", distance);
        if (basePrice < 0 || (basePrice == 0 && distance > 0)) { System.out.println(Utils.RED + "Price calculation error." + Utils.RESET); Utils.pause(sc); return; }

        // Display Route Info
        Utils.printBanner("Selected Route: " + startCity + " -> " + destCity);
        System.out.println(Utils.BLUE_BOLD + "Distance:           " + Utils.MAGENTA_BOLD + distance + " km" + Utils.RESET);
        System.out.println(Utils.BLUE_BOLD + "Est. Travel Time:   " + Utils.GREEN_BOLD + eta + Utils.RESET); System.out.println();

        // Get Travel Date
        String travelDate = Utils.getValidTravelDate(sc);
        if (travelDate == null) { System.out.println(Utils.YELLOW + "\nBooking cancelled." + Utils.RESET); Utils.pause(sc); return; }

        // Select Class
        double classMultiplier = 1.0; String seatClass = "";
        while (seatClass.isEmpty()) {
            System.out.println("\n" + Utils.CYAN_BOLD + "Choose Class:" + Utils.RESET);
            System.out.printf(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " AC First Class (1A) (Rs. %.2f)" + Utils.RESET + "\n", basePrice * 2.5);
            System.out.printf(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Second AC (2A)      (Rs. %.2f)" + Utils.RESET + "\n", basePrice * 2.0);
            System.out.printf(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " Third AC (3A)       (Rs. %.2f)" + Utils.RESET + "\n", basePrice * 1.5);
            System.out.printf(Utils.YELLOW_BOLD + "4." + Utils.RESET + Utils.CYAN + " Sleeper Class (SL)  (Rs. %.2f)" + Utils.RESET + "\n", basePrice * 1.0);
            System.out.printf(Utils.YELLOW_BOLD + "5." + Utils.RESET + Utils.CYAN + " Chair Car (CC)      (Rs. %.2f)" + Utils.RESET + "\n", basePrice * 0.8);
            System.out.printf(Utils.YELLOW_BOLD + "6." + Utils.RESET + Utils.CYAN + " Second Seater (2S)  (Rs. %.2f)" + Utils.RESET + "\n", basePrice * 0.5);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Enter choice: " + Utils.RESET);
            try { /* ... Get choice, set seatClass & multiplier, handle 0/default/error ... */
                int classChoice=sc.nextInt(); sc.nextLine();
                switch(classChoice){case 1:seatClass="AC First Class (1A)";classMultiplier=2.5;break; case 2:seatClass="Second AC (2A)";classMultiplier=2.0;break; case 3:seatClass="Third AC (3A)";classMultiplier=1.5;break; case 4:seatClass="Sleeper Class (SL)";classMultiplier=1.0;break; case 5:seatClass="Chair Car (CC)";classMultiplier=0.8;break; case 6:seatClass="Second Seater (2S)";classMultiplier=0.5;break; case 0:System.out.println(Utils.YELLOW+"Booking cancelled."+Utils.RESET);Utils.pause(sc);return; default:System.out.println(Utils.RED+"Invalid choice."+Utils.RESET);Utils.pause(sc);}
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
        }
        if(seatClass.isEmpty()) return; // Should not happen due to loop, but safeguard
        double finalRoutePrice = basePrice * classMultiplier;

        // Select Provider
        String selectedProvider = "N/A";
        if (providers != null && !providers.isEmpty()) {
            Utils.printBanner("Select Train/Service");
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            for (int i = 0; i < providers.size(); i++) System.out.println(Utils.YELLOW_BOLD + (i + 1) + "." + Utils.RESET + Utils.CYAN + " " + providers.get(i) + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose service number: " + Utils.RESET);
            selectedProvider = null;
            while (selectedProvider == null) {
                try { /* ... Get provider choice, set selectedProvider, handle 0/default/error ... */
                    int providerChoice = sc.nextInt(); sc.nextLine();
                    if(providerChoice == 0){System.out.println(Utils.YELLOW+"Booking cancelled."+Utils.RESET); Utils.pause(sc); return;}
                    if(providerChoice >= 1 && providerChoice <= providers.size()) selectedProvider = providers.get(providerChoice - 1);
                    else {System.out.println(Utils.RED + "Invalid choice." + Utils.RESET);System.out.print(Utils.WHITE_BOLD + "Choose service number: " + Utils.RESET);}
                } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); sc.nextLine(); System.out.print(Utils.WHITE_BOLD + "Choose service number: " + Utils.RESET);}
            }
        } else { System.out.println(Utils.YELLOW + "No specific trains listed, proceeding..." + Utils.RESET); }

        // Use the first TrainBooking object as the manager
        if (trains.isEmpty()) { System.out.println(Utils.RED + "No trains configured." + Utils.RESET); Utils.pause(sc); return; }
        TrainBooking bookingManagerTrain = trains.get(0);

        // Call book
        bookingManagerTrain.book(sc, loggedInUser, startCity, destCity, finalRoutePrice, seatClass, travelDate, selectedProvider);
        Utils.pause(sc);
    }

    /** Handles Bus booking flow. */
    private void selectAndBookBus(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Bus");
        if (selectedRoute == null) { System.out.println(Utils.YELLOW + "Route selection cancelled." + Utils.RESET); return; }

        String startCity = (String) selectedRoute[0]; String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance(); String eta = routeDetail.eta(); List<String> providers = routeDetail.providers();
        double routePrice = Utils.calculatePrice("Bus", distance); // Only one class price for bus
        if (routePrice < 0 || (routePrice == 0 && distance > 0)) { System.out.println(Utils.RED + "Price calculation error." + Utils.RESET); Utils.pause(sc); return; }

        // Display Route Info
        Utils.printBanner("Selected Route: " + startCity + " -> " + destCity);
        System.out.println(Utils.BLUE_BOLD + "Distance:           " + Utils.MAGENTA_BOLD + distance + " km" + Utils.RESET);
        System.out.println(Utils.BLUE_BOLD + "Est. Travel Time:   " + Utils.GREEN_BOLD + eta + Utils.RESET); System.out.println();

        // Get Travel Date
        String travelDate = Utils.getValidTravelDate(sc);
        if (travelDate == null) { System.out.println(Utils.YELLOW + "\nBooking cancelled." + Utils.RESET); Utils.pause(sc); return; }

        // Bus is always Standard class
        String seatClass = "Standard";
        System.out.println("\n" + Utils.CYAN_BOLD + "Class:" + Utils.CYAN + " Standard" + Utils.RESET);
        System.out.printf(Utils.BLUE_BOLD + "Ticket Price: " + Utils.GREEN_BOLD + "Rs. %.2f" + Utils.RESET + "\n", routePrice);

        // Select Provider
        String selectedProvider = "N/A";
        if (providers != null && !providers.isEmpty()) {
            Utils.printBanner("Select Bus Operator/Service");
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            for (int i = 0; i < providers.size(); i++) System.out.println(Utils.YELLOW_BOLD + (i + 1) + "." + Utils.RESET + Utils.CYAN + " " + providers.get(i) + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose service number: " + Utils.RESET);
            selectedProvider = null;
            while (selectedProvider == null) {
                try { /* ... Get provider choice, set selectedProvider, handle 0/default/error ... */
                    int providerChoice = sc.nextInt(); sc.nextLine();
                    if(providerChoice == 0){System.out.println(Utils.YELLOW+"Booking cancelled."+Utils.RESET); Utils.pause(sc); return;}
                    if(providerChoice >= 1 && providerChoice <= providers.size()) selectedProvider = providers.get(providerChoice - 1);
                    else {System.out.println(Utils.RED + "Invalid choice." + Utils.RESET);System.out.print(Utils.WHITE_BOLD + "Choose service number: " + Utils.RESET);}
                } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); sc.nextLine(); System.out.print(Utils.WHITE_BOLD + "Choose service number: " + Utils.RESET);}
            }
        } else { System.out.println(Utils.YELLOW + "No specific bus operators listed, proceeding..." + Utils.RESET); }

        // Use the first BusBooking object as the manager
        if (buses.isEmpty()) { System.out.println(Utils.RED + "No buses configured." + Utils.RESET); Utils.pause(sc); return; }
        BusBooking bookingManagerBus = buses.get(0);

        // Call book
        bookingManagerBus.book(sc, loggedInUser, startCity, destCity, routePrice, seatClass, travelDate, selectedProvider);
        Utils.pause(sc);
    }

    /** Displays user's bookings across all vehicle types. */
    private void viewBookings(Scanner sc) {
        Utils.clearScreen();
        Utils.printBanner("Your Bookings for " + loggedInUser);
        boolean foundBookings = false;
        // Use the displayUserBookings methods which now have better formatting
        for (PlaneBooking plane : planes) if (plane.displayUserBookings(loggedInUser)) foundBookings = true;
        for (TrainBooking train : trains) if (train.displayUserBookings(loggedInUser)) foundBookings = true;
        for (BusBooking bus : buses) if (bus.displayUserBookings(loggedInUser)) foundBookings = true;
        if (!foundBookings) System.out.println(Utils.YELLOW + "\nYou have no active bookings." + Utils.RESET);
        Utils.pause(sc);
    }

    /** Handles booking cancellation. */
    private void cancelBooking(Scanner sc) {
        Utils.clearScreen();
        Utils.printBanner("Cancel Booking");
        System.out.print(Utils.WHITE_BOLD + "Enter Booking ID to cancel (e.g., P1, T1, B1): " + Utils.RESET);
        String bookingId = sc.nextLine().toUpperCase();
        boolean canceled = false;

        // Determine type and try cancelling in the respective list of managers
        // If multiple manager objects exist (e.g., multiple planes), iterate through all
        if (bookingId.startsWith("P")) {
            for (PlaneBooking plane : planes) if (plane.cancelBooking(bookingId, loggedInUser)) { canceled = true; break; }
        } else if (bookingId.startsWith("T")) {
            for (TrainBooking train : trains) if (train.cancelBooking(bookingId, loggedInUser)) { canceled = true; break; }
        } else if (bookingId.startsWith("B")) {
            for (BusBooking bus : buses) if (bus.cancelBooking(bookingId, loggedInUser)) { canceled = true; break; }
        } else { System.out.println(Utils.RED + "Invalid Booking ID format." + Utils.RESET); Utils.pause(sc); return; }

        // Display result
        if (canceled) {
            System.out.println(Utils.GREEN_BOLD + "\nBooking " + bookingId + " cancelled successfully." + Utils.RESET);
            System.out.println(Utils.GREY + "(Changes saved automatically)" + Utils.RESET); // Since we save after calling cancelBooking
        } else { System.out.println(Utils.RED + "\nBooking ID " + bookingId + " not found or unauthorized." + Utils.RESET); }
        Utils.pause(sc);
    }

} // End of BookingSystem class