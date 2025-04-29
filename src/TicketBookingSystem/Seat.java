package TicketBookingSystem;

public class Seat {
    private String seatId;
    private boolean isBooked;
    private String seatType; // e.g., "Economy", "Business", "Lower Berth"

    public Seat(String seatId, String seatType) {
        this.seatId = seatId;
        this.seatType = seatType;
        this.isBooked = false;
    }

    public String getSeatId() {
        return seatId;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void book() {
        this.isBooked = true;
    }

    public String getSeatType() {
        return seatType;
    }

    @Override
    public String toString() {
        return isBooked ? "X" : "O";
    }
}
