package com.chargedminers.launcher.gui;

import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.border.EmptyBorder;

public class ConfirmScreen extends javax.swing.JDialog {

    public static boolean show(final String title, final String message) {
        ConfirmScreen screen = new ConfirmScreen(title, message);
        screen.setVisible(true);
        return screen.isConfirmed;
    }
    private boolean isConfirmed;

    private ConfirmScreen(final String title, final String message) {
        // set title, add border
        super((Frame) null, title, true);

        // set background
        final ImagePanel bgPanel = new ImagePanel(null, true);
        bgPanel.setGradient(true);
        bgPanel.setImage(Resources.getClassiCubeBackground());
        bgPanel.setGradientColor(Resources.colorGradient);
        bgPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        setContentPane(bgPanel);

        initComponents();

        // fill in exception info (if available)
        this.lMessage.setText("<html><b>" + message);

        // focus & highlight [Close]
        getRootPane().setDefaultButton(bYes);

        // Show GridBagLayout who's boss.
        this.imgErrorIcon.setImage(Resources.getWarningIcon());
        this.imgErrorIcon.setMinimumSize(new Dimension(64, 64));
        this.imgErrorIcon.setPreferredSize(new Dimension(64, 64));
        this.imgErrorIcon.setSize(new Dimension(64, 64));

        // Set windows icon, size, and location
        this.setIconImages(Resources.getWindowIcons());
        this.setPreferredSize(new Dimension(400, 130));
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        imgErrorIcon = new com.chargedminers.launcher.gui.ImagePanel();
        lMessage = new javax.swing.JLabel();
        bYes = new com.chargedminers.launcher.gui.JNiceLookingButton();
        bNo = new com.chargedminers.launcher.gui.JNiceLookingButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setType(java.awt.Window.Type.UTILITY);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        imgErrorIcon.setMaximumSize(new java.awt.Dimension(64, 64));
        imgErrorIcon.setMinimumSize(new java.awt.Dimension(64, 64));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        getContentPane().add(imgErrorIcon, gridBagConstraints);

        lMessage.setForeground(new java.awt.Color(255, 255, 255));
        lMessage.setText("Someone set up us the bomb!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(lMessage, gridBagConstraints);

        bYes.setText("Yes");
        bYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bYesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        gridBagConstraints.weightx = 0.1;
        getContentPane().add(bYes, gridBagConstraints);

        bNo.setText("No");
        bNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bNoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        getContentPane().add(bNo, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNoActionPerformed
        this.dispose();
    }//GEN-LAST:event_bNoActionPerformed

    private void bYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bYesActionPerformed
        isConfirmed = true;
        this.dispose();
    }//GEN-LAST:event_bYesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.chargedminers.launcher.gui.JNiceLookingButton bNo;
    private com.chargedminers.launcher.gui.JNiceLookingButton bYes;
    private com.chargedminers.launcher.gui.ImagePanel imgErrorIcon;
    private javax.swing.JLabel lMessage;
    // End of variables declaration//GEN-END:variables
}
