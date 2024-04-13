package org.example.comparators;

import junit.framework.TestCase;

import java.io.IOException;

public class ImageComparatorTest extends TestCase {

    public static String PATH = "C:\\US30\\Before";

    public void testAreImagesSame() throws IOException {
        System.out.println(ImageComparator.calculateImageSimilarity(PATH + "\\test.png", PATH + "\\window_capture.png"));
    }
}