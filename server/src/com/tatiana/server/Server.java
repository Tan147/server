package com.tatiana.server;

import com.tatiana.modules.Connection;
import com.tatiana.modules.FileMessage;
import com.tatiana.modules.Message;
import com.tatiana.modules.SendAndReceive;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static java.util.Collections.copy;

public class Server {
    private int port;
    private final List<Message> messages = new CopyOnWriteArrayList<>();
    private final List<SendAndReceive> connectionHandlers = new CopyOnWriteArrayList<>();
    private final List<FileMessage> fileMessages = new CopyOnWriteArrayList<>();
    public static final int fileSize = 20;
    public static final int maxLength = 200;

    public Server(int port) {
        this.port = port;
    }

    public void startServer(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true){
                try {
                    Socket socket = serverSocket.accept();
                    SendAndReceive connectionHandler = new SendAndReceive(socket);
                    synchronized (connectionHandlers) {
                        connectionHandlers.add(connectionHandler);
                    }
                    new ThreadForClient(connectionHandler).start();
                } catch (Exception e) {
                    System.out.println("Проблема с установкой нового соединения");
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка запуска сервера");
            throw new RuntimeException(e);
        }
    }


    private class ThreadForClient extends Thread {
        private final SendAndReceive connectionHandler;

        public ThreadForClient(SendAndReceive connectionHandler) {
            this.connectionHandler = connectionHandler;
        }

        public synchronized void showFiles(){
            Message message = new Message("server");
            String listFiles = "Список доступных файлов: ";
            String fileInfo = fileMessages.stream()
                    .map(FileMessage::toString)
                    .collect(Collectors.joining(", "));
            message.setText(listFiles + fileInfo);
            try {
                connectionHandler.send(message);
            } catch (IOException e) {
                connectionHandler.close();
            }
        }

        public synchronized void loadFile(FileMessage fileMessage) {
            int randomName = (int) (Math.random() * 1000);
            char[] description = fileMessage.getDescription().toCharArray();
            File filePath = new File(fileMessage.getFilePath());
            String fileName = filePath.getName();
            String mes;
            File fileDestination;
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                fileDestination = new File((randomName + filePath.getName()));
            } else {
                fileDestination = new File(fileName);
            }
            Message message = new Message("server");
            if (!filePath.isDirectory() && filePath.exists()) {
                if (fileMessage.getSize() <= fileSize && description.length <= maxLength) {
                    try {
                        copy(filePath, fileDestination);
                        if (fileDestination.isFile()) {
                            fileMessage.setFilePath(fileDestination.getName());
                            fileMessages.add(fileMessage);
                        }
                        mes = "Файл " + fileDestination.getName() + " загружен на сервер";
                        message.setText(mes);
                        messages.add(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                mes = "Файл по указанному пути не найден или описание превышает 200 символов или файл содержит " +
                        "слишком большой объем информации (больше 20 Мб)";
                message.setText(mes);
                try {
                    connectionHandler.send(message);
                } catch (IOException e) {
                    connectionHandler.close();
                }
            }
        }


        synchronized void saveFile(FileMessage fileMessage) {
            File filePath = new File(fileMessage.getDescription());
            File fileDestination = new File(fileMessage.getFilePath() + fileMessage.getDescription());
            String mes;
            Message message = new Message("server");
            if (fileMessages.isEmpty());
            try {
                copy(filePath, fileDestination);
                if (fileDestination.isFile()){
                    mes = "Файл " + fileDestination.getName() + " сохранен";
                    message.setText(mes);
                    connectionHandler.send(message);
                }
            } catch (IOException e){
                mes = "Проверьте имя и описание файла";
                message.setText(mes);
                try {
                    connectionHandler.send(message);
                } catch (IOException ex){
                    connectionHandler.close();
                }
            }
        }

        public FileMessage createFileMessage(){
            FileMessage fileMessage;
            try {
                fileMessage = connectionHandler.receiveFileDescription();
            } catch (IOException e){
                connectionHandlers.remove(connectionHandler);
                return null;
            } catch (ClassNotFoundException e){
                throw new RuntimeException(e);
            }
            return fileMessage;
        }


        @Override
        public void run() {
            while (true) {
                Message fromClient = null;
                try {
                    if (fromClient != null && !fromClient.getText().equals("файл") &&
                            !fromClient.getText().equals("загрузить файл") && !fromClient.getText().isEmpty()) {
                        Message message = new Message("server: " + fromClient.getSender());
                        message.setText(fromClient.getSentAt() + " " + fromClient.getSender() + ", " + fromClient.getText());
                        messages.add(message);
                    } else if (Objects.requireNonNull(fromClient).getText().equals("файл")) {
                        showFiles();
                    } else if (fromClient.getText().equals("загрузить файл")) {
                        FileMessage fileMessage = createFileMessage();
                        loadFile(Objects.requireNonNull(fileMessage));
                    } else if (fromClient.getText().equals("сохранить файл")) {
                        showFiles();
                        FileMessage fileMessage = createFileMessage();
                        saveFile(Objects.requireNonNull(fileMessage));
                    }
                    Message message = null;
                    if (!messages.isEmpty()) message = messages.getLast();
                    for (SendAndReceive handler : connectionHandlers) {
                        try {
                            handler.send(message);
                        } catch (IOException e) {
                            connectionHandlers.remove(handler);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

