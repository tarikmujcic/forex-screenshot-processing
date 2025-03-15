package org.example.service;

import java.io.Closeable;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import java.io.*;

public class FileHandler {
    private static final List<Closeable> openFiles = new CopyOnWriteArrayList<>();

    static {
        // Register shutdown hook to close all open files
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Application shutting down... Closing open files.");
            closeAllFiles();
        }));
    }

    public static FileInputStream openFile(String filePath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        openFiles.add(fileInputStream);
        return fileInputStream;
    }

    public static void closeFile(Closeable file) {
        try {
            if (file != null) {
                file.close();
                openFiles.remove(file);
                System.out.println("File closed: " + file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeAllFiles() {
        for (Closeable file : openFiles) {
            closeFile(file);
        }
        openFiles.clear();
    }

    public static void main(String[] args) throws IOException {
        FileInputStream file1 = openFile("test1.txt");
        FileInputStream file2 = openFile("test2.txt");

        // Simulate program running
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        // Manually close one file
        closeFile(file1);

        // Remaining files will be closed when the application exits
        System.out.println("Application is still running...");
    }
}