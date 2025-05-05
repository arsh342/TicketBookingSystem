package TicketBookingSystem;

/**
 * Represents a passenger with booking details.
 */
public class Passenger {
    private String name;
    private int age;
    private String gender;
    private Seat bookedSeat; // Reference to the specific seat booked

    public Passenger(String name, int age, String gender, Seat bookedSeat) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bookedSeat = bookedSeat;
    }

    // --- Getters ---
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public Seat getBookedSeat() { return bookedSeat; }

    @Override
    public String toString() {
        // Simple string representation, potentially enhance if needed
        return "\033[1;36mPassenger:\033[0m " + name +
                " \033[1m| Age:\033[0m " + age +
                " \033[1m| Gender:\033[0m " + gender +
                " \033[1m| Seat:\033[0m " + (bookedSeat != null ? bookedSeat.getSeatId() : "N/A");
    }
}