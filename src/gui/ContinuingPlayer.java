package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import model.Player;
import service.QueueManager;

public class ContinuingPlayer extends JDialog {
    private final QueueManager queue;
    private final List<Boolean> continuing = new ArrayList<>();
    private final List<JButton> choiceButtons = new ArrayList<>();
    private List<Player> currentGamePlayers = new ArrayList<>();
    private JPanel rowsPanel;
    private boolean confirmed;

    public ContinuingPlayer(Frame parent, boolean modal, QueueManager queue) {
        super(parent, modal);
        this.queue = queue;
        initComponents();
        setTitle("Continue Playing?");
        setResizable(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public void setPlayers(List<Player> players) {
        currentGamePlayers = new ArrayList<>(players);
        continuing.clear();
        choiceButtons.clear();
        rowsPanel.removeAll();

        for (int i = 0; i < players.size(); i++) {
            continuing.add(false);
            Player player = players.get(i);
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 7, 5, 7)));

            JLabel name = new JLabel(player.getId() + " - " + player.getName()
                + (player.isLeaveAfterGame() ? " (leaving)" : ""));
            name.setFont(Theme.FONT_BODY);
            JButton choice = new JButton("No");
            choice.setFont(Theme.FONT_BUTTON);
            choice.setFocusPainted(false);
            choice.setOpaque(true);
            choice.setPreferredSize(new Dimension(54, 28));
            choice.setEnabled(!player.isLeaveAfterGame());
            choice.addActionListener(event -> toggle(choiceButtons.indexOf(choice)));
            choiceButtons.add(choice);

            row.add(name, BorderLayout.CENTER);
            row.add(choice, BorderLayout.EAST);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowsPanel.add(row);
            rowsPanel.add(Box.createVerticalStrut(5));
            refreshButton(i);
        }

        rowsPanel.revalidate();
        rowsPanel.repaint();
        pack();
        setMinimumSize(new Dimension(320, Math.min(500, 140 + players.size() * 48)));
    }

    private void toggle(int index) {
        if (!continuing.get(index)) {
            int selectedCount = 0;
            for (boolean selected : continuing) {
                if (selected) {
                    selectedCount++;
                }
            }
            int availableSlots = queue.getAvailableSlotsAfterGame(currentGamePlayers);
            if (selectedCount >= availableSlots) {
                JOptionPane.showMessageDialog(this,
                    "The waiting queue is full.\nAvailable slots: " + availableSlots,
                    "Waiting Queue Full", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        continuing.set(index, !continuing.get(index));
        refreshButton(index);
    }

    private void refreshButton(int index) {
        JButton button = choiceButtons.get(index);
        boolean selected = continuing.get(index);
        button.setText(selected ? "Yes" : "No");
        button.setBackground(selected ? Theme.ACCENT : new Color(220, 220, 220));
        button.setForeground(selected ? Color.WHITE : Color.BLACK);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(new Color(245, 245, 245));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7));
        header.setBackground(new Color(0, 140, 0));
        JLabel title = new JLabel("Who wants to continue?");
        title.setForeground(Color.WHITE);
        title.setFont(Theme.FONT_HEADING);
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        rowsPanel = new JPanel();
        rowsPanel.setBackground(new Color(245, 245, 245));
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        root.add(new JScrollPane(rowsPanel), BorderLayout.CENTER);

        JButton confirm = new JButton("Confirm");
        Theme.stylePrimaryButton(confirm);
        confirm.addActionListener(event -> {
            confirmed = true;
            dispose();
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(root.getBackground());
        footer.add(confirm);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        setMinimumSize(new Dimension(320, 240));
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean wantsToContinue(int index) {
        return continuing.get(index);
    }
}
