package org.example.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.example.App;
import org.example.enums.ForexChartType;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DateFileService {

    private static final String CURRENT_DATE_FILE_NAME = "current-date.txt";

    private static final String FOREX_DAYS_OFF_FILE_NAME = "forex-days-off.txt";

    private static final String NON_23H_DAYS_FILE_NAME = "non-23h-days.csv";

    private static final DateTimeFormatter OFF_DAYS_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d yyyy");

    public static final List<LocalDate> forexOffDays = new ArrayList<>();

    public static final Map<LocalDate, Integer> non23hdaysMap = new HashMap<>();

    public static void determineAndWriteNextDate(ForexChartType forexChartType) {
        if (((forexChartType == ForexChartType.HOURLY_1 || forexChartType == ForexChartType.FIVE_MIN) && ScreenshotService.CURRENT_CANDLE == ScreenshotService.CURRENT_CANDLE_MAX)
                || (forexChartType == ForexChartType.HOURLY_23 || forexChartType == ForexChartType.DAILY)) {
            writeNextDate();
        } else if (forexChartType == ForexChartType.WEEKLY) {
            writeNextDateWeekly();
        }
    }

    public static void writeNextDate() {
        LocalDate dateFromFile = getDateFromFile();
        if (dateFromFile == null) {
            throw new RuntimeException("Date read from file is null!");
        }
        LocalDate nextDate = incrementForexLocalDate(dateFromFile);
        writeLocalDateToFile(nextDate);
    }

    public static void writeNextDateWeekly() {
        LocalDate dateFromFile = getDateFromFile();
        if (dateFromFile == null) {
            throw new RuntimeException("Date read from file is null!");
        }
        if (dateFromFile.getDayOfWeek() != DayOfWeek.FRIDAY) {
            throw new RuntimeException(String.format(
                    "You are doing weekly processing and the day in current-day.txt is %s! Please make sure to set it to FRIDAY.", dateFromFile.getDayOfWeek()
            ));
        }
        writeLocalDateToFile(dateFromFile.plusDays(7));
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
        if (dateFromFile == null) {
            throw new RuntimeException("Date read from file is null!");
        }
        return dateFromFile;
    }


    private static LocalDate parseDateFromFile(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateString, formatter);
    }

    private static LocalDate incrementForexLocalDate(LocalDate date) {
        LocalDate nextDate = date.plusDays(1);
        // Keep iterating until a non-off day is found
        while (forexOffDays.contains(nextDate) || nextDate.getDayOfWeek() == DayOfWeek.SATURDAY || nextDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDate = nextDate.plusDays(1);
        }
        return nextDate;
    }

    private static void writeLocalDateToFile(LocalDate date) {
        try {
            FileWriter fileWriter = new FileWriter(CURRENT_DATE_FILE_NAME);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(date.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
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

    public static void initializeNon23hDaysMap() {
        try (CSVReader reader = new CSVReader(new FileReader(NON_23H_DAYS_FILE_NAME))) {
            String[] nextLine;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
            reader.readNext(); // ignore header
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length >= 2) {
                    LocalDate date = LocalDate.parse(nextLine[0], formatter);
                    int candles = Integer.parseInt(nextLine[1]);
                    non23hdaysMap.put(date, candles);
                }
            }
        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getForexHoursForDate(LocalDate date) {
        if (non23hdaysMap.isEmpty()) {
            initializeNon23hDaysMap();
        }
        if (non23hdaysMap.containsKey(date)) {
            return non23hdaysMap.get(date);
        }
        return 23;
    }

    /**
     * Used for HOURLY
     */
    public static LocalDate determineStartDate() {
        App.START_DATE = DateFileService.getDateFromFile();
        // Subtract 5 days from the start date
        for (int i = 0; i < 5; i++) {
            App.START_DATE = App.START_DATE.minusDays(1);

            // Skip Saturdays, Sundays, and off days
            while (App.START_DATE.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    App.START_DATE.getDayOfWeek() == DayOfWeek.SUNDAY ||
                    DateFileService.forexOffDays.contains(App.START_DATE)) {
                App.START_DATE = App.START_DATE.minusDays(1);
            }
        }

        System.out.println("START_DATE: " + App.START_DATE);
        return App.START_DATE;
    }

    public static String determineFileNameForDaily(int id) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%05d-", id))
                .append(dateFormatter.format(App.START_DATE))
                .append("-")
                .append(".png");

        return builder.toString();
    }

    /**
     * Used for HOURLY
     */
    public static String determineFileName() {
        String startDateFormatted = determineStartDate().format(DateTimeFormatter.ofPattern("MM-dd"));
        System.out.println("startDateFormatted: " + startDateFormatted);
        String endDateFormatted = Objects.requireNonNull(DateFileService.getDateFromFile()).format(DateTimeFormatter.ofPattern("MM-dd"));
        String yearShort = String.valueOf(App.START_DATE.getYear()).substring(2); // Get last two digits of the year
        String outputFileName = startDateFormatted + "To" + endDateFormatted + "-" + yearShort + ".png";
        System.out.println("outputFileName: " + outputFileName);
        return outputFileName;
    }
}
