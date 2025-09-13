package org.example.service;

import org.example.App;
import org.example.enums.ForexChartType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class TelegramBotListener {

    private static final String BOT_TOKEN = "7632061028:AAGw3acnUNeWGXBJNMgAuDYcGbMONmY6CIw";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final DateTimeFormatter TELEGRAM_DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    public static Future<?> start() {
        System.out.println("[Telegram] Listener started.");
        return executor.submit(() -> {
            int offset = 0;

            while (true) {
                try {
                    String urlStr = "https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates?timeout=20&offset=" + offset;
                    System.out.println("[Telegram] Polling: " + urlStr);

                    HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    conn.disconnect();

                    String responseText = response.toString();
                    System.out.println("[Telegram] Response: " + responseText);

                    if (responseText.contains("\"update_id\":")) {
                        String[] updates = responseText.split("\\{\"update_id\":");

                        for (int i = 1; i < updates.length; i++) {
                            String updateBlock = updates[i];
                            int updateId = Integer.parseInt(updateBlock.split(",")[0]);
                            offset = updateId + 1;

                            if (updateBlock.contains("\"caption\":\"")) {
                                String caption = updateBlock.split("\"caption\":\"")[1].split("\"")[0].trim();
                                System.out.println("[Telegram] New caption: " + caption);
                                processMessage(caption);
                            } else if (updateBlock.contains("\"text\":\"")) {
                                String message = updateBlock.split("\"text\":\"")[1].split("\"")[0].trim();
                                System.out.println("[Telegram] New text: " + message);
                                processMessage(message);
                            } else {
                                System.out.println("[Telegram] No text found in message.");
                            }
                        }
                    } else {
                        System.out.println("[Telegram] No updates found.");
                    }

                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("[Telegram] Polling failed: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
    }

    private static void processMessage(String message) {
        try {
            System.out.println("[Telegram] Processing message: " + message);

            // Example message: 04-20-2025 12:10:58 PM – SELL 2 NASUSD | Balance: 50,000.00
            String[] parts = message.split("–");
            if (parts.length < 2) {
                System.err.println("[Telegram] Invalid format. Message must contain '–'.");
                return;
            }

            String datePart = parts[0].trim().split(" ")[0]; // e.g., "04-20-2025"
            String[] tradeParts = parts[1].trim().split(" ");

            if (tradeParts.length < 3) {
                System.err.println("[Telegram] Cannot parse trade details from message.");
                return;
            }

            System.out.println("[Telegram] Extracted datePart: " + datePart);
            System.out.println("[Telegram] Extracted currency: " + tradeParts[2]);

            LocalDate date = LocalDate.parse(datePart, TELEGRAM_DATE_FORMAT);
            String currencyCode = tradeParts[2];

            App.LATEST_DATE = date;
            App.FOREX_CURRENCY_CODE = currencyCode;
            App.forexChartType = ForexChartType.FIVE_MIN_LATEST;

            System.out.println("[Telegram] Preparing to take screenshot for " + currencyCode + " @ " + date);

            ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
            System.out.println("[Telegram] Screenshot taken.");

            ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH, App.TARGET_DIRECTORY_PATH, currencyCode);
            System.out.println("[Telegram] Screenshot processed successfully.");

        } catch (Exception e) {
            System.err.println("[Telegram] Error in processMessage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
