package me.coding;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Client {

    private JTextPane      chat;
    private JTextField     field;
    private JScrollPane    scroll;
    private JButton        send;
    private PrintWriter    out;
    private BufferedReader in;
    private StyledDocument doc;
    private String         userId;
    private Socket         socket;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client().startClient());
    }

    private void startClient() {
        JFrame frame = new JFrame("chatter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 480);
        frame.setLocationRelativeTo(null);

        /**
         * 
         * Initialize client user interface
         * 
         */
        frame.getContentPane().setBackground(new Color(240, 243, 247));

        chat = new JTextPane();
        chat.setEditable(false);
        chat.setBackground(Color.WHITE);
        chat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chat.setBorder(new EmptyBorder(10, 10, 10, 10));
        doc = chat.getStyledDocument();

        scroll = new JScrollPane(chat);
        scroll.setBorder(new LineBorder(new Color(200, 200, 200)));
        scroll.setBackground(Color.WHITE);

        field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(new CompoundBorder(
            new LineBorder(new Color(180, 180, 180)),
            new EmptyBorder(5, 8, 5, 8)
        ));

        send = new JButton("Send");
        send.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        send.setBackground(new Color(0, 122, 255));
        send.setForeground(Color.WHITE);
        send.setFocusPainted(false);
        send.setBorder(new EmptyBorder(8, 15, 8, 15));
        send.setCursor(new Cursor(Cursor.HAND_CURSOR));

        send.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                send.setBackground(new Color(10, 132, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                send.setBackground(new Color(0, 122, 255));
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 0));
        bottomPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        bottomPanel.setBackground(frame.getContentPane().getBackground());
        bottomPanel.add(field, BorderLayout.CENTER);
        bottomPanel.add(send, BorderLayout.EAST);

        JLabel header = new JLabel(" chatter client", JLabel.LEFT);
        header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        header.setOpaque(true);
        header.setBackground(new Color(0, 122, 255));
        header.setForeground(Color.WHITE);
        header.setBorder(new EmptyBorder(10, 12, 10, 12));

        frame.add(header, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        /**
         * 
         * Connect to server
         * 
         */
        try {
            socket = new Socket("localhost", 9090);
            out    = new PrintWriter(socket.getOutputStream(), true);
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Listen for server messages
            Thread listener = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String[] parts = message.split(";", 3);
                        switch (parts[0]) {
                            case "#HELLO":
                                this.userId = normalize(parts[1]);
                                appendSystemMessage("Connected as " + userId);
                                break;

                            case "#MESSAGE":
                                addMessage(parts[1], parts[2]);
                                break;

                            default:
                                break;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            /* ignore any error here */
                        }
                    }
                } catch (IOException e) {
                    appendSystemMessage("Connection lost.");
                } finally {
                    appendSystemMessage("Disconnected from server.");
                    /* exit client loop and close resources */
                    cleanup();
                }
            });

            listener.setDaemon(true);
            listener.start();

        } catch (IOException e) {
            appendSystemMessage("Failed to connect to server.");
        }

        send.addActionListener(this::sendMessage);
        field.addActionListener(this::sendMessage);
    }

    private void sendMessage(ActionEvent e) {
        String text = field.getText().trim();
        if (text.isEmpty()) return;

        out.println(text);
        field.setText("");
    }

    private void addMessage(String senderId, String text) {
        SwingUtilities.invokeLater(() -> {
            String sId     = normalize(senderId);
            String uId     = normalize(userId);
            boolean isSelf = sId.equalsIgnoreCase(uId);

            Color color = isSelf ? new Color(0, 122, 255) : new Color(255, 45, 85);
            String prefix = isSelf ? "You: " : sId + ": ";
            appendColoredText(prefix + text + "\n", color);
        });
    }

    private String normalize(String str) {
        return str == null ? "" : str.replaceAll("[\\r\\n]+", "").trim();
    }

    private void appendSystemMessage(String msg) {
        appendColoredText("[system] " + msg + "\n", new Color(100, 100, 100));
    }

    private void appendColoredText(String msg, Color color) {
        try {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs, color);
            StyleConstants.setFontSize(attrs, 14);
            doc.insertString(doc.getLength(), msg, attrs);
            chat.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            /* ignore any error here */
        }
    }

    /**
     * 
     * Gracefully close client socket and resources
     * 
     */
    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            /* ignore any cleanup error */
        }
        /* stop client loop and exit */
    }
}
