package org.example.service;

import org.example.App;
import org.example.enums.ForexChartType;
import org.example.enums.ForexDayType;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ScreenshotService {


    private static final int CAPTURE_X = 0; // x-coordinate of the top-left corner of the capture region
    private static final int CAPTURE_Y = 45; // y-coordinate of the top-left corner of the capture region
    private static final int CAPTURE_WIDTH = 1920; // Width of the capture region
    private static final int CAPTURE_HEIGHT = 972; // Height of the capture region

    public static final String SCREENSHOT_FILE_NAME = "window_capture.png";
    public static LocalDateTime CURRENT_LOCAL_DATE_TIME;

    // Below variables are used for candle calculations for HOURLY_1 and FOUR_HOUR
    public static int CURRENT_CANDLE = 1;
    public static int CURRENT_CANDLE_MAX;
    public static String CURRENT_FOLDER_PATH;
    public static String DEBUG_FOLDER_PATH;

    // for FIVE_MIN folder structuring
    public static int CURRENT_MONTH;
    public static int CURRENT_YEAR;

    // for FOUR_HOUR Label
    public static LocalDate CURRENT_WEEK_START_LOCAL_DATE;
    public static DayOfWeek CURRENT_DAY_OF_WEEK;
    /**
     * Used for file naming of the FOUR_HOUR images. This is needed since the date goes to the next date after midnight
     * and we want to make sure that file name contains always the start date (at the time from 5pm to 9pm)
     */
    public static LocalDateTime CURRENT_DATE_TIME_OF_WEEK;

    // For image comparison
    public static String LAST_IMAGE_PATH;

    public static File takeScreenshot(String targetDirectoryPath, String screenshotFileName) {
        BufferedImage windowCapture = captureWindow();
        return saveImage(windowCapture, targetDirectoryPath, screenshotFileName);
    }

    private static BufferedImage captureWindow() {
        try {
            Robot robot = new Robot();

            Rectangle captureRect = new Rectangle(CAPTURE_X, CAPTURE_Y, CAPTURE_WIDTH, CAPTURE_HEIGHT);

            return robot.createScreenCapture(captureRect);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    private static File saveImage(BufferedImage image, String folderPath, String filename) {
        try {
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File outputFile = new File(folder, filename);
            ImageIO.write(image, "png", outputFile);
            return outputFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void processScreenshot(ForexChartType forexChartType, String sourceDirectoryPath, String targetDirectoryPath) {
        processScreenshot(forexChartType, sourceDirectoryPath, targetDirectoryPath, null);
    }

    public static void processScreenshot(ForexChartType forexChartType, String sourceDirectoryPath, String targetDirectoryPath, String currencyCode) {
        File sourceDirectory = new File(sourceDirectoryPath);
        File[] imageFiles = sourceDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png"));
        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No image files found in the input directory.");
            return;
        }
        if (imageFiles.length > 1) {
            Arrays.stream(imageFiles).forEach(t -> System.out.println(t.getPath()));
            throw new RuntimeException("Multiple image files found in the input directory.");
        }

        File imageFile = Arrays.stream(imageFiles).iterator().next();
        if (forexChartType == ForexChartType.FIVE_MIN) {
            processFiveMinImage(imageFile, targetDirectoryPath);
        }
        if (forexChartType == ForexChartType.HOURLY_1) {
            processHourly1Image(imageFile, targetDirectoryPath);
        }
        if (forexChartType == ForexChartType.HOURLY_23) {
            ImageDrawingService.drawHourly23Info(imageFile, targetDirectoryPath, null);
        } else if (forexChartType == ForexChartType.FOUR_HOUR) {
            processFourHourImage(imageFile, targetDirectoryPath);
        } else if (forexChartType == ForexChartType.DAILY) {
            ImageDrawingService.drawDailyInfo(imageFile, targetDirectoryPath);
        } else if (forexChartType == ForexChartType.WEEKLY) {
            ImageDrawingService.drawWeeklyInfo(imageFile, targetDirectoryPath);
        } else if (forexChartType == ForexChartType.DAILY_LATEST) {
            ImageDrawingService.drawDailyLatestInfo(imageFile, targetDirectoryPath, currencyCode);
        } else if (forexChartType == ForexChartType.HOURLY_23_LATEST) {
            String targetFullPath = targetDirectoryPath + "\\" + currencyCode + "\\" + ImageDrawingService.DEFAULT_FORMATTER.format(App.LATEST_DATE);
            ImageDrawingService.drawHourly23Info(imageFile, targetFullPath, currencyCode);
        } else if (forexChartType == ForexChartType.FIVE_MIN_LATEST) {
            ImageDrawingService.drawFiveMinuteLatestInfo(imageFile, targetDirectoryPath, currencyCode);
        } else if (forexChartType == ForexChartType.FIVE_MIN_WHOLE_DAY) {
            ImageDrawingService.drawFiveMinuteLatestInfo(imageFile, targetDirectoryPath, currencyCode);
        }
        DateFileService.determineAndWriteNextDate(forexChartType);
    }


    private static void processFiveMinImage(File imageFile, String targetDirectoryPath) {
        if (CURRENT_CANDLE == 1) {
            CURRENT_LOCAL_DATE_TIME = DateFileService.getDateFromFile().atTime(18, 0);

            // handle file hierarchy
            int year = CURRENT_LOCAL_DATE_TIME.getYear();
            if (CURRENT_YEAR != year) {
                CURRENT_YEAR = year;
                createFolderInPath(targetDirectoryPath, String.valueOf(CURRENT_YEAR));
            }
            int month = CURRENT_LOCAL_DATE_TIME.getMonthValue();
            if (CURRENT_MONTH != month) {
                CURRENT_MONTH = month;
                createFolderInPath(targetDirectoryPath + File.separator + CURRENT_YEAR, String.valueOf(CURRENT_MONTH));
            }

            App.START_DATE = CURRENT_LOCAL_DATE_TIME.toLocalDate();
            ForexDayType dayType = ForexDayType.determineDayTypeForLocalDate(CURRENT_LOCAL_DATE_TIME.toLocalDate());
            if (dayType == ForexDayType.OFF_DAY) {
                throw new RuntimeException("Unexpected day type. OFF_DAY should never be in the CURRENT_LOCAL_DATE_TIME");
            }
            CURRENT_CANDLE_MAX = dayType == ForexDayType.GOOD_DAY ? 23 * 12 : DateFileService.getForexHoursForDate(CURRENT_LOCAL_DATE_TIME.toLocalDate()) * 12;

            DEBUG_FOLDER_PATH = createFolderInPath(targetDirectoryPath, "Debug");

            // create folder
            CURRENT_FOLDER_PATH = targetDirectoryPath + File.separator + CURRENT_YEAR + File.separator + CURRENT_MONTH + File.separator + CURRENT_LOCAL_DATE_TIME.toLocalDate();
            Path folderPath = Paths.get(CURRENT_FOLDER_PATH);
            try {
                Files.createDirectories(folderPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        int imageId = InstanceCounterService.getAndIncrementFIVE_MIN_INSTANCE_COUNT();
        // 0. If it's first screenshot in the day 6-7PM
        if (CURRENT_CANDLE == 1) {
            copyFileToOtherPath(imageFile, DEBUG_FOLDER_PATH + File.separator + determineFileNameForFiveMinType(imageId));
        }

        // 1. Save ONLY to the Date-Specific folder
        String targetFolderImagePath = CURRENT_FOLDER_PATH + File.separator + determineFileNameForFiveMinType(imageId);
        copyFileToOtherPath(imageFile, targetFolderImagePath);
        LAST_IMAGE_PATH = targetFolderImagePath;

        // PREP DATA FOR NEXT ITERATION:
        CURRENT_LOCAL_DATE_TIME = CURRENT_LOCAL_DATE_TIME.plusMinutes(5);
        incrementCandleInformation();
    }

    private static void processHourly1Image(File imageFile, String targetDirectoryPath) {
        if (CURRENT_CANDLE == 1) {
            CURRENT_LOCAL_DATE_TIME = DateFileService.getDateFromFile().atTime(18, 0);
            App.START_DATE = CURRENT_LOCAL_DATE_TIME.toLocalDate();
            ForexDayType dayType = ForexDayType.determineDayTypeForLocalDate(CURRENT_LOCAL_DATE_TIME.toLocalDate());
            if (dayType == ForexDayType.OFF_DAY) {
                throw new RuntimeException("Unexpected day type. OFF_DAY should never be in the CURRENT_LOCAL_DATE_TIME");
            }
            CURRENT_CANDLE_MAX = dayType == ForexDayType.GOOD_DAY ? 23 : DateFileService.getForexHoursForDate(CURRENT_LOCAL_DATE_TIME.toLocalDate());

            DEBUG_FOLDER_PATH = createFolderInPath(targetDirectoryPath, "Debug");

            // create folder
            CURRENT_FOLDER_PATH = targetDirectoryPath + File.separator + CURRENT_LOCAL_DATE_TIME.toLocalDate();
            Path folderPath = Paths.get(CURRENT_FOLDER_PATH);
            try {
                Files.createDirectories(folderPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        int imageId = InstanceCounterService.getAndIncrementHOURLY_1_INSTANCE_COUNT();
        // 0. If it's first screenshot in the day 6-7PM
        if (CURRENT_CANDLE == 1) {
            copyFileToOtherPath(imageFile, DEBUG_FOLDER_PATH + File.separator + determineFileNameForHourly1Type(imageId));
        }

        // 1. Save to the General Target Folder
        String targetFolderImagePath = targetDirectoryPath + File.separator + determineFileNameForHourly1Type(imageId);
        copyFileToOtherPath(imageFile, targetFolderImagePath);
        LAST_IMAGE_PATH = targetFolderImagePath;

        // 2. Save to the Date-Specific folder
        copyFileToOtherPath(imageFile, CURRENT_FOLDER_PATH + File.separator + determineFileNameForHourly1Type(imageId));

        // PREP DATA FOR NEXT ITERATION:
        CURRENT_LOCAL_DATE_TIME = CURRENT_LOCAL_DATE_TIME.plusHours(1);
        incrementCandleInformation();
    }

    private static void processFourHourImage(File imageFile, String targetDirectoryPath) {
        if (CURRENT_WEEK_START_LOCAL_DATE == null) { // is first run of the app
            LocalDate currentDate = DateFileService.getDateFromFile();
            if (!currentDate.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                CURRENT_WEEK_START_LOCAL_DATE = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1);
            }
        }
        if (CURRENT_CANDLE == 1) {
            CURRENT_LOCAL_DATE_TIME = DateFileService.getDateFromFile().atTime(17, 0);
            CURRENT_DAY_OF_WEEK = CURRENT_LOCAL_DATE_TIME.getDayOfWeek();
            CURRENT_DATE_TIME_OF_WEEK = CURRENT_LOCAL_DATE_TIME;
            App.START_DATE = CURRENT_LOCAL_DATE_TIME.toLocalDate();
            if (App.START_DATE.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                CURRENT_WEEK_START_LOCAL_DATE = App.START_DATE;
            }

            ForexDayType dayType = ForexDayType.determineDayTypeForLocalDate(App.START_DATE);
            if (dayType == ForexDayType.OFF_DAY) {
                throw new RuntimeException("Unexpected day type. OFF_DAY should never be in the CURRENT_LOCAL_DATE_TIME");
            }
            CURRENT_CANDLE_MAX = dayType == ForexDayType.GOOD_DAY ? 6 : DateFileService.getForexFourHoursCountForDate(CURRENT_LOCAL_DATE_TIME.toLocalDate());
        }

        ImageDrawingService.drawFourHourInfo(imageFile, targetDirectoryPath, CURRENT_LOCAL_DATE_TIME);

        // PREP DATA FOR NEXT ITERATION:
        CURRENT_LOCAL_DATE_TIME = CURRENT_LOCAL_DATE_TIME.plusHours(4);
        incrementCandleInformation();
    }

    /**
     * Creates a new file with targetPath (e.g 'path/to/myfile.txt')
     * @param file the file that will get copied
     * @param targetPath the path where to copy the file (including the name of the file)
     */
    private static void copyFileToOtherPath(File file, String targetPath) {
        File newFile = new File(targetPath);
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            Files.copy(file.toPath(), fos);
        } catch (IOException e) {
            System.out.println("Failed to save file to folder: " + targetPath);
            throw new RuntimeException(e);
        }
    }

    private static String determineFileNameForFiveMinType(int id) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        DateTimeFormatter startHourFormatter = DateTimeFormatter.ofPattern("hhmm");
        DateTimeFormatter endHourFormatter = DateTimeFormatter.ofPattern("hhmma");

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%05d-", id))
                .append(dateFormatter.format(App.START_DATE))
                .append("-")
                .append(startHourFormatter.format(CURRENT_LOCAL_DATE_TIME))
                .append("-")
                .append(endHourFormatter.format(CURRENT_LOCAL_DATE_TIME.plusMinutes(5)))
                .append(".png");

        return builder.toString();
    }

    private static String determineFileNameForHourly1Type(int id) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        DateTimeFormatter startHourFormatter = DateTimeFormatter.ofPattern("h");
        DateTimeFormatter endHourFormatter = DateTimeFormatter.ofPattern("ha");

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%05d-", id))
                .append(dateFormatter.format(App.START_DATE))
                .append("-")
                .append(startHourFormatter.format(CURRENT_LOCAL_DATE_TIME))
                .append("-")
                .append(endHourFormatter.format(CURRENT_LOCAL_DATE_TIME.plusHours(1)))
                .append(".png");

        return builder.toString();
    }

    private static void incrementCandleInformation() {
        CURRENT_CANDLE++;

        if (CURRENT_CANDLE == CURRENT_CANDLE_MAX + 1) {
            CURRENT_CANDLE = 1;
            CURRENT_FOLDER_PATH = "";
            CURRENT_LOCAL_DATE_TIME = null;
            // for FOUR_HOUR
            if (App.forexChartType == ForexChartType.FOUR_HOUR) {
                CURRENT_DAY_OF_WEEK = CURRENT_DAY_OF_WEEK.plus(1);
                while (CURRENT_DAY_OF_WEEK == DayOfWeek.SATURDAY || CURRENT_DAY_OF_WEEK == DayOfWeek.SUNDAY) {
                    CURRENT_DAY_OF_WEEK = CURRENT_DAY_OF_WEEK.plus(1);
                }
            }
        }
    }

    public static String createFolderInPath(String directoryPath, String name) {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            throw new RuntimeException("Directory does not exist: " + directoryPath);
        }

        File debugFolder = new File(directory, name);

        if (debugFolder.exists()) {
            return debugFolder.getPath();
        }

        if (debugFolder.mkdir()) {
            System.out.println("Debug folder created successfully.");
            return debugFolder.getPath();
        } else {
            System.out.println("Failed to create Debug folder.");
        }
        return null;
    }
}
