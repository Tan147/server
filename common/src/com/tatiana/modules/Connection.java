package com.tatiana.modules;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

public class Connection implements AutoCloseable{
    //перечислитьсв-ва,необходимые для отправки сообщения по сокет соединению
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;


    //объявить конструктор с необход. параметрами

    public Connection(Socket socket) throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    } //здесь важна последов-ть - сначала outputStream, потом  inputStream


    //метод отправки Message по сокет соединению
    public void send(Message message) throws IOException{
        message.setSentAt(LocalDateTime.now());
        outputStream.writeObject(message);
        outputStream.flush(); //метод нужен,чтобы сообщение было выброшено в аутпутстрим
    }

    //метод получения Message по сокет соединению
    public Message read() throws IOException {
        try {
            return  (Message) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        outputStream.close();
        inputStream.close();
        //socket.close();  - если добавляли в этот класс свойство сокет

    }
}
