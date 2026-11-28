package main;

import gui.*;
import java.sql.*;
import javax.swing.*;
import service.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.setMode(Theme.Mode.LIGHT);
            SqlStore store = new SqlStore();
            try {
                JFrame page = store.hasUsers() ? new LoginPage() : new SignUpPage();
                page.setVisible(true);
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(null,
                    "Could not connect to the database:\n" + exception.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
