package TicketBookingSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
// Reflection imports are no longer needed if modifyBookingDate is adapted
// import java.lang.reflect.Method;

public class BookingSystem {
    private List<PlaneBooking> planes;
    private List<TrainBooking> trains;
    private List<BusBooking> buses;
    private String loggedInUser;
    private int bookingIdCounter = 1; // Counter for generating unique booking IDs

    // Instance of RouteDataManager to access route info
    private final RouteDataManager routeDataManager;

    /**
     * Constructor for BookingSystem.
     * Initializes vehicle lists, loads route data, and loads existing bookings.
     */
    public BookingSystem() {
        // Initialize RouteDataManager first to load data files
        routeDataManager = new RouteDataManager();

        // Initialize vehicle lists (these act as booking managers/templates)
        planes = new ArrayList<>();
        trains = new ArrayList<>();
        buses = new ArrayList<>();

        // Add at least one manager object per type
        // IDs can be generic as they represent the booking manager, not the specific service
        planes.add(new PlaneBooking("PLANE-MANAGER-1", this));
        trains.add(new TrainBooking("TRAIN-MANAGER-1", this));
        buses.add(new BusBooking("BUS-MANAGER-1", this));
        // Example: Add more if you want to simulate multiple physical planes/trains/buses
        // planes.add(new PlaneBooking("PLANE-MANAGER-2", this));

        // Load existing bookings from storage into the respective manager objects
        StorageManager.loadBookings(planes, trains, buses);

        // Set the booking ID counter based on loaded bookings
        updateBookingIdCounter();
    }

    /**
     * Updates the booking ID counter to ensure new IDs are unique.
     * Finds the maximum existing numeric ID part and sets the counter to the next value.
     */
    private void updateBookingIdCounter() {
        int maxId = 0;
        // Create a temporary list to hold all booking maps from all manager objects
        List<Map<String, ?>> allBookingMaps = new ArrayList<>();
        planes.forEach(p -> allBookingMaps.add(p.getBookings()));
        trains.forEach(t -> allBookingMaps.add(t.getBookings()));
        buses.forEach(b -> allBookingMaps.add(b.getBookings()));

        // Iterate through all bookings to find the highest ID number
        for (Map<String, ?> bookingMap : allBookingMaps) {
            for (String bookingId : bookingMap.keySet()) {
                try {
                    // Extract number part (e.g., P1 -> 1, T10 -> 10)
                    // Assumes ID format is Letter + Number
                    int idNum = Integer.parseInt(bookingId.substring(1));
                    if (idNum > maxId) {
                        maxId = idNum;
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    // Ignore IDs that don't fit the expected format during counter update
                    System.err.println(Utils.YELLOW + "Warning: Could not parse booking ID for counter update: " + bookingId + Utils.RESET);
                }
            }
        }
        // Set the counter to one greater than the highest found ID
        this.bookingIdCounter = maxId + 1;
        System.out.println(Utils.GREY + "Booking ID counter initialized to: " + this.bookingIdCounter + Utils.RESET);
    }

    /**
     * Sets the username of the currently logged-in user.
     * @param username The username.
     */
    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
    }

    /**
     * Generates the next unique booking ID number. The prefix (P, T, B) is added by the caller.
     * @param prefix The prefix (not used by this method, but kept for signature consistency).
     * @return The next integer for the booking ID.
     */
    public int getNextBookingId(String prefix) {
        return bookingIdCounter++;
    }

    // --- Getters for vehicle lists (needed for saving data in Main and for AdminDashboard) ---
    public List<PlaneBooking> getPlanes() { return this.planes; }
    public List<TrainBooking> getTrains() { return this.trains; }
    public List<BusBooking> getBuses() { return this.buses; }

