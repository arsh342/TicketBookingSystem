package TicketBookingSystem;

/**
 * Represents a seat with metadata like row, column, class, type, price, and status.
 */
public class Seat {
    private final int row;
    private final String column;
    private final String seatClass;
    private final String transportType; // Plane, Train, Bus
    private final double price;
    private boolean isReserved;

    public Seat(int row, String column, String seatClass, String transportType, double price) {
        this.row = row;
        this.column = column; // Assuming column is like "A", "B" etc.
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
    public void reserve() { isReserved = true; }
    public void unreserve() { isReserved = false; }

    /**
     * Gets a string representation of the seat ID (e.g., "5B", "10A").
     * @return The seat ID string.
     */
    public String getSeatId() {
        return "" + row + column;
    }

    @Override
    public String toString() {
        // Display like O(5B) or X(10A)
        String status = isReserved ? "\033[31mX\033[0m" : "\033[32mO\033[0m";
        return status + "(" + getSeatId() + ")";
    }

    // Optional: equals() and hashCode() if seats need to be compared directly
    // based on row, column, and potentially transport type/class
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return row == seat.row &&
                column.equals(seat.column) &&
                seatClass.equals(seat.seatClass) &&
                transportType.equals(seat.transportType);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(row, column, seatClass, transportType);
    }
}