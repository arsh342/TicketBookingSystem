package TicketBookingSystem;

public class SeatMap {
    public static void printPlane(String type, boolean[][] seats) {
        char[] labels = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
        System.out.println("\nSeat Map (" + type + "):");

        System.out.print("     ");
        for (int j = 0; j < seats[0].length; j++) {
            System.out.print(labels[j] + " ");
            if (type.equals("Economy") && (j + 1) % 3 == 0 && j != seats[0].length - 1) System.out.print("  ");
            if (type.equals("Business") && (j == 0 || j == 1)) System.out.print("  ");
        }
        System.out.println();


        for (int i = 0; i < seats.length; i++) {
            System.out.printf("Row %2d: ", i + 1);
            for (int j = 0; j < seats[i].length; j++) {
                System.out.print((seats[i][j] ? "X" : "O") + " ");
                if (type.equals("Economy") && (j + 1) % 3 == 0 && j != seats[i].length - 1) System.out.print("  ");
                if (type.equals("Business") && (j == 0 || j == 1)) System.out.print("  ");
            }
            System.out.println();
        }
    }

    public static void printTrain(boolean[][] seats) {
        System.out.println("\nTrain Seat Map:");

        System.out.print("     ");
        for (int j = 0; j < seats[0].length; j++) {
            System.out.print((j + 1) + " ");
            if (j == 2) System.out.print("  ");
        }
        System.out.println();

        for (int i = 0; i < seats.length; i++) {
            System.out.printf("Row %2d: ", i + 1);
            for (int j = 0; j < seats[i].length; j++) {
                System.out.print((seats[i][j] ? "X" : "O") + " ");
                if (j == 2) System.out.print("  ");
            }
            System.out.println();
        }
    }

    public static void printBus(boolean[][] seats) {
        System.out.println("\nBus Seat Map:");

        System.out.print("     ");
        for (int j = 0; j < seats[0].length; j++) {
            System.out.print((j + 1) + " ");
            if (j == 1) System.out.print("  ");
        }
        System.out.println();

        for (int i = 0; i < seats.length; i++) {
            System.out.printf("Row %2d: ", i + 1);
            for (int j = 0; j < seats[i].length; j++) {
                System.out.print((seats[i][j] ? "X" : "O") + " ");
                if (j == 1) System.out.print("  ");
            }
            System.out.println();
        }
    }
}