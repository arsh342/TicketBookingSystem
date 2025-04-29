package TicketBookingSystem;

/**
 * Represents a Seat (Available/Reserved)
 */
public class Seat {
    private boolean reserved;

    public Seat() {
        this.reserved = false;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void reserve() {
        this.reserved = true;
    }
}
