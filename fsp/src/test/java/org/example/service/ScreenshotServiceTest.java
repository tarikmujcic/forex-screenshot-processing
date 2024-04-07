package org.example.service;

import junit.framework.TestCase;
import org.example.enums.ForexChartType;

public class ScreenshotServiceTest extends TestCase {

    private static final String SCREENSHOT_PATH = "D:\\Desktop\\ImagesBefore";

    private static final String SOURCE_DIRECTORY_PATH = "D:\\Desktop\\ImagesBefore";
    private static final String TARGET_DIRECTORY_PATH = "D:\\Desktop\\ImagesAfter";

    public void testTakeScreenshot() {
        ScreenshotService.takeScreenshot(SCREENSHOT_PATH);
    }

    public void testProcessScreenshotHourly() {
        ScreenshotService.processScreenshot(ForexChartType.HOURLY, SOURCE_DIRECTORY_PATH, TARGET_DIRECTORY_PATH);
    }

    public void testTakeAndProcessScreenshotHourly() {
        testTakeScreenshot();
        testProcessScreenshotHourly();
    }
}