package model;

import java.sql.Timestamp;

public class Feedback {
    private int feedbackId;
    private int orderId;
    private int token;
    private int customerId;
    private String customerName;
    private int rating; // 1 to 5
    private String comments;
    private Timestamp createdAt;

    public Feedback() {}

    public Feedback(int feedbackId, int orderId, int token, int customerId, String customerName, int rating, String comments, Timestamp createdAt) {
        this.feedbackId = feedbackId;
        this.orderId = orderId;
        this.token = token;
        this.customerId = customerId;
        this.customerName = customerName;
        this.rating = rating;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getToken() { return token; }
    public void setToken(int token) { this.token = token; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getStarDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                sb.append("★");
            } else {
                sb.append("☆");
            }
        }
        return sb.toString();
    }
}
