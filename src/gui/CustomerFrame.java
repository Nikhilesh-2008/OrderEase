package gui;

import model.FoodItem;
import model.Order;
import model.OrderItem;
import model.User;
import repository.FeedbackRepository;
import repository.FoodRepository;
import repository.OrderRepository;
import repository.UserRepository;
import util.FileUtil;
import util.UIUtils;
import util.UIUtils.RoundedButton;
import util.UIUtils.RoundedPanel;
import util.UIUtils.RoundedTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomerFrame extends JFrame {

    private User currentUser;
    private final FoodRepository foodRepository = new FoodRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final FeedbackRepository feedbackRepository = new FeedbackRepository();

    // Menu & Cart components
    private JComboBox<String> canteenFilterBox;
    private RoundedTextField searchField;
    private JComboBox<FoodItem> foodComboBox;
    private JSpinner quantitySpinner;
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel totalLabel;
    private final List<OrderItem> cartItems = new ArrayList<>();
    private List<FoodItem> allMenuItems = new ArrayList<>();

    // Order tracking components
    private DefaultTableModel ordersTableModel;
    private JTable ordersTable;
    private JTabbedPane tabbedPane;
    private JLabel readyAlertBanner;
    private JCheckBox autoRefreshToggle;
    private Timer autoRefreshTimer;
    private final Set<Integer> notifiedReadyOrders = new HashSet<>();

    public CustomerFrame() {
        this(null);
    }

    public CustomerFrame(User user) {
        UIUtils.setupDarkUI();
        this.currentUser = user;
        if (this.currentUser == null) {
            UserRepository userRepo = new UserRepository();
            List<User> users = userRepo.getAllUsers();
            for (User u : users) {
                if ("CUSTOMER".equalsIgnoreCase(u.getRole())) {
                    this.currentUser = u;
                    break;
                }
            }
            if (this.currentUser == null) {
                this.currentUser = new User(1, "GuestCustomer", "CUSTOMER", 1, "Campus Student", "");
            }
        }

        setTitle("OrderEase | Campus Canteen Portal - " + currentUser.getCustomerName());
        setSize(1080, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BACKGROUND);
        setLayout(new BorderLayout());

        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Tabbed Pane for Menu & Orders
        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new UIUtils.ModernTabbedPaneUI());
        tabbedPane.setBackground(UIUtils.BACKGROUND);
        tabbedPane.setForeground(UIUtils.WHITE);
        tabbedPane.setFont(UIUtils.BODY_BOLD);

        tabbedPane.addTab("  🍽  Browse Campus Menu & Order  ", createOrderPanel());
        tabbedPane.addTab("  ⚡  My Orders & Live Tracking  ", createTrackingPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Start Live Auto-Refresh Timer (every 5 seconds)
        autoRefreshTimer = new Timer(5000, e -> {
            if (autoRefreshToggle != null && autoRefreshToggle.isSelected()) {
                refreshOrdersList();
            }
        });
        autoRefreshTimer.start();

        // Initial data load
        refreshOrdersList();

        setVisible(true);
    }

    @Override
    public void dispose() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        super.dispose();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.BACKGROUND_DARK);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.CARD_BORDER),
            BorderFactory.createEmptyBorder(16, 28, 16, 28)
        ));

        // Left Branding & User Chip
        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftContainer.setOpaque(false);

        JLabel logo = new JLabel("OrderEase");
        logo.setFont(new Font(UIUtils.TITLE_FONT.getFamily(), Font.BOLD, 26));
        logo.setForeground(UIUtils.ORANGE);

        // User profile pill
        JPanel profilePill = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        profilePill.setOpaque(false);
        profilePill.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(2, 10, 2, 12)
        ));

        JLabel userIcon = new JLabel("🎓");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        String displayName = currentUser.getCustomerName() != null && !currentUser.getCustomerName().isEmpty()
                ? currentUser.getCustomerName() : currentUser.getUsername();
        JLabel nameLabel = new JLabel(displayName + " (Student / Staff)");
        nameLabel.setFont(UIUtils.BODY_BOLD);
        nameLabel.setForeground(UIUtils.WHITE);

        profilePill.add(userIcon);
        profilePill.add(nameLabel);

        leftContainer.add(logo);
        leftContainer.add(profilePill);

        header.add(leftContainer, BorderLayout.WEST);

        // Right Actions (TV Board + Logout)
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setOpaque(false);

        RoundedButton tvBoardButton = new RoundedButton("📺 Public TV Token Board", UIUtils.BLUE, UIUtils.BLUE_HOVER, Color.WHITE, 8);
        tvBoardButton.setFont(UIUtils.BUTTON_FONT);
        tvBoardButton.setPreferredSize(new Dimension(195, 36));
        tvBoardButton.addActionListener(e -> new TokenDisplayFrame());

        RoundedButton logoutButton = new RoundedButton("Logout", UIUtils.RED, UIUtils.RED_HOVER, Color.WHITE, 8);
        logoutButton.setFont(UIUtils.BUTTON_FONT);
        logoutButton.setPreferredSize(new Dimension(95, 36));
        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
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

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Top Filter & Search Container
        JPanel topFiltersContainer = new JPanel();
        topFiltersContainer.setOpaque(false);
        topFiltersContainer.setLayout(new BoxLayout(topFiltersContainer, BoxLayout.Y_AXIS));

        // 1. Canteen Switcher Bar
        RoundedPanel canteenBar = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        canteenBar.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));

        JLabel canteenLbl = new JLabel("Campus Canteen:");
        canteenLbl.setForeground(UIUtils.WHITE);
        canteenLbl.setFont(UIUtils.BODY_BOLD);

        String[] canteens = {
            "All Campus Canteens",
            FoodRepository.CANTEEN_SNACKS,
            FoodRepository.CANTEEN_CHAI,
            FoodRepository.CANTEEN_FOOD_COURT
        };
        canteenFilterBox = new JComboBox<>(canteens);
        canteenFilterBox.setPreferredSize(new Dimension(280, 36));
        canteenFilterBox.setBackground(UIUtils.INPUT_BG);
        canteenFilterBox.setForeground(UIUtils.WHITE);
        canteenFilterBox.setFont(UIUtils.BODY_FONT);
        canteenFilterBox.addActionListener(e -> filterMenuItems());

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(UIUtils.MUTED);
        searchLbl.setFont(UIUtils.BODY_BOLD);

        searchField = new RoundedTextField(18, 8);
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.setToolTipText("Filter by dish name (e.g. burger, chai, biryani)...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterMenuItems(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterMenuItems(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterMenuItems(); }
        });

        canteenBar.add(canteenLbl);
        canteenBar.add(canteenFilterBox);
        canteenBar.add(searchLbl);
        canteenBar.add(searchField);

        topFiltersContainer.add(canteenBar);
        topFiltersContainer.add(Box.createVerticalStrut(10));

        // 2. Selection & Item control panel
        RoundedPanel selectPanel = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        selectPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));

        JLabel selectLabel = new JLabel("Select Dish:");
        selectLabel.setForeground(UIUtils.WHITE);
        selectLabel.setFont(UIUtils.BODY_BOLD);

        foodComboBox = new JComboBox<>();
        foodComboBox.setPreferredSize(new Dimension(360, 38));
        foodComboBox.setBackground(UIUtils.INPUT_BG);
        foodComboBox.setForeground(UIUtils.WHITE);
        foodComboBox.setFont(UIUtils.BODY_FONT);

        JLabel qtyLabel = new JLabel("Qty:");
        qtyLabel.setForeground(UIUtils.WHITE);
        qtyLabel.setFont(UIUtils.BODY_BOLD);

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        quantitySpinner.setPreferredSize(new Dimension(70, 38));
        quantitySpinner.setFont(UIUtils.BODY_BOLD);
        JComponent editor = quantitySpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(UIUtils.INPUT_BG);
            tf.setForeground(UIUtils.WHITE);
            tf.setCaretColor(UIUtils.WHITE);
            tf.setHorizontalAlignment(JTextField.CENTER);
        }

        RoundedButton addToCartButton = new RoundedButton("+ Add to Cart", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 8);
        addToCartButton.setFont(UIUtils.BUTTON_FONT);
        addToCartButton.setPreferredSize(new Dimension(135, 38));

        RoundedButton refreshMenuButton = new RoundedButton("↻ Refresh", UIUtils.CARD_HOVER, UIUtils.CARD_BORDER, UIUtils.MUTED, 8);
        refreshMenuButton.setFont(UIUtils.SUBTITLE_FONT);
        refreshMenuButton.setPreferredSize(new Dimension(105, 38));

        selectPanel.add(selectLabel);
        selectPanel.add(foodComboBox);
        selectPanel.add(qtyLabel);
        selectPanel.add(quantitySpinner);
        selectPanel.add(addToCartButton);
        selectPanel.add(refreshMenuButton);

        topFiltersContainer.add(selectPanel);

        panel.add(topFiltersContainer, BorderLayout.NORTH);

        // Center Cart Table
        String[] cartCols = {"Item Name", "Price (₹)", "Quantity", "Subtotal (₹)"};
        cartTableModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(cartTableModel);
        UIUtils.styleTable(cartTable);

        JScrollPane scrollPane = UIUtils.createModernScrollPane(cartTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom summary & Checkout
        RoundedPanel bottomPanel = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JPanel summaryLeft = new JPanel();
        summaryLeft.setOpaque(false);
        summaryLeft.setLayout(new BoxLayout(summaryLeft, BoxLayout.Y_AXIS));

        JLabel summaryCaption = new JLabel("ORDER SUMMARY");
        summaryCaption.setFont(UIUtils.BADGE_FONT);
        summaryCaption.setForeground(UIUtils.MUTED);

        totalLabel = new JLabel("Total: ₹0.00");
        totalLabel.setFont(new Font(UIUtils.TITLE_FONT.getFamily(), Font.BOLD, 24));
        totalLabel.setForeground(UIUtils.ORANGE);

        summaryLeft.add(summaryCaption);
        summaryLeft.add(Box.createVerticalStrut(3));
        summaryLeft.add(totalLabel);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        actionButtons.setOpaque(false);

        RoundedButton removeButton = new RoundedButton("Remove Selected", UIUtils.CARD_HOVER, UIUtils.CARD_BORDER, UIUtils.WHITE, 8);
        removeButton.setPreferredSize(new Dimension(150, 42));

        RoundedButton clearButton = new RoundedButton("Clear Cart", UIUtils.CARD_HOVER, UIUtils.CARD_BORDER, UIUtils.MUTED, 8);
        clearButton.setPreferredSize(new Dimension(110, 42));

        RoundedButton checkoutButton = new RoundedButton("Place Order & Get Token  ➔", UIUtils.GREEN, UIUtils.GREEN_HOVER, Color.BLACK, 8);
        checkoutButton.setPreferredSize(new Dimension(230, 42));
        checkoutButton.setFont(UIUtils.BUTTON_FONT);

        actionButtons.add(removeButton);
        actionButtons.add(clearButton);
        actionButtons.add(checkoutButton);

        bottomPanel.add(summaryLeft, BorderLayout.WEST);
        bottomPanel.add(actionButtons, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        addToCartButton.addActionListener(e -> addItemToCart());
        refreshMenuButton.addActionListener(e -> loadMenuData());
        removeButton.addActionListener(e -> removeSelectedCartItem());
        clearButton.addActionListener(e -> clearCart());
        checkoutButton.addActionListener(e -> performCheckout());

        // Initial menu load
        loadMenuData();

        return panel;
    }

    private JPanel createTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Top info bar
        RoundedPanel topBar = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        topBar.setLayout(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel infoTextPanel = new JPanel();
        infoTextPanel.setOpaque(false);
        infoTextPanel.setLayout(new BoxLayout(infoTextPanel, BoxLayout.Y_AXIS));

        JLabel infoTitle = new JLabel("Live Order Status & Campus Token Queue");
        infoTitle.setFont(UIUtils.SECTION_FONT);
        infoTitle.setForeground(UIUtils.WHITE);

        JLabel infoSub = new JLabel("Status flow: PLACED (Cancellable) ➔ PREPARING ➔ READY FOR PICKUP ➔ COLLECTED");
        infoSub.setFont(UIUtils.SUBTITLE_FONT);
        infoSub.setForeground(UIUtils.MUTED);

        infoTextPanel.add(infoTitle);
        infoTextPanel.add(Box.createVerticalStrut(3));
        infoTextPanel.add(infoSub);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        autoRefreshToggle = new JCheckBox("Live Auto-Refresh (5s)", true);
        autoRefreshToggle.setFont(UIUtils.BODY_BOLD);
        autoRefreshToggle.setForeground(UIUtils.ORANGE);
        autoRefreshToggle.setBackground(UIUtils.CARD);
        autoRefreshToggle.setFocusPainted(false);

        RoundedButton refreshOrdersBtn = new RoundedButton("↻ Refresh", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 8);
        refreshOrdersBtn.setPreferredSize(new Dimension(110, 38));
        refreshOrdersBtn.addActionListener(e -> refreshOrdersList());

        rightControls.add(autoRefreshToggle);
        rightControls.add(refreshOrdersBtn);

        topBar.add(infoTextPanel, BorderLayout.WEST);
        topBar.add(rightControls, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);

        // Center Table with Queue Position Column
        String[] orderCols = {"Order ID", "Token", "Items Ordered", "Total (₹)", "Order Time", "Live Status", "Queue Position"};
        ordersTableModel = new DefaultTableModel(orderCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(ordersTableModel);
        UIUtils.styleTable(ordersTable);

        // Custom token badge renderer
        ordersTable.getColumnModel().getColumn(1).setCellRenderer(new UIUtils.TokenBadgeRenderer());
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(95);

        // Custom status pill badge renderer
        ordersTable.getColumnModel().getColumn(5).setCellRenderer(new UIUtils.StatusBadgeRenderer());
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(130);

        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(180);

        JScrollPane scrollPane = UIUtils.createModernScrollPane(ordersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Controls Bar (Cancel, Rate, View Receipt)
        JPanel bottomContainer = new JPanel();
        bottomContainer.setOpaque(false);
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));

        RoundedPanel trackingActions = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        trackingActions.setLayout(new FlowLayout(FlowLayout.RIGHT, 14, 10));

        RoundedButton cancelOrderBtn = new RoundedButton("✕ Cancel Order (If Placed)", UIUtils.RED, UIUtils.RED_HOVER, Color.WHITE, 8);
        cancelOrderBtn.setPreferredSize(new Dimension(210, 40));
        cancelOrderBtn.addActionListener(e -> cancelSelectedOrder());

        RoundedButton rateOrderBtn = new RoundedButton("⭐ Rate Food (Stars)", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 8);
        rateOrderBtn.setPreferredSize(new Dimension(175, 40));
        rateOrderBtn.addActionListener(e -> rateSelectedOrder());

        RoundedButton viewReceiptBtn = new RoundedButton("🧾 View Receipt", UIUtils.CARD_HOVER, UIUtils.CARD_BORDER, UIUtils.WHITE, 8);
        viewReceiptBtn.setPreferredSize(new Dimension(145, 40));
        viewReceiptBtn.addActionListener(e -> viewSelectedOrderReceipt());

        trackingActions.add(cancelOrderBtn);
        trackingActions.add(rateOrderBtn);
        trackingActions.add(viewReceiptBtn);

        // Ready alert banner at bottom if food is ready
        readyAlertBanner = new JLabel("");
        readyAlertBanner.setFont(new Font(UIUtils.BODY_BOLD.getFamily(), Font.BOLD, 15));
        readyAlertBanner.setForeground(UIUtils.GREEN_HOVER);
        readyAlertBanner.setHorizontalAlignment(SwingConstants.CENTER);
        readyAlertBanner.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        readyAlertBanner.setVisible(false);

        bottomContainer.add(trackingActions);
        bottomContainer.add(readyAlertBanner);

        panel.add(bottomContainer, BorderLayout.SOUTH);

        return panel;
    }

    private void loadMenuData() {
        allMenuItems = foodRepository.getAllFoodItems();
        filterMenuItems();
    }

    private void filterMenuItems() {
        if (foodComboBox == null) return;
        foodComboBox.removeAllItems();

        String selectedCanteen = canteenFilterBox != null ? (String) canteenFilterBox.getSelectedItem() : "All Campus Canteens";
        boolean filterCanteen = selectedCanteen != null && !selectedCanteen.contains("All");

        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        for (FoodItem item : allMenuItems) {
            if (filterCanteen && item.getCanteen() != null && !item.getCanteen().equalsIgnoreCase(selectedCanteen)) {
                continue;
            }
            if (!query.isEmpty()) {
                String itemName = item.getName() != null ? item.getName().toLowerCase() : "";
                String category = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
                if (!itemName.contains(query) && !category.contains(query)) {
                    continue;
                }
            }
            foodComboBox.addItem(item);
        }

        foodComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof FoodItem) {
                    FoodItem fi = (FoodItem) value;
                    String status = fi.available ? "" : " (UNAVAILABLE)";
                    String canteenPrefix = fi.getCanteen() != null ? "[" + fi.getCanteen() + "] " : "";
                    setText(canteenPrefix + fi.name + "  —  ₹" + String.format("%.2f", fi.price) + status);
                    if (!fi.available) {
                        setForeground(UIUtils.MUTED_DARK);
                    } else {
                        setForeground(isSelected ? UIUtils.WHITE : UIUtils.MUTED);
                    }
                    setBackground(isSelected ? UIUtils.TABLE_SELECTION : UIUtils.INPUT_BG);
                    setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                }
                return this;
            }
        });
    }

    private void addItemToCart() {
        FoodItem selected = (FoodItem) foodComboBox.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an item from the menu.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!selected.available) {
            JOptionPane.showMessageDialog(this, "'" + selected.name + "' is currently unavailable.", "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty = (int) quantitySpinner.getValue();
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be at least 1.", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean exists = false;
        for (OrderItem it : cartItems) {
            if (it.getItemId() == selected.id) {
                it.setQuantity(it.getQuantity() + qty);
                exists = true;
                break;
            }
        }

        if (!exists) {
            OrderItem newItem = new OrderItem(selected.id, selected.name, qty, selected.price);
            cartItems.add(newItem);
        }

        updateCartTable();
    }

    private void removeSelectedCartItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cartItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item in the cart to remove.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cartItems.remove(row);
        updateCartTable();
    }

    private void clearCart() {
        cartItems.clear();
        updateCartTable();
    }

    private void updateCartTable() {
        cartTableModel.setRowCount(0);
        double total = 0.0;
        for (OrderItem it : cartItems) {
            double sub = it.getSubtotal();
            total += sub;
            cartTableModel.addRow(new Object[]{
                it.getItemName(),
                String.format("%.2f", it.getUnitPrice()),
                it.getQuantity(),
                String.format("%.2f", sub)
            });
        }
        totalLabel.setText("Total: ₹" + String.format("%.2f", total));
    }

    private void performCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Add items before placing an order.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Confirm order placement for " + totalLabel.getText() + "?",
            "Confirm Order",
            JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int custId = currentUser.getCustomerId();
            Order placedOrder = orderRepository.createOrder(custId, cartItems);

            String receipt = FileUtil.generateReceiptText(placedOrder, currentUser.getCustomerName());

            // Save receipt automatically to receipt_TK-XXX.txt
            String filename = "receipt_TK-" + placedOrder.getToken() + ".txt";
            FileUtil.saveOrderReceipt(placedOrder, currentUser.getCustomerName(), filename);

            JTextArea textArea = new JTextArea(receipt);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
            textArea.setBackground(UIUtils.INPUT_BG);
            textArea.setForeground(UIUtils.WHITE);
            textArea.setEditable(false);
            textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setPreferredSize(new Dimension(460, 360));

            JOptionPane.showMessageDialog(this, scroll, "Token #" + placedOrder.getToken() + " - Order Confirmed & Receipt Saved", JOptionPane.INFORMATION_MESSAGE);

            clearCart();
            refreshOrdersList();
            tabbedPane.setSelectedIndex(1);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Order placement failed: " + e.getMessage(), "Order Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table to cancel.", "Select Order", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String orderIdStr = (String) ordersTableModel.getValueAt(selectedRow, 0);
        int orderId = Integer.parseInt(orderIdStr.replace("#", "").trim());
        String token = (String) ordersTableModel.getValueAt(selectedRow, 1);
        String status = (String) ordersTableModel.getValueAt(selectedRow, 5);

        if (!"PLACED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this,
                "Cannot cancel Order " + orderIdStr + " (" + token + ").\n" +
                "Orders can only be cancelled while in 'PLACED' status.\n" +
                "The kitchen has already started preparing this order (" + status + ")!",
                "Cancellation Restricted",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel Order " + orderIdStr + " (" + token + ")?\nThis action cannot be undone.",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = orderRepository.cancelOrder(orderId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Order " + orderIdStr + " (" + token + ") has been successfully cancelled.", "Order Cancelled", JOptionPane.INFORMATION_MESSAGE);
                refreshOrdersList();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel order. The kitchen may have just started cooking it.", "Cancellation Failed", JOptionPane.ERROR_MESSAGE);
                refreshOrdersList();
            }
        }
    }

    private void rateSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a collected order from the table to rate.", "Select Order", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String orderIdStr = (String) ordersTableModel.getValueAt(selectedRow, 0);
        int orderId = Integer.parseInt(orderIdStr.replace("#", "").trim());
        String token = (String) ordersTableModel.getValueAt(selectedRow, 1);
        String items = (String) ordersTableModel.getValueAt(selectedRow, 2);
        String status = (String) ordersTableModel.getValueAt(selectedRow, 5);

        if (!"COLLECTED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this,
                "You can rate your food once it has been COLLECTED from the canteen counter.\nCurrent status: " + status,
                "Rating Pending",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (feedbackRepository.hasFeedbackForOrder(orderId)) {
            JOptionPane.showMessageDialog(this, "You have already submitted a review for Order " + orderIdStr + " (" + token + "). Thank you!", "Already Rated", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel ratingPanel = new JPanel();
        ratingPanel.setLayout(new BoxLayout(ratingPanel, BoxLayout.Y_AXIS));
        ratingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel info = new JLabel("<html><b>Order " + token + "</b>: " + items + "</html>");
        info.setFont(UIUtils.BODY_FONT);
        info.setForeground(UIUtils.WHITE);
        ratingPanel.add(info);
        ratingPanel.add(Box.createVerticalStrut(14));

        JLabel starPrompt = new JLabel("Rate your canteen food experience:");
        starPrompt.setFont(UIUtils.BODY_BOLD);
        starPrompt.setForeground(UIUtils.ORANGE);
        ratingPanel.add(starPrompt);
        ratingPanel.add(Box.createVerticalStrut(6));

        JComboBox<String> starCombo = new JComboBox<>(new String[]{
            "★★★★★ (5 Stars - Excellent)",
            "★★★★☆ (4 Stars - Good)",
            "★★★☆☆ (3 Stars - Average)",
            "★★☆☆☆ (2 Stars - Below Average)",
            "★☆☆☆☆ (1 Star - Poor)"
        });
        starCombo.setFont(new Font("SansSerif", Font.BOLD, 14));
        starCombo.setBackground(UIUtils.INPUT_BG);
        starCombo.setForeground(UIUtils.WHITE);
        ratingPanel.add(starCombo);
        ratingPanel.add(Box.createVerticalStrut(14));

        JLabel commentPrompt = new JLabel("Comments or Suggestions (Optional):");
        commentPrompt.setFont(UIUtils.BODY_FONT);
        commentPrompt.setForeground(UIUtils.MUTED);
        ratingPanel.add(commentPrompt);
        ratingPanel.add(Box.createVerticalStrut(6));

        JTextField commentField = new JTextField(24);
        commentField.setFont(UIUtils.BODY_FONT);
        commentField.setBackground(UIUtils.INPUT_BG);
        commentField.setForeground(UIUtils.WHITE);
        commentField.setCaretColor(UIUtils.WHITE);
        ratingPanel.add(commentField);

        int opt = JOptionPane.showConfirmDialog(this, ratingPanel, "Rate Food - " + token, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            int selectedIndex = starCombo.getSelectedIndex();
            int rating = 5 - selectedIndex;
            String comments = commentField.getText().trim();

            boolean ok = feedbackRepository.addFeedback(orderId, currentUser.getCustomerId(), rating, comments);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Thank you! Your feedback has been submitted to the canteen manager.", "Feedback Submitted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Could not save feedback. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewSelectedOrderReceipt() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table to view its receipt.", "Select Order", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String orderIdStr = (String) ordersTableModel.getValueAt(selectedRow, 0);
        int orderId = Integer.parseInt(orderIdStr.replace("#", "").trim());

        List<Order> orders = orderRepository.getOrdersByCustomerId(currentUser.getCustomerId());
        Order targetOrder = null;
        for (Order o : orders) {
            if (o.getOrderId() == orderId) {
                targetOrder = o;
                break;
            }
        }

        if (targetOrder == null) {
            JOptionPane.showMessageDialog(this, "Order details not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String receipt = FileUtil.generateReceiptText(targetOrder, currentUser.getCustomerName());

        JTextArea textArea = new JTextArea(receipt);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setBackground(UIUtils.INPUT_BG);
        textArea.setForeground(UIUtils.WHITE);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(460, 360));

        Object[] options = {"Save to File", "Close"};
        int opt = JOptionPane.showOptionDialog(this, scroll, "Receipt - Token #" + targetOrder.getToken(),
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (opt == JOptionPane.YES_OPTION) {
            String path = "receipt_TK-" + targetOrder.getToken() + ".txt";
            boolean ok = FileUtil.saveOrderReceipt(targetOrder, currentUser.getCustomerName(), path);
            if (ok) {
                File f = new File(path);
                JOptionPane.showMessageDialog(this, "Receipt saved to: " + f.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void refreshOrdersList() {
        ordersTableModel.setRowCount(0);
        int custId = currentUser.getCustomerId();
        List<Order> orders = orderRepository.getOrdersByCustomerId(custId);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        boolean hasReadyOrder = false;
        StringBuilder readyTokens = new StringBuilder();
        boolean newReadyDetected = false;

        for (Order ord : orders) {
            String timeStr = ord.getOrderTime() != null ? sdf.format(ord.getOrderTime()) : "N/A";
            String st = ord.getStatus();

            String queueEstimate;
            if ("READY".equalsIgnoreCase(st)) {
                queueEstimate = "🔔 Ready for collection!";
                hasReadyOrder = true;
                readyTokens.append("TK-").append(ord.getToken()).append(" ");
                if (!notifiedReadyOrders.contains(ord.getOrderId())) {
                    notifiedReadyOrders.add(ord.getOrderId());
                    newReadyDetected = true;
                }
            } else if ("COLLECTED".equalsIgnoreCase(st)) {
                queueEstimate = "✓ Completed";
            } else if ("CANCELLED".equalsIgnoreCase(st)) {
                queueEstimate = "✕ Cancelled";
            } else {
                int pos = orderRepository.getQueuePosition(ord.getOrderId());
                if (pos == 0) {
                    queueEstimate = "🍳 Preparing next (~2-3 mins)";
                } else {
                    int estMins = (pos + 1) * 3;
                    queueEstimate = pos + " order" + (pos > 1 ? "s" : "") + " ahead (~" + estMins + "m)";
                }
            }

            ordersTableModel.addRow(new Object[]{
                "#" + ord.getOrderId(),
                "TK-" + ord.getToken(),
                ord.getItemsSummary(),
                String.format("%.2f", ord.getTotalAmount()),
                timeStr,
                ord.getStatus(),
                queueEstimate
            });
        }

        if (newReadyDetected) {
            UIUtils.playNotificationChime();
        }

        if (hasReadyOrder && readyAlertBanner != null) {
            readyAlertBanner.setText("🔔 Token(s) " + readyTokens + "are READY for pickup at the counter!");
            readyAlertBanner.setVisible(true);
        } else if (readyAlertBanner != null) {
            readyAlertBanner.setVisible(false);
        }
    }
}