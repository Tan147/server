package com.tatiana.server;

import com.tatiana.modules.Connection;
import com.tatiana.modules.Message;

import java.io.IOException;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private int port;
    //private final List<Message> messages = new ArrayList<>();
    private final ArrayBlockingQueue<Message> messages =
            new ArrayBlockingQueue<>(1000, true);

    // 1 вар - private final List<Connection> connectionHandlers = new ArrayList<>();
    // 2 вар - private final List<Connection> connectionHandlers =
            //Collections.synchronizedList(new ArrayList<>());
    private final List<Connection> connectionHandlers = new CopyOnWriteArrayList<>(); //потокобезопасная коллекция

    public Server(int port) {
        this.port = port;
    }

    // сервер получает сообщение от клиента и рассылает его
    // по всем активным соединениям
    //и как и когда отреагировать на ошибки

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    Connection connectionHandler = new Connection(socket);
                    // обеспечение потокобезопасности необходимо,
                    // так как разные потоки будут менять список
                    synchronized (connectionHandlers) {
                        connectionHandlers.add(connectionHandler);
                    }
                    new ThreadForClient(connectionHandler).start();
                } catch (Exception e) {
                    System.out.println("Проблема с установкой нового соединения");
                    //throw new RuntimeException(e);  проблема одного эл-та не должна стать проблемоц всего приложения
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка запуска сервера");
            throw new RuntimeException(e);
        }
    }


    private class ThreadForClient extends Thread {
        private final Connection connectionHandler;

        public ThreadForClient(Connection connectionHandler) {
            this.connectionHandler = connectionHandler;
        }


        @Override
        public void run() {
            while (true) {
                // удалить соединение из списка,
                // если работа с соединением
                // не может быть продолжена (ошибка на чтение / запись)
                //синхронизация
                Message fromClient = null;
                try {
                    fromClient = connectionHandler.read();
                } catch (IOException e) {
                    //synchronized (connectionHandlers) {
                        connectionHandlers.remove(connectionHandler);
                        return;
                    //}
                }
                System.out.println(fromClient.getText());
                Message message = new Message("server: " + fromClient.getSender());
                message.setText(fromClient.getText());
                    // вариант 1. рассылка по всем соединениям полученного сообщения
               /*
                for (Connection handler : connectionHandlers) {
                    try {
                        handler.send(message);
                    } catch (IOException e) {
                       // synchronized (connectionHandlers) {
                            connectionHandlers.remove(connectionHandler);
                       // }

                    }
                }*/
                // вариант 2. рассылка сообщений из списка в отдельном потоке
                //можно через wait-notify или через блокирующие очереди из канкарент пакета
                //messages.add(message);
                //notify();
                try {
                    messages.put(message);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    private class Sender extends Thread { //внутр класс, имеет доступ к св-вам внешнего класса, поток
        @Override
        public void run(){
            //рассылка сообщений по всем соединениям
            //while (messages.isEmpty()) wait();
            while (true) { //здесь true, а не messages.isEmpty()), т.к. иначе потоки не будут блокироваться,
                // а будут в бесконечном цикле
                try {
                    Message message = messages.take();
                    for (Connection handler : connectionHandlers) {
                        try {
                            handler.send(message);
                        } catch (IOException e) {
                            connectionHandlers.remove(handler);
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

//int threads = Runtime.getRuntime().availableProcessors();
//для подсчета потоков для дз 19.04.24


    /*
    старый вариант с лямбдой, поменяли на вариант с внутр классом

    public void startServer(){
        try (ServerSocket serverSocket = new ServerSocket(port)){
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    Connection connectionHandler = new Connection(socket);
                    connectionHandlers.add(connectionHandler); //сделать synchronized
                    new Thread(()->{  //запускаем в поток
                        while (true) {
                            Message fromClient = connectionHandler.read();
                            System.out.println(fromClient.getText());
                            Message message = new Message("server: " + fromClient.getSender());
                            message.setText(fromClient.getText());
                            messages.add(message); //тоже synchronized, если сообщения отправляет
                            // отдельный поток из списка месседж

                            // 1 вариант - поток разошлет сообщение всем из списка соединений
                            //поэтому надо еще удалять из списка тех, кто отключился
                            //и нужна для этого списка синхронизация
                        /*for (Connection handler : connectionHandlers) {
                            handler.send(message);

                        }
                    }).start();
                    // вариант 2 - рассылка сообщений из списка месседж в отдельном потоке

                } catch (IOException e) {
                    System.out.println("Проблема с соединением");
                } // здесь трай-кетч для одной итерации решает проблему
            }
        } catch (IOException e) {
            System.out.println("Ошибка запуска сервера");
            throw new RuntimeException(e);
        }
    }*/


    //Часть №2
    // Расширение функционала сервера.
    //
    // Сервер может обрабатывать следующие запросы:
    // /help - список доступных запросов и их описание
    // /ping - время ответа сервера
    // /requests - количество успешно обработанных запросов
    // /popular - название самого популярного запроса
    //
    // Если сервер не может обработать запрос (пришла команда, которую не может обработать сервер),
    // он должен отправить клиенту сообщение с соответствующей информацией.

