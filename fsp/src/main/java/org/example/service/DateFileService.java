package org.example.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFileService {

    public static void writeNextDate() {
        LocalDate dateFromFile = getDateFromFile();
        if (dateFromFile == null) {
            throw new RuntimeException("Date read from file is null!");
        }
        LocalDate nextDate = getNextDate(dateFromFile);
        String filename = "output.txt"; // Name of the file to write to
        try {
            FileWriter fileWriter = new FileWriter(filename);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(nextDate.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static LocalDate getDateFromFile() {
        String filename = "output.txt"; // Name of the file to read from
        LocalDate dateFromFile = null;
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                dateFromFile = parseDateFromFile(line);
            }
            bufferedReader.close();
            return dateFromFile;
        } catch (IOException e) {
            System.err.println("Error reading from the file: " + e.getMessage());
        }
        return null;
    }

    private static LocalDate parseDateFromFile(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateString, formatter);
    }

    private static LocalDate getNextDate(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
            date = date.plusDays(3);
        } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            date = date.plusDays(2);
        } else {
            date = date.plusDays(1);
        }
        return date;
    }
}
