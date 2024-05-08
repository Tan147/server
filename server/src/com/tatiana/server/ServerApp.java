package com.tatiana.server;

import com.tatiana.server.Server;

public class ServerApp {
    public static void main(String[] args) {
        new Server(2222).startServer();
    }
}
