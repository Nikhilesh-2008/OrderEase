package gui;

import util.UIUtils;
import util.UIUtils.RoundedButton;
import util.UIUtils.RoundedPanel;
import util.UIUtils.RoundedPasswordField;
import util.UIUtils.RoundedTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public LoginFrame() {
        UIUtils.setupDarkUI();

        setTitle("OrderEase | Smart Canteen Portal");
        setSize(960, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIUtils.BACKGROUND);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(UIUtils.BACKGROUND);

        // ==========================================
        // LEFT HERO / BRANDING PANEL
        // ==========================================
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(UIUtils.BACKGROUND);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(70, 50, 60, 40));

        // Pill tag
        JPanel tagPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 122, 41, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UIUtils.ORANGE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        tagPanel.setOpaque(false);
        tagPanel.setMaximumSize(new Dimension(190, 30));
        tagPanel.setPreferredSize(new Dimension(190, 30));
        tagPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagLabel = new JLabel("⚡ SMART CANTEEN 2.0");
        tagLabel.setFont(UIUtils.BADGE_FONT);
        tagLabel.setForeground(UIUtils.ORANGE);
        tagPanel.add(tagLabel);

        leftPanel.add(tagPanel);
        leftPanel.add(Box.createVerticalStrut(20));

        JLabel logo = new JLabel("OrderEase");
        logo.setFont(new Font(UIUtils.TITLE_HERO.getFamily(), Font.BOLD, 42));
        logo.setForeground(UIUtils.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(logo);

        leftPanel.add(Box.createVerticalStrut(15));

        JLabel heading = new JLabel("<html>Skip the Queue,<br><span style='color:#FF7A29'>Not the Food.</span></html>");
        heading.setFont(new Font(UIUtils.TITLE_HERO.getFamily(), Font.BOLD, 30));
        heading.setForeground(UIUtils.WHITE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(heading);

        leftPanel.add(Box.createVerticalStrut(25));

        JLabel bullet1 = createFeatureBullet("⚡ Instant Token Generation & Live Queue Sync");
        JLabel bullet2 = createFeatureBullet("🍳 Real-Time Kitchen Display Order Processing");
        JLabel bullet3 = createFeatureBullet("📊 Live Customer Order Tracking & Ready Alerts");
        JLabel bullet4 = createFeatureBullet("🔒 Role-Based Portals: Customer, Kitchen, Admin");

        leftPanel.add(bullet1);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(bullet2);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(bullet3);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(bullet4);

        leftPanel.add(Box.createVerticalGlue());

        JLabel footerNote = new JLabel("Fast ordering. Easy tracking. Better canteen experience.");
        footerNote.setFont(UIUtils.SMALL_FONT);
        footerNote.setForeground(UIUtils.MUTED_DARK);
        footerNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(footerNote);

        // ==========================================
        // RIGHT SIDE LOGIN CARD
        // ==========================================
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UIUtils.BACKGROUND);

        RoundedPanel card = new RoundedPanel(18, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));
        card.setPreferredSize(new Dimension(380, 480));

        JLabel loginTitle = new JLabel("Welcome Back");
        loginTitle.setFont(new Font(UIUtils.TITLE_FONT.getFamily(), Font.BOLD, 26));
        loginTitle.setForeground(UIUtils.WHITE);
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(loginTitle);

        card.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Sign in to access your portal");
        subtitle.setFont(UIUtils.SUBTITLE_FONT);
        subtitle.setForeground(UIUtils.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitle);

        card.add(Box.createVerticalStrut(25));

        usernameField = new RoundedTextField(20, 10);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new RoundedPasswordField(20, 10);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleBox = new JComboBox<>(new String[]{"CUSTOMER", "KITCHEN", "ADMIN"});
        roleBox.setFont(UIUtils.BODY_FONT);
        roleBox.setBackground(UIUtils.INPUT_BG);
        roleBox.setForeground(UIUtils.WHITE);
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        roleBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                l.setBackground(isSelected ? UIUtils.TABLE_SELECTION : UIUtils.INPUT_BG);
                l.setForeground(isSelected ? UIUtils.WHITE : UIUtils.MUTED);
                return l;
            }
        });

        card.add(makeLabel("Username"));
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);

        card.add(Box.createVerticalStrut(14));

        card.add(makeLabel("Password"));
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);

        card.add(Box.createVerticalStrut(14));

        card.add(makeLabel("Login Role"));
        card.add(Box.createVerticalStrut(6));
        card.add(roleBox);

        card.add(Box.createVerticalStrut(24));

        RoundedButton loginButton = new RoundedButton("Sign In", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 10);
        loginButton.setFont(new Font(UIUtils.BUTTON_FONT.getFamily(), Font.BOLD, 15));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> login());
        card.add(loginButton);

        card.add(Box.createVerticalStrut(16));

        RoundedButton registerButton = new RoundedButton("Create Customer Account", UIUtils.CARD, UIUtils.CARD_HOVER, UIUtils.ORANGE, 10);
        registerButton.setFont(UIUtils.SUBTITLE_FONT);
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.addActionListener(e -> {
            new RegisterFrame();
            dispose();
        });
        card.add(registerButton);

        rightPanel.add(card);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
        getRootPane().setDefaultButton(loginButton);

        setVisible(true);
    }

    private JLabel createFeatureBullet(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.BODY_FONT);
        label.setForeground(UIUtils.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UIUtils.WHITE);
        label.setFont(new Font(UIUtils.BODY_BOLD.getFamily(), Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter your username and password.",
                "Missing Details",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        service.AuthService authService = new service.AuthService();
        String result = authService.login(username, password, role);

        if (result.equals("INVALID_CREDENTIALS")) {
            JOptionPane.showMessageDialog(
                this,
                "Incorrect username or password.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
            );
        } else if (result.equals("UNAUTHORIZED")) {
            JOptionPane.showMessageDialog(
                this,
                "Unauthorized Access!\n" +
                "Your account does not have the selected role.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
            );
        } else if (result.equals("DATABASE_ERROR")) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to connect to the database.\n" +
                "Please check your MySQL connection.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        } else if (result.equals("CUSTOMER")) {
            model.User user = authService.getAuthenticatedUser(username);
            JOptionPane.showMessageDialog(
                this,
                "Customer login successful! Welcome, " + (user != null ? user.getCustomerName() : username) + "!"
            );
            new CustomerFrame(user);
            dispose();
        } else if (result.equals("KITCHEN")) {
            JOptionPane.showMessageDialog(
                this,
                "Kitchen login successful!"
            );
            new KitchenFrame();
            dispose();
        } else if (result.equals("ADMIN")) {
            JOptionPane.showMessageDialog(
                this,
                "Admin login successful!"
            );
            new AdminFrame();
            dispose();
        }
    }
}