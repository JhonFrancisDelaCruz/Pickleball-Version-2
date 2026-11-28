package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import model.Player;

public class winner extends JDialog {
    private char winningTeam;
    private final JLabel courtHeading = new JLabel("COURT");
    private final JLabel teamAHeading = new JLabel("TEAM A");
    private final JLabel teamBHeading = new JLabel("TEAM B");
    private final JPanel teamAPlayers = new JPanel(new GridLayout(0, 1, 0, 4));
    private final JPanel teamBPlayers = new JPanel(new GridLayout(0, 1, 0, 4));

    public winner(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Select Winner");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(new Color(245, 245, 245));
        root.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7));
        header.setBackground(new Color(0, 140, 0));
        courtHeading.setForeground(Color.WHITE);
        courtHeading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.add(courtHeading);
        root.add(header, BorderLayout.NORTH);

        JPanel teams = new JPanel(new GridLayout(0, 1, 0, 8));
        teams.setBackground(root.getBackground());
        teams.setBorder(new EmptyBorder(8, 14, 4, 14));
        teams.add(createTeamPanel(teamAHeading, teamAPlayers));
        teams.add(createTeamPanel(teamBHeading, teamBPlayers));

        JButton teamAWins = new JButton("Team A Wins");
        JButton teamBWins = new JButton("Team B Wins");
        Theme.stylePrimaryButton(teamAWins);
        Theme.stylePrimaryButton(teamBWins);
        teamAWins.addActionListener(event -> {
            winningTeam = 'A';
            dispose();
        });
        teamBWins.addActionListener(event -> {
            winningTeam = 'B';
            dispose();
        });
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setBackground(root.getBackground());
        buttons.setBorder(new EmptyBorder(0, 14, 0, 14));
        buttons.add(teamAWins);
        buttons.add(teamBWins);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(root.getBackground());
        center.add(teams, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
        setMinimumSize(new Dimension(320, 300));
        pack();
        setLocationRelativeTo(parent);
    }

    private static JPanel createTeamPanel(JLabel heading, JPanel players) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new javax.swing.border.CompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            new EmptyBorder(8, 10, 8, 10)));
        heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(heading, BorderLayout.NORTH);
        panel.add(players, BorderLayout.CENTER);
        return panel;
    }

    public void setGame(int courtNumber, List<Player> players) {
        int teamSize = players.size() / 2;
        courtHeading.setText("COURT " + courtNumber + " - " + teamSize + " vs " + teamSize);
        teamAHeading.setText("TEAM A");
        teamBHeading.setText("TEAM B");
        renderPlayers(teamAPlayers, players.subList(0, teamSize));
        renderPlayers(teamBPlayers, players.subList(teamSize, players.size()));
        pack();
    }

    private static void renderPlayers(JPanel panel, List<Player> players) {
        panel.removeAll();
        for (Player player : players) {
            JLabel label = new JLabel(player.getId() + " - " + player.getName());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(label);
        }
        panel.revalidate();
        panel.repaint();
    }

    public char getWinningTeam() {
        return winningTeam;
    }
}
