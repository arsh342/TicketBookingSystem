# SkyRoute Console Ticket Booking System

SkyRoute is a comprehensive console-based ticket booking application built in Java. It allows users to register, log in, search for routes, book tickets for planes, trains, and buses, view their bookings, and cancel them. The system also features an admin dashboard for managing bookings and users at a higher level. It emphasizes data-driven route selection, provider choice, input validation, and persistent storage of user and booking information.

## Features

* **User Management:**
    * Secure user registration with password hashing (SHA-256).
    * User login with password verification.
    * Data persistence for user accounts in `users.txt`.
* **Admin Functionality:**
    * Dedicated admin login (default: `admin`/`admin`).
    * Admin dashboard with options to:
        * View all bookings across all users and services.
        * View a list of all registered users.
        * Cancel any booking in the system using its Booking ID.
* **Booking Process:**
    * **Transport Selection:** Choose between Plane, Train, or Bus.
    * **Route Selection:** Select origin and destination cities from lists dynamically populated from data files (`airports.txt`, `train_stations.txt`, `bus_stations.txt`).
    * **Route Details Display:** Shows distance and estimated travel time for the selected route.
    * **Provider Selection:** Users choose a specific service provider (e.g., "IndiGo 6E-201", "Rajdhani Express") for their chosen route.
    * **Travel Date Input:** Validated date input (DD-MM-YYYY, future dates only).
    * **Class Selection:** Different travel classes available for planes and trains with corresponding price multipliers. Buses use a "Standard" class.
    * **Seat Selection:** Interactive seat map display (`O` for available, `X` for reserved). Users select seats by row and column.
    * **Passenger Details:** Collects passenger name, age, gender, and email with input validation.
    * **Payment Simulation:** A simulated payment process (no actual payment is processed).
    * **Booking Confirmation:** Displays a detailed confirmation upon successful booking.
* **Booking Management:**
    * View all bookings made by the logged-in user.
    * Cancel existing bookings.
* **Data Persistence:**
    * User accounts are saved in `users.txt`.
    * Confirmed bookings are saved in `bookings.txt`.
* **User Interface:**
    * Console-based interface with styled menus, prompts, and messages using ANSI color codes for better readability.
    * Password masking during input (works best when run directly in a system terminal).

## Project Structure and Classes

The project is organized into several Java classes within the `TicketBookingSystem` package, supported by text-based data files.

### Key Java Classes & Their Roles:

* **`Main.java`**:
    * **Purpose:** Application entry point, main menu, overall flow control.
    * **Responsibilities:** Initializes core components, handles default admin creation, displays the primary menu (Register, Login, View Routes, Exit), directs users to appropriate dashboards (Admin or User), and manages application exit (including data saving).
* **`UserManager.java`**:
    * **Purpose:** Handles user authentication and account management.
    * **Responsibilities:** User registration (with password hashing), user login (verifying credentials), password hashing (SHA-256), providing user data to `StorageManager` and `AdminDashboard`.
* **`BookingSystem.java`**:
    * **Purpose:** Orchestrates the booking process for regular (non-admin) users.
    * **Responsibilities:** Manages lists of vehicle booking "manager" objects (`PlaneBooking`, `TrainBooking`, `BusBooking`), interacts with `RouteDataManager` for route and provider selection, handles class and date selection, calls the appropriate vehicle manager to finalize booking, manages user's own booking viewing and cancellation.
* **`AdminDashboard.java`**:
    * **Purpose:** Provides a dedicated interface for administrative tasks.
    * **Responsibilities:** Displays admin-specific menu, allows viewing of all bookings across all users, lists all registered users (via `UserManager`), enables cancellation of any booking by ID.
* **`RouteDataManager.java`**:
    * **Purpose:** Loads, parses, and provides access to route information from data files.
    * **Responsibilities:** Reads and parses `airports.txt`, `train_stations.txt`, `bus_stations.txt`. Stores route details (distance, ETA, providers) and location information. Provides this data to `BookingSystem` for user choices and to `Main` for the "View Route Information" feature.
* **`PlaneBooking.java` / `TrainBooking.java` / `BusBooking.java`**:
    * **Purpose:** Each class acts as a "manager" for bookings related to its transport type. It handles the specifics of seat layout, booking finalization, and stores bookings associated with its generic manager ID (e.g., "PLANE-MANAGER-1").
    * **Responsibilities:** Initializes and displays seat layouts (using `CustomLinkedList<Seat>`), handles seat selection by the user, collects validated passenger details, simulates payment, creates and stores `Booking` records (including the specific service provider chosen by the user), displays bookings for a user relevant to this manager, and handles booking cancellations for bookings it manages.
