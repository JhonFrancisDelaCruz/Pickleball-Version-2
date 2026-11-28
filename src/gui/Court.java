package gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import model.Player;

public class Court extends JPanel {
    private Runnable onCompleteGame;
    private JLabel courtTitle;
    private JLabel lblTeamA, lblTeamB;
    private JButton completeGame;
    private JPanel headerPanel, contentPanel;
    private JPanel teamAPlayersPanel, teamBPlayersPanel;
    private JPanel teamASection, teamBSection;
    private List<Player> currentTeamA = List.of();
    private List<Player> currentTeamB = List.of();

    public Court() {
        initComponents();
        applyTheme();
        clearPlayers();
    }

    public void applyTheme() {
        setBackground(Theme.CARD);
        setBorder(new LineBorder(Theme.BORDER, 1));

        headerPanel.setBackground(Theme.HEADER);
        courtTitle.setForeground(Color.WHITE);
        courtTitle.setFont(Theme.FONT_HEADING);

        contentPanel.setBackground(Theme.CARD);

        lblTeamA.setForeground(Theme.ACCENT);
        lblTeamB.setForeground(Theme.ACCENT);
        lblTeamA.setFont(Theme.FONT_SMALL);
        lblTeamB.setFont(Theme.FONT_SMALL);

        stylePlayerRows(teamAPlayersPanel);
        stylePlayerRows(teamBPlayersPanel);

        Theme.stylePrimaryButton(completeGame);
        completeGame.setText("Complete");
    }

    public void setCourtNumber(int number) {
        courtTitle.setText("Court " + number);
    }

    public void setOnCompleteGame(Runnable onCompleteGame) {
        this.onCompleteGame = onCompleteGame;
    }

    public void clearPlayers() {
        setPlayers(List.of(), List.of());
    }

    public void setPlayers(List<Player> players) {
        int teamSize = players.size() / 2;
        setPlayers(players.subList(0, teamSize), players.subList(teamSize, players.size()));
    }

    public void setPlayers(List<Player> teamA, List<Player> teamB) {
        currentTeamA = new java.util.ArrayList<>(teamA);
        currentTeamB = new java.util.ArrayList<>(teamB);
        lblTeamA.setText("Team A (" + currentTeamA.size() + ")");
        lblTeamB.setText("Team B (" + currentTeamB.size() + ")");
        renderPlayers(teamAPlayersPanel, currentTeamA);
        renderPlayers(teamBPlayersPanel, currentTeamB);
        int rows = Math.max(teamA.size(), teamB.size());
        int height = Math.max(200, 142 + rows * 48);
        setPreferredSize(new Dimension(200, height));
        revalidate();
    }

    private void renderPlayers(JPanel panel, List<Player> players) {
        panel.removeAll();
        if (players.isEmpty()) {
            JLabel empty = new JLabel("—");
            empty.setForeground(Theme.TEXT_SEC);
            panel.add(empty);
        } else {
            int rowWidth = Math.max(80, (contentPanel.getWidth() - 40) / 2);
            for (Player player : players) {
                JLabel row = new JLabel(wrapPlayerText(player, rowWidth));
                row.setForeground(Theme.TEXT);
                row.setBackground(Theme.INPUT);
                row.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER, 1),
                    new EmptyBorder(4, 6, 4, 6)));
                row.setOpaque(true);
                row.setFont(Theme.FONT_BODY);
                panel.add(row);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private String wrapPlayerText(Player player, int width) {
        String text = player.getId() + " - " + player.getName();
        FontMetrics metrics = getFontMetrics(Theme.FONT_BODY);
        StringBuilder html = new StringBuilder("<html>");
        while (!text.isEmpty()) {
            int end = text.length();
            while (end > 1 && metrics.stringWidth(text.substring(0, end)) > width) {
                end--;
            }
            if (end < text.length()) {
                int space = text.lastIndexOf(' ', end);
                if (space > 0) {
                    end = space;
                }
            }
            String line = text.substring(0, end).trim();
            if (line.isEmpty()) {
                line = text.substring(0, 1);
                end = 1;
            }
            if (html.length() > 6) {
                html.append("<br>");
            }
            html.append(escapeHtml(line));
            text = text.substring(end).trim();
        }
        return html.append("</html>").toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private void stylePlayerRows(JPanel panel) {
        panel.setBackground(Theme.CARD);
        for (Component component : panel.getComponents()) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                label.setForeground(label.getText().equals("—") ? Theme.TEXT_SEC : Theme.TEXT);
                label.setBackground(Theme.INPUT);
                label.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER, 1),
                    new EmptyBorder(4, 6, 4, 6)));
                label.setOpaque(true);
                label.setFont(Theme.FONT_BODY);
            }
        }
    }

    private void initComponents() {
        completeGame = new JButton("Complete");
        headerPanel = new JPanel();
        courtTitle = new JLabel("Court");
        contentPanel = new JPanel();
        lblTeamA = new JLabel("Team A");
        lblTeamB = new JLabel("Team B");
        teamAPlayersPanel = new JPanel();
        teamBPlayersPanel = new JPanel();

        setLayout(new BorderLayout());

        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        headerPanel.add(courtTitle);
        add(headerPanel, BorderLayout.NORTH);

        contentPanel.setLayout(new GridLayout(1, 2, 8, 0));
        contentPanel.setBorder(new EmptyBorder(6, 8, 4, 8));
        teamAPlayersPanel.setLayout(new GridLayout(0, 1, 0, 3));
        teamBPlayersPanel.setLayout(new GridLayout(0, 1, 0, 3));
        teamAPlayersPanel.setOpaque(false);
        teamBPlayersPanel.setOpaque(false);
        teamASection = createTeamSection(lblTeamA, teamAPlayersPanel);
        teamBSection = createTeamSection(lblTeamB, teamBPlayersPanel);
        contentPanel.add(teamASection);
        contentPanel.add(teamBSection);
        contentPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                renderPlayers(teamAPlayersPanel, currentTeamA);
                renderPlayers(teamBPlayersPanel, currentTeamB);
            }
        });
        add(contentPanel, BorderLayout.CENTER);

        completeGame.addActionListener(e -> {
            if (onCompleteGame != null) onCompleteGame.run();
        });
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        btnPanel.setOpaque(false);
        btnPanel.add(completeGame);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createTeamSection(JLabel title, JPanel players) {
        JPanel section = new JPanel(new BorderLayout(0, 3));
        section.setOpaque(false);
        section.add(title, BorderLayout.NORTH);
        section.add(players, BorderLayout.CENTER);
        return section;
    }
}
