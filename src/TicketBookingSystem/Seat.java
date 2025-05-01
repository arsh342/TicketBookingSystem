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
        this.column = column;
        this.seatClass = seatClass;
        this.transportType = transportType;
        this.price = price;
        this.isReserved = false;
    }

    public int getRow() {
        return row;
    }

    public String getColumn() {
        return column;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public String getTransportType() {
        return transportType;
    }

    public double getPrice() {
        return price;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void reserve() {
        isReserved = true;
    }

    public void unreserve() {
        isReserved = false;
    }

    @Override
    public String toString() {
        String status = isReserved ? "\033[31mX\033[0m" : "\033[32mO\033[0m";
        return status + "(" + row + column + ")";
    }
}