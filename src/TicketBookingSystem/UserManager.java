package TicketBookingSystem;

import java.util.HashMap;
import java.util.Scanner;

public class UserManager {
    private HashMap<String, String> users;

    public UserManager() {
        users = new HashMap<>();
    }

    public void register(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        if (users.containsKey(username)) {
            System.out.println("Username already exists. Try logging in.");
            return;
        }
        System.out.print("Enter password: ");
        String password = sc.nextLine();
        users.put(username, password);
        System.out.println("Registration successful.");
    }

    public boolean login(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();
        if (users.containsKey(username) && users.get(username).equals(password)) {
            System.out.println("Login successful.");
            return true;
        } else {
            System.out.println("Invalid credentials. Try again.");
            return false;
        }
    }
}
