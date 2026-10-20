package util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIUtils {

    // --- COLOR PALETTE ---
    public static final Color BACKGROUND = new Color(15, 23, 42);       // #0F172A Deep Navy Slate
    public static final Color BACKGROUND_DARK = new Color(11, 17, 32);  // #0B1120 Midnight
    public static final Color CARD = new Color(30, 41, 59);             // #1E293B Elevated Dark Surface
    public static final Color CARD_HOVER = new Color(38, 51, 73);
    public static final Color CARD_BORDER = new Color(51, 65, 85);      // #334155 Subtle Slate Border

    public static final Color ORANGE = new Color(255, 122, 41);         // #FF7A29 Primary Warm Amber
    public static final Color ORANGE_HOVER = new Color(255, 150, 84);
    public static final Color ORANGE_DARK = new Color(217, 95, 20);

    public static final Color WHITE = new Color(248, 250, 252);         // #F8FAFC Pure Crisp White
    public static final Color MUTED = new Color(148, 163, 184);         // #94A3B8 Secondary Text
    public static final Color MUTED_DARK = new Color(100, 116, 139);

    public static final Color GREEN = new Color(16, 185, 129);          // #10B981 Emerald
    public static final Color GREEN_HOVER = new Color(52, 211, 153);

    public static final Color BLUE = new Color(59, 130, 246);           // #3B82F6 Electric Blue
    public static final Color BLUE_HOVER = new Color(96, 165, 250);

    public static final Color RED = new Color(239, 68, 68);             // #EF4444 Crimson
    public static final Color RED_HOVER = new Color(248, 113, 113);

    public static final Color INPUT_BG = new Color(23, 32, 48);
    public static final Color INPUT_BORDER = new Color(59, 75, 100);
    public static final Color INPUT_BORDER_FOCUS = new Color(255, 122, 41);

    public static final Color TABLE_HEADER_BG = new Color(19, 27, 43);
    public static final Color TABLE_ROW_ALT = new Color(25, 35, 52);
    public static final Color TABLE_SELECTION = new Color(42, 60, 88);

    // --- TYPOGRAPHY ---
    public static final Font TITLE_HERO = getFont(Font.BOLD, 30);
    public static final Font TITLE_FONT = getFont(Font.BOLD, 24);
    public static final Font SECTION_FONT = getFont(Font.BOLD, 18);
    public static final Font SUBTITLE_FONT = getFont(Font.PLAIN, 14);
    public static final Font BODY_FONT = getFont(Font.PLAIN, 14);
    public static final Font BODY_BOLD = getFont(Font.BOLD, 14);
    public static final Font BUTTON_FONT = getFont(Font.BOLD, 14);
    public static final Font BADGE_FONT = getFont(Font.BOLD, 12);
    public static final Font SMALL_FONT = getFont(Font.PLAIN, 12);

    public static Font getFont(int style, int size) {
        String[] preferredFonts = {"Segoe UI", "Inter", "Helvetica Neue", "Arial", "SansSerif"};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        for (String pref : preferredFonts) {
            for (String avail : available) {
                if (pref.equalsIgnoreCase(avail)) {
                    return new Font(pref, style, size);
                }
            }
        }
        return new Font("SansSerif", style, size);
    }

    // --- SETUP GLOBAL LOOK & FEEL AND ANTIALIASING ---
    public static void setupDarkUI() {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("OptionPane.background", CARD);
        UIManager.put("Panel.background", CARD);
        UIManager.put("OptionPane.messageForeground", WHITE);
        UIManager.put("OptionPane.messageFont", BODY_FONT);
        UIManager.put("Button.font", BUTTON_FONT);
        UIManager.put("Button.background", ORANGE);
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("ComboBox.background", INPUT_BG);
        UIManager.put("ComboBox.foreground", WHITE);
        UIManager.put("ComboBox.selectionBackground", TABLE_SELECTION);
        UIManager.put("ComboBox.selectionForeground", WHITE);
    }

    // --- CUSTOM ROUNDED CONTAINER PANEL ---
    public static class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;
        private Color borderColor;
        private int borderWidth;

        public RoundedPanel(int radius, Color bgColor) {
            this(radius, bgColor, null, 0);
        }

        public RoundedPanel(int radius, Color bgColor, Color borderColor, int borderWidth) {
            super();
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.borderWidth = borderWidth;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (bgColor != null) {
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, w, h, radius, radius);
            }

            if (borderColor != null && borderWidth > 0) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderWidth));
                g2.drawRoundRect(borderWidth / 2, borderWidth / 2, w - borderWidth, h - borderWidth, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }

        public void setBgColor(Color color) {
            this.bgColor = color;
            repaint();
        }
    }

    // --- CUSTOM ROUNDED BUTTON ---
    public static class RoundedButton extends JButton {
        private Color normalColor;
        private Color hoverColor;
        private Color pressedColor;
        private Color textColor;
        private int radius;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public RoundedButton(String text, Color bg, Color hover, Color fg) {
            this(text, bg, hover, fg, 10);
        }

        public RoundedButton(String text, Color bg, Color hover, Color fg, int radius) {
            super(text);
            this.normalColor = bg;
            this.hoverColor = hover;
            this.pressedColor = hover.darker();
            this.textColor = fg;
            this.radius = radius;

            setFont(BUTTON_FONT);
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 16, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color currentBg;
            if (!isEnabled()) {
                currentBg = new Color(55, 65, 81);
            } else if (isPressed) {
                currentBg = pressedColor;
            } else if (isHovered) {
                currentBg = hoverColor;
            } else {
                currentBg = normalColor;
            }

            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // Draw text
            FontMetrics fm = g2.getFontMetrics(getFont());
            Rectangle stringBounds = fm.getStringBounds(getText(), g2).getBounds();
            int textX = (getWidth() - stringBounds.width) / 2;
            int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();

            g2.setColor(isEnabled() ? textColor : MUTED);
            g2.setFont(getFont());
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    // --- CUSTOM ROUNDED TEXT FIELD ---
    public static class RoundedTextField extends JTextField {
        private int radius;
        private Color bgColor;
        private Color borderColor;
        private Color focusColor;
        private boolean hasFocus = false;

        public RoundedTextField(int columns) {
            this(columns, 10);
        }

        public RoundedTextField(int columns, int radius) {
            super(columns);
            this.radius = radius;
            this.bgColor = INPUT_BG;
            this.borderColor = INPUT_BORDER;
            this.focusColor = ORANGE;

            setOpaque(false);
            setBackground(bgColor);
            setForeground(WHITE);
            setCaretColor(WHITE);
            setFont(BODY_FONT);
            setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    hasFocus = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    hasFocus = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.setColor(hasFocus ? focusColor : borderColor);
            g2.setStroke(new BasicStroke(hasFocus ? 1.6f : 1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- CUSTOM ROUNDED PASSWORD FIELD ---
    public static class RoundedPasswordField extends JPasswordField {
        private int radius;
        private Color bgColor;
        private Color borderColor;
        private Color focusColor;
        private boolean hasFocus = false;

        public RoundedPasswordField(int columns) {
            this(columns, 10);
        }

        public RoundedPasswordField(int columns, int radius) {
            super(columns);
            this.radius = radius;
            this.bgColor = INPUT_BG;
            this.borderColor = INPUT_BORDER;
            this.focusColor = ORANGE;

            setOpaque(false);
            setBackground(bgColor);
            setForeground(WHITE);
            setCaretColor(WHITE);
            setFont(BODY_FONT);
            setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    hasFocus = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    hasFocus = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.setColor(hasFocus ? focusColor : borderColor);
            g2.setStroke(new BasicStroke(hasFocus ? 1.6f : 1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- SLEEK MODERN SCROLLBAR UI ---
    public static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(BACKGROUND);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(100, 116, 139) : new Color(71, 85, 105));
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }
    }

    // --- SLEEK MODERN TABBED PANE UI ---
    public static class ModernTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(10, 20, 10, 20);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected) {
                g2.setColor(CARD);
                g2.fillRoundRect(x, y, w, h + 5, 8, 8);
                // Active indicator line
                g2.setColor(ORANGE);
                g2.fillRect(x, y + h - 3, w, 3);
            } else {
                g2.setColor(BACKGROUND);
                g2.fillRect(x, y, w, h);
            }
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            // No harsh borders
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(CARD_BORDER);
            g2.drawLine(0, 0, tabPane.getWidth(), 0);
            g2.dispose();
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // Suppress dotted focus rect
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            g.setFont(isSelected ? BODY_BOLD : BODY_FONT);
            g.setColor(isSelected ? ORANGE : MUTED);
            g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }
    }

    // --- TABLE STYLING ---
    public static void styleTable(JTable table) {
        table.setBackground(CARD);
        table.setForeground(WHITE);
        table.setGridColor(new Color(40, 52, 71));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setRowHeight(38);
        table.setFont(BODY_FONT);
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(WHITE);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(ORANGE);
        header.setFont(BODY_BOLD);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CARD_BORDER));
        header.setReorderingAllowed(false);

        // Alternating row renderer with custom padding
        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFoc, row, col);
                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? CARD : TABLE_ROW_ALT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(baseRenderer);
        }
    }

    public static JScrollPane createModernScrollPane(JComponent view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.getViewport().setBackground(CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        return scrollPane;
    }

    // --- BADGE CELL RENDERER FOR STATUS & TOKENS ---
    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String text = value != null ? value.toString().trim() : "";
            JPanel pillContainer = new JPanel(new GridBagLayout()) {
                @Override
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                }
            };
            pillContainer.setOpaque(true);
            pillContainer.setBackground(isSelected ? TABLE_SELECTION : (row % 2 == 0 ? CARD : TABLE_ROW_ALT));

            Color bgPill;
            Color fgPill;

            if ("READY".equalsIgnoreCase(text)) {
                bgPill = new Color(16, 185, 129, 45);
                fgPill = GREEN_HOVER;
                text = "● READY";
            } else if ("PREPARING".equalsIgnoreCase(text)) {
                bgPill = new Color(255, 122, 41, 45);
                fgPill = ORANGE_HOVER;
                text = "● PREPARING";
            } else if ("PLACED".equalsIgnoreCase(text)) {
                bgPill = new Color(59, 130, 246, 45);
                fgPill = BLUE_HOVER;
                text = "● PLACED";
            } else if ("COLLECTED".equalsIgnoreCase(text)) {
                bgPill = new Color(148, 163, 184, 35);
                fgPill = MUTED;
                text = "✓ COLLECTED";
            } else if ("CANCELLED".equalsIgnoreCase(text)) {
                bgPill = new Color(239, 68, 68, 45);
                fgPill = RED_HOVER;
                text = "✕ CANCELLED";
            } else if ("Available".equalsIgnoreCase(text) || "true".equalsIgnoreCase(text)) {
                bgPill = new Color(16, 185, 129, 45);
                fgPill = GREEN_HOVER;
                text = "● Available";
            } else if ("Unavailable".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
                bgPill = new Color(239, 68, 68, 45);
                fgPill = RED_HOVER;
                text = "✕ Unavailable";
            } else {
                bgPill = new Color(148, 163, 184, 30);
                fgPill = WHITE;
            }

            final Color fBgPill = bgPill;
            final Color fFgPill = fgPill;
            final String fText = text;

            JPanel badge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(fBgPill);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                    g2.setFont(BADGE_FONT);
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(fText);
                    int th = fm.getAscent();
                    int tx = (getWidth() - tw) / 2;
                    int ty = (getHeight() - fm.getHeight()) / 2 + th;

                    g2.setColor(fFgPill);
                    g2.drawString(fText, tx, ty);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(115, 26);
                }
            };
            badge.setOpaque(false);
            pillContainer.add(badge);
            return pillContainer;
        }
    }

    public static class TokenBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String token = value != null ? value.toString() : "";
            JPanel container = new JPanel(new GridBagLayout());
            container.setOpaque(true);
            container.setBackground(isSelected ? TABLE_SELECTION : (row % 2 == 0 ? CARD : TABLE_ROW_ALT));

            JPanel badge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 122, 41, 35));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(ORANGE);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                    g2.setFont(BODY_BOLD);
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(token);
                    int tx = (getWidth() - tw) / 2;
                    int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                    g2.setColor(WHITE);
                    g2.drawString(token, tx, ty);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(80, 26);
                }
            };
            badge.setOpaque(false);
            container.add(badge);
            return container;
        }
    }

    public static void playNotificationChime() {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception ignored) {}
    }

    public static class CanteenBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String name = value != null ? value.toString() : "";
            JPanel container = new JPanel(new GridBagLayout());
            container.setOpaque(true);
            container.setBackground(isSelected ? TABLE_SELECTION : (row % 2 == 0 ? CARD : TABLE_ROW_ALT));

            Color bg;
            Color fg;
            if (name.contains("Snack") || name.contains("Shake")) {
                bg = new Color(255, 122, 41, 35);
                fg = ORANGE_HOVER;
            } else if (name.contains("Chai") || name.contains("Juice")) {
                bg = new Color(59, 130, 246, 35);
                fg = BLUE_HOVER;
            } else {
                bg = new Color(16, 185, 129, 35);
                fg = GREEN_HOVER;
            }

            final Color fBg = bg;
            final Color fFg = fg;
            final String fText = name;

            JPanel badge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(fBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                    g2.setFont(SMALL_FONT);
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(fText);
                    int tx = (getWidth() - tw) / 2;
                    int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                    g2.setColor(fFg);
                    g2.drawString(fText, tx, ty);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(170, 24);
                }
            };
            badge.setOpaque(false);
            container.add(badge);
            return container;
        }
    }
}
