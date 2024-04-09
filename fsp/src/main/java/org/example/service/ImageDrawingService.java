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

    public static String UNPROCESSED_DIRECTORY_PATH = "C:\\US30\\After";

    // HOURLY COORDINATES
    private static final String LABEL_9_10 = "9-10";
    private static final int LINE_9_10_X_COORDINATE = 200;
    private static final int LINE_9_10_Y_COORDINATE = 100;
    private static final int LINE_9_10_LENGTH = 600;

    // DAILY COORDINATES

    // WEEKLY COORDINATES

    /**
     * Be careful when using this method. IT DELETES THE SOURCE IMAGE!
     * <p>
     * Creates a new image from the source and draws day and date for each column and adds the lines for 9to10 candle, so it's easily distinguished.
     * @param sourceImageFile Image source file that will be edited
     * @param targetDirectoryPath Path where the new file will be saved
     */
    public static void drawHourly23Info(File sourceImageFile, String targetDirectoryPath) {
        try {
            BufferedImage image = ImageIO.read(sourceImageFile);
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
                draw9to10Line(g2d, imageWidth, i);
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
            File outputFile = new File(targetDirectoryPath + File.separator + DateFileService.determineFileName());
            ImageIO.write(image, "png", outputFile);
            sourceImageFile.delete();
        } catch (IOException e) {
            System.out.println("Error processing image: " + sourceImageFile.getName());
        }
    }

    /**
     * Be careful when using this method. IT DELETES THE SOURCE IMAGE!
     * <p>
     * Creates a new image from the source and draws day and date for the current date.
     * @param sourceImageFile Image source file that will be edited
     * @param targetDirectoryPath Path where the new file will be saved
     */
    public static void drawDailyInfo(File sourceImageFile, String targetDirectoryPath) {
        try {
            BufferedImage image = ImageIO.read(sourceImageFile);
            // Create a graphics object to draw on the image
            Graphics2D g2d = image.createGraphics();

            // Define font and color for drawing days of the week
            Font font = new Font("Arial", Font.BOLD, 24);
            g2d.setFont(font);
            g2d.setColor(Color.BLACK);

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int x = imageWidth / 9 * 8;
            int y = imageHeight / 9 * 8;
            LocalDate currentDate = DateFileService.getDateFromFile();
            assert currentDate != null;
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
            String date = currentDate.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));

            g2d.drawString(dayOfWeek, x, y);
            g2d.drawString(date, x, y + 25);

            currentDate = currentDate.plusDays(1);
            while (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    currentDate.getDayOfWeek() == DayOfWeek.SUNDAY ||
                    DateFileService.forexOffDays.contains(currentDate)) {
                currentDate = currentDate.plusDays(1);
            }

            g2d.dispose();
            File outputFile = new File(targetDirectoryPath + File.separator + "Daily-" + date + ".png");
            ImageIO.write(image, "png", outputFile);
            sourceImageFile.delete();
        } catch (IOException e) {
            System.out.println("Error processing image: " + sourceImageFile.getName());
        }
    }

    /**
     * Be careful when using this method. IT DELETES THE SOURCE IMAGE!
     * <p>
     * Creates a new image from the source and draws week start-end dates for where the end date will be the one found in the current-date.txt file.
     * @param sourceImageFile Image source file that will be edited
     * @param targetDirectoryPath Path where the new file will be saved
     */
    public static void drawWeeklyInfo(File sourceImageFile, String targetDirectoryPath) {
        try {
            BufferedImage image = ImageIO.read(sourceImageFile);
            // Create a graphics object to draw on the image
            Graphics2D g2d = image.createGraphics();

            // Define font and color for drawing days of the week
            Font font = new Font("Arial", Font.BOLD, 24);
            g2d.setFont(font);
            g2d.setColor(Color.BLACK);

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int x = imageWidth / 9 * 8;
            int y = imageHeight / 9 * 8;
            LocalDate startDate = DateFileService.determineStartDate();
            LocalDate currentDate = DateFileService.getDateFromFile();
            assert currentDate != null;
            String startDateFormatted = startDate.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
            String currentDateFormatted = currentDate.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));

            g2d.drawString(startDateFormatted, x, y);
            g2d.drawString("to", x + 50, y + 25);
            g2d.drawString(currentDateFormatted, x, y + 50);

            currentDate = currentDate.plusDays(1);
            while (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    currentDate.getDayOfWeek() == DayOfWeek.SUNDAY ||
                    DateFileService.forexOffDays.contains(currentDate)) {
                currentDate = currentDate.plusDays(1);
            }

            g2d.dispose();
            File outputFile = new File(targetDirectoryPath + File.separator + "Weekly-" + startDate + "to" + currentDate + ".png");
            ImageIO.write(image, "png", outputFile);
            sourceImageFile.delete();
        } catch (IOException e) {
            System.out.println("Error processing image: " + sourceImageFile.getName());
        }
    }

    /**
     * Help method which updates the old screenshots with 9to10 line
     */
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

                for (int i = 0; i < 5; i++) {
                    draw9to10Line(g2d, image.getWidth(), i);
                }

                g2d.dispose();
                ImageIO.write(image, "png", imageFile);
            } catch (IOException e) {
                System.out.println("Error while updating unprocessed directory and the following image: " + imageFile.getName());
            }

        }
    }

    public static void draw9to10Line(Graphics2D g2d, int imageWidth, int index) {
        if (index == 0) {
            int line_x_1 = LINE_9_10_X_COORDINATE + 62;
            g2d.drawLine(line_x_1, LINE_9_10_Y_COORDINATE, line_x_1, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH);
            g2d.drawString(LABEL_9_10, line_x_1 - 20, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH + 20);
        } else if (index == 1) {
            int line_x_2 = LINE_9_10_X_COORDINATE + (imageWidth / 5) + 46;
            g2d.drawLine(line_x_2, LINE_9_10_Y_COORDINATE, line_x_2, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH);
            g2d.drawString(LABEL_9_10, line_x_2 - 20, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH + 20);
        } else if (index == 2) {
            int line_x_3 = LINE_9_10_X_COORDINATE + 2*(imageWidth / 5) + 30;
            g2d.drawLine(line_x_3, LINE_9_10_Y_COORDINATE, line_x_3, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH);
            g2d.drawString(LABEL_9_10, line_x_3 - 20, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH + 20);
        } else if (index == 3) {
            int line_x_4 = LINE_9_10_X_COORDINATE + 3*(imageWidth / 5) + 14;
            g2d.drawLine(line_x_4, LINE_9_10_Y_COORDINATE, line_x_4, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH);
            g2d.drawString(LABEL_9_10, line_x_4 - 20, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH + 20);
        } else if (index == 4) {
            int line_x_5 = LINE_9_10_X_COORDINATE + 4*(imageWidth / 5);
            g2d.drawLine(line_x_5, LINE_9_10_Y_COORDINATE, line_x_5, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH);
            g2d.drawString(LABEL_9_10, line_x_5 - 20, LINE_9_10_Y_COORDINATE + LINE_9_10_LENGTH + 20);
        }
    }
}
