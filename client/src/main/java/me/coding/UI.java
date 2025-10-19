package me.coding;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class UI {
    
    private JFrame         frame;
    private JPanel         panel;
    private JScrollPane    scroll;
    private JTextPane      chat;
    private JTextField     tbox;
    private JButton        send;
    private StyledDocument style;
    private JLabel         header;

    public UI(ActionListener onSend) {
        frame = new JFrame("chatty");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // header = new JLabel(" chatty", JLabel.LEFT);
        // header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        // header.setOpaque(true);
        // header.setBackground(new Color(0, 122, 255));
        // header.setForeground(Color.WHITE);
        // header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        chat = new JTextPane();
        chat.setEditable(false);
        chat.setFont(new Font("Roboto", Font.PLAIN, 14));
        chat.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        style = chat.getStyledDocument();

        scroll = new JScrollPane(chat);
        scroll.setBorder(null);

        tbox = new JTextField();
        tbox.setFont(new Font("Roboto", Font.PLAIN, 14));
        tbox.addActionListener(onSend);

        send = new JButton("Send");
        send.setFont(new Font("", Font.PLAIN, 14));
        send.setBackground(new Color(0, 122, 255));
        send.setForeground(Color.WHITE);
        send.setFocusPainted(false);
        send.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        send.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        send.addActionListener(onSend);

        panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setBackground(frame.getContentPane().getBackground());
        panel.add(tbox, BorderLayout.CENTER);
        panel.add(send, BorderLayout.EAST);

        // frame.add(header, BorderLayout.NORTH);
        frame.add(scroll,  BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public void addMessage(String sender, String message, boolean isSelf) {
        SwingUtilities.invokeLater(() -> {
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setForeground(attributes, isSelf ? new Color(0, 122, 255) : new Color(255, 45, 85));
            try {
                style.insertString(style.getLength(), isSelf ? "[you]" + ": " + message + "\n" : sender + ": " + message + "\n", attributes);
                chat.setCaretPosition(style.getLength());
            } catch (BadLocationException ignored) {}
        });
    }

    public String getInputText() {
        return tbox.getText().trim();
    }

    public void clearInput() {
        tbox.setText("");
    }

    public JFrame     getFrame()      { return frame; }
    public JTextPane  getChatPane()   { return chat; }
    public JTextField getTbox() { return tbox; }
    public JButton    getSend() { return send; }
}
