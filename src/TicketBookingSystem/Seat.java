package TicketBookingSystem;

/**
 * Represents a seat with metadata like row, column, class, type, price, and status.
 */
public class Seat {
    private final int row;
    private final String column; // e.g., "A", "B"
    private final String seatClass;
    private final String transportType; // Plane, Train, Bus
    private final double price;
    private boolean isReserved;

    public Seat(int row, String column, String seatClass, String transportType, double price) {
        this.row = row;
        this.column = column;
        this.seatClass = seatClass;
        this.transportType = transportType;
        this.price = price;
        this.isReserved = false;
    }

    // --- Getters ---
    public int getRow() { return row; }
    public String getColumn() { return column; }
    public String getSeatClass() { return seatClass; }
    public String getTransportType() { return transportType; }
    public double getPrice() { return price; }
    public boolean isReserved() { return isReserved; }

    // --- Modifiers ---
    public void reserve() { this.isReserved = true; }
    public void unreserve() { this.isReserved = false; }

    /**
     * Gets a string representation of the seat ID (e.g., "5B", "10A").
     * @return The seat ID string.
     */
    public String getSeatId() {
        return "" + row + column;
    }

    /**
     * Returns a string representation for display in seat maps.
     * Example: O(1A) for available, X(1A) for reserved.
     */
    @Override
    public String toString() {
        String status = isReserved ? "\033[31mX\033[0m" : "\033[32mO\033[0m"; // Red X, Green O
        return status + "(" + getSeatId() + ")";
    }

    // Optional: equals/hashCode if needed for Set/Map operations based on seat identity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        // Define equality based on row, column, and potentially type/class if needed
        return row == seat.row &&
                java.util.Objects.equals(column, seat.column) &&
                java.util.Objects.equals(transportType, seat.transportType); // Example
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(row, column, transportType); // Example
    }
}