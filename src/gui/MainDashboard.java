package gui;

import java.awt.*;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.*;
import java.util.*;
import java.util.List;
import java.sql.SQLException;
import javax.swing.plaf.basic.BasicScrollBarUI;
import model.Player;
import service.QueueManager;
import service.SqlStore;

public class MainDashboard extends JFrame {
    private static final int INITIAL_COURTS = 3;
    private static final int INITIAL_MAX_WAITING = 15;
    private static final int WAITING_SLOTS_PER_COURT = 4;
    private static final int DEFAULT_MAX_COURTS = 10;
    
    private int completedGamesCount = 0;
    private int totalWinnersCount = 0;
    
    private int courtCount = 0;
    private final int userId;
    private final java.util.List<Court> courts = new java.util.ArrayList<>();
    private final QueueManager queueManager;
    private final Map<Court, List<Player>> activeGames = new HashMap<>();
    private final SqlStore store = new SqlStore();

    // Settings
    private int customWaitingLimit = INITIAL_MAX_WAITING;
    private boolean autoWaiting = true;   // default: auto based on courts
    private int maxCourts = DEFAULT_MAX_COURTS;
    private JButton btnSettings;
    private JButton btnExport;
    private JPanel responsiveDashboard;
    private JPanel statsPanel;
    private JPanel insightsPanel;
    private JPanel courtsSection;
    
   public MainDashboard(int userId) throws SQLException {
    this.userId = userId;
    SqlStore.DashboardData data = store.loadDashboardData(userId);
    queueManager = new QueueManager(data.lastPlayerNumber);
    customWaitingLimit = data.waitingLimit;
    autoWaiting = data.autoWaiting;
    maxCourts = Math.max(1, Math.min(50, data.maxCourts));
    Theme.setMode(data.themeMode);

    initComponents();

    configureResponsiveLayout();

    // btnSettings is created/wired inside initComponents (jPanel13 layout)
    if (btnSettings != null) {
        for (java.awt.event.ActionListener al : btnSettings.getActionListeners()) {
            btnSettings.removeActionListener(al);
        }
        btnSettings.addActionListener(e -> openSettings());
    }

    applyThemeToUI();

    scrollDashboard.getVerticalScrollBar().setUnitIncrement(16);
    WaitingPlayers.setLineWrap(true);
    WaitingPlayers.setWrapStyleWord(true);

    jTextArea4.setEditable(false);
    jTextArea5.setEditable(false);
    jTextArea5.setLineWrap(true);
    jTextArea5.setWrapStyleWord(true);

    int initialCourtCount = Math.max(1, Math.min(data.courtCount, maxCourts));
    for (int i = 0; i < initialCourtCount; i++) {
        courts.add(new Court());
    }
    restoreDashboardData(data);
    refreshCourtPanel();

    refreshAll();
    persistDashboardState();
    log("System ready. " + courts.size() + " court(s) available.");

    setTitle("Pickleball Queue Management System");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(760, 500));
    setPreferredSize(new Dimension(1100, 700));
    pack();
    setSize(1100, 700);
    setLocationRelativeTo(null);
    setResizable(true);
    // Allow true maximize / fullscreen via window controls
    setExtendedState(getExtendedState() & ~JFrame.MAXIMIZED_BOTH);
}

