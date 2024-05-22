package com.tatiana.client;

import com.tatiana.modules.Connection;
import com.tatiana.modules.FileMessage;
import com.tatiana.modules.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.CertStoreSpi;
import java.util.Scanner;

public class Client {

    private InetSocketAddress address;
    private String username;
    private Scanner scanner;
    private Connection connectionHandler;

    public Client(InetSocketAddress address){
        this.address = address;
        scanner = new Scanner(System.in);
    }

     public void requestFile() throws Exception{
        System.out.println("Введите название файла из списка файлов");
        String description = scanner.nextLine();
        FileMessage fileMessage = new FileMessage(description);
        try {
            connectionHandler.sendFileDescription(fileMessage);
        } catch (IOException e) {
            connectionHandler.close();
        }
    }

    public void uploadFile() throws Exception{
            System.out.println("Чтобы загрузить файл, введите путь к файлу");
            String filepath = scanner.nextLine();
            System.out.println("Введите описание файла");
            String description = scanner.nextLine();
            System.out.println("Введите размер содержимого файла в мегабайтах");
            int size = scanner.nextInt();
            FileMessage fileMessage = new FileMessage(description, size);
            try {
                connectionHandler.sendFileDescription(fileMessage);
            } catch (IOException e) {
               connectionHandler.close();
            }
        }

    private void createConnection() throws IOException{
        connectionHandler = new Connection(new Socket(address.getHostName(),
                address.getPort()));
    }

    private class Writer extends Thread {
        public void run(){
            while (true){
                String text = scanner.nextLine();
                if (text.equalsIgnoreCase("загрузить файл"))
                    try {
                        uploadFile();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                System.out.println("Введите текст сообщения");
                text = scanner.nextLine();
                try {
                    if (text.equalsIgnoreCase("exit")) {
                        System.out.println("Соединение завершено");
                        connectionHandler.close();
                        break;
                    }
                } catch (Exception e){
                    throw new RuntimeException(e);
                }

                Message message = new Message(username);
                message.setText(text);
                try {
                    connectionHandler.send(message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private class Reader extends Thread {
        public void run(){
            while (true) {
                Message message = null;
                try {
                    if (message.getText().equalsIgnoreCase("exit")) {
                        connectionHandler.close();
                        break;
                    }
                } catch (IOException ignored) {
                    try {
                        connectionHandler.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println(message.getText());
            }
        }
    }

    public void startClient(){
        System.out.println("Введите имя");
        username = scanner.nextLine();
        try {
            createConnection();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        new Writer().start();
        new Reader().start();
        try(Connection connectionHandler =
            new Connection(new Socket(address.getHostName(),
                    address.getPort()))) {
        } catch (Exception e){
            while (true) {
                System.out.println("Введите текст сообщения");
                String text = scanner.nextLine();
                if ("exit".equalsIgnoreCase(text)) {
                    break;
                }
                Message message = new Message(username);
                message.setText(text);
                try {
                    connectionHandler.send(message);
                    Message fromServer = connectionHandler.read();
                    System.out.println(fromServer.getText());
                } catch (IOException ex){
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