    /**
     * Starts the main booking menu loop for the logged-in user.
     * @param sc Scanner object for user input.
     */
    public void startBooking(Scanner sc) {
        while (true) {
            Utils.clearScreen();
            Utils.printBanner(loggedInUser + "'s Booking Menu");
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Book a Ticket" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " View My Bookings" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " Cancel a Booking" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "4." + Utils.RESET + Utils.CYAN + " Modify Booking Date" + Utils.RESET); // New Option
            System.out.println(Utils.YELLOW_BOLD + "5." + Utils.RESET + Utils.CYAN + " Logout" + Utils.RESET); // Logout shifted
            System.out.print(Utils.WHITE_BOLD + "Choose an option: " + Utils.RESET);

            int choice = -1; // Initialize choice
            try {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume the invalid input
                Utils.pause(sc);
                continue;
            } catch (Exception e) {
                System.out.println(Utils.RED + "An error occurred: " + e.getMessage() + Utils.RESET);
                sc.nextLine(); // Consume potentially leftover input
                Utils.pause(sc);
                continue;
            }

            switch (choice) {
                case 1:
                    bookTicket(sc); // Handles its own flow and returns
                    // Save after a booking attempt, whether successful or cancelled within bookTicket's sub-flow
                    StorageManager.saveBookings(planes, trains, buses);
                    break;
                case 2:
                    viewBookings(sc);
                    break;
                case 3:
                    cancelBooking(sc);
                    StorageManager.saveBookings(planes, trains, buses); // Save immediately after cancel attempt
                    break;
                case 4: // New: Modify Booking Date
                    modifyBookingDate(sc);
                    StorageManager.saveBookings(planes, trains, buses); // Save after modification attempt
                    break;
                case 5: // Logout
                    System.out.println(Utils.GREEN + "\nLogging out..." + Utils.RESET);
                    // Saving should happen in Main after control returns, or just before returning here
                    // StorageManager.saveBookings(planes, trains, buses); // Optional: save explicitly on logout
                    return; // Exit the booking menu loop
                default:
                    System.out.println(Utils.RED + "Invalid option. Try again." + Utils.RESET);
                    Utils.pause(sc);
            }
        }
    }