* **`StorageManager.java`**:
    * **Purpose:** Handles persistence of user and booking data to text files.
    * **Responsibilities:** Saves and loads user credentials (username and hashed passwords) to/from `users.txt`. Saves and loads confirmed booking details (including provider information) to/from `bookings.txt`.
* **`Seat.java`**:
    * **Purpose:** Data model representing a single seat.
    * **Responsibilities:** Stores seat properties (row, column, class, type, price, reserved status). Provides methods to reserve/unreserve and display its status.
* **`Passenger.java`**:
    * **Purpose:** Data model representing a passenger.
    * **Responsibilities:** Stores passenger details (name, age, gender, email) and the `Seat` object they booked.
* **`CustomLinkedList.java`**:
    * **Purpose:** A generic, singly linked list implementation.
    * **Responsibilities:** Used by `Plane/Train/BusBooking` classes to temporarily manage and display the list of `Seat` objects for a specific booking transaction. Implements `Iterable`.
* **`Utils.java`**:
    * **Purpose:** Provides static utility methods used across the application.
    * **Responsibilities:** Defines ANSI color constants for console styling, methods for `pause`, `clearScreen`, `printBanner`, base price calculation, input validation (`getValidTravelDate`, `getValidAge`, `getValidGender`, `getValidEmail`), and payment simulation.

### Data Files (Text-Based):

* `users.txt`: Stores `username:hashedPassword`.
* `bookings.txt`: Stores confirmed bookings. Format: `BookingID:Username:StartCity:DestCity:Price:SeatClass:SeatRow:SeatCol:VehicleManagerID:TravelDate:Provider`
* `airports.txt`, `train_stations.txt`, `bus_stations.txt`: Define locations, routes, distances, ETAs, and service providers. Format: `City|PrimaryName[|AltName1|AltName2...]|RouteDetailsString` where `RouteDetailsString` is `Dest1:Dist1:ETA1:ProvA,ProvB;Dest2...`.

## Key Imports (Examples)

The project utilizes various standard Java libraries. Some key imports include:

* `java.util.Scanner`: For reading user input from the console.
* `java.util.List`, `java.util.ArrayList`: For managing dynamic collections of objects (e.g., vehicle managers, providers, sorted lists for display).
* `java.util.Map`, `java.util.HashMap`: For storing key-value pairs (e.g., user credentials, bookings, route data).
* `java.util.Collections`: For sorting lists (e.g., displaying cities or users alphabetically).
* `java.io.*` (e.g., `BufferedReader`, `BufferedWriter`, `FileReader`, `FileWriter`, `File`, `Console`): For file input/output operations and console interactions (password masking).
* `java.security.MessageDigest`, `java.security.NoSuchAlgorithmException`: For password hashing using SHA-256.
* `java.time.*` (e.g., `LocalDate`, `DateTimeFormatter`, `DateTimeParseException`, `ResolverStyle`): For robust date handling and validation.
* `java.util.regex.Pattern`: For basic email validation.
* `java.util.InputMismatchException`: For handling errors when reading numeric input.

## Data Structures and Algorithms (DSA) Concepts Implemented

* **Arrays:**
    * Used in `Plane/Train/BusBooking` (e.g., `char[] columns`) for defining basic seat column layouts.
    * Used in `Utils` (e.g., `VALID_GENDERS` initialized from `Arrays.asList`).
    * String splitting (`split()`) results in arrays, used extensively in parsing data from files (`RouteDataManager`, `StorageManager`).
* **Linked Lists:**
    * **`CustomLinkedList<T>`:** A custom generic singly linked list is implemented to manage `Seat` objects during a booking transaction. This demonstrates understanding of node-based data structures, traversal, addition, and removal operations.
    * **Conceptual Use:** Used for dynamic storage where the number of elements (seats in a layout) is determined at runtime based on class, and elements need to be iterated over.
* **Hash Maps (Hash Tables):**
    * **`HashMap<String, String> users` in `UserManager`:** Stores username-password (hash) pairs. Provides O(1) average time complexity for lookups (checking if a user exists, retrieving a stored password hash), registrations (checking for existing username), and logins.
    * **`Map<String, Booking> bookings` in `Plane/Train/BusBooking`:** Stores booking ID to `Booking` object mappings. Allows efficient retrieval and cancellation of bookings by ID.
    * **`Map<String, LocationInfo> airport/busStation/trainStationData` in `RouteDataManager`:** Stores city names (keys) to detailed `LocationInfo` objects. Provides efficient lookup of location details and their available routes.
    * **`Map<String, RouteDetail> routes` within `LocationInfo`:** Stores destination city names to `RouteDetail` objects, allowing efficient lookup of specific route information from a given origin.
