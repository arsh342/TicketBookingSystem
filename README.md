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
    * **Payment Simulation:** A simulated payment process with options for Credit Card, Debit Card, or UPI (no actual payment is processed, dummy details are accepted).
    * **Booking Confirmation:** Displays a detailed confirmation upon successful booking.
* **Booking Management:**
    * View all bookings made by the logged-in user.
    * Cancel existing bookings.
* **Data Persistence:**
    * User accounts are saved in `users.txt`.
    * Confirmed bookings are saved in `bookings.txt`.
* **User Interface:**
    * Console-based interface with styled menus, prompts, and messages using ANSI color codes for better readability and visual appeal.
    * Password masking during input (works best when run directly in a system terminal).

## Project Structure

The project is organized into several Java classes and data files within the `TicketBookingSystem` package.

### Java Files:

* **Core Logic & Application Flow:**
    * `Main.java`: Main entry point, top-level menu, application flow control, default admin creation.
    * `BookingSystem.java`: Manages the booking workflow for regular users, interacts with `RouteDataManager` and vehicle booking objects.
    * `AdminDashboard.java`: Provides the menu and functionalities for the admin user.
* **User & Data Management:**
    * `UserManager.java`: Handles user registration, login, and password hashing (SHA-256).
    * `RouteDataManager.java`: Loads, parses, and provides access to route information from `airports.txt`, `train_stations.txt`, `bus_stations.txt`. Contains `LocationInfo` and `RouteDetail` records.
* **Vehicle Booking Classes:** (These act as managers for booking types)
    * `PlaneBooking.java`: Manages plane seat layouts, booking finalization, and stores plane bookings.
    * `TrainBooking.java`: Manages train seat layouts, booking finalization, and stores train bookings.
    * `BusBooking.java`: Manages bus seat layouts, booking finalization, and stores bus bookings.
* **Data Structures/Models:**
    * `Seat.java`: Represents a single seat with its properties (row, column, class, price, status).
    * `Passenger.java`: Represents a passenger with details (name, age, gender, email, booked seat).
    * `CustomLinkedList.java`: A generic linked list used for managing temporary seat layouts during booking.
    * Inner `Booking` class (within `Plane/Train/BusBooking.java`): Represents a confirmed booking record, including the selected service provider.
* **Utilities & Persistence:**
    * `Utils.java`: Contains static helper methods for console styling (ANSI colors), input validation (date, age, gender, email), payment simulation, and price calculation.
    * `StorageManager.java`: Handles saving and loading user data (`users.txt`) and booking data (`bookings.txt`) to/from files.

### Data Files (should be in the root execution directory):

* `users.txt`: Stores user credentials (`username:hashedPassword`).
* `bookings.txt`: Stores confirmed booking details.
    * Format: `BookingID:Username:StartCity:DestCity:Price:SeatClass:SeatRow:SeatCol:VehicleManagerID:TravelDate:Provider`
* `airports.txt`: Data for airport locations, routes, distances, ETAs, and airline providers.
    * Format: `City|Airport Name (Code)|[Opt. Alt Name]|Dest1:Dist1:ETA1:Prov1,Prov2;Dest2...`
* `train_stations.txt`: Data for train stations, routes, distances, ETAs, and train service providers.
    * Format: `City|Station Name (Code)|[Opt. Alt Name]|Dest1:Dist1:ETA1:Prov1,Prov2;Dest2...`
* `bus_stations.txt`: Data for bus stations, routes, distances, ETAs, and bus service providers.
    * Format: `City|Station Name|[Opt. Alt Name]|Dest1:Dist1:ETA1:Prov1,Prov2;Dest2...`

## How it Works

1.  **Initialization:** `Main` initializes `UserManager`, `BookingSystem`, and `RouteDataManager`. `RouteDataManager` loads route data from text files. `UserManager` loads existing users.
2.  **Admin Setup:** `Main` checks if an "admin" user exists. If not, it registers "admin" with the password "admin".
3.  **User Interaction:**
    * Users can register or log in. Passwords are hashed by `UserManager`.
    * Logged-in users are directed to `BookingSystem` for booking tasks, or `AdminDashboard` if they are the admin.
