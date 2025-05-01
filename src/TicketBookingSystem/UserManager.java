package TicketBookingSystem;

import java.util.HashMap;

public class UserManager {
    private HashMap<String, String> users = new HashMap<>();

    public UserManager() {
        users = StorageManager.loadUsers(); // Load existing users at startup
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, password);
        StorageManager.saveUsers(users); // Save users after registration
        return true;
    }

    public boolean login(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
}