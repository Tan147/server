package com.tatiana.modules;


import java.io.Serializable;
import java.nio.file.Paths;

public class FileMessage implements Serializable {
    private String description;
    private int size;
    private String filePath;


    public FileMessage(String description, String filepath) {
        this.description = description;
        this.filePath = filepath;
    }

    public FileMessage(String description, int size) {
        this.description = description;
        this.size = size;
    }

    public FileMessage(String description) {
        this.description = description;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDescription() {
        return description;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        String file = Paths.get(filePath).getFileName().toString();
        return "Имя файла: " + file + ", описание: " + description + ", размер в Мб: " + size;
    }
}
