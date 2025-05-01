package TicketBookingSystem;

public class SeatMap {
    public static void printPlane(String cls, boolean[][] seats) {
        System.out.println("\n\033[1;36mSeat Map for Plane - " + cls + "\033[0m");
        for (int i = 0; i < seats.length; i++) {
            System.out.print("\033[1mRow " + (i + 1) + ":\033[0m ");
            for (int j = 0; j < seats[i].length; j++) {
                String seatStatus = seats[i][j] ? "\033[31mX\033[0m" : "\033[32mO\033[0m";
                System.out.print(seatStatus + " ");
                if ((cls.equalsIgnoreCase("Economy") && (j == 2 || j == 5)) ||
                        (cls.equalsIgnoreCase("Business") && j == 0) ||
                        (cls.equalsIgnoreCase("First") && j == 0)) {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
        System.out.println("\n\033[1mLegend:\033[0m");
        System.out.println("\033[32mO\033[0m - Available");
        System.out.println("\033[31mX\033[0m - Reserved");
    }

    public static void printTrain(boolean[][] seats) {
        System.out.println("\n\033[1;36mSeat Map for Train:\033[0m");
        for (int i = 0; i < seats.length; i++) {
            System.out.print("\033[1mRow " + (i + 1) + ":\033[0m ");
            for (int j = 0; j < seats[i].length; j++) {
                String seatStatus = seats[i][j] ? "\033[31mX\033[0m" : "\033[32mO\033[0m";
                System.out.print(seatStatus + " ");
                if (j == 2) System.out.print("  ");
            }
            System.out.println();
        }
        System.out.println("\n\033[1mLegend:\033[0m");
        System.out.println("\033[32mO\033[0m - Available");
        System.out.println("\033[31mX\033[0m - Reserved");
    }

    public static void printBus(boolean[] seats) {
        System.out.println("\n\033[1;36mSeat Map for Bus:\033[0m");
        for (int i = 0; i < 10; i++) {
            System.out.print("\033[1mRow " + (i + 1) + ":\033[0m ");
            for (int j = 0; j < 4; j++) {
                String seatStatus = seats[i * 4 + j] ? "\033[31mX\033[0m" : "\033[32mO\033[0m";
                System.out.print(seatStatus + " ");
                if (j == 1) System.out.print("  ");
            }
            System.out.println();
        }
        System.out.println("\n\033[1mLegend:\033[0m");
        System.out.println("\033[32mO\033[0m - Available");
        System.out.println("\033[31mX\033[0m - Reserved");
    }
}