package TicketBookingSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List; // Required import
import java.util.Map;
import java.util.Scanner;
// Removed Random and Collectors imports if no longer used

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

        // Initialize vehicle lists
        planes = new ArrayList<>();
        trains = new ArrayList<>();
        buses = new ArrayList<>();

        // Add placeholder vehicle instances (can be enhanced later)
        // Consider more descriptive IDs
        planes.add(new PlaneBooking("FL-A320-1", this));
        planes.add(new PlaneBooking("FL-B737-1", this));
        trains.add(new TrainBooking("TR-EXP-1", this));
        trains.add(new TrainBooking("TR-RAJ-1", this));
        buses.add(new BusBooking("BS-VOLVO-1", this));
        buses.add(new BusBooking("BS-SLEEPER-1", this));

        // Load existing bookings from storage
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
        // Create a temporary list to hold all booking maps
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
        System.out.println(Utils.GREY + "Booking ID counter initialized to: " + this.bookingIdCounter + Utils.RESET); // Debug message
    }

    /**
     * Sets the username of the currently logged-in user.
     * @param username The username.
     */
    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
    }

    /**
     * Generates the next unique booking ID with the given prefix.
     * @param prefix The prefix ("P", "T", or "B").
     * @return The generated booking ID string (e.g., "P101").
     */
    public int getNextBookingId(String prefix) {
        // Simple incrementing counter. In a real system, might need more robust generation.
        return bookingIdCounter++;
    }

    // --- Getters for vehicle lists (needed for saving data in Main) ---
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
            Utils.printBanner(loggedInUser + "'s Booking Menu"); // Personalized banner
            // Styled Menu Options
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Book a Ticket" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " View My Bookings" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " Cancel a Booking" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "4." + Utils.RESET + Utils.CYAN + " Logout" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Choose an option: " + Utils.RESET);

            int choice = 0;
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
                    bookTicket(sc); // This will now use the route data
                    // Save bookings after a potential booking attempt or cancellation within bookTicket flow
                    StorageManager.saveBookings(planes, trains, buses);
                    break;
                case 2:
                    viewBookings(sc);
                    break;
                case 3:
                    cancelBooking(sc);
                    // Save bookings after a potential cancellation
                    StorageManager.saveBookings(planes, trains, buses);
                    break;
                case 4:
                    System.out.println(Utils.GREEN + "\nLogging out..." + Utils.RESET);
                    // Saving should ideally happen in Main after this returns, or before returning
                    // StorageManager.saveBookings(planes, trains, buses); // Optional save here
                    return; // Exit the booking menu loop
                default:
                    System.out.println(Utils.RED + "Invalid option. Try again." + Utils.RESET);
                    Utils.pause(sc); // Pause only needed for invalid option
            }
            // Removed pause from end of loop - handled within methods/cases
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
            // Styled Menu Options
            System.out.println(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Plane" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Train" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " Bus" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back to Booking Menu" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Enter choice: " + Utils.RESET);

            int transportChoice = 0;
            try {
                transportChoice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
                continue; // Loop back to ask for transport type
            } catch (Exception e) {
                System.out.println(Utils.RED + "An error occurred: " + e.getMessage() + Utils.RESET);
                sc.nextLine();
                Utils.pause(sc);
                continue;
            }

            // Call the specific booking method or return
            switch (transportChoice) {
                case 1:
                    selectAndBookPlane(sc);
                    return; // Return to booking menu after attempting plane booking
                case 2:
                    selectAndBookTrain(sc);
                    return; // Return after attempting train booking
                case 3:
                    selectAndBookBus(sc);
                    return; // Return after attempting bus booking
                case 0:
                    return; // Go back to the previous menu
                default:
                    System.out.println(Utils.RED + "Invalid option." + Utils.RESET);
                    Utils.pause(sc); // Pause only for invalid option
            }
        }
    }


    /**
     * Prompts the user to select a valid route (origin and destination)
     * using data loaded from files for a specific transport type.
     * Uses colors for better visual guidance.
     *
     * @param sc Scanner for input.
     * @param transportType "Plane", "Train", or "Bus".
     * @return An Object array [String originCityName, String destinationCityName, RouteDetail routeDetail]
     * or null if the user chooses to go back or an error occurs.
     */
    private Object[] selectValidRoute(Scanner sc, String transportType) {
        Map<String, LocationInfo> locationData;
        String locationTypeName;

        // Determine which data map and name to use
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
            default:
                System.err.println(Utils.RED_BOLD + "Internal Error: Invalid transport type specified." + Utils.RESET);
                return null;
        }

        // Check if data was loaded
        if (locationData == null || locationData.isEmpty()) {
            System.out.println(Utils.RED + "Error: No " + locationTypeName + " data loaded. Cannot proceed." + Utils.RESET);
            Utils.pause(sc);
            return null;
        }

        // Get and sort origin city keys (UPPERCASE)
        List<String> originCityKeys = new ArrayList<>(locationData.keySet());
        Collections.sort(originCityKeys);

        // --- Select Origin City ---
        LocationInfo selectedOriginInfo = null;
        while (selectedOriginInfo == null) {
            Utils.clearScreen();
            Utils.printBanner("Select Origin " + locationTypeName);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            // Display numbered list of origin cities
            for (int i = 0; i < originCityKeys.size(); i++) {
                // Retrieve LocationInfo using the key
                LocationInfo info = locationData.get(originCityKeys.get(i));
                // Display City Name (proper case) and Primary Station Name
                System.out.printf(Utils.YELLOW_BOLD + "%d." + Utils.RESET + Utils.CYAN + " %s (%s)" + Utils.RESET + "\n",
                        i + 1, info.city(), info.primaryName());
            }
            System.out.print(Utils.WHITE_BOLD + "Choose origin city number: " + Utils.RESET);

            try {
                int originChoice = sc.nextInt();
                sc.nextLine(); // Consume newline

                if (originChoice == 0) return null; // User chose back

                // Validate choice range
                if (originChoice >= 1 && originChoice <= originCityKeys.size()) {
                    // Get the selected LocationInfo using the key from the sorted list
                    selectedOriginInfo = locationData.get(originCityKeys.get(originChoice - 1));
                    // Check if the selected origin has any routes defined
                    if (selectedOriginInfo.routes() == null || selectedOriginInfo.routes().isEmpty()) {
                        System.out.println(Utils.YELLOW + "No routes available from " + selectedOriginInfo.city() + ". Please choose a different origin." + Utils.RESET);
                        selectedOriginInfo = null; // Reset to loop again
                        Utils.pause(sc);
                    }
                    // If routes exist, loop terminates as selectedOriginInfo is no longer null
                } else {
                    System.out.println(Utils.RED + "Invalid choice. Please enter a number from the list." + Utils.RESET);
                    Utils.pause(sc);
                }
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            } catch (Exception e) { // Catch other potential errors
                System.out.println(Utils.RED + "An error occurred during origin selection: " + e.getMessage() + Utils.RESET);
                sc.nextLine(); // Consume potentially leftover input
                Utils.pause(sc);
            }
        } // End origin selection loop

        // --- Select Destination City ---
        Map<String, RouteDetail> availableRoutes = selectedOriginInfo.routes();
        // Get destination keys (UPPERCASE city names) and sort them
        List<String> destinationKeys = new ArrayList<>(availableRoutes.keySet());
        Collections.sort(destinationKeys);

        RouteDetail selectedRouteDetail = null;
        // Loop until a valid destination is selected
        while (selectedRouteDetail == null) {
            Utils.clearScreen();
            Utils.printBanner("Select Destination from " + selectedOriginInfo.city());
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back to Origin Selection" + Utils.RESET);

            int validRouteCount = 0; // Counter for actually displayable routes
            List<String> displayOrderDestKeys = new ArrayList<>(); // Store keys in the order they are displayed

            // Iterate through sorted destination keys
            for (String destKey : destinationKeys) {
                RouteDetail route = availableRoutes.get(destKey);
                // Check if route is valid/available for this transport mode
                boolean isAvailable = route.distance() > -1 || !route.eta().equalsIgnoreCase("Not Available");

                if (isAvailable) {
                    validRouteCount++;
                    displayOrderDestKeys.add(destKey); // Add key to list for later retrieval by index
                    String destCityName = route.destinationCity(); // Use proper case name from RouteDetail
                    String eta = route.eta();
                    int distance = route.distance();
                    // Display formatted route option with colors
                    System.out.printf(Utils.YELLOW_BOLD + "%d." + Utils.RESET + Utils.CYAN + " %s (" + Utils.BLUE_BOLD + "Dist:" + Utils.MAGENTA_BOLD + " %d km" + Utils.CYAN + ", ETA: " + Utils.GREEN_BOLD + "%s" + Utils.CYAN + ")" + Utils.RESET + "\n",
                            validRouteCount, destCityName, distance, eta);
                }
            }

            // Handle case where no valid destinations were found after filtering
            if (validRouteCount == 0) {
                System.out.println(Utils.YELLOW + "No valid destinations found from " + selectedOriginInfo.city() + " for " + transportType + "s." + Utils.RESET);
                Utils.pause(sc);
                // Returning null might force user back too far, let's retry origin selection
                // return null;
                return selectValidRoute(sc, transportType); // Go back to origin selection
            }

            System.out.print(Utils.WHITE_BOLD + "Choose destination city number: " + Utils.RESET);

            try {
                int destChoice = sc.nextInt();
                sc.nextLine(); // Consume newline

                if (destChoice == 0) return selectValidRoute(sc, transportType); // Go back to origin selection

                // Validate choice based on the number of *displayed* routes
                if (destChoice >= 1 && destChoice <= validRouteCount) {
                    // Get the actual destination key using the display index
                    String selectedDestCityKey = displayOrderDestKeys.get(destChoice - 1);
                    // Retrieve the corresponding RouteDetail using the key
                    selectedRouteDetail = availableRoutes.get(selectedDestCityKey);
                    // If successful, loop condition becomes false, loop terminates
                } else {
                    System.out.println(Utils.RED + "Invalid choice. Please enter a number from the list." + Utils.RESET);
                    Utils.pause(sc);
                }
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            } catch (Exception e) {
                System.out.println(Utils.RED + "An error occurred during destination selection: " + e.getMessage() + Utils.RESET);
                sc.nextLine();
                Utils.pause(sc);
            }
        } // End destination selection loop

        // Return origin city name (proper case), destination city name (proper case), and the route details
        return new Object[]{selectedOriginInfo.city(), selectedRouteDetail.destinationCity(), selectedRouteDetail};
    }


    // --- Updated Booking Methods ---

    /**
     * Handles the process for selecting and booking a plane ticket.
     * Uses RouteDataManager for route selection and displays provider info.
     * @param sc Scanner object for user input.
     */
    private void selectAndBookPlane(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Plane");

        if (selectedRoute == null) {
            System.out.println(Utils.YELLOW + "Route selection cancelled." + Utils.RESET);
            // Don't pause here, let the calling menu handle flow
            return; // User chose back
        }

        // Extract route details
        String startCity = (String) selectedRoute[0];
        String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance();
        String eta = routeDetail.eta();
        List<String> providers = routeDetail.providers(); // Get providers for this specific route

        // Calculate base price using actual distance
        double basePrice = Utils.calculatePrice("Plane", distance);
        // Basic check for valid price calculation (could be 0 if distance is 0)
        if (basePrice < 0 || (basePrice == 0 && distance > 0)) {
            System.out.println(Utils.RED + "Error calculating price for the selected route ("+distance+" km)." + Utils.RESET);
            Utils.pause(sc);
            return;
        }

        // Display selected route info with styling
        Utils.printBanner("Selected Route: " + startCity + " -> " + destCity);
        System.out.println(Utils.BLUE_BOLD + "Distance:           " + Utils.MAGENTA_BOLD + distance + " km" + Utils.RESET);
        System.out.println(Utils.BLUE_BOLD + "Est. Travel Time:   " + Utils.GREEN_BOLD + eta + Utils.RESET);

        // Display Providers found in the data file for this route
        if (providers != null && !providers.isEmpty()) {
            System.out.println(Utils.BLUE_BOLD + "Airlines/Services: " + Utils.MAGENTA + String.join(", ", providers) + Utils.RESET);
        } else {
            System.out.println(Utils.YELLOW + "No specific provider information listed for this route." + Utils.RESET);
        }
        System.out.println(); // Add a blank line for spacing

        // Get Valid Travel Date using Util method
        String travelDate = Utils.getValidTravelDate(sc);
        if (travelDate == null) { // Handle if user entered 'back'
            System.out.println(Utils.YELLOW + "\nBooking cancelled during date selection." + Utils.RESET);
            Utils.pause(sc);
            return;
        }

        // Select Class
        double classMultiplier = 1.0;
        String seatClass = "";
        while (seatClass.isEmpty()) {
            System.out.println("\n" + Utils.CYAN_BOLD + "Choose Seat Class:" + Utils.RESET);
            // Display class options with calculated prices
            System.out.printf(Utils.YELLOW_BOLD + "1." + Utils.RESET + Utils.CYAN + " Economy (Base Price: Rs. %.2f)" + Utils.RESET + "\n", basePrice * 1.0);
            System.out.printf(Utils.YELLOW_BOLD + "2." + Utils.RESET + Utils.CYAN + " Business (Base Price: Rs. %.2f)" + Utils.RESET + "\n", basePrice * 2.0);
            System.out.printf(Utils.YELLOW_BOLD + "3." + Utils.RESET + Utils.CYAN + " First (Base Price: Rs. %.2f)" + Utils.RESET + "\n", basePrice * 3.0);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            System.out.print(Utils.WHITE_BOLD + "Enter choice: " + Utils.RESET);
            try {
                int classChoice = sc.nextInt();
                sc.nextLine(); // Consume newline
                switch (classChoice) {
                    case 1: seatClass = "Economy"; classMultiplier = 1.0; break;
                    case 2: seatClass = "Business"; classMultiplier = 2.0; break;
                    case 3: seatClass = "First"; classMultiplier = 3.0; break;
                    case 0: System.out.println(Utils.YELLOW + "Booking cancelled." + Utils.RESET); Utils.pause(sc); return; // Go back
                    default: System.out.println(Utils.RED + "Invalid class choice." + Utils.RESET); Utils.pause(sc); continue; // Ask again
                }
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            }
        }
        double finalRoutePrice = basePrice * classMultiplier; // Final price adjusted for class


        // Select Plane Instance (Placeholder selection)
        if (planes.isEmpty()) {
            System.out.println(Utils.RED + "Sorry, no planes are currently configured in the system." + Utils.RESET);
            Utils.pause(sc);
            return;
        }

        PlaneBooking selectedPlane = null;
        while(selectedPlane == null) {
            Utils.printBanner("Select Specific Plane/Aircraft");
            System.out.println(Utils.YELLOW + "(Note: Specific airline services shown above for this route)" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            for (int i = 0; i < planes.size(); i++) {
                // Display available placeholder aircraft IDs
                System.out.println(Utils.YELLOW_BOLD + (i + 1) + "." + Utils.RESET + Utils.CYAN + " Aircraft ID: " + planes.get(i).getFlightId() + Utils.RESET);
            }
            System.out.print(Utils.WHITE_BOLD + "Enter aircraft number to book on: " + Utils.RESET);
            try {
                int flightChoice = sc.nextInt();
                sc.nextLine(); // Consume newline
                if (flightChoice == 0) { System.out.println(Utils.YELLOW+"Booking cancelled."+Utils.RESET); Utils.pause(sc); return; } // Go back
                if (flightChoice >= 1 && flightChoice <= planes.size()) {
                    selectedPlane = planes.get(flightChoice - 1); // Select the chosen placeholder
                } else {
                    System.out.println(Utils.RED + "Invalid aircraft selection." + Utils.RESET);
                    Utils.pause(sc);
                }
            } catch (InputMismatchException e) {
                System.out.println(Utils.RED + "Invalid input. Please enter a number." + Utils.RESET);
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            }
        }

        // Call the book method of the selected PlaneBooking instance
        // This method now includes passenger input validation
        selectedPlane.book(sc, loggedInUser, startCity, destCity, finalRoutePrice, seatClass, travelDate);
        // Pause is handled within the PlaneBooking.book or after returning here if needed
        Utils.pause(sc); // Pause after the booking process completes or fails in book()

    }

    /**
     * Handles the process for selecting and booking a train ticket.
     * Uses RouteDataManager, displays providers, uses validation.
     * @param sc Scanner object for user input.
     */
    private void selectAndBookTrain(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Train");

        if (selectedRoute == null) { System.out.println(Utils.YELLOW + "Route selection cancelled." + Utils.RESET); return; }

        String startCity = (String) selectedRoute[0];
        String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance();
        String eta = routeDetail.eta();
        List<String> providers = routeDetail.providers();

        double basePrice = Utils.calculatePrice("Train", distance);
        if (basePrice < 0 || (basePrice == 0 && distance > 0)) { System.out.println(Utils.RED + "Error calculating price." + Utils.RESET); Utils.pause(sc); return; }

        // Display route and provider info
        Utils.printBanner("Selected Route: " + startCity + " -> " + destCity);
        System.out.println(Utils.BLUE_BOLD + "Distance:           " + Utils.MAGENTA_BOLD + distance + " km" + Utils.RESET);
        System.out.println(Utils.BLUE_BOLD + "Est. Travel Time:   " + Utils.GREEN_BOLD + eta + Utils.RESET);
        if (providers != null && !providers.isEmpty()) {
            System.out.println(Utils.BLUE_BOLD + "Trains/Services:   " + Utils.MAGENTA + String.join(", ", providers) + Utils.RESET);
        } else { System.out.println(Utils.YELLOW + "No specific provider info listed." + Utils.RESET); }
        System.out.println();

        // Get Travel Date
        String travelDate = Utils.getValidTravelDate(sc);
        if (travelDate == null) { System.out.println(Utils.YELLOW + "\nBooking cancelled." + Utils.RESET); Utils.pause(sc); return; }

        // Select Class
        double classMultiplier = 1.0;
        String seatClass = "";
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
                    case 0: System.out.println(Utils.YELLOW + "Booking cancelled." + Utils.RESET); Utils.pause(sc); return;
                    default: System.out.println(Utils.RED + "Invalid class choice." + Utils.RESET); Utils.pause(sc); continue;
                }
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input..." + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
        }
        double finalRoutePrice = basePrice * classMultiplier;

        // Select Train Instance
        if (trains.isEmpty()) { System.out.println(Utils.RED + "No trains configured." + Utils.RESET); Utils.pause(sc); return; }
        TrainBooking selectedTrain = null;
        while(selectedTrain == null) {
            Utils.printBanner("Select Specific Train");
            System.out.println(Utils.YELLOW + "(Note: Specific train services shown above)" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            for (int i = 0; i < trains.size(); i++) {
                System.out.println(Utils.YELLOW_BOLD + (i + 1) + "." + Utils.RESET + Utils.CYAN + " Train ID: " + trains.get(i).getTrainId() + Utils.RESET);
            }
            System.out.print(Utils.WHITE_BOLD + "Enter train number to book on: " + Utils.RESET);
            try {
                int trainChoice = sc.nextInt();
                sc.nextLine();
                if (trainChoice == 0) { System.out.println(Utils.YELLOW+"Booking cancelled."+Utils.RESET); Utils.pause(sc); return; }
                if (trainChoice >= 1 && trainChoice <= trains.size()) {
                    selectedTrain = trains.get(trainChoice - 1);
                } else { System.out.println(Utils.RED + "Invalid train selection." + Utils.RESET); Utils.pause(sc); }
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input..." + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
        }

        // Call book method
        selectedTrain.book(sc, loggedInUser, startCity, destCity, finalRoutePrice, seatClass, travelDate);
        Utils.pause(sc);
    }

    /**
     * Handles the process for selecting and booking a bus ticket.
     * Uses RouteDataManager, displays providers, uses validation.
     * @param sc Scanner object for user input.
     */
    private void selectAndBookBus(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Bus");

        if (selectedRoute == null) { System.out.println(Utils.YELLOW + "Route selection cancelled." + Utils.RESET); return; }

        String startCity = (String) selectedRoute[0];
        String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance();
        String eta = routeDetail.eta();
        List<String> providers = routeDetail.providers();

        // Bus usually only has one class price
        double routePrice = Utils.calculatePrice("Bus", distance);
        if (routePrice < 0 || (routePrice == 0 && distance > 0)) { System.out.println(Utils.RED + "Error calculating price." + Utils.RESET); Utils.pause(sc); return; }

        // Display route and provider info
        Utils.printBanner("Selected Route: " + startCity + " -> " + destCity);
        System.out.println(Utils.BLUE_BOLD + "Distance:           " + Utils.MAGENTA_BOLD + distance + " km" + Utils.RESET);
        System.out.println(Utils.BLUE_BOLD + "Est. Travel Time:   " + Utils.GREEN_BOLD + eta + Utils.RESET);
        if (providers != null && !providers.isEmpty()) {
            System.out.println(Utils.BLUE_BOLD + "Bus Operators:     " + Utils.MAGENTA + String.join(", ", providers) + Utils.RESET);
        } else { System.out.println(Utils.YELLOW + "No specific provider info listed." + Utils.RESET); }
        System.out.println();


        // Get Travel Date
        String travelDate = Utils.getValidTravelDate(sc);
        if (travelDate == null) { System.out.println(Utils.YELLOW + "\nBooking cancelled." + Utils.RESET); Utils.pause(sc); return; }

        // Bus has only 'Standard' class
        String seatClass = "Standard";
        System.out.println("\n" + Utils.CYAN_BOLD + "Class:" + Utils.CYAN + " Standard" + Utils.RESET);
        System.out.printf(Utils.BLUE_BOLD + "Ticket Price: " + Utils.GREEN_BOLD + "Rs. %.2f" + Utils.RESET + "\n", routePrice);


        // Select Bus Instance
        if (buses.isEmpty()) { System.out.println(Utils.RED + "No buses configured." + Utils.RESET); Utils.pause(sc); return; }
        BusBooking selectedBus = null;
        while (selectedBus == null) {
            Utils.printBanner("Select Specific Bus");
            System.out.println(Utils.YELLOW + "(Note: Specific bus operators shown above)" + Utils.RESET);
            System.out.println(Utils.YELLOW_BOLD + "0." + Utils.RESET + Utils.CYAN + " Back" + Utils.RESET);
            for (int i = 0; i < buses.size(); i++) {
                System.out.println(Utils.YELLOW_BOLD + (i + 1) + "." + Utils.RESET + Utils.CYAN + " Bus ID: " + buses.get(i).getBusId() + Utils.RESET);
            }
            System.out.print(Utils.WHITE_BOLD + "Enter bus number to book on: " + Utils.RESET);
            try {
                int busChoice = sc.nextInt();
                sc.nextLine();
                if (busChoice == 0) { System.out.println(Utils.YELLOW+"Booking cancelled."+Utils.RESET); Utils.pause(sc); return; }
                if (busChoice >= 1 && busChoice <= buses.size()) {
                    selectedBus = buses.get(busChoice - 1);
                } else { System.out.println(Utils.RED + "Invalid bus selection." + Utils.RESET); Utils.pause(sc); }
            } catch (InputMismatchException e) { System.out.println(Utils.RED + "Invalid input..." + Utils.RESET); sc.nextLine(); Utils.pause(sc); }
        }

        // Call book method
        // Pass the base route price directly as final price for standard class
        selectedBus.book(sc, loggedInUser, startCity, destCity, routePrice, seatClass, travelDate);
        Utils.pause(sc);
    }


    /**
     * Displays all bookings for the currently logged-in user across all transport types.
     * @param sc Scanner object for pausing.
     */
    private void viewBookings(Scanner sc) {
        Utils.clearScreen();
        Utils.printBanner("Your Bookings for " + loggedInUser);
        boolean foundBookings = false;

        // Check planes, trains, and buses
        for (PlaneBooking plane : planes) {
            // displayUserBookings now returns boolean and handles its own printing
            if (plane.displayUserBookings(loggedInUser)) {
                foundBookings = true;
            }
        }
        for (TrainBooking train : trains) {
            if (train.displayUserBookings(loggedInUser)) {
                foundBookings = true;
            }
        }
        for (BusBooking bus : buses) {
            if (bus.displayUserBookings(loggedInUser)) {
                foundBookings = true;
            }
        }

        // Display message only if no bookings were found across all types
        if (!foundBookings) {
            System.out.println(Utils.YELLOW + "\nYou have no active bookings." + Utils.RESET);
        }
        Utils.pause(sc); // Pause after displaying all bookings or the message
    }


    /**
     * Handles the cancellation of a booking by ID.
     * @param sc Scanner object for user input.
     */
    private void cancelBooking(Scanner sc) {
        Utils.clearScreen();
        Utils.printBanner("Cancel Booking");
        System.out.print(Utils.WHITE_BOLD + "Enter Booking ID to cancel (e.g., P1, T1, B1): " + Utils.RESET);
        String bookingId = sc.nextLine().toUpperCase(); // Use uppercase for consistency
        boolean canceled = false;

        // *** REMINDER: StorageManager.removeBooking() was removed ***
        // Cancellation happens in memory first, saved later.

        // Determine type and attempt cancellation
        if (bookingId.startsWith("P")) {
            for (PlaneBooking plane : planes) {
                // cancelBooking now returns boolean
                if (plane.cancelBooking(bookingId, loggedInUser)) {
                    canceled = true;
                    break; // Exit loop once cancelled
                }
            }
        } else if (bookingId.startsWith("T")) {
            for (TrainBooking train : trains) {
                if (train.cancelBooking(bookingId, loggedInUser)) {
                    canceled = true;
                    break;
                }
            }
        } else if (bookingId.startsWith("B")) {
            for (BusBooking bus : buses) {
                if (bus.cancelBooking(bookingId, loggedInUser)) {
                    canceled = true;
                    break;
                }
            }
        } else {
            System.out.println(Utils.RED + "Invalid Booking ID format (must start with P, T, or B)." + Utils.RESET);
            Utils.pause(sc);
            return; // No need to proceed
        }

        // Display result message
        if (canceled) {
            System.out.println(Utils.GREEN_BOLD + "\nBooking " + bookingId + " cancelled successfully in memory." + Utils.RESET);
            System.out.println(Utils.GREY + "(Changes will be saved automatically on logout/exit)" + Utils.RESET);
            // IMPORTANT: Make sure StorageManager.saveBookings() is called reliably later
        } else {
            System.out.println(Utils.RED + "\nBooking ID " + bookingId + " not found or you are not authorized to cancel it." + Utils.RESET);
        }
        Utils.pause(sc); // Pause after attempt
    }

    // Removed calculateETA as ETA comes from RouteDataManager via RouteDetail
    // public static String calculateETA(...) { ... }

} // End of BookingSystem class