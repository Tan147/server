package com.tatiana.client;

import com.tatiana.modules.Connection;
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

    public Client(InetSocketAddress address) {
        this.address = address;
        scanner = new Scanner(System.in);
    }

    private void createConnection() throws IOException {
        connectionHandler = new Connection(new Socket(address.getHostName(),
                address.getPort()));
    }

    private class Writer extends Thread {
        public void run(){
            while (true){
                System.out.println("Введите текст сообщения");
                String text = scanner.nextLine();
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

    private class Reader extends Thread{
        public void run() {
            while (true) {
                Message message = null;
                try {
                    message = connectionHandler.read();
                    System.out.println(message.getText());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void startClient() /*throws Exception*/ {
            System.out.println("Введите имя");
            username = scanner.nextLine();
            createConnection();
            new Writer().start();
            new Reader().start();
        }
        //try {
        // 1. разрывать соединение с сервером только по команде exit от поль-ля
        // 2. чтение сообщений от поль-ля и отправка на сервер
        // - отдельный поток
        // - сканнер
        // - Connection connectionHandler
        // - имя поль-ля
        // - цикл
        // 3. получение сообщений от сервера и вывод в консоль - отдельный Thread поток

      //  try(Connection connectionHandler =
        //            new Connection(new Socket(address.getHostName(),
        //                  address.getPort()))) {
       // } catch (Exception e) {

        while (true){
            System.out.println("Введите текст сообщения");
            String text = scanner.nextLine();
            if ("exit".equalsIgnoreCase(text)) {
                break;
            }
            //try(Connection connectionHandler =
            //   new Connection(new Socket(address.getHostName(),
            //      address.getPort()))) {  //перенесли до цикла для новой задачи
            Message message = new Message(username);
            message.setText(text);
            try {
                connectionHandler.send(message);
                Message fromServer = connectionHandler.read();
                System.out.println(fromServer.getText());
            } catch (IOException e) {
            }

        //} catch (Exception e) {

        //} // в трай метод close вызывается автоматически

        }
    }
}

//Часть №1
//Клиент и сервер общаются по средствам передачи сообщений.
//Сообщение хранит: текст сообщения и дату и время отправки сообщения с временной зоной.