4.  **Booking Process (`BookingSystem`):**
    * User selects transport type, then origin and destination from lists populated by `RouteDataManager`.
    * Route details (distance, ETA) are displayed.
    * User provides a valid travel date.
    * User selects travel class (if applicable), which affects the price.
    * A list of specific service providers (e.g., "IndiGo 123", "Rajdhani Exp") for that route is shown. User selects one.
    * A "manager" object (e.g., the first `PlaneBooking` object) handles the rest:
        * It initializes and displays a seat map for the chosen class.
        * User selects a seat.
        * User provides passenger details (name, age, gender, email) which are validated.
        * A payment process is simulated.
        * If successful, a `Booking` record (including the selected provider) is created and stored within the manager object.
5.  **Data Persistence (`StorageManager`):**
    * User registrations are saved to `users.txt` immediately.
    * All confirmed bookings from all manager objects are saved to `bookings.txt` when the user logs out from the main booking menu or when the application exits. Admin cancellations also trigger a save.

## Setup & Running

**Prerequisites:**

* Java Development Kit (JDK) installed (e.g., JDK 11 or newer).
* A terminal or command prompt that supports ANSI escape codes for styled output (most modern terminals do).

**Compilation:**

1.  Navigate to the `src` directory in your project (where the `TicketBookingSystem` package folder is located).
2.  Compile all Java files:
    ```bash
    javac TicketBookingSystem/*.java
    ```
    Alternatively, if your `.java` files are directly in `src` and the package statement is `package TicketBookingSystem;`, compile from the directory *above* `src`:
    ```bash
    javac src/TicketBookingSystem/*.java -d out
    ```
    (This command compiles files into an `out` directory, keeping source separate from compiled classes).

**Running:**

1.  Ensure the data files (`airports.txt`, `train_stations.txt`, `bus_stations.txt`, and initially empty or non-existent `users.txt`, `bookings.txt`) are in the correct execution directory.
    * If you compiled into an `out` directory, place the data files in the parent directory (e.g., `D:\ticketbookings\` if `out` is `D:\ticketbookings\out`).
    * If you compiled within `src`, place them in `src`. Generally, it's better to place data files outside the `src` folder.
2.  From the directory containing the `TicketBookingSystem` compiled package (e.g., `out` or your project root if compiled directly):
    ```bash
    java TicketBookingSystem.Main
    ```

**First Run & Admin Login:**

* On the first run (or if `users.txt` is missing/empty), the application will attempt to create a default admin user:
    * Username: `admin`
    * Password: `admin`
* To log in as admin, choose the "Login" option, and use these credentials.

**Note on Password Masking:**
The password input masking (showing `*` or nothing) uses `java.io.Console.readPassword()`. This feature works reliably when the application is run from a standard system terminal (like Command Prompt on Windows, or Terminal on macOS/Linux). It may not work (and will fall back to visible password input) in some IDE-integrated consoles.

## Visual Styling

The application uses ANSI escape codes (via the `Utils` class) to provide basic color and bolding to text in the console, enhancing readability and user experience. This includes:
* Colored menu options and prompts.
* Distinct colors for success, error, and warning messages.
* Styled banners for section titles.
* Colored representation of available (`O`) and reserved (`X`) seats.

## Potential Future Enhancements

* More sophisticated and distinct seat layout configurations for different vehicle types and classes.
* Management of actual vehicle instances rather than using "manager/template" objects.
* Admin functions to add/modify/delete routes and vehicles directly through the dashboard.
* More robust error handling and logging.
* A graphical user interface (GUI) using Java Swing or JavaFX.
* Use of a database (e.g., SQLite, H2, MySQL) for data persistence instead of text files for better scalability and data integrity.
* Password salting for enhanced security.
* Ability to book multiple seats in one transaction.
* Search/filter functionality for routes.
