package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Simple light / dark theme support.
 */
public final class Theme {

    public enum Mode { LIGHT, DARK }

    private static Mode mode = Mode.LIGHT;

    // Current colors (updated by setMode)
    public static Color BG;
    public static Color PANEL;
    public static Color CARD;
    public static Color INPUT;
    public static Color TEXT;
    public static Color TEXT_SEC;
    public static Color BORDER;
    public static Color ACCENT;
    public static Color DANGER;
    public static Color HEADER;
    public static Color SIDEBAR;
    public static Color STAT_BG;

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD, 16);

    public static final class BackgroundPanel extends JPanel {
        private final Image background;

        public BackgroundPanel(LayoutManager layout) {
            super(layout);
            java.net.URL imageUrl = Theme.class.getResource("/images/background.jpg");
            if (imageUrl == null) {
                throw new IllegalStateException("Missing login background: /images/background.jpg");
            }
            background = new ImageIcon(imageUrl).getImage();
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            int width = getWidth();
            int height = getHeight();
            double scale = Math.max(
                (double) width / background.getWidth(this),
                (double) height / background.getHeight(this));
            int imageWidth = (int) Math.ceil(background.getWidth(this) * scale);
            int imageHeight = (int) Math.ceil(background.getHeight(this) * scale);

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(background, (width - imageWidth) / 2,
                (height - imageHeight) / 2, imageWidth, imageHeight, this);
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRect(0, 0, width, height);
            g2.dispose();
        }
    }

    public static final class RoundedPanel extends JPanel {
        public RoundedPanel() {
            setOpaque(false);
            setBackground(new Color(255, 255, 255, 220));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setColor(new Color(255, 255, 255, 170));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
        }
    }

    static {
        setMode(Mode.LIGHT);
    }

    public static Mode getMode() { return mode; }

    public static void setMode(Mode m) {
        mode = m;
        if (m == Mode.DARK) {
            BG       = new Color(30, 30, 34);
            PANEL    = new Color(40, 40, 46);
            CARD     = new Color(45, 45, 52);
            INPUT    = new Color(50, 50, 58);
            TEXT     = new Color(230, 230, 230);
            TEXT_SEC = new Color(160, 160, 170);
            BORDER   = new Color(70, 70, 80);
            ACCENT   = new Color(40, 167, 69);
            DANGER   = new Color(200, 60, 50);
            HEADER   = new Color(35, 140, 60);
            SIDEBAR  = new Color(36, 36, 42);
            STAT_BG  = new Color(50, 50, 58);
        } else {
            BG       = new Color(245, 245, 247);
            PANEL    = Color.WHITE;
            CARD     = Color.WHITE;
            INPUT    = Color.WHITE;
            TEXT     = new Color(30, 30, 30);
            TEXT_SEC = new Color(100, 100, 100);
            BORDER   = new Color(210, 210, 215);
            ACCENT   = new Color(0, 140, 0);
            DANGER   = new Color(190, 45, 40);
            HEADER   = new Color(0, 130, 0);
            SIDEBAR  = new Color(250, 250, 252);
            STAT_BG  = new Color(248, 248, 250);
        }
    }

    public static void stylePrimaryButton(JButton b) {
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BUTTON);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleDangerButton(JButton b) {
        b.setBackground(DANGER);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BUTTON);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(5, 12, 5, 12));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleSecondaryButton(JButton b) {
        b.setBackground(PANEL);
        b.setForeground(TEXT);
        b.setFont(FONT_BUTTON);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(5, 12, 5, 12)
        ));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleTextField(JTextField f) {
        f.setBackground(INPUT);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setFont(FONT_BODY);
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(5, 8, 5, 8)
        ));
    }

    public static void styleTextArea(JTextArea a) {
        a.setBackground(INPUT);
        a.setForeground(TEXT);
        a.setCaretColor(TEXT);
        a.setFont(FONT_BODY);
        a.setBorder(new EmptyBorder(6, 8, 6, 8));
    }

    public static void styleScrollPane(JScrollPane s) {
        s.setBorder(new LineBorder(BORDER, 1));
        s.getViewport().setBackground(PANEL);
        s.setBackground(PANEL);
    }

    public static void styleLabel(JLabel l) {
        l.setForeground(TEXT);
        l.setFont(FONT_BODY);
    }

    public static JPanel authField(String title, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(FONT_SMALL);
        styleTextField(field);
        field.setPreferredSize(new Dimension(300, 30));
        row.add(label, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height + 10));
        return row;
    }

    public static JPanel authAction(JButton button) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(button, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        return row;
    }

    public static JPanel authLink(JButton button) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row.setOpaque(false);
        row.add(button);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    public static void showLike(JFrame current, JFrame next) {
        int state = current.getExtendedState() & ~Frame.ICONIFIED;
        if ((state & Frame.MAXIMIZED_BOTH) == 0) {
            Dimension minimum = next.getMinimumSize();
            next.setBounds(current.getX(), current.getY(),
                Math.max(current.getWidth(), minimum.width),
                Math.max(current.getHeight(), minimum.height));
        }
        next.setExtendedState(state);
        next.setVisible(true);
        current.dispose();
    }

    public static void styleMuted(JLabel l) {
        l.setForeground(TEXT_SEC);
        l.setFont(FONT_SMALL);
    }
}
