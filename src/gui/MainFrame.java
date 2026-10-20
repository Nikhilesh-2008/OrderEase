package gui;

import util.UIUtils;
import util.UIUtils.RoundedButton;
import util.UIUtils.RoundedPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        UIUtils.setupDarkUI();

        setTitle("OrderEase | Smart Canteen Management");
        setSize(940, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BACKGROUND);
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(UIUtils.BACKGROUND_DARK);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.CARD_BORDER),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        JLabel title = new JLabel("OrderEase");
        title.setForeground(UIUtils.ORANGE);
        title.setFont(new Font(UIUtils.TITLE_HERO.getFamily(), Font.BOLD, 36));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Campus Smart Canteen & Public Token Display System");
        subtitle.setForeground(UIUtils.MUTED);
        subtitle.setFont(UIUtils.SUBTITLE_FONT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(UIUtils.BACKGROUND);

        RoundedPanel card = new RoundedPanel(18, UIUtils.CARD, UIUtils.CARD_BORDER, 1);
        card.setLayout(new GridLayout(3, 1, 16, 16));
        card.setBorder(BorderFactory.createEmptyBorder(35, 45, 35, 45));
        card.setPreferredSize(new Dimension(460, 310));

        RoundedButton customerButton = new RoundedButton("🍽  Launch Customer Portal", UIUtils.ORANGE, UIUtils.ORANGE_HOVER, Color.BLACK, 10);
        RoundedButton kitchenButton = new RoundedButton("🍳  Launch Kitchen Station", UIUtils.BLUE, UIUtils.BLUE_HOVER, Color.WHITE, 10);
        RoundedButton tvButton = new RoundedButton("📺  Launch Public TV Token Board", UIUtils.GREEN, UIUtils.GREEN_HOVER, Color.BLACK, 10);

        customerButton.setFont(new Font(UIUtils.BUTTON_FONT.getFamily(), Font.BOLD, 15));
        kitchenButton.setFont(new Font(UIUtils.BUTTON_FONT.getFamily(), Font.BOLD, 15));
        tvButton.setFont(new Font(UIUtils.BUTTON_FONT.getFamily(), Font.BOLD, 15));

        customerButton.addActionListener(e -> {
            new CustomerFrame();
            dispose();
        });

        kitchenButton.addActionListener(e -> {
            new KitchenFrame();
            dispose();
        });

        tvButton.addActionListener(e -> {
            new TokenDisplayFrame();
        });

        card.add(customerButton);
        card.add(kitchenButton);
        card.add(tvButton);

        centerPanel.add(card);
        add(centerPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}