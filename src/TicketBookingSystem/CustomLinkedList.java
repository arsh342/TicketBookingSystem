package TicketBookingSystem;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A generic custom singly linked list implementation.
 * Implements Iterable to allow for use in enhanced for-loops.
 * @param <T> The type of elements held in this list.
 */
public class CustomLinkedList<T> implements Iterable<T> {

    /** Inner Node class */
    public static class Node<T> {
        public T data;
        public Node<T> next;
        public Node(T data) { this.data = data; this.next = null; }
    }

    private Node<T> head;
    private int size;

    /** Constructs an empty list. */
    public CustomLinkedList() { this.head = null; this.size = 0; }

    /** Adds data to the end of the list. */
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) { head = newNode; }
        else { Node<T> temp = head; while (temp.next != null) { temp = temp.next; } temp.next = newNode; }
        size++;
    }

    /** Removes the first occurrence of the data. Returns true if removed. */
    public boolean remove(T data) {
        if (head == null) { return false; }
        if (java.util.Objects.equals(head.data, data)) { head = head.next; size--; return true; }
        Node<T> current = head;
        while (current.next != null && !java.util.Objects.equals(current.next.data, data)) { current = current.next; }
        if (current.next != null) { current.next = current.next.next; size--; return true; }
        return false;
    }

    /** Checks if the list contains the data. */
    public boolean contains(T data) {
        Node<T> temp = head;
        while (temp != null) { if (java.util.Objects.equals(temp.data, data)) { return true; } temp = temp.next; }
        return false;
    }

    /** Displays the list content to the console. */
    public void display() {
        if (head == null) { System.out.println("\033[1;33m(List is empty)\033[0m"); return; }
        System.out.println("\n\033[1;36mLinked List Content (" + size + " items):\033[0m");
        Node<T> temp = head; StringBuilder sb = new StringBuilder();
        while (temp != null) { String dataStr = (temp.data == null) ? "null" : temp.data.toString();
            sb.append("\033[32m").append(dataStr).append("\033[0m -> "); temp = temp.next; }
        sb.append("\033[1;34mnull\033[0m"); System.out.println(sb.toString());
    }

    /** Clears the list. */
    public void clear() { head = null; size = 0; }

    /** Returns the number of elements. */
    public int getSize() { return size; }

    /** Checks if the list is empty. */
    public boolean isEmpty() { return size == 0; }

    /** Returns the head node. */
    public Node<T> getHead() { return head; }

    /** Returns an iterator over elements. */
    @Override
    public Iterator<T> iterator() { return new LinkedListIterator(); }

    /** Private Iterator implementation. */
    private class LinkedListIterator implements Iterator<T> {
        private Node<T> current = head;
        @Override public boolean hasNext() { return current != null; }
        @Override public T next() { if (!hasNext()) throw new NoSuchElementException(); T data = current.data; current = current.next; return data; }
        @Override public void remove() { throw new UnsupportedOperationException(); }
    }
}