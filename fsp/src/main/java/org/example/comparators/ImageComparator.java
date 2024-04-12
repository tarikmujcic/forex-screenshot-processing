package org.example.comparators;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageComparator {

    public static boolean areImagesSimilar(String image1Path, String image2Path) {
        try {
            double similarity = calculateImageSimilarity(image1Path, image2Path);
            return similarity > 0.99;
        } catch (IOException e) {
            System.err.println("Error reading image files: " + e.getMessage());
        }
        return false;
    }

    private static double calculateImageSimilarity(String imagePath1, String imagePath2) throws IOException {
        BufferedImage img1 = ImageIO.read(new File(imagePath1));
        BufferedImage img2 = ImageIO.read(new File(imagePath2));

        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            throw new IllegalArgumentException("Images must have the same dimensions.");
        }

        int width = img1.getWidth();
        int height = img1.getHeight();

        long diff = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                diff += pixelDiff(rgb1, rgb2);
            }
        }

        double maxDiff = 3L * 255 * width * height;

        return 1.0 - (double) diff / maxDiff;
    }

    public static int pixelDiff(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >> 8) & 0xff;
        int b1 = rgb1 & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >> 8) & 0xff;
        int b2 = rgb2 & 0xff;
        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }
}

