package org.example.service;

import junit.framework.TestCase;
import org.example.enums.ForexChartType;

import java.io.File;

public class ImageDrawingServiceTest extends TestCase {

    private final String DIRECTORY_PATH = "D:\\Desktop\\ImagesBefore";

    public void testUpdateUnprocessedDirectoryWith9to10Lines() {
        ImageDrawingService.UNPROCESSED_DIRECTORY_PATH = DIRECTORY_PATH;
        ImageDrawingService.updateUnprocessedDirectoryWith9to10Lines();
    }

    public void testDrawHourlyInfo() {
    }

    public void testDrawDailyInfo() {
        File file = ScreenshotService.takeScreenshot(DIRECTORY_PATH);
        ImageDrawingService.drawDailyInfo(file, DIRECTORY_PATH);
    }

    public void testDrawWeeklyInfo() {
        File file = ScreenshotService.takeScreenshot(DIRECTORY_PATH);
        ImageDrawingService.drawWeeklyInfo(file, DIRECTORY_PATH);
        DateFileService.determineAndWriteNextDate(ForexChartType.WEEKLY);
    }
}