package TicketBookingSystem;

public abstract class Transport {
    protected String source;
    protected String destination;
    protected String date;
    protected int pricePerTicket;

    public abstract void book();

    public abstract void showAvailable();
}
