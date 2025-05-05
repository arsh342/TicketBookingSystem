package TicketBookingSystem;

import java.util.Iterator; // Required for Iterable
import java.util.NoSuchElementException; // Required for Iterator implementation

/**
 * A generic custom singly linked list implementation.
 * Used in this project to manage lists of Seats or Passengers for each vehicle.
 * Implements Iterable to allow for use in enhanced for-loops.
 *
 * @param <T> The type of elements held in this list.
 */
public class CustomLinkedList<T> implements Iterable<T> { // Implement Iterable

    /**
     * Inner class representing a node in the linked list.
     * @param <T> The type of data stored in the node.
     */
    public static class Node<T> {
        public T data;
        public Node<T> next;

        /**
         * Constructs a Node with the given data.
         * @param data The data to store in this node.
         */
        public Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head; // Reference to the first node in the list
    private int size;     // Number of elements in the list

    /**
     * Constructs an empty CustomLinkedList.
     */
    public CustomLinkedList() {
        this.head = null;
        this.size = 0;
    }

    /**
     * Adds the specified data to the end of the list.
     * Handles null data gracefully (stores null if added).
     * @param data The data to add to the list.
     */
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            // List is empty, new node becomes the head
            head = newNode;
        } else {
            // Traverse to the end of the list
            Node<T> temp = head;
            while (temp.next != null) {
                temp = temp.next;
            }
            // Add the new node at the end
            temp.next = newNode;
        }
        size++; // Increment the size
    }

    /**
     * Removes the first occurrence of the specified data from the list.
     * Uses the .equals() method for comparison. Handles null data removal.
     * @param data The data to remove.
     * @return true if the data was found and removed, false otherwise.
     */
    public boolean remove(T data) {
        if (head == null) {
            return false; // List is empty
        }

        // Check if the head node contains the data
        // Handle cases where head.data or data might be null
        if (java.util.Objects.equals(head.data, data)) {
            head = head.next; // Remove head node
            size--;
            return true;
        }

        // Search for the node *before* the one to remove
        Node<T> current = head;
        while (current.next != null && !java.util.Objects.equals(current.next.data, data)) {
            current = current.next;
        }

        // If data was found (current.next is the node to remove)
        if (current.next != null) {
            current.next = current.next.next; // Bypass the node to remove
            size--;
            return true;
        }

        return false; // Data not found
    }

    /**
     * Checks if the list contains the specified data.
     * Uses the .equals() method for comparison. Handles null data check.
     * @param data The data to search for.
     * @return true if the data is found in the list, false otherwise.
     */
    public boolean contains(T data) {
        Node<T> temp = head;
        while (temp != null) {
            if (java.util.Objects.equals(temp.data, data)) {
                return true; // Data found
            }
            temp = temp.next;
        }
        return false; // Data not found
    }

    /**
     * Displays the content of the linked list to the console.
     * Uses ANSI escape codes for basic coloring.
     * Consider returning a String or using the iterator for more flexible display options.
     */
    public void display() {
        if (head == null) {
            System.out.println("\033[1;33m(List is empty)\033[0m"); // Use yellow for empty list
            return;
        }

        System.out.println("\n\033[1;36mLinked List Content (" + size + " items):\033[0m");
        Node<T> temp = head;
        StringBuilder sb = new StringBuilder();
        while (temp != null) {
            // Check for null data before calling toString()
            String dataStr = (temp.data == null) ? "null" : temp.data.toString();
            sb.append("\033[32m").append(dataStr).append("\033[0m -> ");
            temp = temp.next;
        }
        sb.append("\033[1;34mnull\033[0m"); // Blue for null end marker
        System.out.println(sb.toString());
    }

    /**
     * Removes all elements from the list, making it empty.
     */
    public void clear() {
        head = null;
        size = 0;
    }

    /**
     * Returns the number of elements in the list.
     * @return The size of the list.
     */
    public int getSize() {
        return size;
    }

    /**
     * Checks if the list is empty.
     * @return true if the list contains no elements, false otherwise.
     */
    public boolean isEmpty() {
        return size == 0; // Or check if head == null
    }


    /**
     * Returns the head node of the list.
     * Useful for manual traversal if needed, but iteration via iterator() is preferred.
     * @return The first Node<T> in the list, or null if the list is empty.
     */
    public Node<T> getHead() {
        return head;
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * Allows the list to be used in enhanced for-loops.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    /**
     * Private inner class to implement the Iterator interface.
     */
    private class LinkedListIterator implements Iterator<T> {
        private Node<T> current = head; // Start iterator at the head

        /**
         * Checks if there are more elements to iterate over.
         * @return true if the iteration has more elements.
         */
        @Override
        public boolean hasNext() {
            return current != null; // True if current node is not null
        }

        /**
         * Returns the next element in the iteration.
         * @return the next element in the iteration.
         * @throws NoSuchElementException if the iteration has no more elements.
         */
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in the list.");
            }
            T data = current.data;    // Get data from current node
            current = current.next; // Move to the next node
            return data;             // Return the data
        }

        /**
         * Remove operation is not supported by this iterator.
         * @throws UnsupportedOperationException always.
         */
        @Override
        public void remove() {
            // Optional: Implement remove if needed, but it's complex for singly linked list iterator.
            // Requires keeping track of the previous node.
            throw new UnsupportedOperationException("Remove operation is not supported by this iterator.");
        }
    }

} // End of CustomLinkedList class