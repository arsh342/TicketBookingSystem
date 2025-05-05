package TicketBookingSystem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

// --- Data Structures (Records inside the class or separate files) ---

/**
 * Record to hold details of a single route segment.
 * @param destinationCity The name of the destination city.
 * @param distance The distance in kilometers. -1 if not applicable (e.g., NoBus).
 * @param eta The estimated time of arrival string (e.g., "2h30m"). "N/A" if not applicable.
 * @param providers List of provider names (e.g., airline codes, train names, bus operators).
 */
record RouteDetail(String destinationCity, int distance, String eta, List<String> providers) {}

/**
 * Record to hold information about a location (Airport, Bus Station, Train Station).
 * @param city The name of the city.
 * @param primaryName The primary name of the station/airport (e.g., "Indira Gandhi International Airport (DEL)").
 * @param alternateNames Additional names or codes associated with the location.
 * @param routes A map of routes originating from this location, keyed by destination city (UPPERCASE).
 * @param type The type of location ("Airport", "Bus Station", "Train Station").
 */
record LocationInfo(String city, String primaryName, List<String> alternateNames, Map<String, RouteDetail> routes, String type) {}


// --- RouteDataManager Class ---

public class RouteDataManager {

    // Define constants for filenames for easy management
    private static final String AIRPORTS_FILE = "airports.txt";
    private static final String BUS_STATIONS_FILE = "bus_stations.txt";
    private static final String TRAIN_STATIONS_FILE = "train_stations.txt";

    // Maps to store the loaded data, keyed by City name (UPPERCASE for consistency)
    private Map<String, LocationInfo> airportData = new HashMap<>();
    private Map<String, LocationInfo> busStationData = new HashMap<>();
    private Map<String, LocationInfo> trainStationData = new HashMap<>();

    /**
     * Constructor: Loads all data when the manager is created.
     */
    public RouteDataManager() {
        loadAllData();
    }

    /**
     * Provides access to the loaded airport data.
     * @return An unmodifiable view of the airport data map.
     */
    public Map<String, LocationInfo> getAirportData() {
        return Collections.unmodifiableMap(airportData);
    }

    /**
     * Provides access to the loaded bus station data.
     * @return An unmodifiable view of the bus station data map.
     */
    public Map<String, LocationInfo> getBusStationData() {
        return Collections.unmodifiableMap(busStationData);
    }

    /**
     * Provides access to the loaded train station data.
     * @return An unmodifiable view of the train station data map.
     */
    public Map<String, LocationInfo> getTrainStationData() {
        return Collections.unmodifiableMap(trainStationData);
    }


    /**
     * Loads data from all configured files. Called by the constructor.
     */
    private void loadAllData() {
        System.out.println("\033[0;90mLoading route data...\033[0m"); // Use grey for debug loading messages
        airportData = loadLocationData(AIRPORTS_FILE, "Airport");
        busStationData = loadLocationData(BUS_STATIONS_FILE, "Bus Station");
        trainStationData = loadLocationData(TRAIN_STATIONS_FILE, "Train Station");
        System.out.println("\033[1;32mRoute data loaded.\033[0m");
    }

