package TicketBookingSystem;

import java.util.Scanner;

public class Plane {
    private boolean[][] economy;
    private boolean[][] business;
    private boolean[][] firstClass;

    public Plane() {
        economy = new boolean[10][9]; // 10 rows, 9 seats (3-3-3)
        business = new boolean[8][4]; // 8 rows, 4 seats (1-2-1)
        firstClass = new boolean[5][2]; // 5 rows, 2 seats (1-1)
    }

    public void book(Scanner sc) {
        System.out.print("Enter source: ");
        String source = sc.nextLine();
        System.out.print("Enter destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter departure date (YYYY-MM-DD): ");
        String date = sc.nextLine();
        System.out.print("Is this a return trip? (yes/no): ");
        String isReturn = sc.nextLine();
        System.out.print("Enter number of passengers: ");
        int passengers = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter class (Economy/Business/First): ");
        String cls = sc.nextLine().toLowerCase();

        boolean[][] selectedClass;
        int pricePerSeat;

        switch (cls) {
            case "economy":
                selectedClass = economy;
                pricePerSeat = 100;
                break;
            case "business":
                selectedClass = business;
                pricePerSeat = 250;
                break;
            case "first":
                selectedClass = firstClass;
                pricePerSeat = 500;
                break;
            default:
                System.out.println("Invalid class.");
                return;
        }

        SeatMap.printPlane(cls.substring(0, 1).toUpperCase() + cls.substring(1), selectedClass);

        for (int i = 0; i < passengers; i++) {
            System.out.print("Example: To book Row 2 Seat C, enter: 2 C\n");
            System.out.print("Enter seat (row letter): ");
            int row = sc.nextInt();
            char seatChar = sc.next().charAt(0);
            int col = seatChar - 'A';
            row--;

            if (row >= 0 && row < selectedClass.length && col >= 0 && col < selectedClass[0].length) {
                if (!selectedClass[row][col]) {
                    selectedClass[row][col] = true;
                    System.out.println("Seat booked successfully.");
                } else {
                    System.out.println("Seat already reserved.");
                }
            } else {
                System.out.println("Invalid seat.");
            }
        }

        int totalCost = pricePerSeat * passengers;
        if (isReturn.equalsIgnoreCase("yes")) {
            totalCost *= 2;
        }

        System.out.println("Total Price: $" + totalCost);
    }
}
