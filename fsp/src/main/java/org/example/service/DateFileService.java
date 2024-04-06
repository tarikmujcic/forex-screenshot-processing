package org.example.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateFileService {

    private static final String CURRENT_DATE_FILE_NAME = "current-date.txt";

    private static final String FOREX_DAYS_OFF_FILE_NAME = "forex-days-off.txt";

    private static final DateTimeFormatter OFF_DAYS_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d yyyy");

    public static final List<LocalDate> forexOffDays = new ArrayList<>();

    public static void writeNextDate() {
        LocalDate dateFromFile = getDateFromFile();
        if (dateFromFile == null) {
            throw new RuntimeException("Date read from file is null!");
        }
        LocalDate nextDate = getNextDate(dateFromFile);
        try {
            FileWriter fileWriter = new FileWriter(CURRENT_DATE_FILE_NAME);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(nextDate.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static LocalDate getDateFromFile() {
        if (forexOffDays.isEmpty()) {
            initializeForexOffDays();
        }
        LocalDate dateFromFile = null;
        try {
            FileReader fileReader = new FileReader(CURRENT_DATE_FILE_NAME);
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
        LocalDate nextDate = date.plusDays(1);

        // Keep iterating until a non-off day is found
        while (forexOffDays.contains(nextDate) || nextDate.getDayOfWeek() == DayOfWeek.SATURDAY || nextDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDate = nextDate.plusDays(1);
        }

        return nextDate;
    }

    public static void initializeForexOffDays() {
        try (InputStream inputStream = Files.newInputStream(Paths.get(FOREX_DAYS_OFF_FILE_NAME))) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LocalDate date = LocalDate.parse(line, OFF_DAYS_DATE_FORMATTER);
                    forexOffDays.add(date);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + FOREX_DAYS_OFF_FILE_NAME, e);
        }
    }
}
