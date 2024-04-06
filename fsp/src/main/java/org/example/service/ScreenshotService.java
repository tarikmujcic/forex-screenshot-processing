package org.example.service;

import org.example.App;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

public class ScreenshotService {

    private static final String HEADER_TEXT = "H1";

    private static final int CAPTURE_X = 0; // x-coordinate of the top-left corner of the capture region
    private static final int CAPTURE_Y = 45; // y-coordinate of the top-left corner of the capture region
    private static final int CAPTURE_WIDTH = 1920; // Width of the capture region
    private static final int CAPTURE_HEIGHT = 960; // Height of the capture region

    private static final String SCREENSHOT_FILE_NAME = "window_capture.png";

    public static void takeScreenshot(String targetDirectoryPath) {
        BufferedImage windowCapture = captureWindow();
        saveImage(windowCapture, targetDirectoryPath, SCREENSHOT_FILE_NAME);
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

    private static void saveImage(BufferedImage image, String folderPath, String filename) {
        try {
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File outputFile = new File(folder, filename);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void processScreenshot(String sourceDirectoryPath, String targetDirectoryPath) {
        File sourceDirectory = new File(sourceDirectoryPath);
        File[] imageFiles = sourceDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png"));
        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No image files found in the input directory.");
            return;
        }

        for (File imageFile : imageFiles) {
            try {
                BufferedImage image = ImageIO.read(imageFile);
                // Create a graphics object to draw on the image
                Graphics2D g2d = image.createGraphics();

                // Define font and color for drawing days of the week
                Font font = new Font("Arial", Font.BOLD, 24);
                g2d.setFont(font);
                g2d.setColor(Color.BLACK);

                g2d.drawString(HEADER_TEXT, 50, 50);

                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();
                int x = imageWidth / 5 - (int) (imageWidth * 0.15);
                int y = (imageHeight - 50) / 2;

                LocalDate currentDate = determineStartDate();
                int heightHelp = y;
                for (int i = 0; i < 5; i++) {
                    String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
                    String date = currentDate.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
                    if (App.USE_CUSTOM_Y_COORDINATES) {
                        if (App.CUSTOM_Y_COORDINATES[i] == 1) {
                            heightHelp = imageHeight / 5;
                        } else if (App.CUSTOM_Y_COORDINATES[i] == 2) {
                            heightHelp = y;
                        } else if (App.CUSTOM_Y_COORDINATES[i] == 3) {
                            heightHelp = imageHeight / 9 * 8;
                        }
                    }

                    g2d.drawString(dayOfWeek, x, heightHelp);
                    g2d.drawString(date, x, heightHelp + 25);
                    x += (imageWidth / 5);

                    currentDate = currentDate.plusDays(1);
                    while (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                            currentDate.getDayOfWeek() == DayOfWeek.SUNDAY ||
                            DateFileService.forexOffDays.contains(currentDate)) {
                        currentDate = currentDate.plusDays(1);
                    }
                }

                g2d.dispose(); // Dispose the graphics object

                // Save the modified image to the output directory
                File outputFile = new File(targetDirectoryPath + File.separator + determineFileName());
                ImageIO.write(image, "png", outputFile);
                imageFile.delete();
            } catch (IOException e) {
                System.out.println("Error processing image: " + imageFile.getName());
            }
        }
        DateFileService.writeNextDate();
    }

    private static LocalDate determineStartDate() {
        App.START_DATE = DateFileService.getDateFromFile();
        if (App.START_DATE == null) {
            throw new RuntimeException("An error occurred while trying to determine the start date. App.START_DATE is null!");
        }
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

    private static String determineFileName() {
        String startDateFormatted = determineStartDate().format(DateTimeFormatter.ofPattern("MM-dd"));
        System.out.println("startDateFormatted: " + startDateFormatted);
        String endDateFormatted = Objects.requireNonNull(DateFileService.getDateFromFile()).format(DateTimeFormatter.ofPattern("MM-dd"));
        String yearShort = String.valueOf(App.START_DATE.getYear()).substring(2); // Get last two digits of the year
        String outputFileName = startDateFormatted + "To" + endDateFormatted + "-" + yearShort + ".png";
        System.out.println("outputFileName: " + outputFileName);
        return outputFileName;
    }

}
