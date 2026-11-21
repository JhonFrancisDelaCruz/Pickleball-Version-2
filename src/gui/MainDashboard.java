package gui;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.util.List;
import javax.swing.plaf.basic.BasicScrollBarUI;
import model.Player;
import service.QueueManager;

public class MainDashboard extends JFrame {
    private static final int INITIAL_COURTS = 3;
    private static final int INITIAL_MAX_WAITING = 15;
    private static final int WAITING_SLOTS_PER_COURT = 4;
    
    private int completedGamesCount = 0;
    private int totalWinnersCount = 0;
    
    private int courtCount = 0;
    private final java.util.List<Court> courts = new java.util.ArrayList<>();
    private final QueueManager queueManager = new QueueManager();
    private final Map<Court, List<Player>> activeGames = new HashMap<>();
    
   public MainDashboard() {
    setUndecorated(true); 
    initComponents();
    
    customizeScrollBar(scrollCourts);
    customizeScrollBar(scrollWaitingPlayers);
    customizeScrollBar(scrollDashboard);
    scrollDashboard.getVerticalScrollBar().setUnitIncrement(20);
    WaitingPlayers.setLineWrap(true);
    WaitingPlayers.setWrapStyleWord(true);

    pnlCourt.setLayout(new BoxLayout(pnlCourt, BoxLayout.X_AXIS));
    pnlCourt.setPreferredSize(null);

    scrollCourts.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollCourts.setPreferredSize(new Dimension(0, 308));
    scrollCourts.setMinimumSize(new Dimension(0, 308));
    scrollCourts.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

    jTextArea4.setEditable(false);
    jTextArea5.setEditable(false);
    jTextArea4.setForeground(Color.WHITE);
    jTextArea5.setForeground(Color.WHITE);
    jTextArea5.setLineWrap(true);
    jTextArea5.setWrapStyleWord(true);

    addCourt();
    addCourt();
    addCourt();

    refreshAll();
    log("System ready. 3 courts available.");

    setTitle("Pickleball Queue Management System");
    setSize(1210, 700);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);
}
   
   private int getMaxWaitingPlayers() {
    return INITIAL_MAX_WAITING+ ((courtCount - INITIAL_COURTS) * WAITING_SLOTS_PER_COURT);
}

   private void refreshCourtPanel() {
    pnlCourt.removeAll();
    courtCount = 0;
    for (Court court : courts) {
        courtCount++;
        court.setCourtNumber(courtCount);
        court.setOnCompleteGame(() -> completeGame(court));
        Dimension courtSize = new Dimension(300, 280);
        court.setPreferredSize(courtSize);
        court.setMinimumSize(courtSize);
        court.setMaximumSize(courtSize);

        court.setAlignmentY(Component.TOP_ALIGNMENT);
        if (courtCount > 1) {
            pnlCourt.add(Box.createHorizontalStrut(10));
        }
        pnlCourt.add(court);
    }

    pnlCourt.revalidate();
    pnlCourt.repaint();
    scrollCourts.revalidate();
    scrollCourts.repaint();
}
    
  public void refreshWaitingPlayers() {
    WaitingPlayers.setText(queueManager.getPlayerQueue().display());

    jTextArea4.setText("");
    for (Player player : queueManager.peekNextEligible(QueueManager.PLAYERS_PER_GAME)) {
        jTextArea4.append("  " + player.getName() + " - " + player.getId() + "\n");
    }

    updateStatistics();
}

  private void updateStatistics() {
    TotaWaitingPlayers.setText(String.valueOf(queueManager.getPlayerQueue().size()));
    ActiveCourt.setText(String.valueOf(activeGames.size()));
    NonActiveCourt.setText(String.valueOf(courts.size() - activeGames.size()));
    CompletedGames.setText(String.valueOf(completedGamesCount));
    TotalWinner.setText(String.valueOf(totalWinnersCount));
    jLabel10.setText("TOTAL COURTS: "+String.valueOf(courts.size()));
    WaitingLimit.setText(String.valueOf(getMaxWaitingPlayers()));
    
    
}

  private void refreshAll() {
    queueManager.setMaxWaitingPlayers(getMaxWaitingPlayers());
    refreshWaitingPlayers();
}

  private void log(String message) {
    String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
    jTextArea5.append("[" + time + "] " + message + "\n");
    jTextArea5.setCaretPosition(jTextArea5.getDocument().getLength());
  }

  private String names(List<Player> list) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(list.get(i).getName());
    }
    return sb.toString();
  }

  private Player findActivePlayer(String id) {
    for (List<Player> game : activeGames.values()) {
        for (Player p : game) {
            if (p.getId().equalsIgnoreCase(id)) return p;
        }
    }
    return null;
  }

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

    List<Player> winners = recordResult(court, team, gamePlayers);
    List<Player> noRoom = queueManager.completeGame(gamePlayers, continuing);

    logGameResult(courtNumber, team, winners, gamePlayers, continuing, noRoom);
    refreshAll();

    if (!noRoom.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "The waiting queue is full ("
            + queueManager.getMaxWaitingPlayers() + " players).\n"
            + "These players could not rejoin and were removed: "
            + names(noRoom));
    }
  }

  private char askWinningTeam(int courtNumber, List<Player> gamePlayers) {
    winner winnerDialog = new winner(this, true);
    winnerDialog.setGame(courtNumber, gamePlayers);
    winnerDialog.setLocationRelativeTo(this);
    winnerDialog.setVisible(true);
    return winnerDialog.getWinningTeam();
  }

  private List<Player> askWhoContinues(List<Player> gamePlayers) {
<<<<<<< HEAD
    ContinuingPlayer continueDialog = new ContinuingPlayer(this, true);

=======
    ContinuingPlayer continueDialog = new ContinuingPlayer(this,true,queueManager );
>>>>>>> 6a49074ee3a6bc2b71725774bd3ac655d9132bfd
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
<<<<<<< HEAD
=======

>>>>>>> 6a49074ee3a6bc2b71725774bd3ac655d9132bfd
  private List<Player> recordResult(Court court, char team, List<Player> gamePlayers) {
    List<Player> winners = new ArrayList<>(gamePlayers.subList(team == 'A' ? 0 : 2, team == 'A' ? 2 : 4));
    for (Player p : winners) p.addWin();
    totalWinnersCount += winners.size();
    completedGamesCount++;
    addWinnerRow(completedGamesCount, team, winners);
    activeGames.remove(court);
    court.clearPlayers();
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

  private Court findAvailableCourt() {
    for (Court court : courts) {
        if (!activeGames.containsKey(court)) {
            return court;
        }
    }
    return null;
  }

  /** Puts the 4 chosen players on the court and marks the court as busy. */
  private void assignPlayersToCourt(Court court, List<Player> gamePlayers) {
    court.setPlayers(gamePlayers);
    activeGames.put(court, gamePlayers);
    log("Court " + (courts.indexOf(court) + 1) + " - GAME STARTED: " + names(gamePlayers) + ".");
  }

  /** Shows the winning team of a finished game in the LIST OF WINNER table. */
  private void addWinnerRow(int gameNo, char team, List<Player> winners) {
    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) ListOfWinner.getModel();
    Player p1 = winners.get(0);
    Player p2 = winners.get(1);
    Object[] row = { gameNo, "Team " + team, p1.getId(), p1.getName(), p2.getId(), p2.getName() };

    int target = -1;
    for (int i = 0; i < model.getRowCount(); i++) {
        if (model.getValueAt(i, 0) == null) { target = i; break; }
    }
    if (target == -1) {
        model.addRow(row);
        target = model.getRowCount() - 1;
    } else {
        for (int c = 0; c < row.length; c++) model.setValueAt(row[c], target, c);
    }
    ListOfWinner.scrollRectToVisible(ListOfWinner.getCellRect(target, 0, true));
  }

    @SuppressWarnings("unchecked")
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
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        scrollDashboard.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel3.setBackground(new java.awt.Color(204, 204, 204));
        jPanel3.setPreferredSize(new java.awt.Dimension(972, 1022));

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

        getContentPane().add(scrollDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 0, 1000, 700));

        navbar.setBackground(new java.awt.Color(51, 51, 51));

        Register.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 12)); // NOI18N
        Register.setText("REGISTER");
        Register.addActionListener(this::RegisterActionPerformed);

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

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(scrollWaitingPlayers)
                    .addComponent(logout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel3)
                .addGap(6, 6, 6)
                .addComponent(scrollWaitingPlayers, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        getContentPane().add(navbar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 700));

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void RegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegisterActionPerformed
<<<<<<< HEAD
      if (queueManager.isQueueFull()) {
        JOptionPane.showMessageDialog(
            this,
            "Maximum registered players reached!\n"
            + "Total registered: "
            + queueManager.getTotalRegisteredPlayers()
            + "\nMaximum capacity: "
            + queueManager.getMaxWaitingPlayers(),
            "Registration Full",
            JOptionPane.WARNING_MESSAGE
        );
=======
       if (queueManager.isQueueFull()) {
        JOptionPane.showMessageDialog(this,
            "The waiting queue is full!\nMaximum capacity: "
            + queueManager.getMaxWaitingPlayers() + " players.",
            "Queue Full",
            JOptionPane.WARNING_MESSAGE);
>>>>>>> 6a49074ee3a6bc2b71725774bd3ac655d9132bfd
        return;
    }

    register dialog = new register(this, true, queueManager);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
<<<<<<< HEAD

    Player added = dialog.getRegisteredPlayer();

    if (added != null) {
        log("Registered " + added.getName()
            + " (" + added.getId() + ").");
    }

=======
    Player added = dialog.getRegisteredPlayer();

    if (added != null) {log("Registered " + added.getName() + " (" + added.getId() + ")."); }
    
>>>>>>> 6a49074ee3a6bc2b71725774bd3ac655d9132bfd
    refreshWaitingPlayers();
    }//GEN-LAST:event_RegisterActionPerformed

    private void StartGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartGameActionPerformed

    if (queueManager.getPlayerQueue().size() < QueueManager.PLAYERS_PER_GAME) {
        JOptionPane.showMessageDialog(this,"Not enough players! You need 4 players to start a game.");
        return;
    }
    if (queueManager.countEligible() < QueueManager.PLAYERS_PER_GAME) {
        JOptionPane.showMessageDialog(this,"Not enough eligible players! Some waiting players must skip this game selection.");
        return;}

    Court availableCourt = findAvailableCourt();
    if (availableCourt == null) {
        JOptionPane.showMessageDialog(this,"There are no available courts!");
        return;}
    assignPlayersToCourt(availableCourt, queueManager.selectPlayersForGame());
    refreshWaitingPlayers();

    JOptionPane.showMessageDialog(this,"Game started successfully!");
    
    }//GEN-LAST:event_StartGameActionPerformed

    private void PlayerIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlayerIdActionPerformed

    }//GEN-LAST:event_PlayerIdActionPerformed

    private void LeaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LeaveActionPerformed
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
            }
            PlayerId.setText("");
            return;
        }

        JOptionPane.showMessageDialog(this, "Player ID not found!");
    }//GEN-LAST:event_LeaveActionPerformed

    private void AddCourtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddCourtActionPerformed
        addCourt();
        log("Court " + courts.size() + " added.");
    }//GEN-LAST:event_AddCourtActionPerformed

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
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

        log("Last court deleted. " + courts.size() + " court(s) remaining.");
        int maxWaiting = getMaxWaitingPlayers();
        int currentWaiting = queueManager.getPlayerQueue().size();

        if (currentWaiting > maxWaiting) {
            JOptionPane.showMessageDialog(this,
                    "The waiting queue currently has " + currentWaiting
                    + " players, but the new limit is " + maxWaiting
                    + ". New registrations will be restricted until the queue decreases.");
        }
    }
    }//GEN-LAST:event_deleteActionPerformed

    private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
        "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
           new LoginPage().setVisible(true);
        this.dispose();
       
    }
    }//GEN-LAST:event_logoutActionPerformed

private void addCourt() {
    Court court = new Court();
    courts.add(court);

    refreshCourtPanel();
    refreshAll();
}

private void customizeScrollBar(JScrollPane scrollPane) {

    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();

    verticalBar.setPreferredSize(new Dimension(12, 0));
    verticalBar.setUI(new BasicScrollBarUI() {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(160, 160, 160);
            trackColor = new Color(220, 220, 220);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(220, 220, 220));
            g.fillRect(trackBounds.x, trackBounds.y,
                    trackBounds.width, trackBounds.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(new Color(160, 160, 160));

            g2.fillRoundRect(
                    thumbBounds.x + 2,
                    thumbBounds.y,
                    thumbBounds.width - 4,
                    thumbBounds.height,
                    10,
                    10
            );

            g2.dispose();
        }
    });
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