package me.coding;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
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
        frame.setSize(450, 400);

        // --- Initialize UI ---
        chat = new JTextPane();
        chat.setEditable(false);
        chat.setBackground(Color.WHITE);
        chat.setFont(new Font("Verdana", Font.PLAIN, 14));
        doc = chat.getStyledDocument();

        scroll = new JScrollPane(chat);
        field  = new JTextField();
        send   = new JButton("Send");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(field, BorderLayout.CENTER);
        bottomPanel.add(send,   BorderLayout.EAST);

        frame.add(scroll,      BorderLayout.CENTER);
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
            // Normalize once for safety
            String sId = normalize(senderId);
            String uId = normalize(userId);

            // Is the message for us?
            boolean isSelf = sId.equalsIgnoreCase(uId);
            
            Color color    = isSelf ? new Color(0, 70, 180) : new Color(180, 0, 0);
            String prefix  = isSelf ? "[you]: " : "[" + sId + "]: ";

            appendColoredText(prefix + text + "\n", color);
        });
    }

    private String normalize(String str) {
        return str == null ? "" : str.replaceAll("[\\r\\n]+", "").trim();
    }

    private void appendSystemMessage(String msg) {
        appendColoredText("[system]: " + msg + "\n", Color.GRAY);
    }

    private void appendColoredText(String msg, Color color) {
        try {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs, color);
            doc.insertString(doc.getLength(), msg, attrs);
            chat.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            /* ignore any error here */
        }
    }
}
