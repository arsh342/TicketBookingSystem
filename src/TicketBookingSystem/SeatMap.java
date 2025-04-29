package TicketBookingSystem;

public class SeatMap {
    public static void printPlane(String cls, boolean[][] seats) {
        System.out.println("\nSeat Map for Plane - " + cls);
        for (int i = 0; i < seats.length; i++) {
            System.out.print("Row " + (i + 1) + ": ");
            for (int j = 0; j < seats[i].length; j++) {
                System.out.print((seats[i][j] ? "X" : "O") + " ");
                if ((cls.equalsIgnoreCase("Economy") && (j == 2 || j == 5)) ||
                        (cls.equalsIgnoreCase("Business") && j == 0) ||
                        (cls.equalsIgnoreCase("First") && j == 0)) {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
    }

    public static void printTrain(boolean[][] seats) {
        System.out.println("\nSeat Map for Train:");
        for (int i = 0; i < seats.length; i++) {
            System.out.print("Row " + (i + 1) + ": ");
            for (int j = 0; j < seats[i].length; j++) {
                System.out.print((seats[i][j] ? "X" : "O") + " ");
                if (j == 2) System.out.print("  ");
            }
            System.out.println();
        }
    }

    public static void printBus(boolean[] seats) {
        System.out.println("\nSeat Map for Bus:");
        for (int i = 0; i < 10; i++) {
            System.out.print("Row " + (i + 1) + ": ");
            for (int j = 0; j < 4; j++) {
                System.out.print((seats[i * 4 + j] ? "X" : "O") + " ");
                if (j == 1) System.out.print("  ");
            }
            System.out.println();
        }
    }
}
