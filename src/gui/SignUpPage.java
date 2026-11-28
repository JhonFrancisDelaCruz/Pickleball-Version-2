package gui;

import java.awt.*;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import service.SqlStore;

public class SignUpPage extends JFrame {
    private final SqlStore store = new SqlStore();
    private final JTextField fullName = new JTextField();
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField confirmPassword = new JPasswordField();

    public SignUpPage() {
        setTitle("Pickleball Queue - Sign Up");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(380, 440));

        Theme.BackgroundPanel background = new Theme.BackgroundPanel(new GridBagLayout());
        background.setBorder(new EmptyBorder(20, 16, 20, 16));
        JLabel title = LoginPage.heading("Create Account", Font.BOLD, 16);
        JLabel subtitle = LoginPage.heading("Register a new account", Font.PLAIN, 12);

        Theme.RoundedPanel card = new Theme.RoundedPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(Theme.authField("Full name", fullName));
        card.add(Theme.authField("Username", username));
        card.add(Theme.authField("Password", password));
        card.add(Theme.authField("Confirm password", confirmPassword));

        JButton create = new JButton("Sign Up");
        Theme.stylePrimaryButton(create);
        create.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        create.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton back = new JButton("Back to Login");
        back.setFont(Theme.FONT_SMALL);
        back.setForeground(new Color(0, 100, 180));
        back.setBorderPainted(false);
        back.setContentAreaFilled(false);
        back.setFocusPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Theme.authAction(create));
        card.add(Box.createVerticalStrut(8));
        card.add(Theme.authLink(back));

        LoginPage.addContent(background, LoginPage.content(title, subtitle, card));
        setContentPane(background);
        setSize(420, 500);
        setLocationRelativeTo(null);

        create.addActionListener(event -> createAccount());
        confirmPassword.addActionListener(event -> createAccount());
        back.addActionListener(event -> {
            Theme.showLike(this, new LoginPage());
        });
    }

    private void createAccount() {
        String name = fullName.getText().trim();
        String user = username.getText().trim();
        char[] pass = password.getPassword();
        char[] confirmation = confirmPassword.getPassword();

        try {
            if (name.isEmpty() || user.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full name and username are required.",
                    "Sign Up", JOptionPane.WARNING_MESSAGE);
            } else if (pass.length < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.",
                    "Sign Up", JOptionPane.WARNING_MESSAGE);
            } else if (!Arrays.equals(pass, confirmation)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Sign Up", JOptionPane.WARNING_MESSAGE);
            } else {
                store.createUser(user, name, pass);
                JOptionPane.showMessageDialog(this, "Account created. You can now sign in.",
                    "Sign Up", JOptionPane.INFORMATION_MESSAGE);
                Theme.showLike(this, new LoginPage());
            }
        } catch (SQLException | GeneralSecurityException exception) {
            String message = exception instanceof SQLException
                    && ((SQLException) exception).getSQLState() != null
                    && ((SQLException) exception).getSQLState().startsWith("23")
                ? "That username is already in use."
                : "Could not save the account:\n" + exception.getMessage();
            JOptionPane.showMessageDialog(this, message, "Sign Up Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(pass, '\0');
            Arrays.fill(confirmation, '\0');
        }
    }
}
