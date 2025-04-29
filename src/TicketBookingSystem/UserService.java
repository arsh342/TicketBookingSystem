package TicketBookingSystem;

import java.util.HashMap;
import java.util.Scanner;

public class UserService {
    private final HashMap<String, String> users = new HashMap<>();

    public void register(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (users.containsKey(username)) {
            System.out.println("User already exists!");
        } else {
            users.put(username, password);
            System.out.println("Registered successfully!");
        }
    }

    public boolean login(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (users.containsKey(username) && users.get(username).equals(password)) {
            System.out.println("Login successful!");
            return true;
        } else {
            System.out.println("Invalid credentials.");
            return false;
        }
    }
}