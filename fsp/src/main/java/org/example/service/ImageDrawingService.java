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

public class ImageDrawingService {

    private static final String HEADER_TEXT = "H1";

    public static String UNPROCESSED_DIRECTORY_PATH = "D:\\Desktop\\ImagesAfter";

    // HOURLY COORDINATES
    private static final String LABEL_9_10 = "9-10";
    private static final int LINE_9_10_X_COORDINATE = 200;
    private static final int LINE_9_10_Y_COORDINATE = 450;
    private static final int LINE_9_10_LENGTH = 300;

    // DAILY COORDINATES

    // WEEKLY COORDINATES

    public static void drawDayDate5Times(File imageFile, String targetDirectoryPath) {
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
            int line_x = LINE_9_10_X_COORDINATE;

            LocalDate currentDate = DateFileService.determineStartDate();
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
                g2d.drawLine(line_x, LINE_9_10_Y_COORDINATE, line_x, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH);
                g2d.drawString(LABEL_9_10, line_x - 20, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH + 20);
                x += (imageWidth / 5);
                line_x += (imageWidth / 5);

                currentDate = currentDate.plusDays(1);
                while (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        currentDate.getDayOfWeek() == DayOfWeek.SUNDAY ||
                        DateFileService.forexOffDays.contains(currentDate)) {
                    currentDate = currentDate.plusDays(1);
                }
            }

            g2d.dispose(); // Dispose the graphics object

            // Save the modified image to the output directory
            File outputFile = new File(targetDirectoryPath + File.separator + DateFileService.determineFileName());
            ImageIO.write(image, "png", outputFile);
            imageFile.delete();
        } catch (IOException e) {
            System.out.println("Error processing image: " + imageFile.getName());
        }
    }

    public static void updateUnprocessedDirectoryWith9to10Lines() {
        File sourceDirectory = new File(UNPROCESSED_DIRECTORY_PATH);
        File[] imageFiles = sourceDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png"));
        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No image files found in the input directory.");
            return;
        }

        for (File imageFile : imageFiles) {
            try {
                BufferedImage image = ImageIO.read(imageFile);
                Graphics2D g2d = image.createGraphics();

                Font font = new Font("Arial", Font.BOLD, 24);
                g2d.setFont(font);
                g2d.setColor(Color.BLACK);

                int imageWidth = image.getWidth();
                int line_x = LINE_9_10_X_COORDINATE;

                for (int i = 0; i < 5; i++) {
                    g2d.drawLine(line_x, LINE_9_10_Y_COORDINATE, line_x, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH);
                    g2d.drawString(LABEL_9_10, line_x - 20, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH + 20);
                    line_x += (imageWidth / 5);
                }

                g2d.dispose();
                ImageIO.write(image, "png", imageFile);
            } catch (IOException e) {
                System.out.println("Error while updating unprocessed directory and the following image: " + imageFile.getName());
            }

        }
    }
}
