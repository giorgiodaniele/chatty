package me.coding;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class Controller {
    private Client client;
    private String serverAddress = "localhost";
    private int    serverPort    = 9090;
    private UI     ui;

    public Controller() {
        client = new Client();
        ui     = new UI(this::onSendClicked);

        /**
         * 
         * Generate a new client and connect the server
         * 
         */

        try {
            client.connect(this.serverAddress, this.serverPort, this::onMessageReceived);
        } catch (IOException e) {
            ui.addMessage("system", "Failed to connect to server.", false);
        }
    }

    private void onSendClicked(ActionEvent e) {
        String text = ui.getInputText();
        if (text.isEmpty()) {
            return;
        }
        client.send(text);
        ui.clearInput();
    }

    private void onMessageReceived(String sender, String message) {
        boolean isSelf = sender.equalsIgnoreCase(client.getId());
        ui.addMessage("[" + sender + "]", message, isSelf);
    }

    public static void main(String[] args) {
        new Controller();
    }
}
