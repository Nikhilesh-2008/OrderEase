package gui;

import model.Order;
import model.OrderItem;
import repository.FoodRepository;
import repository.OrderRepository;
import util.UIUtils;
import util.UIUtils.RoundedButton;
import util.UIUtils.RoundedPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KitchenFrame extends JFrame {

    private final OrderRepository orderRepository = new OrderRepository();
    private final FoodRepository foodRepository = new FoodRepository();

    private DefaultTableModel tableModel;
    private JTable ordersTable;
    private List<Order> currentOrdersList = new ArrayList<>();
    private List<Order> filteredOrdersList = new ArrayList<>();
    private JLabel queueCountLabel;

    private JComboBox<String> canteenStationBox;
    private JCheckBox autoRefreshToggle;
    private Timer autoRefreshTimer;
    private final Set<Integer> knownOrderIds = new HashSet<>();

    public KitchenFrame() {
        UIUtils.setupDarkUI();

        setTitle("OrderEase | Kitchen Display System (Real-time Campus Processing)");
        setSize(1080, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BACKGROUND);
        setLayout(new BorderLayout());

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Center Content
        JPanel contentPanel = new JPanel(new BorderLayout(16, 16));
        contentPanel.setBackground(UIUtils.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Subheader filter bar
        RoundedPanel filterBar = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        filterBar.setLayout(new BorderLayout());
        filterBar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftFilter.setOpaque(false);

        JLabel stationLbl = new JLabel("Canteen Counter / Station:");
        stationLbl.setFont(UIUtils.BODY_BOLD);
        stationLbl.setForeground(UIUtils.WHITE);

        String[] stations = {
            "All Campus Canteens",
            FoodRepository.CANTEEN_SNACKS,
            FoodRepository.CANTEEN_CHAI,
            FoodRepository.CANTEEN_FOOD_COURT
        };
        canteenStationBox = new JComboBox<>(stations);
        canteenStationBox.setPreferredSize(new Dimension(270, 36));
        canteenStationBox.setBackground(UIUtils.INPUT_BG);
        canteenStationBox.setForeground(UIUtils.WHITE);
        canteenStationBox.setFont(UIUtils.BODY_FONT);
        canteenStationBox.addActionListener(e -> applyCanteenFilter());

        leftFilter.add(stationLbl);
        leftFilter.add(canteenStationBox);

        JPanel rightFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightFilter.setOpaque(false);

        autoRefreshToggle = new JCheckBox("Live Auto-Refresh (5s)", true);
        autoRefreshToggle.setFont(UIUtils.BODY_BOLD);
        autoRefreshToggle.setForeground(UIUtils.ORANGE);
        autoRefreshToggle.setBackground(UIUtils.CARD);
        autoRefreshToggle.setFocusPainted(false);

        rightFilter.add(autoRefreshToggle);

        filterBar.add(leftFilter, BorderLayout.WEST);
        filterBar.add(rightFilter, BorderLayout.EAST);

        contentPanel.add(filterBar, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"Token", "Order ID", "Customer", "Food Items", "Ordered At", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(tableModel);
        UIUtils.styleTable(ordersTable);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom token badge renderer
        ordersTable.getColumnModel().getColumn(0).setCellRenderer(new UIUtils.TokenBadgeRenderer());
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(95);

        // Custom status pill badge renderer
        ordersTable.getColumnModel().getColumn(5).setCellRenderer(new UIUtils.StatusBadgeRenderer());
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(130);

        JScrollPane scrollPane = UIUtils.createModernScrollPane(ordersTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons Bar
        RoundedPanel actionPanel = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        actionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 14));

        RoundedButton preparingButton = new RoundedButton("⚡ Start Preparing", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 8);
        preparingButton.setPreferredSize(new Dimension(175, 42));

        RoundedButton readyButton = new RoundedButton("✓ Mark READY", UIUtils.GREEN, UIUtils.GREEN_HOVER, Color.BLACK, 8);
        readyButton.setPreferredSize(new Dimension(165, 42));

        RoundedButton collectedButton = new RoundedButton("📦 Mark COLLECTED", UIUtils.BLUE, UIUtils.BLUE_HOVER, Color.WHITE, 8);
        collectedButton.setPreferredSize(new Dimension(185, 42));

        RoundedButton refreshButton = new RoundedButton("↻ Refresh Queue", UIUtils.CARD_HOVER, UIUtils.CARD_BORDER, UIUtils.WHITE, 8);
        refreshButton.setPreferredSize(new Dimension(165, 42));

        actionPanel.add(preparingButton);
        actionPanel.add(readyButton);
        actionPanel.add(collectedButton);
        actionPanel.add(refreshButton);

        contentPanel.add(actionPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Event Listeners
        preparingButton.addActionListener(e -> updateStatus("PREPARING", "PLACED", "Order is now PREPARING."));
        readyButton.addActionListener(e -> updateStatus("READY", "PREPARING", "Order marked READY for customer pickup!"));
        collectedButton.addActionListener(e -> updateStatus("COLLECTED", "READY", "Order marked COLLECTED. Token fulfilled!"));
        refreshButton.addActionListener(e -> loadActiveOrders());

        // Setup timer for hands-free live polling
        autoRefreshTimer = new Timer(5000, e -> {
            if (autoRefreshToggle != null && autoRefreshToggle.isSelected()) {
                loadActiveOrders();
            }
        });
        autoRefreshTimer.start();

        // Initial Load
        loadActiveOrders();

        setVisible(true);
    }

    @Override
    public void dispose() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        super.dispose();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.BACKGROUND_DARK);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.CARD_BORDER),
            BorderFactory.createEmptyBorder(16, 28, 16, 28)
        ));

        JPanel leftContainer = new JPanel();
        leftContainer.setOpaque(false);
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);

        JLabel title = new JLabel("OrderEase");
        title.setFont(new Font(UIUtils.TITLE_FONT.getFamily(), Font.BOLD, 26));
        title.setForeground(UIUtils.ORANGE);

        JPanel kdsTag = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UIUtils.BLUE);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        kdsTag.setOpaque(false);
        kdsTag.setPreferredSize(new Dimension(145, 24));
        JLabel kdsText = new JLabel("KITCHEN STATION (KDS)");
        kdsText.setFont(UIUtils.BADGE_FONT);
        kdsText.setForeground(UIUtils.BLUE_HOVER);
        kdsTag.add(kdsText);

        titleRow.add(title);
        titleRow.add(kdsTag);

        queueCountLabel = new JLabel("Live Queue: 0 orders in progress");
        queueCountLabel.setFont(UIUtils.SUBTITLE_FONT);
        queueCountLabel.setForeground(UIUtils.MUTED);

        JLabel pipelineLegend = new JLabel("Lifecycle: PLACED ➔ PREPARING ➔ READY ➔ COLLECTED");
        pipelineLegend.setFont(UIUtils.SMALL_FONT);
        pipelineLegend.setForeground(UIUtils.MUTED_DARK);

        leftContainer.add(titleRow);
        leftContainer.add(Box.createVerticalStrut(4));
        leftContainer.add(queueCountLabel);
        leftContainer.add(Box.createVerticalStrut(2));
        leftContainer.add(pipelineLegend);

        header.add(leftContainer, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setOpaque(false);

        RoundedButton tvBoardButton = new RoundedButton("📺 Launch TV Board", UIUtils.BLUE, UIUtils.BLUE_HOVER, Color.WHITE, 8);
        tvBoardButton.setFont(UIUtils.BUTTON_FONT);
        tvBoardButton.setPreferredSize(new Dimension(175, 36));
        tvBoardButton.addActionListener(e -> new TokenDisplayFrame());

        RoundedButton logoutButton = new RoundedButton("Logout", UIUtils.RED, UIUtils.RED_HOVER, Color.WHITE, 8);
        logoutButton.setFont(UIUtils.BUTTON_FONT);
        logoutButton.setPreferredSize(new Dimension(95, 36));

        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of Kitchen Dashboard?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                new LoginFrame();
                dispose();
            }
        });

        rightActions.add(tvBoardButton);
        rightActions.add(logoutButton);

        header.add(rightActions, BorderLayout.EAST);
        return header;
    }

    private void loadActiveOrders() {
        currentOrdersList = orderRepository.getAllActiveOrders();

        boolean newOrderArrived = false;
        for (Order ord : currentOrdersList) {
            if (!knownOrderIds.contains(ord.getOrderId())) {
                knownOrderIds.add(ord.getOrderId());
                newOrderArrived = true;
            }
        }

        if (newOrderArrived && knownOrderIds.size() > currentOrdersList.size()) {
            UIUtils.playNotificationChime();
        }

        applyCanteenFilter();
    }

    private void applyCanteenFilter() {
        tableModel.setRowCount(0);
        filteredOrdersList.clear();

        String selectedStation = canteenStationBox != null ? (String) canteenStationBox.getSelectedItem() : "All Campus Canteens";
        boolean filterStation = selectedStation != null && !selectedStation.contains("All");

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        for (Order ord : currentOrdersList) {
            // If filtering by station, check if order contains any item from this canteen
            if (filterStation) {
                boolean matches = false;
                if (ord.getItems() != null) {
                    for (OrderItem item : ord.getItems()) {
                        model.FoodItem fi = foodRepository.getFoodItemById(item.getItemId());
                        if (fi != null && selectedStation.equalsIgnoreCase(fi.getCanteen())) {
                            matches = true;
                            break;
                        }
                    }
                }
                if (!matches) {
                    continue;
                }
            }

            filteredOrdersList.add(ord);
            String timeStr = ord.getOrderTime() != null ? sdf.format(ord.getOrderTime()) : "-";
            String cust = ord.getCustomerName() != null ? ord.getCustomerName() : "Customer #" + ord.getCustomerId();
            tableModel.addRow(new Object[]{
                "TK-" + ord.getToken(),
                "#" + ord.getOrderId(),
                cust,
                ord.getItemsSummary(),
                timeStr,
                ord.getStatus()
            });
        }

        if (queueCountLabel != null) {
            queueCountLabel.setText("Active Queue: " + filteredOrdersList.size() + " orders requiring preparation/pickup");
        }
    }

    private void updateStatus(String targetStatus, String requiredCurrentStatus, String successMsg) {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= filteredOrdersList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Order selectedOrder = filteredOrdersList.get(selectedRow);
        String currentStatus = selectedOrder.getStatus();

        if (!currentStatus.equalsIgnoreCase(requiredCurrentStatus)) {
            JOptionPane.showMessageDialog(
                this,
                "Cannot change to " + targetStatus + ".\nCurrent status is '" + currentStatus + "', but must be '" + requiredCurrentStatus + "'.",
                "Invalid Status Transition",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean ok = orderRepository.updateOrderStatus(selectedOrder.getOrderId(), targetStatus);
        if (ok) {
            JOptionPane.showMessageDialog(this, successMsg, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            loadActiveOrders();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update status in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}