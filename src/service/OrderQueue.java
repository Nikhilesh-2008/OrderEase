package service;

import java.util.LinkedList;
import java.util.Queue;
import model.Order;

public class OrderQueue {

    Queue<Order> orders = new LinkedList<>();
    int tokenNumber = 100;

    public synchronized void placeOrder(Order order) {
        tokenNumber++;

        order.token = tokenNumber;
        order.status = "PLACED";

        orders.add(order);

        System.out.println("\nOrder placed successfully!");
        System.out.println("Your token number is: " + order.token);

        notifyAll();
    }

    public synchronized Order getOrder() {
        while (orders.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return orders.remove();
    }
}