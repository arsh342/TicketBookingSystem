package TicketBookingSystem;

/**
 * Custom Linked List Implementation
 * No use of built-in LinkedList libraries.
 * @param <T> generic type
 */
public class CustomLinkedList<T> {
    private Node<T> head;

    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> temp = head;
            while (temp.next != null) {
                temp = temp.next;
            }
            temp.next = newNode;
        }
    }

    public boolean exists(String username, String password) {
        Node<T> temp = head;
        while (temp != null) {
            User user = (User) temp.data;
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
            temp = temp.next;
        }
        return false;
    }

    public boolean usernameExists(String username) {
        Node<T> temp = head;
        while (temp != null) {
            User user = (User) temp.data;
            if (user.getUsername().equals(username)) {
                return true;
            }
            temp = temp.next;
        }
        return false;
    }
}

class Node<T> {
    T data;
    Node<T> next;
    Node(T data) {
        this.data = data;
        this.next = null;
    }
}
