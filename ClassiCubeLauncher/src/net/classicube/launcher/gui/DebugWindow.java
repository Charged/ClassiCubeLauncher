package net.classicube.launcher.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class DebugWindow extends javax.swing.JFrame {

    static DebugWindow instance;

    public static void showWindow() {
        instance = new DebugWindow();
        instance.setVisible(true);
    }

    public static synchronized void writeLine(String str) {
        instance.printStream.println(str);
    }

    PrintStream printStream;

    private DebugWindow() {
        initComponents();
        TextAreaOutputStream outStream = new TextAreaOutputStream(tConsole);
        printStream = new PrintStream(outStream);
        System.setOut(printStream);
        System.setErr(printStream);
    }

    public static void setWindowTitle(final String newTitle) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                instance.setTitle("ClassiCube Log - " + newTitle);
            }
        });
    }

    private static class TextAreaOutputStream extends OutputStream {

        private final JTextArea textArea;
        private final StringBuilder sb = new StringBuilder();

        public TextAreaOutputStream(final JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        @Override
        public void write(int b) throws IOException {

            if (b == '\r') {
                return;
            }

            if (b == '\n') {
                final String text = sb.toString() + "\n";
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textArea.append(text);
                    }
                });
                sb.setLength(0);

                return;
            }

            sb.append((char) b);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        tConsole = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tConsole.setEditable(false);
        tConsole.setBackground(new java.awt.Color(0, 0, 0));
        tConsole.setColumns(80);
        tConsole.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        tConsole.setForeground(new java.awt.Color(204, 204, 204));
        tConsole.setLineWrap(true);
        tConsole.setRows(20);
        tConsole.setTabSize(4);
        tConsole.setWrapStyleWord(true);
        tConsole.setBorder(null);
        tConsole.setCaretColor(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(tConsole);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea tConsole;
    // End of variables declaration//GEN-END:variables
}