private void restoreDashboardData(SqlStore.DashboardData data) {
    Map<Integer, List<SqlStore.PlayerState>> playing = new TreeMap<>();
    for (SqlStore.PlayerState state : data.players) {
        queueManager.restoreRegisteredPlayer(state.player);
        if ("WAITING".equals(state.status)) {
            queueManager.restoreWaitingPlayer(state.player);
        } else if ("PLAYING".equals(state.status)) {
            queueManager.restorePlayingPlayer(state.player);
            if (state.courtNumber > 0) {
                playing.computeIfAbsent(state.courtNumber, key -> new ArrayList<>()).add(state);
            }
        }
        totalWinnersCount += state.player.getWins();
    }

    for (Map.Entry<Integer, List<SqlStore.PlayerState>> game : playing.entrySet()) {
        int courtIndex = game.getKey() - 1;
        if (courtIndex < 0 || courtIndex >= courts.size()) {
            continue;
        }
        game.getValue().sort(Comparator.comparingInt(state -> state.courtSlot));
        List<Player> players = new ArrayList<>();
        for (SqlStore.PlayerState state : game.getValue()) {
            players.add(state.player);
        }
        activeGames.put(courts.get(courtIndex), players);
        courts.get(courtIndex).setPlayers(players);
    }

    javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
        new Object[0][5],
        new String[]{"Game #", "Court", "Match", "Winning Team", "Winning Players"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    ListOfWinner.setModel(model);
    for (SqlStore.GameRecord game : data.games) {
        model.addRow(new Object[]{
            game.gameId, game.courtNumber, game.teamSize + " vs " + game.teamSize,
            "Team " + game.winningTeam, String.join(", ", game.winnerNames)
        });
    }
    completedGamesCount = data.games.size();
}

private void configureResponsiveLayout() {
    responsiveDashboard = new ResponsivePanel();
    responsiveDashboard.setLayout(new BoxLayout(responsiveDashboard, BoxLayout.Y_AXIS));
    responsiveDashboard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

    statsPanel = new JPanel(new GridLayout(0, 3, 10, 10));
    statsPanel.setOpaque(false);
    for (JPanel card : new JPanel[]{jPanel2, jPanel11, jPanel4, jPanel9, jPanel5, jPanel6}) {
        card.setPreferredSize(new Dimension(140, 54));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        statsPanel.add(card);
    }

    jPanel1.setLayout(new BorderLayout(8, 0));
    jPanel1.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    jPanel1.removeAll();
    jPanel1.add(jLabel10, BorderLayout.WEST);
    JPanel courtActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    courtActions.setOpaque(false);
    courtActions.add(AddCourt);
    courtActions.add(delete);
    jPanel1.add(courtActions, BorderLayout.EAST);
    jPanel1.setPreferredSize(new Dimension(0, 44));
    jPanel1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

    pnlCourt.setLayout(new GridLayout(0, 1, 8, 8));
    pnlCourt.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
    pnlCourt.setPreferredSize(null);
    pnlCourt.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    scrollCourts.setViewportView(null);
    scrollCourts.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollCourts.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

    courtsSection = new JPanel(new BorderLayout(0, 4));
    courtsSection.setOpaque(false);
    courtsSection.add(jPanel1, BorderLayout.NORTH);
    courtsSection.add(pnlCourt, BorderLayout.CENTER);

    jPanel7.setLayout(new BorderLayout(0, 4));
    jPanel7.removeAll();
    jPanel7.add(jLabel8, BorderLayout.NORTH);
    jPanel7.add(jScrollPane1, BorderLayout.CENTER);
    jPanel7.setMinimumSize(new Dimension(0, 170));
    jPanel7.setPreferredSize(new Dimension(270, 220));

    jPanel10.setLayout(new BorderLayout(0, 4));
    jPanel10.removeAll();
    jPanel10.add(jLabel9, BorderLayout.NORTH);
    jPanel10.add(jScrollPane5, BorderLayout.CENTER);
    jPanel10.setMinimumSize(new Dimension(0, 170));
    jPanel10.setPreferredSize(new Dimension(300, 220));

    insightsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
    insightsPanel.setOpaque(false);
    insightsPanel.add(jPanel7);
    insightsPanel.add(jPanel10);

    jPanel12.setLayout(new BorderLayout(0, 4));
    jPanel12.removeAll();
    jPanel12.add(jLabel12, BorderLayout.NORTH);
    jPanel12.add(jScrollPane3, BorderLayout.CENTER);
    jPanel12.setMinimumSize(new Dimension(0, 190));
    jPanel12.setPreferredSize(new Dimension(600, 260));
    ListOfWinner.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

    for (JComponent section : new JComponent[]{statsPanel, courtsSection, insightsPanel, jPanel12}) {
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    responsiveDashboard.add(statsPanel);
    responsiveDashboard.add(Box.createVerticalStrut(14));
    responsiveDashboard.add(courtsSection);
    responsiveDashboard.add(Box.createVerticalStrut(14));
    responsiveDashboard.add(insightsPanel);
    responsiveDashboard.add(Box.createVerticalStrut(14));
    responsiveDashboard.add(jPanel12);

    scrollDashboard.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollDashboard.setViewportView(responsiveDashboard);
    scrollDashboard.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent e) {
            updateResponsiveCourtColumns();
        }
    });

    JPanel actions = new JPanel();
    actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
    actions.setBorder(BorderFactory.createEmptyBorder(16, 12, 12, 12));
    actions.setOpaque(false);
    for (JComponent component : new JComponent[]{jLabel1, PlayerName, Register, jPanel8, jLabel2}) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(component);
        actions.add(Box.createVerticalStrut(7));
    }

    JPanel leaveControls = new JPanel(new BorderLayout(6, 0));
    leaveControls.setOpaque(false);
    leaveControls.add(PlayerId, BorderLayout.CENTER);
    leaveControls.add(Leave, BorderLayout.EAST);
    leaveControls.setAlignmentX(Component.LEFT_ALIGNMENT);
    actions.add(leaveControls);
    actions.add(Box.createVerticalStrut(12));

    jPanel8.setLayout(new BorderLayout());
    jPanel8.removeAll();
    jPanel8.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
    jPanel8.add(StartGame, BorderLayout.CENTER);

    jPanel13.setLayout(new BorderLayout(0, 6));
    jPanel13.removeAll();
    jPanel13.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
    btnExport = new JButton("Export / Print Report");
    btnExport.addActionListener(e -> openReportPreview());
    jPanel13.add(jLabel3, BorderLayout.NORTH);
    jPanel13.add(scrollWaitingPlayers, BorderLayout.CENTER);
    JPanel sidebarFooter = new JPanel(new GridLayout(0, 1, 0, 6));
    sidebarFooter.setOpaque(false);
    sidebarFooter.add(btnSettings);
    sidebarFooter.add(btnExport);
    sidebarFooter.add(logout);
    jPanel13.add(sidebarFooter, BorderLayout.SOUTH);

    navbar.setLayout(new BorderLayout(0, 8));
    navbar.removeAll();
    navbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
    navbar.add(actions, BorderLayout.NORTH);
    navbar.add(jPanel13, BorderLayout.CENTER);
    navbar.setPreferredSize(new Dimension(210, 0));
    navbar.setMinimumSize(new Dimension(180, 0));

    updateResponsiveCourtColumns();
}

private void updateResponsiveCourtColumns() {
    int viewportWidth = scrollDashboard.getViewport().getWidth();
    int availableWidth = Math.max(1, viewportWidth - 48);
    int minimumCourtWidth = 208;
    for (List<Player> game : activeGames.values()) {
        if (game.size() >= 6) {
            minimumCourtWidth = 280;
            break;
        }
    }
    int columns = Math.max(1, Math.min(Math.max(1, courts.size()),
        (availableWidth + 8) / (minimumCourtWidth + 8)));
    if (pnlCourt.getLayout() instanceof GridLayout) {
        GridLayout layout = (GridLayout) pnlCourt.getLayout();
        if (layout.getColumns() != columns) {
            layout.setColumns(columns);
            pnlCourt.revalidate();
            responsiveDashboard.revalidate();
        }
    }
}

private static final class ResponsivePanel extends JPanel implements Scrollable {
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(900, 600);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(16, visibleRect.height - 16);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}

