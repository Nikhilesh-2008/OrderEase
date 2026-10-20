package gui;

import model.Feedback;
import model.FoodItem;
import model.Order;
import model.User;
import repository.FeedbackRepository;
import repository.FoodRepository;
import repository.OrderRepository;
import repository.UserRepository;
import util.UIUtils;
import util.UIUtils.RoundedButton;
import util.UIUtils.RoundedPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminFrame extends JFrame {

    private final FoodRepository foodRepository = new FoodRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final UserRepository userRepository = new UserRepository();
    private final FeedbackRepository feedbackRepository = new FeedbackRepository();

    // Menu components
    private JComboBox<String> menuCanteenFilter;
    private DefaultTableModel menuTableModel;
    private JTable menuTable;
    private List<FoodItem> currentMenuList = new ArrayList<>();

    // Order monitoring components
    private DefaultTableModel ordersTableModel;
    private JTable ordersTable;
    private JLabel totalOrdersLabel;
    private JLabel totalRevenueLabel;
    private JLabel activeOrdersLabel;

    // Canteen breakdown labels
    private JLabel snacksRevLabel;
    private JLabel chaiRevLabel;
    private JLabel foodCourtRevLabel;

    // Users components
    private DefaultTableModel usersTableModel;
    private JTable usersTable;

    // Feedback components
    private DefaultTableModel feedbackTableModel;
    private JTable feedbackTable;
    private JLabel avgRatingLabel;
    private JLabel totalFeedbackLabel;

    public AdminFrame() {
        UIUtils.setupDarkUI();

        setTitle("OrderEase | Administrator Command Center - Campus Multi-Canteen");
        setSize(1140, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BACKGROUND);
        setLayout(new BorderLayout());

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new UIUtils.ModernTabbedPaneUI());
        tabbedPane.setBackground(UIUtils.BACKGROUND);
        tabbedPane.setForeground(UIUtils.WHITE);
        tabbedPane.setFont(UIUtils.BODY_BOLD);

        tabbedPane.addTab("  🍔  Campus Menu Catalog  ", createMenuPanel());
        tabbedPane.addTab("  📈  Sales Analytics & Multi-Canteen Reports  ", createOrdersPanel());
        tabbedPane.addTab("  ⭐  Student Ratings & Reviews  ", createFeedbackPanel());
        tabbedPane.addTab("  👥  User Directory  ", createUsersPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Initial load
        refreshMenuTable();
        refreshOrdersTable();
        refreshFeedbackTable();
        refreshUsersTable();

        setVisible(true);
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

        JPanel adminTag = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 68, 68, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UIUtils.RED);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        adminTag.setOpaque(false);
        adminTag.setPreferredSize(new Dimension(145, 24));
        JLabel adminText = new JLabel("CAMPUS ADMIN");
        adminText.setFont(UIUtils.BADGE_FONT);
        adminText.setForeground(UIUtils.RED_HOVER);
        adminTag.add(adminText);

        titleRow.add(title);
        titleRow.add(adminTag);

        JLabel subtitle = new JLabel("Multi-canteen management: Snacks & Shakes, Chai & Juice Bar, and Food Court.");
        subtitle.setFont(UIUtils.SUBTITLE_FONT);
        subtitle.setForeground(UIUtils.MUTED);

        leftContainer.add(titleRow);
        leftContainer.add(Box.createVerticalStrut(4));
        leftContainer.add(subtitle);

        header.add(leftContainer, BorderLayout.WEST);

        // Right Actions (TV Board + Logout)
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
                "Are you sure you want to log out of Admin Dashboard?",
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

    // --- TAB 1: MENU MANAGEMENT ---
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Top Filter Bar
        RoundedPanel filterBar = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));

        JLabel filterLbl = new JLabel("Filter by Canteen:");
        filterLbl.setFont(UIUtils.BODY_BOLD);
        filterLbl.setForeground(UIUtils.WHITE);

        String[] canteens = {
            "All Campus Canteens",
            FoodRepository.CANTEEN_SNACKS,
            FoodRepository.CANTEEN_CHAI,
            FoodRepository.CANTEEN_FOOD_COURT
        };
        menuCanteenFilter = new JComboBox<>(canteens);
        menuCanteenFilter.setPreferredSize(new Dimension(280, 36));
        menuCanteenFilter.setBackground(UIUtils.INPUT_BG);
        menuCanteenFilter.setForeground(UIUtils.WHITE);
        menuCanteenFilter.setFont(UIUtils.BODY_FONT);
        menuCanteenFilter.addActionListener(e -> refreshMenuTable());

        filterBar.add(filterLbl);
        filterBar.add(menuCanteenFilter);

        panel.add(filterBar, BorderLayout.NORTH);

        String[] cols = {"Item ID", "Dish Name", "Canteen", "Category", "Price (₹)", "Status / Availability"};
        menuTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        menuTable = new JTable(menuTableModel);
        UIUtils.styleTable(menuTable);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Highlight Canteen
        menuTable.getColumnModel().getColumn(2).setCellRenderer(new UIUtils.CanteenBadgeRenderer());
        menuTable.getColumnModel().getColumn(2).setPreferredWidth(190);

        // Highlight availability pill
        menuTable.getColumnModel().getColumn(5).setCellRenderer(new UIUtils.StatusBadgeRenderer());
        menuTable.getColumnModel().getColumn(5).setPreferredWidth(130);

        JScrollPane scrollPane = UIUtils.createModernScrollPane(menuTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        RoundedPanel actionPanel = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        actionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 12));

        RoundedButton addButton = new RoundedButton("+ Add New Item", UIUtils.GREEN, UIUtils.GREEN_HOVER, Color.BLACK, 8);
        RoundedButton editButton = new RoundedButton("✎ Edit Item", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 8);
        RoundedButton toggleButton = new RoundedButton("⇄ Toggle Availability", UIUtils.BLUE, UIUtils.BLUE_HOVER, Color.WHITE, 8);
        RoundedButton deleteButton = new RoundedButton("✕ Delete Item", UIUtils.RED, UIUtils.RED_HOVER, Color.WHITE, 8);
        RoundedButton refreshButton = new RoundedButton("↻ Refresh Menu", UIUtils.CARD_HOVER, UIUtils.CARD_BORDER, UIUtils.WHITE, 8);

        addButton.setPreferredSize(new Dimension(150, 40));
        editButton.setPreferredSize(new Dimension(130, 40));
        toggleButton.setPreferredSize(new Dimension(175, 40));
        deleteButton.setPreferredSize(new Dimension(140, 40));
        refreshButton.setPreferredSize(new Dimension(140, 40));

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(toggleButton);
        actionPanel.add(deleteButton);
        actionPanel.add(refreshButton);

        panel.add(actionPanel, BorderLayout.SOUTH);

        // Events
        addButton.addActionListener(e -> showAddItemDialog());
        editButton.addActionListener(e -> showEditItemDialog());
        toggleButton.addActionListener(e -> toggleItemAvailability());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        refreshButton.addActionListener(e -> refreshMenuTable());

        return panel;
    }

    // --- TAB 2: ORDER MONITORING & SALES ---
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Top summary cards container
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // 1. Overall Metrics
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsPanel.setOpaque(false);

        totalOrdersLabel = new JLabel("0");
        totalRevenueLabel = new JLabel("₹0.00");
        activeOrdersLabel = new JLabel("0");

        metricsPanel.add(createMetricCard("Total Campus Orders", totalOrdersLabel, UIUtils.ORANGE));
        metricsPanel.add(createMetricCard("Total Revenue Generated", totalRevenueLabel, UIUtils.GREEN));
        metricsPanel.add(createMetricCard("Active in Kitchen Queue", activeOrdersLabel, UIUtils.BLUE));

        topContainer.add(metricsPanel);
        topContainer.add(Box.createVerticalStrut(10));

        // 2. Multi-Canteen Revenue Breakdown
        JPanel breakdownPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        breakdownPanel.setOpaque(false);

        snacksRevLabel = new JLabel("₹0.00");
        chaiRevLabel = new JLabel("₹0.00");
        foodCourtRevLabel = new JLabel("₹0.00");

        breakdownPanel.add(createMetricCard("Snacks & Shakes Rev", snacksRevLabel, UIUtils.ORANGE_HOVER));
        breakdownPanel.add(createMetricCard("Chai & Juice Bar Rev", chaiRevLabel, UIUtils.BLUE_HOVER));
        breakdownPanel.add(createMetricCard("Food Court Meals Rev", foodCourtRevLabel, UIUtils.GREEN_HOVER));

        topContainer.add(breakdownPanel);

        panel.add(topContainer, BorderLayout.NORTH);

        // Orders Table
        String[] cols = {"Order ID", "Token", "Customer", "Ordered Items", "Total (₹)", "Order Time", "Status"};
        ordersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        ordersTable = new JTable(ordersTableModel);
        UIUtils.styleTable(ordersTable);

        // Token renderer
        ordersTable.getColumnModel().getColumn(1).setCellRenderer(new UIUtils.TokenBadgeRenderer());
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(95);

        // Status renderer
        ordersTable.getColumnModel().getColumn(6).setCellRenderer(new UIUtils.StatusBadgeRenderer());
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(130);

        JScrollPane scrollPane = UIUtils.createModernScrollPane(ordersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Refresh & Export Bar
        RoundedPanel bottomBar = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        bottomBar.setLayout(new FlowLayout(FlowLayout.RIGHT, 16, 10));

        RoundedButton exportCsvBtn = new RoundedButton("📥 Export Sales to CSV", UIUtils.GREEN, UIUtils.GREEN_HOVER, Color.BLACK, 8);
        exportCsvBtn.setPreferredSize(new Dimension(190, 38));
        exportCsvBtn.addActionListener(e -> exportOrdersToCsv());

        RoundedButton refreshOrdersBtn = new RoundedButton("↻ Refresh Analytics", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 8);
        refreshOrdersBtn.setPreferredSize(new Dimension(175, 38));
        refreshOrdersBtn.addActionListener(e -> refreshOrdersTable());

        bottomBar.add(exportCsvBtn);
        bottomBar.add(refreshOrdersBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    // --- TAB 3: STUDENT FEEDBACK & REVIEWS ---
    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Top Metrics
        JPanel metricsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        metricsPanel.setOpaque(false);

        avgRatingLabel = new JLabel("0.0 / 5.0");
        totalFeedbackLabel = new JLabel("0");

        metricsPanel.add(createMetricCard("Average Campus Star Rating", avgRatingLabel, UIUtils.ORANGE));
        metricsPanel.add(createMetricCard("Total Student Reviews Submitted", totalFeedbackLabel, UIUtils.GREEN));

        panel.add(metricsPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Token", "Student Name", "Rating", "Review Comments", "Submitted At"};
        feedbackTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        feedbackTable = new JTable(feedbackTableModel);
        UIUtils.styleTable(feedbackTable);

        feedbackTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        feedbackTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        feedbackTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        // Golden stars renderer
        feedbackTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setForeground(new Color(255, 193, 7)); // Golden yellow
                setFont(new Font("SansSerif", Font.BOLD, 15));
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
        feedbackTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        feedbackTable.getColumnModel().getColumn(4).setPreferredWidth(320);

        JScrollPane scrollPane = UIUtils.createModernScrollPane(feedbackTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Bar
        RoundedPanel bottomBar = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        bottomBar.setLayout(new FlowLayout(FlowLayout.RIGHT, 16, 10));

        RoundedButton refreshFeedbackBtn = new RoundedButton("↻ Refresh Reviews", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 8);
        refreshFeedbackBtn.setPreferredSize(new Dimension(175, 38));
        refreshFeedbackBtn.addActionListener(e -> refreshFeedbackTable());
        bottomBar.add(refreshFeedbackBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    // --- TAB 4: USER DIRECTORY ---
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        String[] cols = {"User ID", "Username", "Role", "Customer ID", "Profile Name", "Phone"};
        usersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        usersTable = new JTable(usersTableModel);
        UIUtils.styleTable(usersTable);

        JScrollPane scrollPane = UIUtils.createModernScrollPane(usersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        RoundedPanel bottomBar = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        bottomBar.setLayout(new FlowLayout(FlowLayout.RIGHT, 16, 10));

        RoundedButton refreshUsersBtn = new RoundedButton("↻ Refresh User Accounts", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 8);
        refreshUsersBtn.setPreferredSize(new Dimension(195, 38));
        refreshUsersBtn.addActionListener(e -> refreshUsersTable());
        bottomBar.add(refreshUsersBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accent) {
        RoundedPanel card = new RoundedPanel(14, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIUtils.SMALL_FONT);
        titleLbl.setForeground(UIUtils.MUTED);

        valueLabel.setFont(new Font(UIUtils.TITLE_FONT.getFamily(), Font.BOLD, 22));
        valueLabel.setForeground(accent);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        return card;
    }

    // Menu CRUD Handlers
    private void refreshMenuTable() {
        menuTableModel.setRowCount(0);
        String selectedCanteen = menuCanteenFilter != null ? (String) menuCanteenFilter.getSelectedItem() : null;
        currentMenuList = foodRepository.getFoodItemsByCanteen(selectedCanteen);

        for (FoodItem item : currentMenuList) {
            menuTableModel.addRow(new Object[]{
                item.getId(),
                item.getName(),
                item.getCanteen() != null ? item.getCanteen() : FoodRepository.CANTEEN_SNACKS,
                item.getCategory() != null ? item.getCategory() : "General",
                String.format("%.2f", item.getPrice()),
                item.isAvailable() ? "Available" : "Unavailable"
            });
        }
    }

    private void showAddItemDialog() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();

        JComboBox<String> canteenBox = new JComboBox<>(FoodRepository.CANTEENS);
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{
            "Fast Food", "Milkshakes", "Snacks", "Tea & Coffee", "Fresh Juices", "Tiffins", "Biryanis", "Chapati & Meals"
        });

        JCheckBox availableBox = new JCheckBox("Available Immediately", true);
        availableBox.setBackground(UIUtils.CARD);
        availableBox.setForeground(UIUtils.WHITE);

        Object[] message = {
            "Item Name:", nameField,
            "Price (₹):", priceField,
            "Canteen Station:", canteenBox,
            "Food Category:", categoryBox,
            availableBox
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Campus Menu Item", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String canteen = (String) canteenBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();

            if (name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both item name and price.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                if (price < 0) {
                    JOptionPane.showMessageDialog(this, "Price cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                boolean ok = foodRepository.addFoodItem(name, price, availableBox.isSelected(), canteen, category);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Item added successfully to " + canteen + "!");
                    refreshMenuTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric price.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void showEditItemDialog() {
        int row = menuTable.getSelectedRow();
        if (row < 0 || row >= currentMenuList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FoodItem item = currentMenuList.get(row);
        JTextField nameField = new JTextField(item.getName());
        JTextField priceField = new JTextField(String.valueOf(item.getPrice()));

        JComboBox<String> canteenBox = new JComboBox<>(FoodRepository.CANTEENS);
        if (item.getCanteen() != null) {
            canteenBox.setSelectedItem(item.getCanteen());
        }

        JComboBox<String> categoryBox = new JComboBox<>(new String[]{
            "Fast Food", "Milkshakes", "Snacks", "Tea & Coffee", "Fresh Juices", "Tiffins", "Biryanis", "Chapati & Meals", "General"
        });
        if (item.getCategory() != null) {
            categoryBox.setSelectedItem(item.getCategory());
        }

        JCheckBox availableBox = new JCheckBox("Available", item.isAvailable());
        availableBox.setBackground(UIUtils.CARD);
        availableBox.setForeground(UIUtils.WHITE);

        Object[] message = {
            "Item Name:", nameField,
            "Price (₹):", priceField,
            "Canteen Station:", canteenBox,
            "Food Category:", categoryBox,
            availableBox
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit Menu Item #" + item.getId(), JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String canteen = (String) canteenBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();

            if (name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                boolean ok = foodRepository.updateFoodItem(item.getId(), name, price, availableBox.isSelected(), canteen, category);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Item updated successfully!");
                    refreshMenuTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric price.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void toggleItemAvailability() {
        int row = menuTable.getSelectedRow();
        if (row < 0 || row >= currentMenuList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to toggle availability.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FoodItem item = currentMenuList.get(row);
        boolean newStatus = !item.isAvailable();
        boolean ok = foodRepository.toggleAvailability(item.getId(), newStatus);
        if (ok) {
            refreshMenuTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update availability.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0 || row >= currentMenuList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FoodItem item = currentMenuList.get(row);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete '" + item.getName() + "'?\n(Items already linked in existing orders cannot be deleted).",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = foodRepository.deleteFoodItem(item.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Item deleted successfully!");
                refreshMenuTable();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete this item as it is linked in previous order records.\nYou can toggle its availability to 'Unavailable' instead.", "Delete Restricted", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // Orders Handlers
    private void refreshOrdersTable() {
        ordersTableModel.setRowCount(0);
        List<Order> allOrders = orderRepository.getAllOrders();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        double totalRev = 0.0;
        int activeCount = 0;

        for (Order ord : allOrders) {
            String timeStr = ord.getOrderTime() != null ? sdf.format(ord.getOrderTime()) : "-";
            String cust = ord.getCustomerName() != null ? ord.getCustomerName() : "Cust #" + ord.getCustomerId();
            totalRev += ord.getTotalAmount();

            String st = ord.getStatus();
            if ("PLACED".equalsIgnoreCase(st) || "PREPARING".equalsIgnoreCase(st) || "READY".equalsIgnoreCase(st)) {
                activeCount++;
            }

            ordersTableModel.addRow(new Object[]{
                "#" + ord.getOrderId(),
                "TK-" + ord.getToken(),
                cust,
                ord.getItemsSummary(),
                String.format("%.2f", ord.getTotalAmount()),
                timeStr,
                ord.getStatus()
            });
        }

        totalOrdersLabel.setText(String.valueOf(allOrders.size()));
        totalRevenueLabel.setText("₹" + String.format("%.2f", totalRev));
        activeOrdersLabel.setText(String.valueOf(activeCount));

        // Update canteen breakdown
        Map<String, Double> breakdown = orderRepository.getSalesByCanteen();
        Double snacks = breakdown.get(FoodRepository.CANTEEN_SNACKS);
        Double chai = breakdown.get(FoodRepository.CANTEEN_CHAI);
        Double foodCourt = breakdown.get(FoodRepository.CANTEEN_FOOD_COURT);

        snacksRevLabel.setText("₹" + String.format("%.2f", snacks != null ? snacks : 0.0));
        chaiRevLabel.setText("₹" + String.format("%.2f", chai != null ? chai : 0.0));
        foodCourtRevLabel.setText("₹" + String.format("%.2f", foodCourt != null ? foodCourt : 0.0));
    }

    private void refreshFeedbackTable() {
        feedbackTableModel.setRowCount(0);
        List<Feedback> feedbackList = feedbackRepository.getAllFeedback();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Feedback fb : feedbackList) {
            String timeStr = fb.getCreatedAt() != null ? sdf.format(fb.getCreatedAt()) : "-";
            feedbackTableModel.addRow(new Object[]{
                "#" + fb.getFeedbackId(),
                "TK-" + fb.getToken(),
                fb.getCustomerName(),
                fb.getStarDisplay(),
                fb.getComments().isEmpty() ? "(No comment)" : fb.getComments(),
                timeStr
            });
        }

        double avg = feedbackRepository.getAverageRating();
        int total = feedbackRepository.getTotalFeedbackCount();

        if (avgRatingLabel != null) {
            avgRatingLabel.setText(String.format("⭐ %.1f / 5.0", avg));
        }
        if (totalFeedbackLabel != null) {
            totalFeedbackLabel.setText(String.valueOf(total) + " reviews");
        }
    }

    private void exportOrdersToCsv() {
        List<Order> allOrders = orderRepository.getAllOrders();
        if (allOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No orders to export.", "Empty", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String filename = "orderease_campus_sales_report.csv";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("Order ID,Token,Customer,Items Ordered,Total Amount (INR),Order Time,Status");
            for (Order o : allOrders) {
                String timeStr = o.getOrderTime() != null ? sdf.format(o.getOrderTime()) : "N/A";
                String cust = o.getCustomerName() != null ? o.getCustomerName().replace(",", " ") : "Customer #" + o.getCustomerId();
                String items = o.getItemsSummary() != null ? o.getItemsSummary().replace(",", ";") : "";

                pw.println(String.format("#%d,TK-%d,\"%s\",\"%s\",%.2f,%s,%s",
                        o.getOrderId(), o.getToken(), cust, items, o.getTotalAmount(), timeStr, o.getStatus()));
            }

            File f = new File(filename);
            JOptionPane.showMessageDialog(this, "Sales Report exported successfully to:\n" + f.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Users Handlers
    private void refreshUsersTable() {
        usersTableModel.setRowCount(0);
        List<User> users = userRepository.getAllUsers();
        for (User u : users) {
            usersTableModel.addRow(new Object[]{
                u.getUserId(),
                u.getUsername(),
                u.getRole(),
                u.getCustomerId() > 0 ? u.getCustomerId() : "-",
                u.getCustomerName(),
                u.getPhone() != null && !u.getPhone().isEmpty() ? u.getPhone() : "-"
            });
        }
    }
}