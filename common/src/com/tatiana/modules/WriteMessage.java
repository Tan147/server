package com.tatiana.modules;

import java.io.IOException;
import java.util.Scanner;


public class WriteMessage extends Thread {
    private SendAndReceive connectionHandler;
    private String username;
    private Scanner scanner;

    public WriteMessage(SendAndReceive sendAndReceive, String username, Scanner scanner) {
        this.connectionHandler = sendAndReceive;
        this.username = username;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        System.out.println("Введите имя");
        username = scanner.nextLine();

        while (true) {
            System.out.println("Введите текст сообщения");
            String text = scanner.nextLine();
            if (text.equals("exit")) break;
            Message message = new Message(username);
            message.setText(text);
            try {
                Message fromServer;
                try {
                    fromServer = connectionHandler.receiveFile();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(fromServer.getText());
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }
}

