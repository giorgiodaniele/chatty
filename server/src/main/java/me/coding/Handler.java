package me.coding;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class Handler implements Runnable {

    private final Broadcaster cast;
    private final User        user;

    private static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public Handler(Socket socket, Broadcaster cast) {
        this.cast = cast;
        this.user = new User(Handler.generateId(), socket);
        this.cast.addUser(this.user);
    }

    @Override
    public void run() {
        Socket s = this.user.getSocket();

        try (
            /**
             * Get input and output stream associated with client socket
             */
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter   out = new PrintWriter(s.getOutputStream(), true)
        ) {
            /**
             * 
             * Send the to user an id, which is going to be used as identifier
             * by client when chatting
             * 
             */
            out.println("#HELLO;" + this.user.getUserId());

            /**
             * 
             * Listen for incoming message from client and add them to queue
             * 
             */
            String message;
            while ((message = in.readLine()) != null) {
                if (message.trim().isEmpty()) {
                    continue;
                }
                cast.addMessage("#MESSAGE;" + this.user.getUserId() + ";" + message);
            }

        } catch (Exception e) { /* ignore any errore here */}
    }
}
