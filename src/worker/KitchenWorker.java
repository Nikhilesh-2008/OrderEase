package worker;

import javax.swing.SwingWorker;
import java.util.List;
import model.Order;
import repository.OrderRepository;

public class KitchenWorker extends SwingWorker<List<Order>, Void> {

    private final OrderRepository orderRepository;
    private final KitchenUpdateListener listener;

    public interface KitchenUpdateListener {
        void onOrdersLoaded(List<Order> orders);
    }

    public KitchenWorker(OrderRepository orderRepository, KitchenUpdateListener listener) {
        this.orderRepository = orderRepository;
        this.listener = listener;
    }

    @Override
    protected List<Order> doInBackground() {
        return orderRepository.getAllActiveOrders();
    }

    @Override
    protected void done() {
        try {
            List<Order> orders = get();
            if (listener != null) {
                listener.onOrdersLoaded(orders);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
