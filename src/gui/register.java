
package gui;

import javax.swing.JOptionPane;
import model.Player;
import service.QueueManager;
import queue.PlayerQueue;
import java.util.*;

public class register extends javax.swing.JDialog {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(register.class.getName());
    private QueueManager queueManager;
    private Player registeredPlayer = null;

    public Player getRegisteredPlayer() {
        return registeredPlayer;
    }
    
    public register(java.awt.Frame parent, boolean modal,QueueManager queueManager) {
        super(parent, modal);
        this.queueManager = queueManager;
        initComponents();
    }
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        PlayerID = new javax.swing.JTextField();
        PlayerName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        Confirm = new javax.swing.JButton();
        Cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));

        jPanel2.setBackground(new java.awt.Color(51, 51, 51));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("REGISTER NEW PLAYER");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(jLabel1)
                .addContainerGap(121, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PlayerID.addActionListener(this::PlayerIDActionPerformed);

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Name: ");

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Player ID:");

        Confirm.setBackground(new java.awt.Color(0, 153, 0));
        Confirm.setForeground(new java.awt.Color(255, 255, 255));
        Confirm.setText("Confirm");
        Confirm.addActionListener(this::ConfirmActionPerformed);

        Cancel.setBackground(new java.awt.Color(153, 153, 153));
        Cancel.setText("Cancel");
        Cancel.addActionListener(this::CancelActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(9, 9, 9)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3)))
                        .addComponent(PlayerID, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(Confirm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(Cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(PlayerName, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PlayerID, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PlayerName, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Confirm)
                    .addComponent(Cancel))
                .addGap(0, 49, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 310, 270));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void PlayerIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlayerIDActionPerformed
    
    }//GEN-LAST:event_PlayerIDActionPerformed

    private void ConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConfirmActionPerformed
    String name = PlayerName.getText().trim();
    String id = PlayerID.getText().trim();
    
    if (name.isEmpty() || id.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in all fields!");
        return;
    }
    
    Player player = new Player(id, name);

    QueueManager.RegisterResult result = queueManager.registerPlayer(player);
    if (result == QueueManager.RegisterResult.DUPLICATE) {
<<<<<<< HEAD
        JOptionPane.showMessageDialog(this, "Player ID already exists!");
        return;
    }
    if (result == QueueManager.RegisterResult.QUEUE_FULL) {
    JOptionPane.showMessageDialog(this,
        "The waiting queue is full! (Maximum of "
        + queueManager.getMaxWaitingPlayers()
        + " waiting players)");
    return;
}
=======
        JOptionPane.showMessageDialog(this, "Player ID already exists!"); return;
    }
>>>>>>> 6a49074ee3a6bc2b71725774bd3ac655d9132bfd
    registeredPlayer = player;

    dispose();
    }//GEN-LAST:event_ConfirmActionPerformed

    private void CancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelActionPerformed
         dispose();
    }//GEN-LAST:event_CancelActionPerformed

    public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
            QueueManager manager = new QueueManager();
                register dialog = new register(new javax.swing.JFrame(),true,manager);

            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    });
}
    
    public void playersInfo() {
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Cancel;
    private javax.swing.JButton Confirm;
    private javax.swing.JTextField PlayerID;
    private javax.swing.JTextField PlayerName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables
}
