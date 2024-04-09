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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotService {


    private static final int CAPTURE_X = 0; // x-coordinate of the top-left corner of the capture region
    private static final int CAPTURE_Y = 45; // y-coordinate of the top-left corner of the capture region
    private static final int CAPTURE_WIDTH = 1920; // Width of the capture region
    private static final int CAPTURE_HEIGHT = 960; // Height of the capture region

    private static final String SCREENSHOT_FILE_NAME = "window_capture.png";

    // Below variables are used for HOURLY_1
    public static int CURRENT_CANDLE = 1;
    public static LocalDateTime CURRENT_LOCAL_DATE_TIME;
    public static int CURRENT_CANDLE_MAX;
    public static String CURRENT_FOLDER_PATH;

    public static File takeScreenshot(String targetDirectoryPath) {
        BufferedImage windowCapture = captureWindow();
        return saveImage(windowCapture, targetDirectoryPath, SCREENSHOT_FILE_NAME);
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
            System.out.println("Image saved to: " + outputFile.getAbsolutePath());
            return outputFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void processScreenshot(ForexChartType forexChartType, String sourceDirectoryPath, String targetDirectoryPath) {
        File sourceDirectory = new File(sourceDirectoryPath);
        File[] imageFiles = sourceDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png"));
        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No image files found in the input directory.");
            return;
        }

        for (File imageFile : imageFiles) {
            if (forexChartType == ForexChartType.HOURLY_1) {
                if (CURRENT_CANDLE == 1) {
                    CURRENT_LOCAL_DATE_TIME = DateFileService.getDateFromFile().atTime(18, 0);
                    App.START_DATE = CURRENT_LOCAL_DATE_TIME.toLocalDate();
                    ForexDayType dayType = ForexDayType.determineDayTypeForLocalDate(CURRENT_LOCAL_DATE_TIME.toLocalDate());
                    if (dayType == ForexDayType.OFF_DAY) {
                        throw new RuntimeException("Unexpected day type. OFF_DAY should never be in the CURRENT_LOCAL_DATE_TIME");
                    }
                    CURRENT_CANDLE_MAX = dayType == ForexDayType.GOOD_DAY ? 23 : DateFileService.getForexHoursForDate(CURRENT_LOCAL_DATE_TIME.toLocalDate());

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
                // 1. Save to the General Target Folder
                copyFileToOtherPath(imageFile, targetDirectoryPath + File.separator + determineFileNameForHourly1Type(imageId));

                // 2. Save to the Date-Specific folder
                copyFileToOtherPath(imageFile, CURRENT_FOLDER_PATH + File.separator + determineFileNameForHourly1Type(imageId));

                // PREP DATA FOR NEXT ITERATION:
                CURRENT_LOCAL_DATE_TIME = CURRENT_LOCAL_DATE_TIME.plusHours(1);
                CURRENT_CANDLE++;

                if (CURRENT_CANDLE == CURRENT_CANDLE_MAX + 1) {
                    CURRENT_CANDLE = 1;
                    CURRENT_FOLDER_PATH = "";
                    CURRENT_LOCAL_DATE_TIME = null;
                }
            }
            if (forexChartType == ForexChartType.HOURLY_23) {
                ImageDrawingService.drawHourly23Info(imageFile, targetDirectoryPath);
            } else if (forexChartType == ForexChartType.DAILY) {
                ImageDrawingService.drawDailyInfo(imageFile, targetDirectoryPath);
            } else if (forexChartType == ForexChartType.WEEKLY) {
                ImageDrawingService.drawWeeklyInfo(imageFile, targetDirectoryPath);
            }
        }
        DateFileService.determineAndWriteNextDate(forexChartType);
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
        }
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
}
