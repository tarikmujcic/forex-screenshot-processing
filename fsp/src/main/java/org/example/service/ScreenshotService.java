package org.example.service;

import org.example.enums.ForexChartType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScreenshotService {


    private static final int CAPTURE_X = 0; // x-coordinate of the top-left corner of the capture region
    private static final int CAPTURE_Y = 45; // y-coordinate of the top-left corner of the capture region
    private static final int CAPTURE_WIDTH = 1920; // Width of the capture region
    private static final int CAPTURE_HEIGHT = 960; // Height of the capture region

    private static final String SCREENSHOT_FILE_NAME = "window_capture.png";

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
            if (forexChartType == ForexChartType.HOURLY) {
                ImageDrawingService.drawHourlyInfo(imageFile, targetDirectoryPath);
            } else if (forexChartType == ForexChartType.DAILY) {
                ImageDrawingService.drawDailyInfo(imageFile, targetDirectoryPath);
            } else if (forexChartType == ForexChartType.WEEKLY) {
                ImageDrawingService.drawWeeklyInfo(imageFile, targetDirectoryPath);
            }
        }
        DateFileService.determineAndWriteNextDate(forexChartType);
    }
}
