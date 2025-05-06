package TicketBookingSystem;

/**
 * Represents a passenger with booking details.
 */
public class Passenger {
    private String name;
    private int age;
    private String gender;
    private String email; // <-- ADDED Email field
    private Seat bookedSeat;

    // Update constructor to accept email
    public Passenger(String name, int age, String gender, String email, Seat bookedSeat) {
        this.name = name;
        this.age = age;
        // Ensure gender is stored consistently (optional standardization)
        this.gender = (gender != null) ? gender : "Not Specified";
        // Basic validation or store as is
        this.email = (email != null && !email.trim().isEmpty()) ? email.trim() : "Not Provided"; // <-- STORE Email
        this.bookedSeat = bookedSeat;
    }

    // --- Getters ---
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getEmail() { return email; } // <-- ADDED Getter
    public Seat getBookedSeat() { return bookedSeat; }

    // Update toString to include email
    @Override
    public String toString() {
        // Using String.format for potentially better alignment if needed elsewhere
        return String.format(Utils.BLUE_BOLD + "Passenger: " + Utils.MAGENTA + "%s" + Utils.RESET +
                        Utils.BLUE_BOLD + " | Age: " + Utils.MAGENTA + "%d" + Utils.RESET +
                        Utils.BLUE_BOLD + " | Gender: " + Utils.MAGENTA + "%s" + Utils.RESET +
                        Utils.BLUE_BOLD + " | Email: " + Utils.MAGENTA + "%s" + Utils.RESET + // <-- ADDED Email display
                        Utils.BLUE_BOLD + " | Seat: " + Utils.YELLOW_BOLD + "%s" + Utils.RESET,
                name, age, gender, email, bookedSeat.getSeatId());
    }
}