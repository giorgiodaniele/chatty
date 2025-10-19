package me.coding;


import java.net.Socket;

public class User {
    private String userId;
    private Socket socket;

    public User(String userId, Socket socket) {
        this.userId = userId;
        this.socket = socket;
    }

    public String getUserId() {
        return this.userId;
    }

    public Socket getSocket() {
        return this.socket;
    }
}