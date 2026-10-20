package service;

import model.Order;

public class Kitchen extends Thread {

    OrderQueue queue;

    public Kitchen(OrderQueue queue) {
        this.queue = queue;
    }

    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

            Order order = queue.getOrder();

            if (order == null) {
                break;
            }

            order.status = "PREPARING";

            System.out.println("\nChef received the order!");
            System.out.println("Token: " + order.token);
            System.out.println("Food: " + order.itemName);
            System.out.println("Quantity: " + order.quantity);
            System.out.println("Status: " + order.status);

            try {
                // Simulates the chef preparing the food.
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            order.status = "READY";

            System.out.println("\n----------------------------");
            System.out.println("ORDER READY!");
            System.out.println("Token: " + order.token);
            System.out.println("Food: " + order.itemName);
            System.out.println("Please collect your order.");
            System.out.println("----------------------------");
        }
    }
}