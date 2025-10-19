package me.coding;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class Server {

    private static final Logger    LOGGER = Logger.getLogger(Server.class);
    private static String          serverPort;
    private static Listener        listener;
    private static ExecutorService pool = Executors.newFixedThreadPool(20); 

    public static void main(String[] args) {

        LOGGER.info("Loading configuration");

        Properties props = new Properties();
        try (InputStream in = Server.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                LOGGER.error("Can't find config.properties");
                return;
            }
            props.load(in);
            LOGGER.info("Server configuration has been loaded");
        } catch (IOException e) {
            LOGGER.error(e);
            return;
        }
        
        // Run the listener
        listener = new Listener();
        pool.submit(listener);

        // Set the port
        serverPort = props.getProperty("server.port");

        // Start server
        try (ServerSocket ss = new ServerSocket(Integer.parseInt(props.getProperty("server.port")))) {
            LOGGER.info("Server is running on port " + serverPort);
            while (true) {
                try {
                    Socket cs = ss.accept();
                    pool.submit(new Handler(cs, listener));
                    LOGGER.info("Client connected: " + cs.getInetAddress());
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        } finally { }
    }
}
