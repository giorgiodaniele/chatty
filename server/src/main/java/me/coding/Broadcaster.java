package me.coding;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class Broadcaster implements Runnable {

    private final List<User>            users = new CopyOnWriteArrayList<>();
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public void addMessage(String message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void removeUser(User user) {
        // TODO
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String message = queue.take();
                for (User user : this.users) {
                    try {
                        PrintWriter  out = new PrintWriter(user.getSocket().getOutputStream(), true); 
                        out.println(message); 
                    } catch (Exception e) {
                        /* ignore any error here */
                    }
                }
                Thread.sleep(50); 
            } catch (Exception e) {
                /* ignore any error here */
            }
        }
    }
}
