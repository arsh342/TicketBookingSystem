package TicketBookingSystem;

import java.util.ArrayList;
import java.util.Collections; // Import for sorting
import java.util.InputMismatchException; // Import for input validation
import java.util.List;
// Removed Random import as it's no longer needed for distance/price
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BookingSystem {
    private List<PlaneBooking> planes;
    private List<TrainBooking> trains;
    private List<BusBooking> buses;
    private String loggedInUser;
    private int bookingIdCounter = 1;
    // private static final Random random = new Random(); // No longer needed

    // *** Add RouteDataManager instance ***
    private final RouteDataManager routeDataManager;

    public BookingSystem() {
        // *** Initialize RouteDataManager ***
        // This loads airports.txt, bus_stations.txt, train_stations.txt
        routeDataManager = new RouteDataManager();

        planes = new ArrayList<>();
        trains = new ArrayList<>();
        buses = new ArrayList<>();

        // Add some default vehicles (Could be enhanced later based on data files)
        planes.add(new PlaneBooking("FL001", this));
        planes.add(new PlaneBooking("FL002", this));
        trains.add(new TrainBooking("TR001", this));
        trains.add(new TrainBooking("TR002", this));
        buses.add(new BusBooking("BS001", this));
        buses.add(new BusBooking("BS002", this));

        // Load existing bookings from bookings.txt AFTER vehicles are initialized
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
                    // Extract number part (P1 -> 1, T10 -> 10)
                    int idNum = Integer.parseInt(bookingId.substring(1));
                    if (idNum > maxId) {
                        maxId = idNum;
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    // Ignore malformed IDs during counter update
                    System.err.println("\033[0;90mWarning: Skipping potentially malformed booking ID '" + bookingId + "' during counter update.\033[0m");
                }
            }
        }
        this.bookingIdCounter = maxId + 1; // Start from next available ID
        System.out.println("\033[0;90mBooking ID counter initialized to: " + this.bookingIdCounter + "\033[0m"); // Optional debug message
    }


    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
    }

    // getNextBookingId remains the same
    public int getNextBookingId(String prefix) {
        return bookingIdCounter++;
    }

    // startBooking menu remains largely the same
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
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
                continue;
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline
                Utils.pause(sc);
                continue;
            }


            switch (choice) {
                case 1:
                    bookTicket(sc); // This will now use the new logic
                    // Save bookings after a potential booking attempt
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
                    System.out.println("\033[1;32mLogging out...\033[0m");
                    // Save bookings one last time before logout
                    StorageManager.saveBookings(planes, trains, buses);
                    return; // Exit the booking menu
                default:
                    System.out.println("\033[1;31mInvalid option. Try again.\033[0m");
                    Utils.pause(sc); // Pause only needed for invalid option
            }
            // Remove the pause here, pauses are handled within methods or on errors
        }
    }

    // bookTicket menu remains the same
    private void bookTicket(Scanner sc) {
        while (true) {
            Utils.clearScreen(); // Clear screen at the start of the loop
            Utils.printBanner("Select Mode of Transportation");
            System.out.println("\033[1;33m1.\033[0m Plane");
            System.out.println("\033[1;33m2.\033[0m Train");
            System.out.println("\033[1;33m3.\033[0m Bus");
            System.out.println("\033[1;33m0.\033[0m Back to Booking Menu"); // Changed option 0 meaning
            System.out.print("\033[1mEnter choice: \033[0m");
            int transportChoice = 0;
            try {
                transportChoice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
                continue; // Loop back to ask for transport type
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline
                Utils.pause(sc);
                continue;
            }


            switch (transportChoice) {
                case 1:
                    selectAndBookPlane(sc);
                    return; // Return after attempting plane booking
                case 2:
                    selectAndBookTrain(sc);
                    return; // Return after attempting train booking
                case 3:
                    selectAndBookBus(sc);
                    return; // Return after attempting bus booking
                case 0:
                    return; // Go back to the previous menu
                default:
                    System.out.println("\033[1;31mInvalid option.\033[0m");
                    Utils.pause(sc); // Pause only for invalid option
            }
        }
    }


    /**
     * Prompts the user to select a valid route (origin and destination)
     * using data loaded from files for a specific transport type.
     *
     * @param sc Scanner for input.
     * @param transportType "Plane", "Train", or "Bus".
     * @return An Object array [String originCity, String destinationCity, RouteDetail routeDetail]
     * or null if the user chooses to go back or if no valid route can be selected.
     */
    private Object[] selectValidRoute(Scanner sc, String transportType) {
        Map<String, LocationInfo> locationData;
        String locationTypeName;

        // Get the correct data map based on transport type
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
                System.out.println("\033[1;31mInternal Error: Invalid transport type specified.\033[0m");
                return null;
        }

        if (locationData == null || locationData.isEmpty()) {
            System.out.println("\033[1;31mError: No " + locationTypeName + " data loaded. Cannot proceed.\033[0m");
            Utils.pause(sc);
            return null;
        }

        List<String> originCityKeys = new ArrayList<>(locationData.keySet());
        Collections.sort(originCityKeys); // Sort city keys alphabetically

        // --- Select Origin City ---
        int originChoice = -1;
        LocationInfo selectedOriginInfo = null;
        String selectedOriginKey = null;

        while (selectedOriginInfo == null) {
            Utils.clearScreen(); // Clear screen for selection
            Utils.printBanner("Select Origin " + locationTypeName);
            System.out.println("\033[1;33m0.\033[0m Back");
            for (int i = 0; i < originCityKeys.size(); i++) {
                // Display City Name (and maybe primary station name)
                String cityKey = originCityKeys.get(i);
                LocationInfo info = locationData.get(cityKey);
                if (info != null) { // Check if info exists for the key
                    String cityName = info.city();
                    String primaryName = info.primaryName();
                    System.out.printf("\033[1;33m%d.\033[0m %s (%s)\n", i + 1, cityName, primaryName);
                } else {
                    // Should ideally not happen if keyset is derived from map
                    System.out.printf("\033[1;31m%d. Error: No data for key %s\033[0m\n", i + 1, cityKey);
                }
            }
            System.out.print("\033[1mChoose origin city number: \033[0m");

            try {
                originChoice = sc.nextInt();
                sc.nextLine(); // Consume newline

                if (originChoice == 0) return null; // User chose to go back

                if (originChoice >= 1 && originChoice <= originCityKeys.size()) {
                    selectedOriginKey = originCityKeys.get(originChoice - 1);
                    selectedOriginInfo = locationData.get(selectedOriginKey);

                    if (selectedOriginInfo == null) {
                        // Should not happen if keys match, but handle defensively
                        System.out.println("\033[1;31mError: Could not retrieve data for selected origin key.\033[0m");
                        Utils.pause(sc);
                        continue; // Loop back to ask again
                    }

                    // Check if there are any routes defined from this origin
                    if (selectedOriginInfo.routes() == null || selectedOriginInfo.routes().isEmpty()) {
                        System.out.println("\033[1;31mNo routes defined from " + selectedOriginInfo.city() + " in the data file. Please choose a different origin.\033[0m");
                        selectedOriginInfo = null; // Reset to loop again
                        Utils.pause(sc);
                    }
                } else {
                    System.out.println("\033[1;31mInvalid choice. Please enter a number from the list.\033[0m");
                    Utils.pause(sc);
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred selecting origin: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline
                Utils.pause(sc);
            }
        } // End of origin selection loop

        // --- Select Destination City ---
        Map<String, RouteDetail> availableRoutes = selectedOriginInfo.routes();
        List<String> destinationCityKeys = new ArrayList<>(availableRoutes.keySet());
        Collections.sort(destinationCityKeys); // Sort destination keys alphabetically

        int destChoice = -1;
        RouteDetail selectedRouteDetail = null;
        String selectedDestCityKey = null;

        // This loop continues until a valid destination is chosen or user goes back
        while (selectedRouteDetail == null) {
            Utils.clearScreen(); // Clear screen for selection
            Utils.printBanner("Select Destination from " + selectedOriginInfo.city());
            System.out.println("\033[1;33m0.\033[0m Back to Origin Selection");

            // Filter and display only *valid* destinations for the current transport mode
            List<String> validDestKeys = new ArrayList<>();
            List<String> validDestDisplayNames = new ArrayList<>();

            for (String destKey : destinationCityKeys) {
                RouteDetail route = availableRoutes.get(destKey);
                if (route == null) continue; // Skip if route detail is missing

                // A route is valid if its distance is not -1 (which signifies 'NoBus'/'NoTrain' etc.)
                boolean isAvailable = route.distance() != -1; // The specific "Not Available" eta is handled during display

                if (isAvailable) {
                    validDestKeys.add(destKey);
                    String destCityName = route.destinationCity();
                    String eta = route.eta();
                    int distance = route.distance();
                    validDestDisplayNames.add(String.format("%s (\033[1;33m%d km\033[0m, ETA: \033[1;32m%s\033[0m)",
                            destCityName, distance, eta));
                }
            }

            // Check if there are any valid destinations after filtering
            if (validDestKeys.isEmpty()) {
                System.out.println("\033[1;31mNo valid destinations found from " + selectedOriginInfo.city() + " for " + transportType + "s according to the data file.\033[0m");
                Utils.pause(sc);
                // Go back to origin selection automatically
                return selectValidRoute(sc, transportType);
            }

            // Display the filtered list
            for (int i = 0; i < validDestDisplayNames.size(); i++) {
                System.out.printf("\033[1;33m%d.\033[0m %s\n", i + 1, validDestDisplayNames.get(i));
            }

            System.out.print("\033[1mChoose destination city number: \033[0m");

            try {
                destChoice = sc.nextInt();
                sc.nextLine(); // Consume newline

                if (destChoice == 0) {
                    selectedOriginInfo = null; // Reset origin to force re-selection
                    return selectValidRoute(sc, transportType); // Recursive call to go back to origin selection
                }

                // Use the size of the filtered list for validation
                if (destChoice >= 1 && destChoice <= validDestKeys.size()) {
                    selectedDestCityKey = validDestKeys.get(destChoice - 1); // Get key from the filtered list
                    selectedRouteDetail = availableRoutes.get(selectedDestCityKey);
                    // Now we have a valid route selected, the loop will exit.
                } else {
                    System.out.println("\033[1;31mInvalid choice. Please enter a number from the list.\033[0m");
                    Utils.pause(sc);
                    // Loop continues to ask for destination again
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred selecting destination: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline
                Utils.pause(sc);
            }
        } // End of destination selection loop

        // Return origin city name (from LocationInfo), destination city name (from RouteDetail), and the route details
        return new Object[]{selectedOriginInfo.city(), selectedRouteDetail.destinationCity(), selectedRouteDetail};
    }


    // --- Updated Booking Methods ---

    private void selectAndBookPlane(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Plane");

        if (selectedRoute == null) {
            System.out.println("\033[1;33mRoute selection cancelled or no valid routes found.\033[0m");
            Utils.pause(sc);
            return; // User chose back or no route possible
        }

        String startCity = (String) selectedRoute[0];
        String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance(); // Already validated in selectValidRoute to be >= 0
        String eta = routeDetail.eta();
        List<String> providers = routeDetail.providers(); // Flight numbers/airlines

        // Calculate price using actual distance
        // Assuming Utils.calculatePrice exists and works based on type and distance
        double basePrice = Utils.calculatePrice("Plane", distance);
        if (basePrice <= 0) {
            System.out.println("\033[1;31mError calculating price (Rs. " + basePrice + ") for the selected route (Distance: " + distance + " km). Using default price.\033[0m");
            // Fallback or handle error - using a default for now
            basePrice = distance > 0 ? distance * 5.0 : 5000.0; // Example fallback
            Utils.pause(sc);
        }


        // Display selected route info
        Utils.printBanner("Selected Route");
        System.out.println("\033[1mFrom:\033[0m " + startCity);
        System.out.println("\033[1mTo:\033[0m " + destCity);
        System.out.println("\033[1mDistance:\033[0m \033[1;33m" + distance + " km\033[0m");
        System.out.println("\033[1mEstimated Travel Time:\033[0m \033[1;32m" + eta + "\033[0m");
        if (providers != null && !providers.isEmpty()) {
            System.out.println("\033[1mAvailable Airlines/Flights:\033[0m \033[0;35m" + String.join(", ", providers) + "\033[0m");
        }


        // --- Continue with existing logic for date, class, plane selection ---
        // Prompt for travel date
        String travelDate = "";
        while (travelDate.isEmpty()) {
            System.out.print("\033[1mEnter travel date (DD-MM-YYYY): \033[0m");
            travelDate = sc.nextLine().trim();
            // Basic format validation (optional but recommended)
            if (!travelDate.matches("\\d{2}-\\d{2}-\\d{4}")) {
                System.out.println("\033[1;31mInvalid date format. Please use DD-MM-YYYY.\033[0m");
                travelDate = ""; // Reset to ask again
            }
        }

        // Select class
        double classMultiplier = 1.0;
        String seatClass = "";
        while (seatClass.isEmpty()) {
            Utils.clearScreen(); // Clear screen for class selection
            Utils.printBanner("Choose Seat Class for " + startCity + " -> " + destCity);
            System.out.printf("\033[1;33m1.\033[0m Economy (Price: Rs. %.2f)\n", basePrice * 1.0);
            System.out.printf("\033[1;33m2.\033[0m Business (Price: Rs. %.2f)\n", basePrice * 2.0);
            System.out.printf("\033[1;33m3.\033[0m First (Price: Rs. %.2f)\n", basePrice * 3.0);
            System.out.println("\033[1;33m0.\033[0m Back to Transport Selection");
            System.out.print("\033[1mEnter choice: \033[0m");
            try {
                int classChoice = sc.nextInt();
                sc.nextLine(); // Consume newline

                switch (classChoice) {
                    case 1: seatClass = "Economy"; classMultiplier = 1.0; break;
                    case 2: seatClass = "Business"; classMultiplier = 2.0; break;
                    case 3: seatClass = "First"; classMultiplier = 3.0; break;
                    case 0: return; // Go back to transport selection
                    default: System.out.println("\033[1;31mInvalid class choice.\033[0m"); Utils.pause(sc); continue; // Ask again
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred choosing class: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline
                Utils.pause(sc);
            }
        }
        double finalRoutePrice = basePrice * classMultiplier; // Price adjusted for class


        // Select Plane (Keep simple selection for now, could be filtered by 'providers' later)
        if (planes.isEmpty()) {
            System.out.println("\033[1;31mSorry, no planes are currently configured in the system.\033[0m");
            Utils.pause(sc);
            return;
        }

        PlaneBooking selectedPlane = null;
        while(selectedPlane == null) {
            Utils.clearScreen(); // Clear screen for plane selection
            Utils.printBanner("Select Plane for " + startCity + " -> " + destCity);
            // Optional: Filter planes based on providers list? For now, show all.
            System.out.println("\033[1;36mShowing all available planes (Future enhancement: filter by route providers):\033[0m");
            if (providers != null && !providers.isEmpty()) {
                System.out.println("\033[0;90m(Route providers: " + String.join(", ", providers) + ")\033[0m");
            }

            System.out.println("\033[1;33m0.\033[0m Back to Class Selection");
            for (int i = 0; i < planes.size(); i++) {
                // Ideally, filter planes based on the 'providers' list for the route
                System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Flight ID: " + planes.get(i).getFlightId());
            }
            System.out.print("\033[1mEnter flight number to book: \033[0m");
            try {
                int flightChoice = sc.nextInt();
                sc.nextLine(); // Consume newline
                if (flightChoice == 0) return; // Go back (will loop back to class selection)
                if (flightChoice >= 1 && flightChoice <= planes.size()) {
                    selectedPlane = planes.get(flightChoice - 1);
                } else {
                    System.out.println("\033[1;31mInvalid flight selection.\033[0m");
                    Utils.pause(sc);
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred selecting flight: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline
                Utils.pause(sc);
            }
        }


        // Call the book method with accurate route data
        // Pass the class-adjusted price
        selectedPlane.book(sc, loggedInUser, startCity, destCity, finalRoutePrice, seatClass, travelDate);
        Utils.pause(sc); // Pause after booking attempt

    }

    // --- selectAndBookTrain --- (Similar structure to selectAndBookPlane)
    private void selectAndBookTrain(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Train");

        if (selectedRoute == null) {
            System.out.println("\033[1;33mRoute selection cancelled or no valid routes found.\033[0m");
            Utils.pause(sc);
            return;
        }

        String startCity = (String) selectedRoute[0];
        String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance();
        String eta = routeDetail.eta();
        List<String> providers = routeDetail.providers(); // Train names/numbers

        double basePrice = Utils.calculatePrice("Train", distance);
        if (basePrice <= 0) {
            System.out.println("\033[1;31mError calculating price (Rs. " + basePrice + ") for the selected route (Distance: " + distance + " km). Using default price.\033[0m");
            basePrice = distance > 0 ? distance * 1.0 : 1000.0; // Example fallback
            Utils.pause(sc);
        }

        Utils.printBanner("Selected Route");
        System.out.println("\033[1mFrom:\033[0m " + startCity);
        System.out.println("\033[1mTo:\033[0m " + destCity);
        System.out.println("\033[1mDistance:\033[0m \033[1;33m" + distance + " km\033[0m");
        System.out.println("\033[1mEstimated Travel Time:\033[0m \033[1;32m" + eta + "\033[0m");
        if (providers != null && !providers.isEmpty()) {
            System.out.println("\033[1mAvailable Trains:\033[0m \033[0;35m" + String.join(", ", providers) + "\033[0m");
        }


        String travelDate = "";
        while (travelDate.isEmpty()) {
            System.out.print("\033[1mEnter travel date (DD-MM-YYYY): \033[0m");
            travelDate = sc.nextLine().trim();
            if (!travelDate.matches("\\d{2}-\\d{2}-\\d{4}")) {
                System.out.println("\033[1;31mInvalid date format. Please use DD-MM-YYYY.\033[0m");
                travelDate = ""; // Reset to ask again
            }
        }

        // Select class
        double classMultiplier = 1.0;
        String seatClass = "";
        while (seatClass.isEmpty()) {
            Utils.clearScreen();
            Utils.printBanner("Choose Train Class for " + startCity + " -> " + destCity);
            System.out.printf("\033[1;33m1.\033[0m AC First Class (1A) (Price: Rs. %.2f)\n", basePrice * 2.5);
            System.out.printf("\033[1;33m2.\033[0m Second AC (2A) (Price: Rs. %.2f)\n", basePrice * 2.0);
            System.out.printf("\033[1;33m3.\033[0m Third AC (3A) (Price: Rs. %.2f)\n", basePrice * 1.5);
            System.out.printf("\033[1;33m4.\033[0m Sleeper Class (SL) (Price: Rs. %.2f)\n", basePrice * 1.0);
            System.out.printf("\033[1;33m5.\033[0m Chair Car (CC) (Price: Rs. %.2f)\n", basePrice * 0.8);
            System.out.printf("\033[1;33m6.\033[0m Second Seater (2S) (Price: Rs. %.2f)\n", basePrice * 0.5);
            System.out.println("\033[1;33m0.\033[0m Back to Transport Selection");
            System.out.print("\033[1mEnter choice: \033[0m");

            try {
                int classChoice = sc.nextInt();
                sc.nextLine(); // Consume newline
                switch (classChoice) {
                    case 1: seatClass = "AC First Class (1A)"; classMultiplier = 2.5; break;
                    case 2: seatClass = "Second AC (2A)"; classMultiplier = 2.0; break;
                    case 3: seatClass = "Third AC (3A)"; classMultiplier = 1.5; break;
                    case 4: seatClass = "Sleeper Class (SL)"; classMultiplier = 1.0; break;
                    case 5: seatClass = "Chair Car (CC)"; classMultiplier = 0.8; break;
                    case 6: seatClass = "Second Seater (2S)"; classMultiplier = 0.5; break;
                    case 0: return; // Go back
                    default: System.out.println("\033[1;31mInvalid class choice.\033[0m"); Utils.pause(sc); continue; // Ask again
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred choosing class: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline
                Utils.pause(sc);
            }
        }
        double finalRoutePrice = basePrice * classMultiplier;


        // Select Train
        if (trains.isEmpty()) {
            System.out.println("\033[1;31mSorry, no trains are currently configured in the system.\033[0m");
            Utils.pause(sc);
            return;
        }
        TrainBooking selectedTrain = null;
        while(selectedTrain == null) {
            Utils.clearScreen();
            Utils.printBanner("Select Train for " + startCity + " -> " + destCity);
            System.out.println("\033[1;36mShowing all available trains:\033[0m");
            if (providers != null && !providers.isEmpty()) {
                System.out.println("\033[0;90m(Route providers: " + String.join(", ", providers) + ")\033[0m");
            }
            System.out.println("\033[1;33m0.\033[0m Back to Class Selection");
            for (int i = 0; i < trains.size(); i++) {
                System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Train ID: " + trains.get(i).getTrainId());
            }
            System.out.print("\033[1mEnter train number to book: \033[0m");
            try {
                int trainChoice = sc.nextInt();
                sc.nextLine(); // Consume newline
                if (trainChoice == 0) return; // Go back
                if (trainChoice >= 1 && trainChoice <= trains.size()) {
                    selectedTrain = trains.get(trainChoice - 1);
                } else {
                    System.out.println("\033[1;31mInvalid train selection.\033[0m");
                    Utils.pause(sc);
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred selecting train: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline
                Utils.pause(sc);
            }
        }

        selectedTrain.book(sc, loggedInUser, startCity, destCity, finalRoutePrice, seatClass, travelDate);
        Utils.pause(sc);
    }

    // --- selectAndBookBus --- (Similar structure)
    private void selectAndBookBus(Scanner sc) {
        Utils.clearScreen();
        Object[] selectedRoute = selectValidRoute(sc, "Bus");

        if (selectedRoute == null) {
            System.out.println("\033[1;33mRoute selection cancelled or no valid routes found.\033[0m");
            Utils.pause(sc);
            return;
        }

        String startCity = (String) selectedRoute[0];
        String destCity = (String) selectedRoute[1];
        RouteDetail routeDetail = (RouteDetail) selectedRoute[2];
        int distance = routeDetail.distance();
        String eta = routeDetail.eta();
        List<String> providers = routeDetail.providers(); // Bus operators

        double routePrice = Utils.calculatePrice("Bus", distance); // Bus has only one class usually
        if (routePrice <= 0) {
            System.out.println("\033[1;31mError calculating price (Rs. " + routePrice + ") for the selected route (Distance: " + distance + " km). Using default price.\033[0m");
            routePrice = distance > 0 ? distance * 0.5 : 500.0; // Example fallback
            Utils.pause(sc);
        }

        Utils.printBanner("Selected Route");
        System.out.println("\033[1mFrom:\033[0m " + startCity);
        System.out.println("\033[1mTo:\033[0m " + destCity);
        System.out.println("\033[1mDistance:\033[0m \033[1;33m" + distance + " km\033[0m");
        System.out.println("\033[1mEstimated Travel Time:\033[0m \033[1;32m" + eta + "\033[0m");
        if (providers != null && !providers.isEmpty()) {
            System.out.println("\033[1mAvailable Bus Operators:\033[0m \033[0;35m" + String.join(", ", providers) + "\033[0m");
        }


        String travelDate = "";
        while (travelDate.isEmpty()) {
            System.out.print("\033[1mEnter travel date (DD-MM-YYYY): \033[0m");
            travelDate = sc.nextLine().trim();
            if (!travelDate.matches("\\d{2}-\\d{2}-\\d{4}")) {
                System.out.println("\033[1;31mInvalid date format. Please use DD-MM-YYYY.\033[0m");
                travelDate = ""; // Reset to ask again
            }
        }


        // Bus usually has only 'Standard' class
        String seatClass = "Standard";
        System.out.println("\n\033[1;36mClass:\033[0m Standard");
        System.out.printf("\033[1mPrice:\033[0m Rs. %.2f\n", routePrice);


        // Select Bus
        if (buses.isEmpty()) {
            System.out.println("\033[1;31mSorry, no buses are currently configured in the system.\033[0m");
            Utils.pause(sc);
            return;
        }
        BusBooking selectedBus = null;
        while (selectedBus == null) {
            Utils.clearScreen();
            Utils.printBanner("Select Bus for " + startCity + " -> " + destCity);
            System.out.println("\033[1;36mShowing all available buses:\033[0m");
            if (providers != null && !providers.isEmpty()) {
                System.out.println("\033[0;90m(Route providers: " + String.join(", ", providers) + ")\033[0m");
            }
            System.out.println("\033[1;33m0.\033[0m Back to Transport Selection");
            for (int i = 0; i < buses.size(); i++) {
                System.out.println("\033[1;33m" + (i + 1) + ".\033[0m Bus ID: " + buses.get(i).getBusId());
            }
            System.out.print("\033[1mEnter bus number to book: \033[0m");
            try {
                int busChoice = sc.nextInt();
                sc.nextLine(); // Consume newline
                if (busChoice == 0) return; // Go back
                if (busChoice >= 1 && busChoice <= buses.size()) {
                    selectedBus = buses.get(busChoice - 1);
                } else {
                    System.out.println("\033[1;31mInvalid bus selection.\033[0m");
                    Utils.pause(sc);
                }
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                sc.nextLine(); // Consume invalid input
                Utils.pause(sc);
            } catch (Exception e) {
                System.out.println("\033[1;31mAn error occurred selecting bus: " + e.getMessage() + "\033[0m");
                sc.nextLine(); // Consume newline
                Utils.pause(sc);
            }
        }

        // Bus booking takes the base route price directly
        selectedBus.book(sc, loggedInUser, startCity, destCity, routePrice, seatClass, travelDate);
        Utils.pause(sc);
    }


    // --- Remove or Deprecate Old/Unused Methods ---
    /*
    private String[] selectRoute(Scanner sc) { // Old free-text input
        // ... This is replaced by selectValidRoute ...
    }

    private int generateRandomDistance() { // No longer needed
        // ... Distance comes from file ...
    }

    private double generateRandomPrice(int distance, String transportType) { // No longer needed
       // ... Price calculated from actual distance ...
    }

    // calculateETA in Utils might be used as fallback, but ETA from file is primary
    public static String calculateETA(int distance, String transportType) { ... }
    */

    // viewBookings and cancelBooking remain the same conceptually, but use boolean returns
    private void viewBookings(Scanner sc) {
        Utils.clearScreen(); // Clear screen before showing bookings
        Utils.printBanner("Your Bookings");
        boolean foundBookings = false;

        // Check Planes
        if (planes != null) {
            for (PlaneBooking plane : planes) {
                if (plane != null && plane.displayUserBookings(loggedInUser)) {
                    foundBookings = true;
                }
            }
        }

        // Check Trains
        if (trains != null) {
            for (TrainBooking train : trains) {
                if (train != null && train.displayUserBookings(loggedInUser)) {
                    foundBookings = true;
                }
            }
        }

        // Check Buses
        if (buses != null) {
            for (BusBooking bus : buses) {
                if (bus != null && bus.displayUserBookings(loggedInUser)) {
                    foundBookings = true;
                }
            }
        }

        if (!foundBookings) {
            System.out.println("\033[1;33mYou have no active bookings.\033[0m");
        }
        Utils.pause(sc); // Pause after displaying
    }

    private void cancelBooking(Scanner sc) {
        Utils.clearScreen(); // Clear screen
        Utils.printBanner("Cancel Booking");
        System.out.print("\033[1mEnter Booking ID to cancel (e.g., P1, T1, B1): \033[0m");
        String bookingId = sc.nextLine().toUpperCase().trim(); // Convert to upper for consistency and trim whitespace
        boolean canceled = false;

        if (bookingId.isEmpty()) {
            System.out.println("\033[1;31mBooking ID cannot be empty.\033[0m");
            Utils.pause(sc);
            return;
        }


        if (bookingId.startsWith("P") && planes != null) {
            for (PlaneBooking plane : planes) {
                if (plane != null && plane.cancelBooking(bookingId, loggedInUser)) { // Use boolean return
                    canceled = true;
                    break;
                }
            }
        } else if (bookingId.startsWith("T") && trains != null) {
            for (TrainBooking train : trains) {
                if (train != null && train.cancelBooking(bookingId, loggedInUser)) { // Use boolean return
                    canceled = true;
                    break;
                }
            }
        } else if (bookingId.startsWith("B") && buses != null) {
            for (BusBooking bus : buses) {
                if (bus != null && bus.cancelBooking(bookingId, loggedInUser)) { // Use boolean return
                    canceled = true;
                    break;
                }
            }
        } else if (!bookingId.startsWith("P") && !bookingId.startsWith("T") && !bookingId.startsWith("B")) {
            System.out.println("\033[1;31mInvalid Booking ID format (must start with P, T, or B).\033[0m");
        }
        // Implicitly handles cases where planes/trains/buses lists are null

        if (canceled) {
            System.out.println("\033[1;32mBooking " + bookingId + " canceled successfully in memory.\033[0m");
            System.out.println("\033[0;90mChanges will be saved permanently upon logout or next booking/cancellation.\033[0m");
            // Note: StorageManager.removeBooking immediately rewrites the file,
            // which can be inefficient. Saving periodically (like after booking/cancelling/logout)
            // is generally better. We are already saving in the main booking loop.
            // StorageManager.removeBooking(bookingId, planes, trains, buses); // Optional immediate save
        } else {
            // Avoid printing this if the format was invalid (already handled)
            if (bookingId.startsWith("P") || bookingId.startsWith("T") || bookingId.startsWith("B")) {
                System.out.println("\033[1;31mBooking ID " + bookingId + " not found for user '" + loggedInUser + "' or could not be cancelled.\033[0m");
            }
        }
        Utils.pause(sc); // Pause after attempt
    }

} // End of BookingSystem class