    /**
     * Displays the menu for selecting the mode of transportation.
     * @param sc Scanner object for user input.
     */
    private void bookTicket(Scanner sc) {
        while (true) {
            Utils.clearScreen();
            Utils.printBanner("Select Mode of Transportation");
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Plane" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Train" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " Bus" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back to Booking Menu" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Enter choice: " + Utils.RESET);

            int transportChoice = -1; // Initialize
            try {
                transportChoice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
                continue; // Loop back
            } catch (Exception e) {
                System.out.println(Utils.RED + "An error occurred: " + e.getMessage() + Utils.RESET);
                sc.nextLine();
                Utils.pause(sc);
                continue;
            }

            // Call the specific booking method or return
            switch (transportChoice) {
                case 1: selectAndBookPlane(sc); return; // Return to booking menu after this flow
                case 2: selectAndBookTrain(sc); return;
                case 3: selectAndBookBus(sc); return;
                case 0: return; // Go back
                default: System.out.println(Utils.RED + "Invalid option." + Utils.RESET); Utils.pause(sc);
            }
        }
    }


    /**
     * Guides the user to select a valid origin and destination from loaded route data.
     * @param sc Scanner for input.
     * @param transportType "Plane", "Train", or "Bus".
     * @return Object array [String originCityName, String destinationCityName, RouteDetail routeDetail] or null.
     */
    private Object[] selectValidRoute(Scanner sc, String transportType) {
        Map<String, LocationInfo> locationData; String locationTypeName;
        switch (transportType) {
            case "Plane": locationData = routeDataManager.getAirportData(); locationTypeName = "Airport"; break;
            case "Train": locationData = routeDataManager.getTrainStationData(); locationTypeName = "Train Station"; break;
            case "Bus": locationData = routeDataManager.getBusStationData(); locationTypeName = "Bus Station"; break;
            default: System.err.println(Utils.RED_BOLD + "Internal Error: Invalid transport." + Utils.RESET); return null;
        }
        if (locationData == null || locationData.isEmpty()) { System.out.println(Utils.RED + "Error: No " + locationTypeName + " data." + Utils.RESET); Utils.pause(sc); return null; }

        List<String> originCityKeys = new ArrayList<>(locationData.keySet()); Collections.sort(originCityKeys);
        LocationInfo selectedOriginInfo = null;
        while (selectedOriginInfo == null) { // Origin Selection Loop
            Utils.clearScreen(); Utils.printBanner("Select Origin " + locationTypeName);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            for (int i = 0; i < originCityKeys.size(); i++) { LocationInfo info = locationData.get(originCityKeys.get(i)); System.out.printf(Utils.YELLOW_BOLD + "%d." + Utils.RESET + Utils.CYAN + " %s (%s)" + Utils.RESET + "\n", i + 1, info.city(), info.primaryName()); }
            System.out.print(Utils.WHITE_BOLD + "Choose origin city number: " + Utils.RESET);
            try {
                int originChoice = sc.nextInt(); sc.nextLine(); if (originChoice == 0) return null;
                if (originChoice >= 1 && originChoice <= originCityKeys.size()) { selectedOriginInfo = locationData.get(originCityKeys.get(originChoice - 1)); if (selectedOriginInfo.routes() == null || selectedOriginInfo.routes().isEmpty()) { System.out.println(Utils.YELLOW + "No routes from " + selectedOriginInfo.city() + "." + Utils.RESET); selectedOriginInfo = null; Utils.pause(sc); } }
                else { System.out.println(Utils.RED + "Invalid choice." + Utils.RESET); Utils.pause(sc); }
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
            catch (Exception e) { System.out.println(Utils.RED + "Error: " + e.getMessage() + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
        }

        Map<String, RouteDetail> availableRoutes = selectedOriginInfo.routes(); List<String> destinationKeys = new ArrayList<>(availableRoutes.keySet()); Collections.sort(destinationKeys);
        RouteDetail selectedRouteDetail = null;
        while (selectedRouteDetail == null) { // Destination Selection Loop
            Utils.clearScreen(); Utils.printBanner("Select Destination from " + selectedOriginInfo.city());
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back to Origin Selection" + Utils.RESET);
            int validRouteCount = 0; List<String> displayOrderDestKeys = new ArrayList<>();
            for (String destKey : destinationKeys) { RouteDetail route = availableRoutes.get(destKey); boolean isAvailable = route.distance() > -1 || !route.eta().equalsIgnoreCase("Not Available"); if (isAvailable) { validRouteCount++; displayOrderDestKeys.add(destKey); System.out.printf(Utils.YELLOW_BOLD + "%d." + Utils.RESET + Utils.CYAN + " %s (" + Utils.BLUE_BOLD + "Dist:" + Utils.MAGENTA_BOLD + " %d km" + Utils.CYAN + ", ETA: " + Utils.GREEN_BOLD + "%s" + Utils.CYAN + ")" + Utils.RESET + "\n", validRouteCount, route.destinationCity(), route.distance(), route.eta()); } }
            if (validRouteCount == 0) { System.out.println(Utils.YELLOW + "No valid destinations." + Utils.RESET); Utils.pause(sc); return selectValidRoute(sc, transportType); }
            System.out.print(Utils.WHITE_BOLD + "Choose destination city number: " + Utils.RESET);
            try {
                int destChoice = sc.nextInt(); sc.nextLine(); if (destChoice == 0) return selectValidRoute(sc, transportType); // Go back to origin selection
                if (destChoice >= 1 && destChoice <= validRouteCount) { String selectedDestCityKey = displayOrderDestKeys.get(destChoice - 1); selectedRouteDetail = availableRoutes.get(selectedDestCityKey); }
                else { System.out.println(Utils.RED + "Invalid choice." + Utils.RESET); Utils.pause(sc); }
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input." + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
            catch (Exception e) { System.out.println(Utils.RED + "Error: " + e.getMessage() + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
        }
        return new Object[]{selectedOriginInfo.city(), selectedRouteDetail.destinationCity(), selectedRouteDetail};
    }

    /** Handles Plane booking flow. */
    private void selectAndBookPlane(Scanner sc) {
        Utils.clearScreen(); Object[] selectedRoute = selectValidRoute(sc, "Plane");
        if (selectedRoute == null) { System.out.println(Utils.YELLOW + "Route selection cancelled." + Utils.RESET); return; }
        String startCity = (String) selectedRoute[0]; String destCity = (String) selectedRoute[1]; RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance(); String eta = routeDetail.eta(); List<String> providers = routeDetail.providers();
        double basePrice = Utils.calculatePrice("Plane", distance);
        if (basePrice < 0 || (basePrice == 0 && distance > 0)) { System.out.println(Utils.RED + "Price calculation error." + Utils.RESET); Utils.pause(sc); return; }

        Utils.printBanner("Selected Route: " + startCity + " -> " + destCity); System.out.println(Utils.BLUE_BOLD + "Distance:           " + Utils.MAGENTA_BOLD + distance + " km" + Utils.RESET); System.out.println(Utils.BLUE_BOLD + "Est. Travel Time:   " + Utils.GREEN_BOLD + eta + Utils.RESET); System.out.println();
        String travelDate = Utils.getValidTravelDate(sc); if (travelDate == null) { System.out.println(Utils.YELLOW + "\nBooking cancelled." + Utils.RESET); Utils.pause(sc); return; }
        double classMultiplier = 1.0; String seatClass = "";
        while (seatClass.isEmpty()) { /* ... Class selection ... */
            System.out.println("\n" + Utils.CYAN_BOLD + "Choose Seat Class:" + Utils.RESET); System.out.printf(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Economy (Rs. %.2f)" + Utils.RESET + "\n", basePrice * 1.0); System.out.printf(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Business (Rs. %.2f)" + Utils.RESET + "\n", basePrice * 2.0); System.out.printf(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " First (Rs. %.2f)" + Utils.RESET + "\n", basePrice * 3.0); System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET); System.out.print(Utils.WHITE_BOLD + "Choice: " + Utils.RESET);
            try { int choice=sc.nextInt();sc.nextLine(); switch(choice){case 1:seatClass="Economy";classMultiplier=1.0;break; case 2:seatClass="Business";classMultiplier=2.0;break; case 3:seatClass="First";classMultiplier=3.0;break; case 0:System.out.println(Utils.YELLOW+"Cancelled."+Utils.RESET);Utils.pause(sc);return; default:System.out.println(Utils.RED+"Invalid."+Utils.RESET);Utils.pause(sc);}} catch (InputMismatchException e) {System.out.println(Utils.RED+"Invalid."+Utils.RESET);sc.nextLine();Utils.pause(sc);}
        }
        if(seatClass.isEmpty()) return; double finalRoutePrice = basePrice * classMultiplier;

        String selectedProvider = "N/A";
        if (providers != null && !providers.isEmpty()) { /* ... Provider selection ... */
            Utils.printBanner("Select Airline/Service"); System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET); for (int i = 0; i < providers.size(); i++) System.out.println(Utils.YELLOW_BOLD + (i + 1) + "." + Utils.RESET + Utils.CYAN + " " + providers.get(i) + Utils.RESET); System.out.print(Utils.WHITE_BOLD + "Choice: " + Utils.RESET); selectedProvider = null;
            while (selectedProvider == null) { try { int choice = sc.nextInt(); sc.nextLine(); if (choice == 0) {System.out.println(Utils.YELLOW+"Cancelled."+Utils.RESET);Utils.pause(sc);return;} if (choice >= 1 && choice <= providers.size()) selectedProvider = providers.get(choice - 1); else {System.out.println(Utils.RED+"Invalid."+Utils.RESET);System.out.print(Utils.WHITE_BOLD + "Choice: " + Utils.RESET);} } catch (InputMismatchException e) {System.out.println(Utils.RED+"Invalid."+Utils.RESET);sc.nextLine();System.out.print(Utils.WHITE_BOLD + "Choice: " + Utils.RESET);}}
        } else { System.out.println(Utils.YELLOW + "No specific services listed." + Utils.RESET); }
        if(selectedProvider == null && (providers != null && !providers.isEmpty())) return; // If provider selection was cancelled

        if (planes.isEmpty()) { System.out.println(Utils.RED + "No planes configured." + Utils.RESET); Utils.pause(sc); return; }
        PlaneBooking bookingManagerPlane = planes.get(0); // Use first plane manager
        bookingManagerPlane.book(sc, loggedInUser, startCity, destCity, finalRoutePrice, seatClass, travelDate, selectedProvider);
        Utils.pause(sc);
    }

    /** Handles Train booking flow. */
    private void selectAndBookTrain(Scanner sc) { /* ... Similar to Plane, use TrainBooking and Train-specific classes ... */
        Utils.clearScreen(); Object[] selectedRoute = selectValidRoute(sc, "Train"); if (selectedRoute == null) { System.out.println(Utils.YELLOW + "Cancelled." + Utils.RESET); return; }
        String sC = (String)selectedRoute[0]; String dC = (String)selectedRoute[1]; RouteDetail rd = (RouteDetail)selectedRoute[2]; int dist = rd.distance(); String eta = rd.eta(); List<String> provs = rd.providers(); double bPrice = Utils.calculatePrice("Train", dist); if (bPrice<0||(bPrice==0&&dist>0)) {System.out.println(Utils.RED+"Price error."+Utils.RESET);Utils.pause(sc);return;}
        Utils.printBanner(sC+" -> "+dC); System.out.println(Utils.BLUE_BOLD+"Dist: "+Utils.MAGENTA_BOLD+dist+"km"+Utils.RESET); System.out.println(Utils.BLUE_BOLD+"ETA: "+Utils.GREEN_BOLD+eta+Utils.RESET);System.out.println();
        String tDate = Utils.getValidTravelDate(sc); if(tDate==null){System.out.println(Utils.YELLOW+"Cancelled."+Utils.RESET);Utils.pause(sc);return;}
        double cMult=1.0; String sClass=""; while(sClass.isEmpty()){System.out.println("\n"+Utils.CYAN_BOLD+"Class:"+Utils.RESET); System.out.printf(Utils.YELLOW_BOLD+"1."+Utils.RESET+Utils.CYAN+" 1A (Rs.%.2f)\n",bPrice*2.5);System.out.printf(Utils.YELLOW_BOLD+"2."+Utils.RESET+Utils.CYAN+" 2A (Rs.%.2f)\n",bPrice*2.0);System.out.printf(Utils.YELLOW_BOLD+"3."+Utils.RESET+Utils.CYAN+" 3A (Rs.%.2f)\n",bPrice*1.5);System.out.printf(Utils.YELLOW_BOLD+"4."+Utils.RESET+Utils.CYAN+" SL (Rs.%.2f)\n",bPrice*1.0);System.out.printf(Utils.YELLOW_BOLD+"5."+Utils.RESET+Utils.CYAN+" CC (Rs.%.2f)\n",bPrice*0.8);System.out.printf(Utils.YELLOW_BOLD+"6."+Utils.RESET+Utils.CYAN+" 2S (Rs.%.2f)\n",bPrice*0.5);System.out.println(Utils.YELLOW_BOLD+"0."+Utils.RESET+Utils.CYAN+" Back"+Utils.RESET);System.out.print(Utils.WHITE_BOLD+"Choice: "+Utils.RESET);try{int ch=sc.nextInt();sc.nextLine();switch(ch){case 1:sClass="AC First Class (1A)";cMult=2.5;break;case 2:sClass="Second AC (2A)";cMult=2.0;break;case 3:sClass="Third AC (3A)";cMult=1.5;break;case 4:sClass="Sleeper Class (SL)";cMult=1.0;break;case 5:sClass="Chair Car (CC)";cMult=0.8;break;case 6:sClass="Second Seater (2S)";cMult=0.5;break;case 0:System.out.println(Utils.YELLOW+"Cancelled."+Utils.RESET);Utils.pause(sc);return;default:System.out.println(Utils.RED+"Invalid."+Utils.RESET);Utils.pause(sc);}}catch(Exception e){System.out.println(Utils.RED+"Invalid."+Utils.RESET);sc.nextLine();Utils.pause(sc);}}if(sClass.isEmpty())return; double fPrice = bPrice*cMult;
        String selProv="N/A"; if(provs!=null&&!provs.isEmpty()){/*Provider selection*/Utils.printBanner("Select Train/Service");System.out.println(Utils.YELLOW_BOLD+"0."+Utils.RESET+Utils.CYAN+" Back"+Utils.RESET);for(int i=0;i<provs.size();i++)System.out.println(Utils.YELLOW_BOLD+(i+1)+"."+Utils.RESET+Utils.CYAN+" "+provs.get(i)+Utils.RESET);System.out.print(Utils.WHITE_BOLD+"Choice: "+Utils.RESET);selProv=null;while(selProv==null){try{int ch=sc.nextInt();sc.nextLine();if(ch==0){System.out.println(Utils.YELLOW+"Cancelled."+Utils.RESET);Utils.pause(sc);return;}if(ch>=1&&ch<=provs.size())selProv=provs.get(ch-1);else{System.out.println(Utils.RED+"Invalid."+Utils.RESET);System.out.print(Utils.WHITE_BOLD+"Choice: "+Utils.RESET);}}catch(Exception e){System.out.println(Utils.RED+"Invalid."+Utils.RESET);sc.nextLine();System.out.print(Utils.WHITE_BOLD+"Choice: "+Utils.RESET);}}}else{System.out.println(Utils.YELLOW+"No specific trains."+Utils.RESET);}if(selProv==null&&(provs!=null&&!provs.isEmpty()))return;
        if(trains.isEmpty()){System.out.println(Utils.RED+"No trains."+Utils.RESET);Utils.pause(sc);return;} TrainBooking bmTrain=trains.get(0); bmTrain.book(sc,loggedInUser,sC,dC,fPrice,sClass,tDate,selProv); Utils.pause(sc);
    }

    /** Handles Bus booking flow. */
    private void selectAndBookBus(Scanner sc) { /* ... Similar to Plane/Train, use BusBooking and Standard class ... */
        Utils.clearScreen(); Object[] selectedRoute = selectValidRoute(sc, "Bus"); if (selectedRoute == null) { System.out.println(Utils.YELLOW + "Cancelled." + Utils.RESET); return; }
        String sC = (String)selectedRoute[0]; String dC = (String)selectedRoute[1]; RouteDetail rd = (RouteDetail)selectedRoute[2]; int dist = rd.distance(); String eta = rd.eta(); List<String> provs = rd.providers(); double rPrice = Utils.calculatePrice("Bus", dist); if (rPrice<0||(rPrice==0&&dist>0)) {System.out.println(Utils.RED+"Price error."+Utils.RESET);Utils.pause(sc);return;}
        Utils.printBanner(sC+" -> "+dC); System.out.println(Utils.BLUE_BOLD+"Dist: "+Utils.MAGENTA_BOLD+dist+"km"+Utils.RESET); System.out.println(Utils.BLUE_BOLD+"ETA: "+Utils.GREEN_BOLD+eta+Utils.RESET);System.out.println();
        String tDate = Utils.getValidTravelDate(sc); if(tDate==null){System.out.println(Utils.YELLOW+"Cancelled."+Utils.RESET);Utils.pause(sc);return;}
        String sClass="Standard"; System.out.println("\n"+Utils.CYAN_BOLD+"Class:"+Utils.CYAN+" Standard"+Utils.RESET); System.out.printf(Utils.BLUE_BOLD+"Price: "+Utils.GREEN_BOLD+"Rs. %.2f"+Utils.RESET+"\n",rPrice);
        String selProv="N/A"; if(provs!=null&&!provs.isEmpty()){/*Provider selection*/Utils.printBanner("Select Bus Operator");System.out.println(Utils.YELLOW_BOLD+"0."+Utils.RESET+Utils.CYAN+" Back"+Utils.RESET);for(int i=0;i<provs.size();i++)System.out.println(Utils.YELLOW_BOLD+(i+1)+"."+Utils.RESET+Utils.CYAN+" "+provs.get(i)+Utils.RESET);System.out.print(Utils.WHITE_BOLD+"Choice: "+Utils.RESET);selProv=null;while(selProv==null){try{int ch=sc.nextInt();sc.nextLine();if(ch==0){System.out.println(Utils.YELLOW+"Cancelled."+Utils.RESET);Utils.pause(sc);return;}if(ch>=1&&ch<=provs.size())selProv=provs.get(ch-1);else{System.out.println(Utils.RED+"Invalid."+Utils.RESET);System.out.print(Utils.WHITE_BOLD+"Choice: "+Utils.RESET);}}catch(Exception e){System.out.println(Utils.RED+"Invalid."+Utils.RESET);sc.nextLine();System.out.print(Utils.WHITE_BOLD+"Choice: "+Utils.RESET);}}}else{System.out.println(Utils.YELLOW+"No specific operators."+Utils.RESET);}if(selProv==null&&(provs!=null&&!provs.isEmpty()))return;
        if(buses.isEmpty()){System.out.println(Utils.RED+"No buses."+Utils.RESET);Utils.pause(sc);return;} BusBooking bmBus=buses.get(0); bmBus.book(sc,loggedInUser,sC,dC,rPrice,sClass,tDate,selProv); Utils.pause(sc);
    }

    /** Allows user to modify the travel date of an existing booking. */
    private void modifyBookingDate(Scanner sc) {
        Utils.clearScreen(); Utils.printBanner("Modify Booking Travel Date");
        System.out.print(Utils.WHITE_BOLD + "Enter Booking ID to modify: " + Utils.RESET);
        String bookingIdInput = sc.nextLine().toUpperCase();

        Object bookingToModify = null; // Will hold the actual Booking object
        String currentTravelDate = ""; String vehicleTypeLabel = "";
        Object bookingManager = null; // Will hold PlaneBooking, TrainBooking, or BusBooking object

        // Search in Planes
        for (PlaneBooking plane : planes) { if (plane.getBookings().containsKey(bookingIdInput)) { PlaneBooking.Booking b = (PlaneBooking.Booking)plane.getBookings().get(bookingIdInput); if(b.getUsername().equals(loggedInUser)){ bookingToModify=b; currentTravelDate=b.getTravelDate(); vehicleTypeLabel="Plane"; bookingManager=plane; break;}}}
        // Search in Trains if not found
        if (bookingToModify == null) { for (TrainBooking train : trains) { if (train.getBookings().containsKey(bookingIdInput)) { TrainBooking.Booking b = (TrainBooking.Booking)train.getBookings().get(bookingIdInput); if(b.getUsername().equals(loggedInUser)){ bookingToModify=b; currentTravelDate=b.getTravelDate(); vehicleTypeLabel="Train"; bookingManager=train; break;}}}}
        // Search in Buses if not found
        if (bookingToModify == null) { for (BusBooking bus : buses) { if (bus.getBookings().containsKey(bookingIdInput)) { BusBooking.Booking b = (BusBooking.Booking)bus.getBookings().get(bookingIdInput); if(b.getUsername().equals(loggedInUser)){ bookingToModify=b; currentTravelDate=b.getTravelDate(); vehicleTypeLabel="Bus"; bookingManager=bus; break;}}}}

        if (bookingToModify != null) {
            try {
                String provider = (String) bookingToModify.getClass().getMethod("getProvider").invoke(bookingToModify);
                String startC = (String) bookingToModify.getClass().getMethod("getStartCity").invoke(bookingToModify);
                String destC = (String) bookingToModify.getClass().getMethod("getDestCity").invoke(bookingToModify);
                Seat seat = (Seat) bookingToModify.getClass().getMethod("getSeat").invoke(bookingToModify);

                System.out.println(Utils.CYAN + "\n--- Current Booking ---" + Utils.RESET);
                System.out.println(Utils.BLUE_BOLD + "Booking ID: " + Utils.YELLOW_BOLD + bookingIdInput + Utils.RESET);
                System.out.println(Utils.BLUE_BOLD + "Service:    " + Utils.CYAN + provider + Utils.RESET);
                System.out.println(Utils.BLUE_BOLD + "Route:      " + Utils.CYAN + startC + " -> " + destC + Utils.RESET);
                System.out.println(Utils.BLUE_BOLD + "Seat:       " + Utils.YELLOW_BOLD + seat.getSeatId() + Utils.RESET);
                System.out.println(Utils.BLUE_BOLD + "Current Date: " + Utils.MAGENTA_BOLD + currentTravelDate + Utils.RESET);

                System.out.println(Utils.YELLOW_BOLD + "\nEnter new travel date (or type 'cancel'):" + Utils.RESET);
                String newDate = Utils.getValidTravelDate(sc);

                if (newDate == null || newDate.equalsIgnoreCase("cancel")) { System.out.println(Utils.YELLOW + "Modification cancelled." + Utils.RESET); }
                else if (newDate.equals(currentTravelDate)) { System.out.println(Utils.YELLOW + "New date is same as current. No changes." + Utils.RESET); }
                else {
                    // Use reflection to call setTravelDate on the specific Booking object type
                    bookingToModify.getClass().getMethod("setTravelDate", String.class).invoke(bookingToModify, newDate);
                    System.out.println(Utils.GREEN_BOLD + "\nTravel date for Booking " + bookingIdInput + " updated to " + newDate + "." + Utils.RESET);
                }
            } catch (Exception e) { System.out.println(Utils.RED + "Error displaying/modifying booking: " + e.getMessage() + Utils.RESET); e.printStackTrace(); }
        } else { System.out.println(Utils.RED + "Booking ID '" + bookingIdInput + "' not found or not yours." + Utils.RESET); }
        Utils.pause(sc);
    }

    /** Displays current user's bookings. */
    private void viewBookings(Scanner sc) { /* ... (as before, styled) ... */
        Utils.clearScreen(); Utils.printBanner("Your Bookings for " + loggedInUser); boolean found=false;
        for(PlaneBooking p:planes) if(p.displayUserBookings(loggedInUser)) found=true;
        for(TrainBooking t:trains) if(t.displayUserBookings(loggedInUser)) found=true;
        for(BusBooking b:buses) if(b.displayUserBookings(loggedInUser)) found=true;
        if(!found) System.out.println(Utils.YELLOW+"\nNo active bookings."+Utils.RESET); Utils.pause(sc);
    }

    /** Handles cancellation of user's own booking. */
    private void cancelBooking(Scanner sc) { /* ... (as before, styled) ... */
        Utils.clearScreen(); Utils.printBanner("Cancel Booking"); System.out.print(Utils.WHITE_BOLD + "Enter Booking ID: " + Utils.RESET); String bId = sc.nextLine().toUpperCase(); boolean done = false;
        if(bId.startsWith("P")){for(PlaneBooking p:planes)if(p.cancelBooking(bId,loggedInUser)){done=true;break;}}
        else if(bId.startsWith("T")){for(TrainBooking t:trains)if(t.cancelBooking(bId,loggedInUser)){done=true;break;}}
        else if(bId.startsWith("B")){for(BusBooking b:buses)if(b.cancelBooking(bId,loggedInUser)){done=true;break;}}
        else{System.out.println(Utils.RED+"Invalid ID format."+Utils.RESET);Utils.pause(sc);return;}
        if(done){System.out.println(Utils.GREEN_BOLD+"\nBooking "+bId+" cancelled."+Utils.RESET);System.out.println(Utils.GREY+"(Changes saved)"+Utils.RESET);}
        else{System.out.println(Utils.RED+"\nBooking "+bId+" not found or not yours."+Utils.RESET);} Utils.pause(sc);
    }

} // End of BookingSystem class