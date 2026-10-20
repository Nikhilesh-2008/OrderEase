package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {

    public int orderId;
    public int token;
    public int customerId;
    public String customerName;
    public Timestamp orderTime;
    public double totalAmount;
    public String status;

    // For single item legacy compatibility
    public String itemName;
    public int quantity;

    // Full multi-item list
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
        this.status = "PLACED";
    }

    public Order(String itemName, int quantity, double price) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.totalAmount = quantity * price;
        this.status = "PLACED";
    }

    public Order(int orderId, int token, int customerId, String customerName, Timestamp orderTime, double totalAmount, String status) {
        this.orderId = orderId;
        this.token = token;
        this.customerId = customerId;
        this.customerName = customerName;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        if (items != null && !items.isEmpty()) {
            this.itemName = items.get(0).getItemName();
            this.quantity = items.get(0).getQuantity();
        }
    }

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        if (this.itemName == null) {
            this.itemName = item.getItemName();
            this.quantity = item.getQuantity();
        }
    }

    public String getItemsSummary() {
        if (items == null || items.isEmpty()) {
            if (itemName != null) {
                return itemName + " x " + quantity;
            }
            return "No items";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            OrderItem it = items.get(i);
            sb.append(it.getItemName()).append(" x ").append(it.getQuantity());
            if (i < items.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public void displayOrder() {
        System.out.println("\n----- ORDER DETAILS -----");
        System.out.println("Order ID: " + orderId);
        System.out.println("Token: TK-" + token);
        System.out.println("Customer ID: " + customerId);
        System.out.println("Items: " + getItemsSummary());
        System.out.println("Total: Rs." + String.format("%.2f", totalAmount));
        System.out.println("Status: " + status);
    }
}