private void openSettings() {
    SettingsDialog dlg = new SettingsDialog(this, customWaitingLimit, autoWaiting,
        Theme.getMode(), maxCourts, courts.size());
    dlg.setVisible(true);
    if (dlg.isSaved()) {
        int newWaitingLimit = dlg.getWaitingLimit();
        boolean newAutoWaiting = dlg.isAutoWaiting();
        int newMaxCourts = dlg.getMaxCourts();
        Theme.Mode newThemeMode = dlg.getThemeMode();
        try {
            store.saveSettings(userId, courts.size(), newMaxCourts, newWaitingLimit,
                newAutoWaiting, newThemeMode);
        } catch (SQLException exception) {
            showDatabaseError(exception);
            return;
        }
        customWaitingLimit = newWaitingLimit;
        autoWaiting = newAutoWaiting;
        maxCourts = newMaxCourts;
        Theme.setMode(newThemeMode);
        applyThemeToUI();
        // Re-apply theme to existing courts
        for (Court c : courts) c.applyTheme();
        refreshAll();
        log("Settings updated. Waiting limit: " +
            (autoWaiting ? "Auto (" + getMaxWaitingPlayers() + ")" : customWaitingLimit));
        log("Maximum courts: " + maxCourts + ".");
    }
}

private void persistDashboardState() {
    try {
        store.saveSettings(userId, courts.size(), maxCourts, customWaitingLimit,
            autoWaiting, Theme.getMode());
        store.saveDashboardState(userId, queueManager.getAllPlayers(),
            queueManager.getWaitingPlayers(), activeGamesByCourtNumber());
    } catch (SQLException exception) {
        showDatabaseError(exception);
    }
}

private Map<Integer, List<Player>> activeGamesByCourtNumber() {
    Map<Integer, List<Player>> games = new HashMap<>();
    for (Map.Entry<Court, List<Player>> entry : activeGames.entrySet()) {
        games.put(courts.indexOf(entry.getKey()) + 1, entry.getValue());
    }
    return games;
}

private void showDatabaseError(SQLException exception) {
    JOptionPane.showMessageDialog(this,
        "The database operation failed:\n" + exception.getMessage()
            + "\n\nChanges may not be saved.",
        "Database Error", JOptionPane.ERROR_MESSAGE);
}