    /**
     * Generic method to load and parse data from a location file.
     * @param filename The name of the file to read.
     * @param type The type of location (e.g., "Airport", "Bus Station").
     * @return A Map where the key is the city name (UPPERCASE) and the value is the LocationInfo.
     */
    private Map<String, LocationInfo> loadLocationData(String filename, String type) {
        Map<String, LocationInfo> dataMap = new HashMap<>();
        // Use try-with-resources for automatic file closing
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0; // Initialize lineNumber here
            while ((line = reader.readLine()) != null) {
                lineNumber++; // Increment for each line read
                // Skip empty or commented lines
                if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;

                // Split the line based on the main delimiter '|'
                String[] parts = line.split("\\|");
                // Basic validation: need at least city, primary name, and routes string
                if (parts.length < 3) {
                    System.err.println("\033[1;33mWarning:\033[0m Skipping malformed line #" + lineNumber + " in " + filename + ": Insufficient parts. Content: " + line + "\033[0m");
                    continue;
                }

                String city = parts[0].trim();
                String primaryName = parts[1].trim();
                List<String> alternateNames = new ArrayList<>();
                // If there are more than 3 parts, intermediate parts are alternate names
                for (int i = 2; i < parts.length - 1; i++) {
                    alternateNames.add(parts[i].trim());
                }

                String routesString = parts[parts.length - 1].trim();
                // *** Pass lineNumber to parseRoutes ***
                Map<String, RouteDetail> routes = parseRoutes(routesString, filename, lineNumber);

                LocationInfo info = new LocationInfo(city, primaryName, alternateNames, routes, type);
                // Store using uppercase city name for consistent lookups
                dataMap.put(city.toUpperCase(), info);
            }
        } catch (FileNotFoundException e) {
            System.err.println("\033[1;31mError:\033[0m File not found: " + filename);
        } catch (IOException e) {
            System.err.println("\033[1;31mError:\033[0m Failed to read " + type + " data from " + filename + ": " + e.getMessage());
        } catch (Exception e) { // Catch unexpected parsing errors
            System.err.println("\033[1;31mError:\033[0m Unexpected error parsing " + filename + ": " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
        System.out.println("\033[0;90m -> Loaded " + dataMap.size() + " entries from " + filename + "\033[0m"); // Loading count
        return dataMap;
    }

    /**
     * Parses the routes string for a given location.
     * Example format: Dest1:Dist1:ETA1:Prov1,Prov2;Dest2:Dist2:ETA2:Prov3...
     * Handles special cases like Dest:NoBus: or Dest:NoTrain:
     * @param routesString The string containing route information.
     * @param filename For error reporting.
     * @param lineNumber For error reporting. *** ADDED PARAMETER ***
     * @return A Map where the key is the destination city (UPPERCASE) and the value is the RouteDetail.
     */
    // *** Updated method signature ***
    private Map<String, RouteDetail> parseRoutes(String routesString, String filename, int lineNumber) {
        Map<String, RouteDetail> routes = new HashMap<>();
        if (routesString.isEmpty() || routesString.equalsIgnoreCase("N/A")) {
            return routes; // No routes defined for this location
        }

        // Split into individual route entries based on ';'
        String[] routeEntries = routesString.split(";");

        for (String entry : routeEntries) {
            if (entry.trim().isEmpty()) continue; // Skip empty entries if they occur

            // Split each route entry based on ':'
            String[] details = entry.split(":");
            if (details.length < 2) { // Must have at least Dest:Info
                // *** Use lineNumber in error message ***
                System.err.println("\033[1;33mWarning:\033[0m Skipping malformed route entry in " + filename + " line #" + lineNumber + ": " + entry + "\033[0m");
                continue;
            }

            String destination = details[0].trim();
            int distance = -1; // Default value indicating not applicable/available
            String eta = "N/A"; // Default value
            List<String> providers = new ArrayList<>();

            // Check for standard route format: Dest:Dist:ETA:Providers
            if (details.length >= 4) {
                try {
                    distance = Integer.parseInt(details[1].trim());
                    if (distance < 0) { // Optional: Validate distance is non-negative
                        System.err.println("\033[1;33mWarning:\033[0m Skipping route due to negative distance in " + filename + " line #" + lineNumber + ": " + entry + "\033[0m");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("\033[1;33mWarning:\033[0m Skipping route due to invalid distance format in " + filename + " line #" + lineNumber + ": " + entry + "\033[0m");
                    continue; // Skip this route if distance is not a number
                }
                eta = details[2].trim();
                // Split providers by comma, trim whitespace, remove empty strings
                providers = Arrays.stream(details[3].split(","))
                        .map(String::trim)
                        .filter(p -> !p.isEmpty())
                        .collect(Collectors.toList());
            }
            // Check for special "Not Available" cases like Dest:NoBus: or Dest:NoTrain:
            else if (details.length >= 2 && (details[1].equalsIgnoreCase("NoBus") || details[1].equalsIgnoreCase("NoTrain"))) {
                eta = "Not Available"; // Explicitly set ETA for these cases
            }
            // Handle potential format Dest:Dist:ETA: (missing providers list but has valid fields before)
            else if (details.length == 3) {
                try {
                    distance = Integer.parseInt(details[1].trim());
                    if (distance < 0) {
                        System.err.println("\033[1;33mWarning:\033[0m Skipping route due to negative distance in " + filename + " line #" + lineNumber + ": " + entry + "\033[0m");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("\033[1;33mWarning:\033[0m Skipping route due to invalid distance format in " + filename + " line #" + lineNumber + ": " + entry + "\033[0m");
                    continue;
                }
                eta = details[2].trim();
                // providers list remains empty
            }
            else { // Includes cases with only Dest:Info where Info is not NoBus/NoTrain
                System.err.println("\033[1;33mWarning:\033[0m Skipping unhandled route format in " + filename + " line #" + lineNumber + ": " + entry + "\033[0m");
                continue; // Skip formats we don't understand
            }

            // Create the RouteDetail object
            RouteDetail routeDetail = new RouteDetail(destination, distance, eta, providers);
            // Store using uppercase destination for consistent lookups
            routes.put(destination.toUpperCase(), routeDetail);
        }
        return routes;
    }


    // --- Display Methods (Used by Main menu option) ---

    /**
     * Displays all loaded airport information.
     */
    public void displayAllAirports() {
        displayLocationInfo(airportData, "Airports");
    }

    /**
     * Displays all loaded bus station information.
     */
    public void displayAllBusStations() {
        displayLocationInfo(busStationData, "Bus Stations");
    }

    /**
     * Displays all loaded train station information.
     */
    public void displayAllTrainStations() {
        displayLocationInfo(trainStationData, "Train Stations");
    }

    /**
     * Helper method to format and print location information from a data map.
     * @param data The map containing the location data (keyed by UPPERCASE city name).
     * @param title The title to print for this section (e.g., "Airports").
     */
    private void displayLocationInfo(Map<String, LocationInfo> data, String title) {
        Utils.printBanner(title);
        if (data == null || data.isEmpty()) {
            System.out.println("\033[1;33mNo data loaded for " + title + ".\033[0m");
            return;
        }

        // Sort cities alphabetically for consistent display order
        List<String> sortedCityKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedCityKeys);

        for (String cityKey : sortedCityKeys) {
            LocationInfo info = data.get(cityKey);
            // Use ANSI escape codes for better formatting/colors
            System.out.println("\n\033[1;34mCity:\033[0m " + info.city()); // Blue bold for City
            System.out.print("\033[1;34m" + info.type() + " Name(s):\033[0m " + info.primaryName()); // Blue bold for Type Name
            if (!info.alternateNames().isEmpty()) {
                System.out.print(" \033[0;90m| " + String.join(" | ", info.alternateNames()) + "\033[0m"); // Grey for alternate names
            }
            System.out.println(); // Newline after names

            Map<String, RouteDetail> routes = info.routes();
            if (routes == null || routes.isEmpty()) {
                System.out.println("  \033[1;33mNo routes defined from this location.\033[0m"); // Yellow bold for warning
            } else {
                System.out.println("  \033[1;36mRoutes From This Location:\033[0m"); // Cyan bold for Routes

                // Sort routes by destination city name for consistent output
                List<RouteDetail> sortedRoutes = new ArrayList<>(routes.values());
                sortedRoutes.sort((r1, r2) -> r1.destinationCity().compareToIgnoreCase(r2.destinationCity()));

                for (RouteDetail route : sortedRoutes) {
                    // Check for "Not Available" routes explicitly
                    if (route.distance() == -1 && route.eta().equalsIgnoreCase("Not Available")) {
                        System.out.printf("    - \033[1mTo %s:\033[0m \033[0;31mNot Available via this mode\033[0m\n", route.destinationCity()); // Red for N/A
                    } else {
                        // Format regular route details with colors
                        System.out.printf("    - \033[1mTo %s:\033[0m Distance: \033[1;33m%d km\033[0m, ETA: \033[1;32m%s\033[0m, Providers: \033[0;35m%s\033[0m\n",
                                route.destinationCity(), route.distance(), route.eta(),
                                route.providers().isEmpty() ? "N/A" : String.join(", ", route.providers()));
                    }
                }
            }
        }
    }

    /**
     * Displays all available information (Airport, Bus, Train) for a specific city.
     * Handles case-insensitivity for the city search.
     * @param city The city name to search for.
     * @param sc Scanner for pausing after display.
     */
    public void displayInfoForCity(String city, Scanner sc) {
        Utils.clearScreen();
        String searchKey = city.toUpperCase(); // Use uppercase for map lookup
        Utils.printBanner("Information for " + city);
        boolean found = false;

        // Check and display Airport info
        LocationInfo airport = airportData.get(searchKey);
        if (airport != null) {
            found = true;
            displaySingleLocation(airport);
        } else {
            System.out.println("\n\033[1;33mNo Airport data found for " + city + ".\033[0m");
        }

        // Check and display Bus Station info
        LocationInfo busStation = busStationData.get(searchKey);
        if (busStation != null) {
            found = true;
            displaySingleLocation(busStation);
        } else {
            System.out.println("\n\033[1;33mNo Bus Station data found for " + city + ".\033[0m");
        }

        // Check and display Train Station info
        LocationInfo trainStation = trainStationData.get(searchKey);
        if (trainStation != null) {
            found = true;
            displaySingleLocation(trainStation);
        } else {
            System.out.println("\n\033[1;33mNo Train Station data found for " + city + ".\033[0m");
        }

        // Message if no data found at all for the city
        if (!found) {
            System.out.println("\n\033[1;31mNo information found for city: " + city + "\033[0m");
        }
        Utils.pause(sc); // Pause after displaying information
    }

    /**
     * Helper method to display the details of a single LocationInfo object.
     * Used by displayInfoForCity.
     * @param info The LocationInfo object to display.
     */
    private void displaySingleLocation(LocationInfo info) {
        System.out.println("\n\033[1;34m--- " + info.type() + " Information ---"); // Blue bold header
        System.out.print("\033[1;34mName(s):\033[0m " + info.primaryName());
        if (!info.alternateNames().isEmpty()) {
            System.out.print(" \033[0;90m| " + String.join(" | ", info.alternateNames()) + "\033[0m"); // Grey alternates
        }
        System.out.println(); // Newline

        Map<String, RouteDetail> routes = info.routes();
        if (routes == null || routes.isEmpty()) {
            System.out.println("  \033[1;33mNo routes defined from this location.\033[0m"); // Yellow bold
        } else {
            System.out.println("  \033[1;36mRoutes From This Location:\033[0m"); // Cyan bold
            // Sort routes by destination city name
            List<RouteDetail> sortedRoutes = new ArrayList<>(routes.values());
            sortedRoutes.sort((r1, r2) -> r1.destinationCity().compareToIgnoreCase(r2.destinationCity()));

            for (RouteDetail route : sortedRoutes) {
                if (route.distance() == -1 && route.eta().equalsIgnoreCase("Not Available")) {
                    System.out.printf("    - \033[1mTo %s:\033[0m \033[0;31mNot Available via this mode\033[0m\n", route.destinationCity()); // Red N/A
                } else {
                    System.out.printf("    - \033[1mTo %s:\033[0m Distance: \033[1;33m%d km\033[0m, ETA: \033[1;32m%s\033[0m, Providers: \033[0;35m%s\033[0m\n", // Colors
                            route.destinationCity(), route.distance(), route.eta(),
                            route.providers().isEmpty() ? "N/A" : String.join(", ", route.providers()));
                }
            }
        }
    }

} // End of RouteDataManager class