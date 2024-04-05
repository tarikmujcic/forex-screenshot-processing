package org.example.service;

import junit.framework.TestCase;

public class ScreenshotServiceTest extends TestCase {

    private static final String SCREENSHOT_PATH = "D:\\Desktop\\ImagesBefore";

    public void testTakeScreenshot() {
        ScreenshotService.takeScreenshot(SCREENSHOT_PATH);
    }
}