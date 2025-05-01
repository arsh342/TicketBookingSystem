package TicketBookingSystem;

/**
 * Generic custom linked list implementation to manage seats, bookings, and passengers.
 */
public class CustomLinkedList<T> {
    private Node<T> head;
    private int size;

    public CustomLinkedList() {
        head = null;
        size = 0;
    }

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
        size++;
    }

    public boolean remove(T data) {
        if (head == null) return false;

        if (head.data.equals(data)) {
            head = head.next;
            size--;
            return true;
        }

        Node<T> current = head;
        while (current.next != null && !current.next.data.equals(data)) {
            current = current.next;
        }

        if (current.next == null) return false;

        current.next = current.next.next;
        size--;
        return true;
    }

    public boolean contains(T data) {
        Node<T> temp = head;
        while (temp != null) {
            if (temp.data.equals(data)) return true;
            temp = temp.next;
        }
        return false;
    }

    public void display() {
        if (head == null) {
            System.out.println("\033[1;31mList is empty.\033[0m");
            return;
        }

        System.out.println("\n\033[1;36mLinked List Content:\033[0m");
        Node<T> temp = head;
        while (temp != null) {
            System.out.print("\033[32m" + temp.data + "\033[0m -> ");
            temp = temp.next;
        }
        System.out.println("\033[1;34mnull\033[0m");
    }

    // Add clear method to reset the list
    public void clear() {
        head = null;
        size = 0;
    }

    public int getSize() {
        return size;
    }

    public Node<T> getHead() {
        return head;
    }

    public static class Node<T> {
        public T data;
        public Node<T> next;

        public Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
}