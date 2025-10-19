package me.coding;


import java.io.IOException;
import java.net.ServerSocket;


public class Server {

    private static final int PORT = 9090;

    public static void main(String[] args) {

        // Generate broacaster instance
        Broadcaster broadcaster = new Broadcaster();

        // Create broacaster thread
        Thread broadcasterThread = new Thread(broadcaster, "-broadcasterThread-");
        broadcasterThread.start();

        try (ServerSocket ss = new ServerSocket(PORT)) {
            while (true)
                new Thread(new Handler(ss.accept(), broadcaster)).start();
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            broadcasterThread.interrupt();
            /* stop broacaster loop and exit */
        }
    }
}
