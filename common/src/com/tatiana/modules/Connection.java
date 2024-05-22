package com.tatiana.modules;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

public class Connection implements AutoCloseable {
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public Connection(Socket socket) throws IOException{
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException{
        message.setSentAt(LocalDateTime.now());
        outputStream.writeObject(message);
        outputStream.flush();
    }

    public Message read() throws IOException{
        try {
            return  (Message) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception{
        outputStream.close();
        inputStream.close();
    }

    public void sendFileDescription(FileMessage fileMessage) throws IOException{
        outputStream.writeObject(fileMessage);
        outputStream.flush();
    }

}
