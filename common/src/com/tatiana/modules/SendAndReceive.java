package com.tatiana.modules;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class SendAndReceive implements AutoCloseable {
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket socket;

    public SendAndReceive(Socket socket) throws IOException{
        this.socket = Objects.requireNonNull(socket);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException{
        LocalDateTime localTime = LocalDateTime.now();
        message.setSentAt(localTime);
        outputStream.writeObject(message);
        outputStream.flush();
    }

    public Message receiveFile() throws IOException, ClassNotFoundException{
        try {
            return (Message) inputStream.readObject();
        } catch (ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public FileMessage receiveFileDescription() throws IOException, ClassNotFoundException{
        try {
            return (FileMessage) inputStream.readObject();
        } catch (ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (!socket.isClosed()) {
                inputStream.close();
                outputStream.close();
                socket.close();
            }
        } catch (IOException e){
            System.out.println("Ошибка при закрытии потоков");
        }
    }
}

