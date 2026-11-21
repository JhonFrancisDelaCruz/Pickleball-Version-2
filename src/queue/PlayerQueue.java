package queue;

import model.Player;
import java.util.ArrayList;
import java.util.List;

public class PlayerQueue {

    private static class Node {
        Player data;
        Node next;
        Node(Player data) {
            this.data = data;
        }
    }

    private Node front; 
    private Node rear; 
    private int size;

    public void enqueue(Player player) {
        Node node = new Node(player);
        if (isEmpty()) {
            front = node;
        } else {
            rear.next = node;
        }
        rear = node;
        size++;
    }

    public Player dequeue() {
        if (isEmpty()) {
            return null;
        }
        Player player = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return player;
    }

    public Player peek() {
        return isEmpty() ? null : front.data;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public String display() {
        StringBuilder sb = new StringBuilder();
        for (Node cur = front; cur != null; cur = cur.next) {
            sb.append("  ").append(cur.data.getName()).append(" - ").append(cur.data.getId());
            if (!cur.data.isEligible()) {
                sb.append("  (skips next game)");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public int size() {
        return size;
    }

    public List<Player> toList() {
        List<Player> list = new ArrayList<>();
        for (Node cur = front; cur != null; cur = cur.next) {
            list.add(cur.data);
        }
        return list;
    }

    public Player remove(String playerId) {
        Node prev = null;
        for (Node cur = front; cur != null; prev = cur, cur = cur.next) {
            if (cur.data.getId().equalsIgnoreCase(playerId)) {
                if (prev == null) {
                    front = cur.next;
                } else {
                    prev.next = cur.next;}
                
                if (cur == rear) { rear = prev;}
                size--;
                return cur.data;
            }
        }
        return null;
    }
}