private void openReportPreview() {
    final String report;
    try {
        report = store.createPlayerReport(userId);
    } catch (SQLException exception) {
        showDatabaseError(exception);
        return;
    }

    JDialog preview = new JDialog(this, "Player Report Preview", true);
    preview.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    JTextArea paperText = new JTextArea(report);
    paperText.setEditable(false);
    paperText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    paperText.setBackground(Color.WHITE);
    paperText.setForeground(Color.BLACK);
    paperText.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));
    paperText.setCaretPosition(0);

    JPanel paper = new JPanel(new BorderLayout());
    paper.setBackground(Color.WHITE);
    paper.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190)));
    paper.add(paperText, BorderLayout.CENTER);

    JScrollPane pageView = new JScrollPane(paper);
    pageView.getViewport().setBackground(new Color(225, 225, 225));
    pageView.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

    JButton save = new JButton("Save as TXT");
    JButton print = new JButton("Print...");
    JButton close = new JButton("Close");
    Theme.styleSecondaryButton(save);
    Theme.stylePrimaryButton(print);
    Theme.styleSecondaryButton(close);
    save.addActionListener(e -> saveReport(preview, report));
    print.addActionListener(e -> {
        try {
            paperText.print(
                new java.text.MessageFormat("Pickleball Player Report"),
                new java.text.MessageFormat("Page {0}"),
                false, null, null, true);
        } catch (PrinterException exception) {
            JOptionPane.showMessageDialog(preview,
                "The report could not be printed:\n" + exception.getMessage(),
                "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    });
    close.addActionListener(e -> preview.dispose());

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
    buttons.add(save);
    buttons.add(print);
    buttons.add(close);

    preview.add(pageView, BorderLayout.CENTER);
    preview.add(buttons, BorderLayout.SOUTH);
    preview.setSize(820, 650);
    preview.setMinimumSize(new Dimension(560, 420));
    preview.setLocationRelativeTo(this);
    preview.setVisible(true);
}

private void saveReport(Component parent, String report) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Save Player Report");
    chooser.setSelectedFile(new java.io.File("pickleball-player-report.txt"));
    if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
        return;
    }
    try {
        Files.write(chooser.getSelectedFile().toPath(), report.getBytes(StandardCharsets.UTF_8));
        JOptionPane.showMessageDialog(parent, "Report saved successfully.",
            "Export Complete", JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException exception) {
        JOptionPane.showMessageDialog(parent,
            "The report could not be saved:\n" + exception.getMessage(),
            "Export Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void applyThemeToUI() {
    getContentPane().setBackground(Theme.BG);
    if (jPanel3 != null) jPanel3.setBackground(Theme.BG);
    if (responsiveDashboard != null) responsiveDashboard.setBackground(Theme.BG);
    if (statsPanel != null) statsPanel.setBackground(Theme.BG);
    if (insightsPanel != null) insightsPanel.setBackground(Theme.BG);
    if (courtsSection != null) courtsSection.setBackground(Theme.BG);
    if (navbar != null) navbar.setBackground(Theme.PANEL);
    if (pnlCourt != null) pnlCourt.setBackground(Theme.BG);

    // Sidebar-ish panels
    if (jPanel1 != null) jPanel1.setBackground(Theme.SIDEBAR);
    if (jPanel8 != null) jPanel8.setBackground(Theme.SIDEBAR);
    if (jPanel13 != null) jPanel13.setBackground(Theme.SIDEBAR);

    // Cards / sections
    if (jPanel9 != null) { jPanel9.setBackground(Theme.CARD); }
    if (jPanel10 != null) { jPanel10.setBackground(Theme.PANEL); }
    if (jPanel11 != null) { jPanel11.setBackground(Theme.CARD); }
    if (jPanel12 != null) { jPanel12.setBackground(Theme.CARD); }

    // Stat cards – soft tints
    Color[] tints = Theme.getMode() == Theme.Mode.DARK
        ? new Color[]{ new Color(40,70,50), new Color(55,55,65), new Color(40,60,80),
                       new Color(55,55,60), new Color(70,65,40), new Color(70,65,35) }
        : new Color[]{ new Color(220,240,220), new Color(235,235,235), new Color(210,230,250),
                       new Color(230,230,230), new Color(250,240,210), new Color(250,235,190) };
    JPanel[] cards = { jPanel2, jPanel4, jPanel5, jPanel6, jPanel7 };
    for (int i = 0; i < cards.length; i++) {
        if (cards[i] != null) {
            cards[i].setBackground(tints[i]);
            cards[i].setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        }
    }

    // Labels
    for (JLabel lbl : new JLabel[]{ TotaWaitingPlayers, ActiveCourt, NonActiveCourt,
                                    CompletedGames, TotalWinner, WaitingLimit }) {
        if (lbl != null) { lbl.setForeground(Theme.TEXT); lbl.setFont(Theme.FONT_STAT); }
    }
    for (JLabel lbl : new JLabel[]{ jLabel1, jLabel2, jLabel3, jLabel4, jLabel5, jLabel11 }) {
        if (lbl != null) { lbl.setForeground(Theme.TEXT_SEC); lbl.setFont(Theme.FONT_SMALL); }
    }
    for (JLabel lbl : new JLabel[]{ jLabel6, jLabel7, jLabel8, jLabel9, jLabel10 }) {
        if (lbl != null) { lbl.setForeground(Theme.TEXT); lbl.setFont(Theme.FONT_HEADING); }
    }
    if (jLabel12 != null) { jLabel12.setForeground(Theme.TEXT_SEC); }
    if (jLabel13 != null) { jLabel13.setForeground(Theme.TEXT_SEC); }

    // Inputs
    if (PlayerName != null) Theme.styleTextField(PlayerName);
    if (PlayerId != null) Theme.styleTextField(PlayerId);

    // Buttons
    Theme.stylePrimaryButton(Register);
    Theme.stylePrimaryButton(StartGame);
    Theme.styleSecondaryButton(AddCourt);
    Theme.styleDangerButton(delete);
    Theme.styleDangerButton(Leave);
    Theme.styleSecondaryButton(logout);
    if (btnSettings != null) Theme.styleSecondaryButton(btnSettings);
    if (btnExport != null) Theme.styleSecondaryButton(btnExport);

    // Text areas
    if (WaitingPlayers != null) Theme.styleTextArea(WaitingPlayers);
    if (jTextArea4 != null) Theme.styleTextArea(jTextArea4);
    if (jTextArea5 != null) Theme.styleTextArea(jTextArea5);

    // Scroll panes
    if (scrollCourts != null) Theme.styleScrollPane(scrollCourts);
    if (scrollWaitingPlayers != null) Theme.styleScrollPane(scrollWaitingPlayers);
    if (scrollDashboard != null) Theme.styleScrollPane(scrollDashboard);
    if (jScrollPane1 != null) Theme.styleScrollPane(jScrollPane1);
    if (jScrollPane3 != null) Theme.styleScrollPane(jScrollPane3);
    if (jScrollPane5 != null) Theme.styleScrollPane(jScrollPane5);

    // Table
    if (ListOfWinner != null) {
        ListOfWinner.setBackground(Theme.INPUT);
        ListOfWinner.setForeground(Theme.TEXT);
        ListOfWinner.setGridColor(Theme.BORDER);
        ListOfWinner.setSelectionBackground(Theme.ACCENT);
        ListOfWinner.setSelectionForeground(Color.WHITE);
        ListOfWinner.setFont(Theme.FONT_BODY);
        if (ListOfWinner.getTableHeader() != null) {
            ListOfWinner.getTableHeader().setBackground(Theme.PANEL);
            ListOfWinner.getTableHeader().setForeground(Theme.TEXT);
            ListOfWinner.getTableHeader().setFont(Theme.FONT_BUTTON);
        }
    }

    revalidate();
    repaint();
}

// Waiting limit: auto (base + 4 per extra court) or custom fixed value
private int getMaxWaitingPlayers() {
    if (autoWaiting) {
        return customWaitingLimit + Math.max(0, (courtCount - INITIAL_COURTS) * WAITING_SLOTS_PER_COURT);
    }
    return customWaitingLimit;
}

   // Redraws all courts, renumbers them and re-links their buttons.
   private void refreshCourtPanel() {
    pnlCourt.removeAll();
    courtCount = 0;
    for (Court court : courts) {
        courtCount++;
        court.setCourtNumber(courtCount);
        court.setOnCompleteGame(() -> completeGame(court));
        int teamSize = activeGames.containsKey(court) ? activeGames.get(court).size() / 2 : 2;
        int courtHeight = Math.max(200, 142 + teamSize * 48);
        court.setPreferredSize(new Dimension(200, courtHeight));
        court.setMinimumSize(new Dimension(0, 190));
        court.setMaximumSize(new Dimension(Integer.MAX_VALUE, courtHeight));
        court.applyTheme();

        court.setAlignmentY(Component.TOP_ALIGNMENT);
        pnlCourt.add(court);
    }

    updateResponsiveCourtColumns();
    pnlCourt.revalidate();
    pnlCourt.repaint();
    scrollCourts.revalidate();
    scrollCourts.repaint();
}
    
  // Updates the waiting list, "next 4" box and statistics.
  public void refreshWaitingPlayers() {
    WaitingPlayers.setText(queueManager.displayWaitingPlayers());

    jTextArea4.setText("");
    for (Player player : queueManager.peekNextGamePlayers()) {
        jTextArea4.append("  " + player.getName() + " - " + player.getId() + "\n");
    }

    updateStatistics();
}

  // Updates the numbers at the top (waiting, courts, games, winners).
  private void updateStatistics() {
    TotaWaitingPlayers.setText(String.valueOf(queueManager.getWaitingPlayers().size()));
    ActiveCourt.setText(String.valueOf(activeGames.size()));
    NonActiveCourt.setText(String.valueOf(courts.size() - activeGames.size()));
    CompletedGames.setText(String.valueOf(completedGamesCount));
    TotalWinner.setText(String.valueOf(totalWinnersCount));
    jLabel10.setText("TOTAL COURTS: "+String.valueOf(courts.size()));
    WaitingLimit.setText(String.valueOf(getMaxWaitingPlayers()));

}

  // Updates the waiting limit and refreshes the screen.
  private void refreshAll() {
    queueManager.setMaxWaitingPlayers(getMaxWaitingPlayers());
    refreshWaitingPlayers();
}

  // Adds a timestamped line to the activity log.
  private void log(String message) {
    String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
    jTextArea5.append("[" + time + "] " + message + "\n");
    jTextArea5.setCaretPosition(jTextArea5.getDocument().getLength());
  }

  // Joins player names into one text like "A, B, C".
  private String names(List<Player> list) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(list.get(i).getName());
    }
    return sb.toString();
  }

  // Finds a player who is currently playing by ID.
  private Player findActivePlayer(String id) {
    for (List<Player> game : activeGames.values()) {
        for (Player p : game) {
            if (p.getId().equalsIgnoreCase(id)) return p;
        }
    }
    return null;
  }

  // Finishes a game: asks winner and who continues, then updates everything.
  private void completeGame(Court court) {
    List<Player> gamePlayers = activeGames.get(court);
    if (gamePlayers == null) {
        JOptionPane.showMessageDialog(this, "There is no game in progress on this court.");
        return;
    }
    int courtNumber = courts.indexOf(court) + 1;

    char team = askWinningTeam(courtNumber, gamePlayers);
    if (team == 0) {return;}
    List<Player> continuing = askWhoContinues(gamePlayers);
    if (continuing == null) { return;}

    try {
        store.saveCompletedGame(userId, courtNumber, team, gamePlayers);
    } catch (SQLException exception) {
        showDatabaseError(exception);
        return;
    }
    List<Player> winners = recordResult(court, courtNumber, team, gamePlayers);
    List<Player> noRoom = queueManager.completeGame(gamePlayers, continuing);

    logGameResult(courtNumber, team, winners, gamePlayers, continuing, noRoom);
    refreshAll();
    persistDashboardState();

    if (!noRoom.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "The waiting queue is full ("
            + queueManager.getMaxWaitingPlayers() + " players).\n"
            + "These players could not rejoin and were removed: "
            + names(noRoom));
    }
  }

  // Opens the winner popup and returns the chosen team.
  private char askWinningTeam(int courtNumber, List<Player> gamePlayers) {
    winner winnerDialog = new winner(this, true);
    winnerDialog.setGame(courtNumber, gamePlayers);
    winnerDialog.setLocationRelativeTo(this);
    winnerDialog.setVisible(true);
    return winnerDialog.getWinningTeam();
  }

  // Opens the continue popup and returns the players who stay.
  private List<Player> askWhoContinues(List<Player> gamePlayers) {
    ContinuingPlayer continueDialog = new ContinuingPlayer(this,true,queueManager );
    continueDialog.setPlayers(gamePlayers);
    continueDialog.setLocationRelativeTo(this);
    continueDialog.setVisible(true);

    if (!continueDialog.isConfirmed()) {
        return null;
    }

    List<Player> continuing = new ArrayList<>();

    for (int i = 0; i < gamePlayers.size(); i++) {
        if (continueDialog.wantsToContinue(i)) {
            continuing.add(gamePlayers.get(i));
        }
    }

    return continuing;
}

  // Saves the winners, counts the game and frees the court.
  private List<Player> recordResult(Court court, int courtNumber, char team, List<Player> gamePlayers) {
    int teamSize = gamePlayers.size() / 2;
    List<Player> winners = new ArrayList<>(gamePlayers.subList(
        team == 'A' ? 0 : teamSize, team == 'A' ? teamSize : gamePlayers.size()));
    totalWinnersCount += winners.size();
    completedGamesCount++;
    addWinnerRow(completedGamesCount, courtNumber, teamSize, team, winners);
    activeGames.remove(court);
    court.clearPlayers();
    updateResponsiveCourtColumns();
    return winners;
  }

  private void logGameResult(int courtNumber, char team, List<Player> winners,
                             List<Player> gamePlayers, List<Player> continuing, List<Player> noRoom) {
    log("Court " + courtNumber + " - GAME COMPLETED. Team " + team + " won (" + names(winners) + ").");
    for (Player p : gamePlayers) {
        if (noRoom.contains(p)) {
            log(p.getName() + " (" + p.getId() + ") wanted to continue but the queue is full - removed.");
        } else if (continuing.contains(p) && !p.isLeaveAfterGame()) {
            log(p.getName() + " (" + p.getId() + ") returned to the end of the queue"
                + (p.isEligible() ? "." : " (must skip the next game selection)."));
        } else {
            log(p.getName() + " (" + p.getId() + ") left the system.");
        }
    }
  }

  // Returns the first court with no game, or null.
  private Court findAvailableCourt() {
    for (Court court : courts) {
        if (!activeGames.containsKey(court)) {
            return court;
        }
    }
    return null;
  }

  // Puts both teams on the court and marks it as busy.
  private void assignPlayersToCourt(Court court, List<Player> teamA, List<Player> teamB) {
    List<Player> gamePlayers = new ArrayList<>(teamA);
    gamePlayers.addAll(teamB);
    court.setPlayers(teamA, teamB);
    activeGames.put(court, gamePlayers);
    updateResponsiveCourtColumns();
    log("Court " + (courts.indexOf(court) + 1) + " - GAME STARTED: "
        + names(teamA) + " vs " + names(teamB) + ".");
  }

  // Shows the winning team of a finished game in the LIST OF WINNER table.
  private void addWinnerRow(int gameNo, int courtNumber, int teamSize, char team, List<Player> winners) {
    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) ListOfWinner.getModel();
    Object[] row = {gameNo, courtNumber, teamSize + " vs " + teamSize,
        "Team " + team, names(winners)};
    model.addRow(row);
    int target = model.getRowCount() - 1;
    ListOfWinner.scrollRectToVisible(ListOfWinner.getCellRect(target, 0, true));
  }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollDashboard = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        WaitingLimit = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        ActiveCourt = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        CompletedGames = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        TotalWinner = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea4 = new javax.swing.JTextArea();
        jLabel8 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextArea5 = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        scrollCourts = new javax.swing.JScrollPane();
        pnlCourt = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        AddCourt = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        delete = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        NonActiveCourt = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        TotaWaitingPlayers = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        ListOfWinner = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        navbar = new javax.swing.JPanel();
        Register = new javax.swing.JButton();
        PlayerName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        StartGame = new javax.swing.JButton();
        PlayerId = new javax.swing.JTextField();
        Leave = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        scrollWaitingPlayers = new javax.swing.JScrollPane();
        WaitingPlayers = new javax.swing.JTextArea();
        logout = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        scrollDashboard.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel3.setBackground(new java.awt.Color(204, 204, 204));
        jPanel3.setPreferredSize(null);

        jPanel2.setBackground(new java.awt.Color(0, 153, 153));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jPanel2.setPreferredSize(new java.awt.Dimension(140, 44));

        jLabel13.setText("WAITING LIMIT");

        WaitingLimit.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        WaitingLimit.setText("0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(WaitingLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 30, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel13)
                .addGap(0, 0, 0)
                .addComponent(WaitingLimit)
                .addGap(0, 4, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(153, 255, 153));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jPanel4.setPreferredSize(new java.awt.Dimension(140, 44));

        jLabel5.setText("ACTIVE COURT");

        ActiveCourt.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        ActiveCourt.setText("0");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(ActiveCourt, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 30, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel5)
                .addGap(0, 0, 0)
                .addComponent(ActiveCourt)
                .addGap(0, 4, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(153, 153, 153));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jPanel5.setPreferredSize(new java.awt.Dimension(140, 44));

        jLabel6.setText("COMPLETED GAMES");

        CompletedGames.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        CompletedGames.setText("0");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(CompletedGames, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 21, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel6)
                .addGap(0, 0, 0)
                .addComponent(CompletedGames)
                .addGap(0, 4, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(255, 204, 0));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jPanel6.setPreferredSize(new java.awt.Dimension(140, 44));

        jLabel7.setText("TOTAL WINNER");

        TotalWinner.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TotalWinner.setText("0");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(TotalWinner, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 30, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel7)
                .addGap(0, 0, 0)
                .addComponent(TotalWinner)
                .addGap(0, 4, Short.MAX_VALUE))
        );

        jPanel7.setBackground(new java.awt.Color(204, 204, 204));

        jTextArea4.setEditable(false);
        jTextArea4.setBackground(new java.awt.Color(102, 102, 102));
        jTextArea4.setColumns(20);
        jTextArea4.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea4.setRows(5);
        jScrollPane1.setViewportView(jTextArea4);

        jLabel8.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        jLabel8.setText("NEXT 4 ELIGIBLE");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
            .addComponent(jLabel8)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
        );

        jPanel10.setBackground(new java.awt.Color(204, 204, 204));

        jTextArea5.setEditable(false);
        jTextArea5.setBackground(new java.awt.Color(102, 102, 102));
        jTextArea5.setColumns(20);
        jTextArea5.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea5.setRows(5);
        jScrollPane5.setViewportView(jTextArea5);

        jLabel9.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        jLabel9.setText("ACTIVITY LOG");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
            .addComponent(jLabel9)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
        );

        scrollCourts.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        pnlCourt.setBackground(new java.awt.Color(204, 204, 204));
        pnlCourt.setPreferredSize(new java.awt.Dimension(231, 258));
        scrollCourts.setViewportView(pnlCourt);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        AddCourt.setBackground(new java.awt.Color(0, 204, 51));
        AddCourt.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        AddCourt.setForeground(new java.awt.Color(255, 255, 255));
        AddCourt.setText("+ Add Court");
        AddCourt.addActionListener(this::AddCourtActionPerformed);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("TOTAL COURTS : ");

        delete.setBackground(new java.awt.Color(204, 51, 0));
        delete.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        delete.setForeground(new java.awt.Color(255, 255, 255));
        delete.setText("Delete Court");
        delete.addActionListener(this::deleteActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel10)
                .addGap(0, 537, Short.MAX_VALUE)
                .addComponent(AddCourt, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 1, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel10)
                    .addComponent(AddCourt, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 1, Short.MAX_VALUE))
        );

        jPanel9.setBackground(new java.awt.Color(255, 102, 102));
        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jPanel9.setPreferredSize(new java.awt.Dimension(140, 44));

        jLabel11.setText("NON ACTIVE COURT");

        NonActiveCourt.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        NonActiveCourt.setText("0");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(NonActiveCourt, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 21, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel11)
                .addGap(0, 0, 0)
                .addComponent(NonActiveCourt)
                .addGap(0, 4, Short.MAX_VALUE))
        );

        jPanel11.setBackground(new java.awt.Color(153, 153, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jPanel11.setPreferredSize(new java.awt.Dimension(140, 44));

        jLabel4.setText("WAITING PLAYERS");

        TotaWaitingPlayers.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TotaWaitingPlayers.setText("0");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(TotaWaitingPlayers, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 30, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel4)
                .addGap(0, 0, 0)
                .addComponent(TotaWaitingPlayers)
                .addGap(0, 4, Short.MAX_VALUE))
        );

        jPanel12.setBackground(new java.awt.Color(204, 204, 204));

        ListOfWinner.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "No", "TEAM", "PLAYER 1 ID", "PLAYER 1 NAME", "PLAYER 2 ID", "PLAYER 2 NAME"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            // Makes the winner table read-only.
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(ListOfWinner);
        if (ListOfWinner.getColumnModel().getColumnCount() > 0) {
            ListOfWinner.getColumnModel().getColumn(0).setPreferredWidth(40);
            ListOfWinner.getColumnModel().getColumn(1).setPreferredWidth(80);
            ListOfWinner.getColumnModel().getColumn(2).setPreferredWidth(120);
            ListOfWinner.getColumnModel().getColumn(3).setPreferredWidth(200);
            ListOfWinner.getColumnModel().getColumn(4).setPreferredWidth(120);
            ListOfWinner.getColumnModel().getColumn(5).setPreferredWidth(200);
        }

        jLabel12.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jLabel12.setText("LIST OF WINNER");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
            .addComponent(jLabel12)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(69, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollCourts, javax.swing.GroupLayout.PREFERRED_SIZE, 900, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(scrollCourts, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 59, Short.MAX_VALUE))
        );

        scrollDashboard.setViewportView(jPanel3);

        getContentPane().add(scrollDashboard, BorderLayout.CENTER);

        navbar.setBackground(new java.awt.Color(51, 51, 51));

        Register.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 12)); // NOI18N
        Register.setText("REGISTER");
        Register.addActionListener(this::RegisterActionPerformed);

        PlayerName.setToolTipText("Type the player's name");
        PlayerName.addActionListener(this::PlayerNameActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("REGISTER PLAYER");

        jPanel8.setBackground(new java.awt.Color(51, 51, 51));
        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        StartGame.setBackground(new java.awt.Color(51, 204, 0));
        StartGame.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        StartGame.setForeground(new java.awt.Color(255, 255, 255));
        StartGame.setText("START GAME");
        StartGame.addActionListener(this::StartGameActionPerformed);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(StartGame, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(StartGame, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        PlayerId.addActionListener(this::PlayerIdActionPerformed);

        Leave.setBackground(new java.awt.Color(102, 102, 102));
        Leave.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        Leave.setForeground(new java.awt.Color(255, 255, 255));
        Leave.setText("Leave");
        Leave.addActionListener(this::LeaveActionPerformed);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("LEAVE QUEUE");

        jPanel13.setBackground(new java.awt.Color(51, 51, 51));
        jPanel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setFont(new java.awt.Font("Franklin Gothic Demi", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("WAITING PLAYERS");

        WaitingPlayers.setEditable(false);
        WaitingPlayers.setBackground(new java.awt.Color(51, 51, 51));
        WaitingPlayers.setColumns(20);
        WaitingPlayers.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        WaitingPlayers.setForeground(new java.awt.Color(255, 255, 255));
        WaitingPlayers.setRows(5);
        WaitingPlayers.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        WaitingPlayers.setRequestFocusEnabled(false);
        scrollWaitingPlayers.setViewportView(WaitingPlayers);

        logout.setBackground(new java.awt.Color(102, 0, 0));
        logout.setFont(new java.awt.Font("Arial Black", 1, 12)); // NOI18N
        logout.setForeground(new java.awt.Color(255, 255, 255));
        logout.setText("Logout");
        logout.addActionListener(this::logoutActionPerformed);

        // Settings button created early so layout can include it
        if (btnSettings == null) {
            btnSettings = new JButton("Settings");
            btnSettings.addActionListener(e -> openSettings());
        }

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(scrollWaitingPlayers)
                    .addComponent(btnSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel3)
                .addGap(6, 6, 6)
                .addComponent(scrollWaitingPlayers, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(btnSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );

        javax.swing.GroupLayout navbarLayout = new javax.swing.GroupLayout(navbar);
        navbar.setLayout(navbarLayout);
        navbarLayout.setHorizontalGroup(
            navbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(navbarLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(navbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(PlayerName, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Register, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addGroup(navbarLayout.createSequentialGroup()
                        .addComponent(PlayerId, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(Leave, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        navbarLayout.setVerticalGroup(
            navbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navbarLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addComponent(PlayerName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(Register, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jLabel2)
                .addGap(4, 4, 4)
                .addGroup(navbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PlayerId, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Leave, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        navbar.setPreferredSize(new Dimension(200, 0));
        getContentPane().add(navbar, BorderLayout.WEST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void RegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegisterActionPerformed
    // Registers the typed name with an auto-made ID (P001, P002, ...)
    String name = PlayerName.getText().trim();
    if (name.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter the player's name!");
        return;
    }
    Player added = queueManager.registerPlayer(name);
    if (added == null) {
        JOptionPane.showMessageDialog(this,
            "Maximum registered players reached!\n"
            + "Total registered (waiting + playing): " + queueManager.getTotalRegisteredPlayers()
            + "\nMaximum capacity: " + queueManager.getMaxWaitingPlayers(),
            "Registration Full",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    log("Registered " + added.getName() + " (" + added.getId() + ").");
    PlayerName.setText("");
    PlayerName.requestFocusInWindow();
    refreshWaitingPlayers();
    persistDashboardState();
    }//GEN-LAST:event_RegisterActionPerformed

    private void PlayerNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlayerNameActionPerformed
        // Pressing Enter in the name box registers the player
        RegisterActionPerformed(evt);
    }//GEN-LAST:event_PlayerNameActionPerformed

    private void StartGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartGameActionPerformed
    List<Player> waitingPlayers = queueManager.getWaitingPlayers();
    if (waitingPlayers.size() < 2) {
        JOptionPane.showMessageDialog(this, "At least two waiting players are needed to start a match.");
        return;
    }

    Court availableCourt = findAvailableCourt();
    if (availableCourt == null) {
        JOptionPane.showMessageDialog(this, "There are no available courts!");
        return;
    }

    MatchSetupDialog setup = new MatchSetupDialog(this, waitingPlayers);
    setup.setVisible(true);
    if (!setup.isConfirmed()) {
        return;
    }

    List<Player> teamA = setup.getTeamAPlayers();
    List<Player> teamB = setup.getTeamBPlayers();
    List<Player> selectedPlayers = setup.getSelectedPlayers();
    if (!queueManager.selectPlayersForGame(selectedPlayers, setup.getTeamSize())) {
        JOptionPane.showMessageDialog(this,
            "The selected players are no longer available. Please choose again.",
            "Match Setup", JOptionPane.WARNING_MESSAGE);
        return;
    }

    assignPlayersToCourt(availableCourt, teamA, teamB);
    refreshWaitingPlayers();
    persistDashboardState();

    log("Started " + setup.getTeamSize() + " vs " + setup.getTeamSize()
        + " on Court " + (courts.indexOf(availableCourt) + 1) + ".");
    
    }//GEN-LAST:event_StartGameActionPerformed

    private void PlayerIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlayerIdActionPerformed
        // Leave ID field Enter key (not used).

    }//GEN-LAST:event_PlayerIdActionPerformed

    private void LeaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LeaveActionPerformed
        // Leave button: removes a waiting player, or marks a playing one to leave after the game.
        String id = PlayerId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Player ID of the player who wants to leave.");
            return;
        }

        Player removed = queueManager.leaveQueue(id);
        if (removed != null) {
            log(removed.getName() + " (" + removed.getId() + ") left the queue.");
            PlayerId.setText("");
            refreshWaitingPlayers();
            persistDashboardState();
            return;
        }

        Player playing = findActivePlayer(id);
        if (playing != null) {
            if (playing.isLeaveAfterGame()) {
                JOptionPane.showMessageDialog(this, playing.getName() + " is already set to leave after the game.");
            } else {
                playing.setLeaveAfterGame(true);
                log(playing.getName() + " (" + playing.getId() + ") is playing and will leave after the game.");
                JOptionPane.showMessageDialog(this, playing.getName() + " is currently playing and will leave after the game.");
                persistDashboardState();
            }
            PlayerId.setText("");
            return;
        }

        JOptionPane.showMessageDialog(this, "Player ID not found!");
    }//GEN-LAST:event_LeaveActionPerformed

    private void AddCourtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddCourtActionPerformed
        // Stops adding courts at the configured maximum.
        if (courts.size() >= maxCourts) {
            JOptionPane.showMessageDialog(this, "Maximum of " + maxCourts + " courts reached.",
                    "Add Court", JOptionPane.WARNING_MESSAGE);
            return;
        }
        addCourt();
        log("Court " + courts.size() + " added.");
        persistDashboardState();
    }//GEN-LAST:event_AddCourtActionPerformed

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
        // Delete Court button: removes the last court if it is free.
      if (courts.size() <= 1) {
        JOptionPane.showMessageDialog(this, "You must keep at least one court.",
                "Delete Court",JOptionPane.WARNING_MESSAGE);
        return;
    }

    Court lastCourt = courts.get(courts.size() - 1);

    if (activeGames.containsKey(lastCourt)) { JOptionPane.showMessageDialog(this,
                "The last court has a game in progress. Complete the game first!",
                "Delete Court",
                JOptionPane.WARNING_MESSAGE);
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete the last court?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        courts.remove(lastCourt);

        refreshCourtPanel();
        refreshAll();
        persistDashboardState();

        log("Last court deleted. " + courts.size() + " court(s) remaining.");
        int maxWaiting = getMaxWaitingPlayers();
        int currentWaiting = queueManager.getTotalRegisteredPlayers();

        if (currentWaiting > maxWaiting) {
            JOptionPane.showMessageDialog(this,
                    "There are currently " + currentWaiting
                    + " registered players (waiting + playing), but the new limit is " + maxWaiting
                    + ". New registrations will be restricted until the queue decreases.");
        }
    }
    }//GEN-LAST:event_deleteActionPerformed

    private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
        // Logout button: asks to confirm then returns to the login page.
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
        "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
           new LoginPage().setVisible(true);
        this.dispose();
       
    }
    }//GEN-LAST:event_logoutActionPerformed

// Adds one new court and refreshes the screen.
private void addCourt() {
    Court court = new Court();
    courts.add(court);

    refreshCourtPanel();
    refreshAll();
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ActiveCourt;
    private javax.swing.JButton AddCourt;
    private javax.swing.JLabel CompletedGames;
    private javax.swing.JButton Leave;
    private javax.swing.JTable ListOfWinner;
    private javax.swing.JLabel NonActiveCourt;
    private javax.swing.JTextField PlayerId;
    private javax.swing.JButton Register;
    private javax.swing.JTextField PlayerName;
    private javax.swing.JButton StartGame;
    private javax.swing.JLabel TotaWaitingPlayers;
    private javax.swing.JLabel TotalWinner;
    private javax.swing.JLabel WaitingLimit;
    private javax.swing.JTextArea WaitingPlayers;
    private javax.swing.JButton delete;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JTextArea jTextArea5;
    private javax.swing.JButton logout;
    private javax.swing.JPanel navbar;
    private javax.swing.JPanel pnlCourt;
    private javax.swing.JScrollPane scrollCourts;
    private javax.swing.JScrollPane scrollDashboard;
    private javax.swing.JScrollPane scrollWaitingPlayers;
    // End of variables declaration//GEN-END:variables
}