* **Sets:**
    * **`Set<String> VALID_GENDERS` in `Utils`:** Uses a `HashSet` for efficient checking (`contains()`) if a provided gender string (after converting to uppercase) is one of the allowed options. O(1) average time complexity for lookups.
* **Lists (Dynamic Arrays):**
    * **`ArrayList<Plane/Train/BusBooking>` in `BookingSystem`:** Manages the collection of vehicle booking manager objects.
    * **`ArrayList<String> providers` in `RouteDetail`:** Stores the list of service providers for a route.
    * **`ArrayList<String>` used for sorting keys** in `RouteDataManager` and `UserManager` before displaying lists to the user for better readability (e.g., `originCityKeys`, `destinationKeys`, `sortedUsernames`).
* **Strings and String Manipulation:**
    * Extensively used for parsing data from files (splitting lines by delimiters), formatting output for display, and handling user input.
* **Searching:**
    * **Linear Search:**
        * Implicit in `CustomLinkedList.remove()` and `CustomLinkedList.contains()`.
        * `findSeat()` in `Plane/Train/BusBooking` performs a linear search through the `CustomLinkedList<Seat>`.
        * Locating the correct `Plane/Train/BusBooking` manager object in `BookingSystem` during loading or cancellation involves iterating through the `ArrayList` of these objects (linear search).
    * **Hash-based Search:** Utilized by `HashMap.containsKey()` and `HashMap.get()` for O(1) average time lookups in `UserManager`, `RouteDataManager`, and the `bookings` maps within vehicle booking classes.
* **Sorting:**
    * **`Collections.sort()`:** Used to sort lists of city names, destination names, and usernames alphabetically before displaying them to the user, enhancing user experience. This typically uses a variation of MergeSort or TimSort in Java, offering O(n log n) time complexity.
* **Recursion:**
    * A light form of recursion is used in `BookingSystem.selectValidRoute()` if the user chooses to go "Back to Origin Selection" from the destination selection menu, the method calls itself to restart the origin selection.
* **Hashing (Cryptographic):**
    * **SHA-256:** Used in `UserManager.hashPassword()` for secure one-way hashing of passwords before storage. This is a cryptographic concept, not a typical DSA data structure, but an important algorithm used.

## Setup & Running the Project

**Prerequisites:**

* Java Development Kit (JDK) installed (version 17 or newer recommended for records, but adaptable).
* A terminal or command prompt that supports ANSI escape codes for styled output (e.g., Windows Terminal, PowerShell, macOS Terminal, most Linux terminals).

**Compilation:**

1.  Ensure all `.java` files are in a directory structure matching their package statement (e.g., `TicketBookingSystem` folder).
2.  Navigate to the directory *containing* the `TicketBookingSystem` package folder in your terminal.
3.  Compile all Java files:
    ```bash
    javac TicketBookingSystem/*.java
    ```
    Or, to compile into a separate `out` directory (recommended):
    ```bash
    javac -d out src/TicketBookingSystem/*.java
    ```
    (Assuming your source files are in `src/TicketBookingSystem` and you are in the project root).

**Running:**

1.  Ensure the data files (`airports.txt`, `train_stations.txt`, `bus_stations.txt`) are present in the execution directory (usually the project root if running from there, or the directory where `TicketBookingSystem.Main.class` is located).
2.  `users.txt` and `bookings.txt` will be created automatically in the execution directory if they don't exist.
3.  From the directory where the compiled `TicketBookingSystem` package is accessible (e.g., from the project root if you used `-d out` above, then `cd out`):
    ```bash
    java TicketBookingSystem.Main
    ```

**First Run & Admin Login:**

* On the very first run (or if `users.txt` is deleted/empty), the application will create a default admin user:
    * **Username:** `admin`
    * **Password:** `admin`
* To log in as admin, select the "Login" option and use these credentials.

**Password Masking Note:**
Password input masking (not showing characters as they are typed) is handled by `java.io.Console.readPassword()`. This works best when running the application from a true system terminal. Many IDE-integrated consoles do not fully support this, and passwords might be visible during input in such environments (the system will fall back to standard input).

## Visual Styling

The application uses ANSI escape codes, managed via constants in the `Utils` class, to provide:
* Colored text for menus, prompts, success messages, warnings, and errors.
* Bolding for emphasis.
* Styled banners for section titles.
* Colored indicators for seat availability (`O` vs `X`).

This enhances the command-line user interface's readability and visual appeal.
