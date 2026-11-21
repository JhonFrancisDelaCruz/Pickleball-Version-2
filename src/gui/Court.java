package gui;

import java.util.List;
import model.Player;

public class Court extends javax.swing.JPanel {
    private Runnable onCompleteGame;

    public Court() {
        initComponents();
        clearPlayers();
    }
    public void setCourtNumber(int number) {courtTitle.setText("COURT " + number);}
    public void setOnCompleteGame(Runnable onCompleteGame) {this.onCompleteGame = onCompleteGame;}

    public void clearPlayers() {
        Player1.setText(" ");
        Player2.setText(" ");
        Player3.setText(" ");
        Player4.setText(" ");
    }
    
    public void setPlayers(List<Player> players) {
    clearPlayers();
    if (players.size() > 0) {Player1.setText(players.get(0).getId() + " - " + players.get(0).getName());}
    if (players.size() > 1) {Player2.setText(players.get(1).getId() + " - " + players.get(1).getName());}
    if (players.size() > 2) {Player3.setText(players.get(2).getId() + " - " + players.get(2).getName());}
    if (players.size() > 3) {Player4.setText(players.get(3).getId() + " - " + players.get(3).getName());}
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        completeGame = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        courtTitle = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        Player1 = new javax.swing.JLabel();
        sdf = new javax.swing.JPanel();
        Player2 = new javax.swing.JLabel();
        fg = new javax.swing.JPanel();
        Player3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        Player4 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(0, 0, 0));

        completeGame.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        completeGame.setText("Complete Game");
        completeGame.addActionListener(this::completeGameActionPerformed);

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        courtTitle.setBackground(new java.awt.Color(255, 255, 255));
        courtTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        courtTitle.setForeground(new java.awt.Color(255, 255, 255));
        courtTitle.setText("COURT");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(courtTitle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(courtTitle))
        );

        jPanel2.setBackground(new java.awt.Color(51, 51, 51));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        Player1.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        Player1.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Player1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Player1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sdf.setBackground(new java.awt.Color(51, 51, 51));
        sdf.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        sdf.setPreferredSize(new java.awt.Dimension(243, 32));

        Player2.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        Player2.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout sdfLayout = new javax.swing.GroupLayout(sdf);
        sdf.setLayout(sdfLayout);
        sdfLayout.setHorizontalGroup(
            sdfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sdfLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Player2, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sdfLayout.setVerticalGroup(
            sdfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sdfLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Player2, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                .addContainerGap())
        );

        fg.setBackground(new java.awt.Color(51, 51, 51));
        fg.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        fg.setPreferredSize(new java.awt.Dimension(243, 32));

        Player3.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        Player3.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout fgLayout = new javax.swing.GroupLayout(fg);
        fg.setLayout(fgLayout);
        fgLayout.setHorizontalGroup(
            fgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fgLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Player3, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fgLayout.setVerticalGroup(
            fgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fgLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Player3, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(51, 51, 51));
        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setPreferredSize(new java.awt.Dimension(4, 32));

        Player4.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        Player4.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Player4, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Player4, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(completeGame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sdf, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addComponent(fg, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sdf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(completeGame, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void completeGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_completeGameActionPerformed
        if (onCompleteGame != null) {
            onCompleteGame.run();
        }
    }//GEN-LAST:event_completeGameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Player1;
    private javax.swing.JLabel Player2;
    private javax.swing.JLabel Player3;
    private javax.swing.JLabel Player4;
    private javax.swing.JButton completeGame;
    private javax.swing.JLabel courtTitle;
    private javax.swing.JPanel fg;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel sdf;
    // End of variables declaration//GEN-END:variables
}
