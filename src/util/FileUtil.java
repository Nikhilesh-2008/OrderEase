package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import model.Order;
import model.OrderItem;

public class FileUtil {

    public static String generateReceiptText(Order order, String customerName) {
        if (order == null) return "No order details available.";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = order.getOrderTime() != null ? sdf.format(order.getOrderTime()) : "N/A";

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("               ORDEREASE                 \n");
        sb.append("    Smart Campus Canteen Management     \n");
        sb.append("=========================================\n");
        sb.append("Token Number : TK-").append(order.getToken()).append("\n");
        sb.append("Order ID     : #").append(order.getOrderId()).append("\n");
        sb.append("Customer     : ").append(customerName != null ? customerName : "Campus Student / Staff").append("\n");
        sb.append("Date & Time  : ").append(timeStr).append("\n");
        sb.append("Status       : ").append(order.getStatus()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-22s %5s %10s\n", "Item", "Qty", "Price"));
        sb.append("-----------------------------------------\n");
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem it : order.getItems()) {
                sb.append(String.format("%-22s %5d %10.2f\n", it.getItemName(), it.getQuantity(), it.getSubtotal()));
            }
        } else if (order.itemName != null) {
            sb.append(String.format("%-22s %5d %10.2f\n", order.itemName, order.quantity, order.getTotalAmount()));
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("TOTAL AMOUNT: Rs. %.2f\n", order.getTotalAmount()));
        sb.append("=========================================\n");
        sb.append("      Skip the Queue, Not the Food!      \n");
        sb.append("=========================================\n");
        return sb.toString();
    }

    public static boolean saveOrderReceipt(Order order, String customerName, String filepath) {
        if (order == null) return false;

        String receiptContent = generateReceiptText(order, customerName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
            writer.print(receiptContent);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
