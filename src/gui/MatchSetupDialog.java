package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Player;

public class MatchSetupDialog extends JDialog {
    private final JComboBox<String> teamSizePicker =
        new JComboBox<>(new String[]{"1 vs 1", "2 vs 2", "3 vs 3"});
    private final JList<Player> teamAList = new JList<>();
    private final JList<Player> teamBList = new JList<>();
    private final DefaultListModel<Player> teamAPlayers = new DefaultListModel<>();
    private final DefaultListModel<Player> teamBPlayers = new DefaultListModel<>();
    private final List<Player> availablePlayers = new ArrayList<>();
    private final JLabel teamACount = new JLabel();
    private final JLabel teamBCount = new JLabel();
    private final JLabel selectionMessage = new JLabel();
    private final JButton startButton = new JButton("Start Match");
    private boolean confirmed;
    private boolean updatingSelections;

    public MatchSetupDialog(Frame parent, List<Player> waitingPlayers) {
        super(parent, "Choose Match Teams", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        availablePlayers.addAll(waitingPlayers);
        teamAList.setModel(teamAPlayers);
        teamBList.setModel(teamBPlayers);
        refreshTeamList(teamAList, Collections.emptyList(), Collections.emptyList());
        refreshTeamList(teamBList, Collections.emptyList(), Collections.emptyList());
        configurePlayerList(teamAList);
        configurePlayerList(teamBList);
        teamSizePicker.setSelectedIndex(1);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 14, 16));

        JPanel matchOptions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        matchOptions.add(new JLabel("Players per team:"));
        matchOptions.add(teamSizePicker);
        matchOptions.add(new JLabel(
            "Ctrl-click to toggle; assigned players disappear from the other team."));
        root.add(matchOptions, BorderLayout.NORTH);

        JPanel teams = new JPanel(new java.awt.GridLayout(1, 2, 12, 0));
        teams.add(createTeamPanel("Team A", teamAList, teamACount));
        teams.add(createTeamPanel("Team B", teamBList, teamBCount));
        root.add(teams, BorderLayout.CENTER);

        JButton cancel = new JButton("Cancel");
        Theme.styleSecondaryButton(cancel);
        Theme.stylePrimaryButton(startButton);
        cancel.addActionListener(event -> dispose());
        startButton.addActionListener(event -> {
            confirmed = true;
            dispose();
        });

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.add(cancel);
        actionButtons.add(startButton);
        JPanel actions = new JPanel(new BorderLayout());
        selectionMessage.setFont(Theme.FONT_SMALL);
        actions.add(selectionMessage, BorderLayout.CENTER);
        actions.add(actionButtons, BorderLayout.EAST);
        root.add(actions, BorderLayout.SOUTH);

        teamSizePicker.addActionListener(event -> updateCounts());
        teamAList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateTeamChoices();
            }
        });
        teamBList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateTeamChoices();
            }
        });

        setContentPane(root);
        updateCounts();
        setSize(820, 520);
        setMinimumSize(new Dimension(640, 420));
        setLocationRelativeTo(parent);
    }

    private static void configurePlayerList(JList<Player> list) {
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(8);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> source, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    source, value, index, selected, focus);
                Player player = (Player) value;
                label.setText(player.getId() + " - " + player.getName()
                    + (player.isEligible() ? "" : " (skip next game)"));
                label.setBorder(new EmptyBorder(5, 6, 5, 6));
                return label;
            }
        });
    }

    private static JPanel createTeamPanel(String title, JList<Player> list, JLabel count) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(8, 8, 8, 8)));
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.FONT_HEADING);
        count.setFont(Theme.FONT_SMALL);

        JPanel header = new JPanel(new BorderLayout());
        header.add(heading, BorderLayout.WEST);
        header.add(count, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private void updateCounts() {
        int required = getTeamSize();
        teamACount.setText(teamAList.getSelectedValuesList().size() + " / " + required);
        teamBCount.setText(teamBList.getSelectedValuesList().size() + " / " + required);

        List<Player> selectedA = teamAList.getSelectedValuesList();
        List<Player> selectedB = teamBList.getSelectedValuesList();
        boolean overlap = selectedA.stream().anyMatch(selectedB::contains);
        if (overlap) {
            selectionMessage.setText("A player cannot be selected for both teams.");
        } else if (selectedA.size() != required || selectedB.size() != required) {
            selectionMessage.setText("Select exactly " + required + " player(s) on each team.");
        } else {
            selectionMessage.setText("Teams are ready.");
        }
        startButton.setEnabled(selectedA.size() == required
            && selectedB.size() == required && !overlap);
    }

    private void updateTeamChoices() {
        if (updatingSelections) {
            return;
        }

        List<Player> selectedA = teamAList.getSelectedValuesList();
        List<Player> selectedB = teamBList.getSelectedValuesList();
        updatingSelections = true;
        try {
            refreshTeamList(teamAList, selectedA, selectedB);
            refreshTeamList(teamBList, selectedB, selectedA);
        } finally {
            updatingSelections = false;
        }
        updateCounts();
    }

    private void refreshTeamList(JList<Player> list, List<Player> selected,
                                 List<Player> selectedForOtherTeam) {
        DefaultListModel<Player> model = (DefaultListModel<Player>) list.getModel();
        Set<Player> unavailable = new HashSet<>(selectedForOtherTeam);
        Set<Player> selectedPlayers = new HashSet<>(selected);
        model.clear();
        for (Player player : availablePlayers) {
            if (!unavailable.contains(player)) {
                model.addElement(player);
            }
        }

        int[] selectedIndices = new int[selectedPlayers.size()];
        int selectedCount = 0;
        for (int i = 0; i < model.size(); i++) {
            if (selectedPlayers.contains(model.get(i))) {
                selectedIndices[selectedCount++] = i;
            }
        }
        list.setSelectedIndices(Arrays.copyOf(selectedIndices, selectedCount));
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getTeamSize() {
        return teamSizePicker.getSelectedIndex() + 1;
    }

    public List<Player> getTeamAPlayers() {
        return new ArrayList<>(teamAList.getSelectedValuesList());
    }

    public List<Player> getTeamBPlayers() {
        return new ArrayList<>(teamBList.getSelectedValuesList());
    }

    public List<Player> getSelectedPlayers() {
        List<Player> players = getTeamAPlayers();
        players.addAll(getTeamBPlayers());
        return players;
    }
}
