
package org.example;

import org.example.comparators.ImageComparator;
import org.example.enums.ForexChartType;
import org.example.service.DateFileService;
import org.example.service.FocusedAppCheckerService;
import org.example.service.InstanceCounterService;
import org.example.service.KeyListenerService;
import org.example.service.KeyPressSimulationService;
import org.example.service.ScreenshotService;
import org.example.service.ImageDrawingService;

import java.awt.event.KeyEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final String SOURCE_DIRECTORY_PATH = "C:\\US30\\Before";
    private static final String TARGET_DIRECTORY_PATH = "C:\\US30\\After";

    private static final String TEST_SCREENSHOT_PATH = ScreenshotService.createFolderInPath("C:\\US30", "Check");

    public static LocalDate START_DATE;
    public static LocalDate TODAY = LocalDate.now();

    public static final boolean USE_CUSTOM_Y_COORDINATES = true;

    /**
     * VALUES USED FOR THE Y COORDINATE ARE 1,2 and 3 ! ! !
     */
    public static final int[] CUSTOM_Y_COORDINATES = {3, 3, 3, 3, 3};

    public static boolean IS_FULLY_AUTOMATED = true;
    public static boolean IS_TRIGGER_KEY_PRESSED = false;

    public static ForexChartType forexChartType = ForexChartType.HOURLY_23_LATEST;

    public static final List<String> FOREX_CURRENCY_CODE_LIST = new ArrayList<>(
            List.of("U30USD", "SPXUSD", "NASUSD", "XAUUSD", "USOUSD", "EURUSD", "USDCAD", "GBPUSD", "AUDUSD", "USDJPY"));


    /**
     * Used for the ForexChartType.DAILY_LATEST to Highlight which day is being screenshotted.
     * It takes today's date by default, but you can change it with e.g LocalDate.now().plusDays(1) or .minusDays(1)
     */
    public static LocalDate LATEST_DATE = LocalDate.now();

    public static void main(String[] args) throws InterruptedException {
        ScreenshotService.createFolderInPath(TARGET_DIRECTORY_PATH, "Debug");
        KeyListenerService.initializeGlobalKeyListener();
        InstanceCounterService.initializeInstanceCounters();
        Thread.sleep(5000); // Wait for 5s at the start
        START_DATE = DateFileService.getDateFromFile();

        // Handle special case of ForexChartType.DAILY_LATEST
        if (forexChartType == ForexChartType.DAILY_LATEST || forexChartType == ForexChartType.HOURLY_23_LATEST) {
            for (String currencyCode : FOREX_CURRENCY_CODE_LIST) {
                System.out.println("Hit B key to process the screenshot for the currency: " + currencyCode + " and date: " + ImageDrawingService.DEFAULT_FORMATTER.format(LATEST_DATE));
                while (!IS_TRIGGER_KEY_PRESSED) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                ScreenshotService.takeScreenshot(SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
                ScreenshotService.processScreenshot(forexChartType, SOURCE_DIRECTORY_PATH, TARGET_DIRECTORY_PATH, currencyCode);
                IS_TRIGGER_KEY_PRESSED = false;
            }
            System.out.println("Execution of ForexChartType.DAILY_LATEST completed successfully.");
            return; // exit out of the application
        }

        if (START_DATE == null) {
            throw new RuntimeException("Start date is null - make sure that current-date.txt contains a valid date.");
        }
        while (START_DATE.isBefore(TODAY)) {
            if (!IS_FULLY_AUTOMATED) {
                System.out.println("Hit B key to process the screenshot");
                while (!IS_TRIGGER_KEY_PRESSED) {
                    try {
                        Thread.sleep(100); // Check every 100 milliseconds
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (FocusedAppCheckerService.isTraderAppFocused()) {
                ScreenshotService.takeScreenshot(SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
                ScreenshotService.processScreenshot(forexChartType, SOURCE_DIRECTORY_PATH, TARGET_DIRECTORY_PATH);
                IS_TRIGGER_KEY_PRESSED = false;

                int numberOfPresses = forexChartType == ForexChartType.HOURLY_23 ? DateFileService.getForexHoursForDate(START_DATE.plusDays(1)) : 1;
                while (!FocusedAppCheckerService.isTraderAppFocused()) {
                    Thread.sleep(500);
                }
                KeyPressSimulationService.sendKeyPressToSpecificWindow(FocusedAppCheckerService.APPLICATION_NAME, KeyEvent.VK_F12, numberOfPresses);
                if (forexChartType == ForexChartType.HOURLY_1 || forexChartType == ForexChartType.FIVE_MIN) {
                    simulateF12UntilChartIsMoved();
                }

                Thread.sleep(100); // Delay after F12
            } else {
                System.out.println("Trader Application not focused.");
                Thread.sleep(1000);
            }
        }
        System.out.println("SCREENSHOT PROCESSING COMPLETED SUCCESSFULLY.");
    }

    private static void simulateF12UntilChartIsMoved() {
        File screenshot = ScreenshotService.takeScreenshot(TEST_SCREENSHOT_PATH, "test.png");
        while (ImageComparator.areImagesSame(screenshot.getPath(), ScreenshotService.LAST_IMAGE_PATH)) {
            KeyPressSimulationService.sendKeyPressToSpecificWindow(FocusedAppCheckerService.APPLICATION_NAME, KeyEvent.VK_F12, 1);
            screenshot = ScreenshotService.takeScreenshot(TEST_SCREENSHOT_PATH, "test.png");
        }
    }
}