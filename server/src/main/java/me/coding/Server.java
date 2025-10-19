package me.coding;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class);
    private static String       serverPort;
    private static Listener     listener;
    private static Thread       listenerThread;

    public static void main(String[] args) {

        LOGGER.info("Loading configuration");

        /**
         * 
         * Load application properties
         * 
         */
        Properties props = new Properties();
        try (InputStream in = Server.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                LOGGER.error("Can't find config.properties");
                return;
            }
            props.load(in);
        } catch (IOException e) {
            LOGGER.error(e);
            return;
        }

        LOGGER.info("Configuration loaded");

        /**
         * 
         * 
         * Start broadcaster runtime
         * 
         * 
         */
        LOGGER.info("Starting broacaster");

        listener       = new Listener();
        listenerThread = new Thread(listener);
        listenerThread.start();

        LOGGER.info("Broadcaster loaded");

        /**
         * 
         * 
         * Start server socket listener
         * 
         * 
         */
        serverPort = props.getProperty("server.port");
        if (serverPort == null) {
            LOGGER.error("Can't find port in config.properties");
            return;
        }

        try (ServerSocket ss = new ServerSocket(Integer.parseInt(serverPort))) {
            LOGGER.info("Server is running on port " + serverPort);
            while (true) {
                try {
                    Socket cs = ss.accept();
                    String id = "thread-" + cs.getInetAddress().toString() + "-" + cs.getPort(); 
                    new Thread(new Handler(cs, listener), id).start();
                    LOGGER.info("Client connected: " + cs.getInetAddress());
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        } finally {
            LOGGER.info("Stopping broacaster");
            listenerThread.interrupt();
            LOGGER.info("Broadcaster stopped");
            /* stop broacaster loop and exit */
        }
        LOGGER.info("Service stopped");
    }
}
