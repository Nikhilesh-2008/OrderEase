package gui;

import model.Order;
import model.OrderItem;
import repository.FoodRepository;
import repository.OrderRepository;
import util.UIUtils;
import util.UIUtils.RoundedButton;
import util.UIUtils.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TokenDisplayFrame extends JFrame {

    private final OrderRepository orderRepository = new OrderRepository();
    private final FoodRepository foodRepository = new FoodRepository();

    private JComboBox<String> canteenStationBox;
    private JLabel clockLabel;
    private JPanel preparingContainer;
    private JPanel readyContainer;

    private Timer liveTimer;
    private final Set<Integer> announcedReadyTokens = new HashSet<>();

    public TokenDisplayFrame() {
        UIUtils.setupDarkUI();

        setTitle("OrderEase | Public Canteen Token Board (TV / Monitor Screen)");
        setSize(1200, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BACKGROUND_DARK);
        setLayout(new BorderLayout());

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Center Split Panel (PREPARING vs READY)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(UIUtils.BACKGROUND_DARK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        centerPanel.add(createColumnPanel("NOW PREPARING", new Color(255, 122, 41), "🍳 Kitchen is currently preparing these orders", true));
        centerPanel.add(createColumnPanel("READY FOR PICKUP", new Color(16, 185, 129), "🔔 Please collect your food at the counter", false));

        add(centerPanel, BorderLayout.CENTER);

        // Live refresh timer (runs every 3 seconds for fast TV display sync)
        liveTimer = new Timer(3000, e -> updateTokenBoard());
        liveTimer.start();

        // Initial load
        updateTokenBoard();

        setVisible(true);
    }

    @Override
    public void dispose() {
        if (liveTimer != null) {
            liveTimer.stop();
        }
        super.dispose();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(9, 14, 26));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.CARD_BORDER),
            BorderFactory.createEmptyBorder(18, 30, 18, 30)
        ));

        // Left Branding
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("OrderEase");
        logo.setFont(new Font(UIUtils.TITLE_HERO.getFamily(), Font.BOLD, 30));
        logo.setForeground(UIUtils.ORANGE);

        JLabel sub = new JLabel("LIVE CAMPUS TOKEN BOARD");
        sub.setFont(new Font(UIUtils.BADGE_FONT.getFamily(), Font.BOLD, 14));
        sub.setForeground(UIUtils.MUTED);

        left.add(logo);
        left.add(sub);

        // Center Canteen Station Filter
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        center.setOpaque(false);

        JLabel filterLbl = new JLabel("Canteen Station:");
        filterLbl.setFont(UIUtils.BODY_BOLD);
        filterLbl.setForeground(UIUtils.WHITE);

        String[] stations = {
            "All Campus Canteens",
            FoodRepository.CANTEEN_SNACKS,
            FoodRepository.CANTEEN_CHAI,
            FoodRepository.CANTEEN_FOOD_COURT
        };
        canteenStationBox = new JComboBox<>(stations);
        canteenStationBox.setFont(UIUtils.BODY_FONT);
        canteenStationBox.setBackground(UIUtils.INPUT_BG);
        canteenStationBox.setForeground(UIUtils.WHITE);
        canteenStationBox.setPreferredSize(new Dimension(270, 36));
        canteenStationBox.addActionListener(e -> updateTokenBoard());

        center.add(filterLbl);
        center.add(canteenStationBox);

        // Right Live Clock & Fullscreen
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        clockLabel.setForeground(UIUtils.WHITE);
        updateClock();

        Timer clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        RoundedButton fullscreenBtn = new RoundedButton("⛶ Fullscreen", UIUtils.CARD_HOVER, UIUtils.CARD_BORDER, UIUtils.WHITE, 8);
        fullscreenBtn.setPreferredSize(new Dimension(120, 36));
        fullscreenBtn.addActionListener(e -> toggleFullscreen());

        right.add(clockLabel);
        right.add(fullscreenBtn);

        header.add(left, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private void updateClock() {
        if (clockLabel != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            clockLabel.setText(sdf.format(new Date()));
        }
    }

    private boolean isFullscreen = false;
    private void toggleFullscreen() {
        dispose();
        setUndecorated(!isFullscreen);
        if (!isFullscreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            isFullscreen = true;
        } else {
            setExtendedState(JFrame.NORMAL);
            setSize(1200, 780);
            setLocationRelativeTo(null);
            isFullscreen = false;
        }
        setVisible(true);
    }

    private JPanel createColumnPanel(String title, Color accentColor, String subtitle, boolean isPreparing) {
        RoundedPanel panel = new RoundedPanel(18, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        // Column Top Header
        JPanel colHeader = new JPanel();
        colHeader.setOpaque(false);
        colHeader.setLayout(new BoxLayout(colHeader, BoxLayout.Y_AXIS));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setOpaque(false);

        JPanel tag = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        tag.setOpaque(false);
        tag.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        JLabel tagText = new JLabel(title);
        tagText.setFont(new Font(UIUtils.TITLE_FONT.getFamily(), Font.BOLD, 22));
        tagText.setForeground(accentColor);
        tag.add(tagText);

        badgeRow.add(tag);
        colHeader.add(badgeRow);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UIUtils.SMALL_FONT);
        subLbl.setForeground(UIUtils.MUTED);
        subLbl.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 0));
        colHeader.add(subLbl);

        panel.add(colHeader, BorderLayout.NORTH);

        // Scrollable Grid of Token Cards
        JPanel tokensGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        tokensGrid.setBackground(UIUtils.CARD);

        if (isPreparing) {
            preparingContainer = tokensGrid;
        } else {
            readyContainer = tokensGrid;
        }

        JScrollPane scroll = UIUtils.createModernScrollPane(tokensGrid);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void updateTokenBoard() {
        if (preparingContainer == null || readyContainer == null) return;

        List<Order> activeOrders = orderRepository.getAllActiveOrders();
        String selectedStation = canteenStationBox != null ? (String) canteenStationBox.getSelectedItem() : "All Campus Canteens";
        boolean filterStation = selectedStation != null && !selectedStation.contains("All");

        List<Order> preparingOrders = new ArrayList<>();
        List<Order> readyOrders = new ArrayList<>();

        for (Order o : activeOrders) {
            if (filterStation && o.getItems() != null) {
                boolean match = false;
                for (OrderItem item : o.getItems()) {
                    model.FoodItem fi = foodRepository.getFoodItemById(item.getItemId());
                    if (fi != null && selectedStation.equalsIgnoreCase(fi.getCanteen())) {
                        match = true;
                        break;
                    }
                }
                if (!match) continue;
            }

            if ("PREPARING".equalsIgnoreCase(o.getStatus())) {
                preparingOrders.add(o);
            } else if ("READY".equalsIgnoreCase(o.getStatus())) {
                readyOrders.add(o);
            }
        }

        // Check for new ready tokens to chime
        boolean newReady = false;
        for (Order r : readyOrders) {
            if (!announcedReadyTokens.contains(r.getToken())) {
                announcedReadyTokens.add(r.getToken());
                newReady = true;
            }
        }
        if (newReady) {
            UIUtils.playNotificationChime();
        }

        renderTokenCards(preparingContainer, preparingOrders, new Color(255, 122, 41), "PREPARING");
        renderTokenCards(readyContainer, readyOrders, new Color(16, 185, 129), "READY");
    }

    private void renderTokenCards(JPanel container, List<Order> orders, Color accent, String statusText) {
        container.removeAll();

        if (orders.isEmpty()) {
            JLabel empty = new JLabel("No tokens currently " + statusText.toLowerCase() + ".");
            empty.setFont(UIUtils.SUBTITLE_FONT);
            empty.setForeground(UIUtils.MUTED_DARK);
            empty.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 0));
            container.add(empty);
        } else {
            for (Order o : orders) {
                JPanel card = createSingleTokenCard(o, accent, statusText);
                container.add(card);
            }
        }

        container.revalidate();
        container.repaint();
    }

    private JPanel createSingleTokenCard(Order o, Color accent, String statusText) {
        RoundedPanel card = new RoundedPanel(16, UIUtils.INPUT_BG, accent, 2);
        card.setPreferredSize(new Dimension(210, 115));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel tokenLbl = new JLabel("TK-" + o.getToken());
        tokenLbl.setFont(new Font(UIUtils.TITLE_HERO.getFamily(), Font.BOLD, 32));
        tokenLbl.setForeground(accent);
        tokenLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        String custName = o.getCustomerName() != null ? o.getCustomerName() : "Student";
        if (custName.length() > 18) {
            custName = custName.substring(0, 16) + "...";
        }
        JLabel custLbl = new JLabel(custName);
        custLbl.setFont(UIUtils.SMALL_FONT);
        custLbl.setForeground(UIUtils.WHITE);
        custLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusTag = new JLabel(statusText);
        statusTag.setFont(new Font(UIUtils.BADGE_FONT.getFamily(), Font.BOLD, 12));
        statusTag.setForeground(accent);
        statusTag.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(tokenLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(custLbl);
        card.add(Box.createVerticalStrut(3));
        card.add(statusTag);

        return card;
    }
}
