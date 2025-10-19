package me.coding;

import java.io.*;
import java.net.*;
import java.util.function.BiConsumer;

public class Client {
    private Socket         sock;                        // TCP socket    
    private PrintWriter    out;                         // Output stream
    private BufferedReader in;                          // Input stream
    private String         id;                          // ID associated to chat client
    private BiConsumer<String, String> messageHandler;  // Consumers

    public void connect(String host, int port, BiConsumer<String, String> onMessage) throws IOException {

        /**
         * 
         * Connect remote server and get output and input stream.
         * Any I/O error will cause the method throw an exeception
         * 
         */

        sock = new Socket(host, port);
        out  = new PrintWriter(sock.getOutputStream(), true);
        in   = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        this.messageHandler = onMessage;

        new Thread(this::listen).start();
    }

    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {

                String[] parts = line.split(";", 3);
                if (parts[0].equals("#HELLO")) {
                    id = parts[1].trim();
                    messageHandler.accept("system", "connected as " + id);
                } else if (parts[0].equals("#MESSAGE")) {
                    messageHandler.accept(parts[1], parts[2]);
                }
            }
        } catch (IOException e) {
            messageHandler.accept("system", "connection has been lost");
        }
    }

    public void send(String text) {
        if (out != null) out.println(text);
    }

    public void disconnect() {
        try {
            if (sock != null) sock.close();
        } catch (IOException ignored) {}
    }

    public String getId() {
        return id;
    }
}
