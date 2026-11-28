package gui;

import java.awt.*;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import service.SqlStore;

public class LoginPage extends JFrame {
    private final SqlStore store = new SqlStore();
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();

    public LoginPage() {
        setTitle("Pickleball Queue - Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(360, 340));

        Theme.BackgroundPanel background = new Theme.BackgroundPanel(new GridBagLayout());
        background.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel title = heading(
            "<html><div style='text-align:center'>Pickleball Open Play<br>Queue Management</div></html>",
            Font.BOLD, 16);
        JLabel subtitle = heading("Sign in to continue", Font.PLAIN, 12);

        Theme.RoundedPanel card = new Theme.RoundedPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(Theme.authField("Username", username));
        card.add(Theme.authField("Password", password));

        JButton signIn = new JButton("Sign In");
        Theme.stylePrimaryButton(signIn);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton signUp = new JButton("Create an account");
        signUp.setFont(Theme.FONT_SMALL);
        signUp.setForeground(new Color(0, 100, 180));
        signUp.setBorderPainted(false);
        signUp.setContentAreaFilled(false);
        signUp.setFocusPainted(false);
        signUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUp.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Theme.authAction(signIn));
        card.add(Box.createVerticalStrut(8));
        card.add(Theme.authLink(signUp));

        JPanel content = content(title, subtitle, card);
        addContent(background, content);
        setContentPane(background);
        setSize(400, 360);
        setLocationRelativeTo(null);

        username.addActionListener(event -> password.requestFocusInWindow());
        password.addActionListener(event -> signIn());
        signIn.addActionListener(event -> signIn());
        signUp.addActionListener(event -> {
            Theme.showLike(this, new SignUpPage());
        });
    }

    static JLabel heading(String text, int style, int size) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    static JPanel content(JLabel title, JLabel subtitle, JComponent card) {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setMaximumSize(new Dimension(560, Integer.MAX_VALUE));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(14));
        content.add(card);
        return content;
    }

    static void addContent(JPanel background, JPanel content) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        background.add(content, constraints);
    }

    private void signIn() {
        char[] enteredPassword = password.getPassword();
        try {
            int userId = store.authenticate(username.getText().trim(), enteredPassword);
            if (userId < 0) {
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new MainDashboard(userId).setVisible(true);
            dispose();
        } catch (SQLException | GeneralSecurityException exception) {
            JOptionPane.showMessageDialog(this,
                "Could not connect to or read the database:\n" + exception.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(enteredPassword, '\0');
        }
    }

    public static void main(String[] args) {
        main.Main.main(args);
    }
}
