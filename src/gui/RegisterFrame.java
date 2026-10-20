package gui;

import service.AuthService;
import util.UIUtils;
import util.UIUtils.RoundedButton;
import util.UIUtils.RoundedPanel;
import util.UIUtils.RoundedPasswordField;
import util.UIUtils.RoundedTextField;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private final AuthService authService = new AuthService();

    public RegisterFrame() {
        UIUtils.setupDarkUI();

        setTitle("OrderEase | Create Customer Account");
        setSize(960, 640);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

        JPanel tagPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 185, 129, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UIUtils.GREEN);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        tagPanel.setOpaque(false);
        tagPanel.setMaximumSize(new Dimension(190, 30));
        tagPanel.setPreferredSize(new Dimension(190, 30));
        tagPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagLabel = new JLabel("✨ QUICK REGISTRATION");
        tagLabel.setFont(UIUtils.BADGE_FONT);
        tagLabel.setForeground(UIUtils.GREEN_HOVER);
        tagPanel.add(tagLabel);

        leftPanel.add(tagPanel);
        leftPanel.add(Box.createVerticalStrut(20));

        JLabel logo = new JLabel("OrderEase");
        logo.setFont(new Font(UIUtils.TITLE_HERO.getFamily(), Font.BOLD, 42));
        logo.setForeground(UIUtils.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(logo);

        leftPanel.add(Box.createVerticalStrut(15));

        JLabel heading = new JLabel("<html>Join the Future of<br><span style='color:#10B981'>Smart Canteens.</span></html>");
        heading.setFont(new Font(UIUtils.TITLE_HERO.getFamily(), Font.BOLD, 30));
        heading.setForeground(UIUtils.WHITE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(heading);

        leftPanel.add(Box.createVerticalStrut(25));

        JLabel bullet1 = createFeatureBullet("✓ Browse full daily menu with live pricing");
        JLabel bullet2 = createFeatureBullet("✓ Add items to your cart & place orders in seconds");
        JLabel bullet3 = createFeatureBullet("✓ Receive sequential digital tokens instantly");
        JLabel bullet4 = createFeatureBullet("✓ Live tracking status from preparation to pickup");

        leftPanel.add(bullet1);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(bullet2);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(bullet3);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(bullet4);

        leftPanel.add(Box.createVerticalGlue());

        JLabel footerNote = new JLabel("Sign up in 30 seconds. No waiting in physical lines.");
        footerNote.setFont(UIUtils.SMALL_FONT);
        footerNote.setForeground(UIUtils.MUTED_DARK);
        footerNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(footerNote);

        // ==========================================
        // RIGHT REGISTRATION CARD
        // ==========================================
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UIUtils.BACKGROUND);

        RoundedPanel card = new RoundedPanel(18, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));
        card.setPreferredSize(new Dimension(380, 520));

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font(UIUtils.TITLE_FONT.getFamily(), Font.BOLD, 26));
        title.setForeground(UIUtils.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);

        card.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Register as a new customer");
        subtitle.setFont(UIUtils.SUBTITLE_FONT);
        subtitle.setForeground(UIUtils.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitle);

        card.add(Box.createVerticalStrut(20));

        usernameField = new RoundedTextField(20, 10);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        phoneField = new RoundedTextField(20, 10);
        phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        phoneField.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new RoundedPasswordField(20, 10);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        confirmPasswordField = new RoundedPasswordField(20, 10);
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        confirmPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(makeLabel("Username"));
        card.add(Box.createVerticalStrut(5));
        card.add(usernameField);

        card.add(Box.createVerticalStrut(10));

        card.add(makeLabel("Phone Number (Optional)"));
        card.add(Box.createVerticalStrut(5));
        card.add(phoneField);

        card.add(Box.createVerticalStrut(10));

        card.add(makeLabel("Password (Min. 8 characters)"));
        card.add(Box.createVerticalStrut(5));
        card.add(passwordField);

        card.add(Box.createVerticalStrut(10));

        card.add(makeLabel("Confirm Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(confirmPasswordField);

        card.add(Box.createVerticalStrut(20));

        RoundedButton registerButton = new RoundedButton("Create Account", UIUtils.GREEN, UIUtils.GREEN_HOVER, Color.BLACK, 10);
        registerButton.setFont(new Font(UIUtils.BUTTON_FONT.getFamily(), Font.BOLD, 15));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.addActionListener(e -> register());
        card.add(registerButton);

        card.add(Box.createVerticalStrut(12));

        RoundedButton backButton = new RoundedButton("Back to Sign In", UIUtils.CARD, UIUtils.CARD_HOVER, UIUtils.ORANGE, 10);
        backButton.setFont(UIUtils.SUBTITLE_FONT);
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });
        card.add(backButton);

        rightPanel.add(card);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
        getRootPane().setDefaultButton(registerButton);

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

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please fill in all required fields.",
                "Missing Details",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!username.matches("[a-zA-Z0-9_]{3,50}")) {
            JOptionPane.showMessageDialog(
                this,
                "Username must contain 3-50 characters.\nUse only letters, numbers, and underscores.",
                "Invalid Username",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(
                this,
                "Password must contain at least 8 characters.",
                "Weak Password",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(
                this,
                "Passwords do not match.",
                "Password Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String phone = phoneField.getText().trim();
        registerButtonAction(username, phone, password);
    }

    private void registerButtonAction(String username, String phone, String password) {
        String result = authService.register(username, password, phone);

        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(
                this,
                "Account created successfully!\nYou can now sign in.",
                "Registration Successful",
                JOptionPane.INFORMATION_MESSAGE
            );
            new LoginFrame();
            dispose();
        } else if ("USERNAME_EXISTS".equals(result)) {
            JOptionPane.showMessageDialog(
                this,
                "This username already exists.\nPlease choose another username.",
                "Username Unavailable",
                JOptionPane.WARNING_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Could not create your account.\nCheck the terminal for database errors.",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}