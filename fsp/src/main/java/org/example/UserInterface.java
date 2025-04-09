package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import org.example.enums.ForexChartType;
import org.example.service.ScreenshotService;
import com.toedter.calendar.JCalendar;

public class UserInterface extends JFrame {

    // Holds the date picked by the user.
    private LocalDate pickedDate = App.LATEST_DATE;
    private final JLabel selectedDateLabel;

    // Currency list items.
    private static final String[] CURRENCY_CODES = {
            "NASUSD", "OIL", "U30USD", "SPXUSD", "GOLD", "EURUSD", "USDCAD", "GBPUSD", "AUDUSD", "USDJPY", "SILVER"
    };

    // Currency radio buttons and group.
    private final JRadioButton[] currencyRadioButtons;
    private final ButtonGroup currencyButtonGroup;

    private final JRadioButton mondayButton;
    private final JRadioButton tuesdayButton;
    private final JRadioButton wednesdayButton;
    private final JRadioButton thursdayButton;
    private final JRadioButton fridayButton;

    public UserInterface() {
        setTitle("Forex Screenshot UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 350);

        // Currency selection panel.
        JPanel currencyPanel = new JPanel();
        JLabel currencyLabel = new JLabel("Select Currency: ");
        currencyPanel.add(currencyLabel);

        // Create radio buttons for each currency.
        currencyRadioButtons = new JRadioButton[CURRENCY_CODES.length];
        currencyButtonGroup = new ButtonGroup();
        JPanel currencyRadioPanel = new JPanel(new GridLayout(0, 3)); // 3 columns layout
        for (int i = 0; i < CURRENCY_CODES.length; i++) {
            String code = CURRENCY_CODES[i];
            JRadioButton rb = new JRadioButton(code);
            currencyRadioButtons[i] = rb;
            currencyButtonGroup.add(rb);
            currencyRadioPanel.add(rb);
            // Select default currency ("GOLD")
            if (code.equals(App.FOREX_CURRENCY_CODE)) {
                rb.setSelected(true);
            }
        }
        currencyPanel.add(currencyRadioPanel);

        // Copy Currency button.
        JButton copyCurrencyButton = new JButton("Copy Currency");
        copyCurrencyButton.addActionListener(e -> {
            String selectedCurrency = getSelectedCurrency();
            if (selectedCurrency != null) {
                StringSelection selection = new StringSelection(selectedCurrency);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
            }
        });
        currencyPanel.add(copyCurrencyButton);

        // Create buttons for Daily Latest, Five Minute Latest, Pick Date, and Yesterday.
        JButton dailyLatestButton = new JButton("Daily Latest");
        JButton fiveMinuteLatestButton = new JButton("Five Minute Latest");
        JButton pickDateButton = new JButton("Pick Date");
        JButton yesterdayButton = new JButton("Yesterday");

        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.add(dailyLatestButton);
        actionButtonPanel.add(fiveMinuteLatestButton);
        actionButtonPanel.add(pickDateButton);
        actionButtonPanel.add(yesterdayButton);

        // Label to display the selected date.
        selectedDateLabel = new JLabel("Selected Date: " + formatDate(pickedDate));

        // Copy Date button.
        JButton copyDateButton = new JButton("Copy Date");
        copyDateButton.addActionListener(e -> {
            String dateText = selectedDateLabel.getText().replace("Selected Date: ", "");
            StringSelection selection = new StringSelection(dateText);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        });

        // Panel to hold the date label and copy date button.
        JPanel datePanel = new JPanel();
        datePanel.add(selectedDateLabel);
        datePanel.add(copyDateButton);

        // Weekday selector panel with radio buttons and Back/Forward buttons.
        JPanel weekdayPanel = new JPanel(new FlowLayout());
        mondayButton = new JRadioButton("Monday");
        tuesdayButton = new JRadioButton("Tuesday");
        wednesdayButton = new JRadioButton("Wednesday");
        thursdayButton = new JRadioButton("Thursday");
        fridayButton = new JRadioButton("Friday");

        ButtonGroup weekdayButtonGroup = new ButtonGroup();
        weekdayButtonGroup.add(mondayButton);
        weekdayButtonGroup.add(tuesdayButton);
        weekdayButtonGroup.add(wednesdayButton);
        weekdayButtonGroup.add(thursdayButton);
        weekdayButtonGroup.add(fridayButton);

        weekdayPanel.add(mondayButton);
        weekdayPanel.add(tuesdayButton);
        weekdayPanel.add(wednesdayButton);
        weekdayPanel.add(thursdayButton);
        weekdayPanel.add(fridayButton);

        JButton backButton = new JButton("Back");
        JButton forwardButton = new JButton("Forward");
        weekdayPanel.add(backButton);
        weekdayPanel.add(forwardButton);

        // Initialize UI selection and synchronize with App.LATEST_DATE.
        updateDateLabelAndWeekdaySelection();

        // Bottom panel combining weekday selector and date panel.
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(weekdayPanel);
        bottomPanel.add(datePanel);

        // Set main layout.
        setLayout(new BorderLayout());
        add(currencyPanel, BorderLayout.NORTH);
        add(actionButtonPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action for Pick Date button.
        pickDateButton.addActionListener(e -> pickDate());

        // Action for Daily Latest button.
        dailyLatestButton.addActionListener(e -> processDailyLatest());

        // Action for Five Minute Latest button.
        fiveMinuteLatestButton.addActionListener(e -> processFiveMinuteLatest());

        // Action for Yesterday button using today's date as reference.
        yesterdayButton.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            DayOfWeek todayDow = today.getDayOfWeek();
            if (todayDow == DayOfWeek.MONDAY) {
                pickedDate = today.minusDays(3);
            } else if (todayDow == DayOfWeek.SATURDAY) {
                pickedDate = today.minusDays(1);
            } else if (todayDow == DayOfWeek.SUNDAY) {
                pickedDate = today.minusDays(2);
            } else {
                pickedDate = today.minusDays(1);
            }
            updateDateLabelAndWeekdaySelection();
        });

        // Radio button actions update the picked date relative to today's baseline.
        mondayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.MONDAY));
        tuesdayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.TUESDAY));
        wednesdayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.WEDNESDAY));
        thursdayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.THURSDAY));
        fridayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.FRIDAY));

        // Back and Forward buttons adjust the picked date by one week.
        backButton.addActionListener(e -> {
            pickedDate = pickedDate.minusWeeks(1);
            updateDateLabelAndWeekdaySelection();
        });
        forwardButton.addActionListener(e -> {
            pickedDate = pickedDate.plusWeeks(1);
            updateDateLabelAndWeekdaySelection();
        });
    }

    // Helper method to get the selected currency from radio buttons.
    private String getSelectedCurrency() {
        for (JRadioButton rb : currencyRadioButtons) {
            if (rb.isSelected()) {
                return rb.getText();
            }
        }
        return null;
    }

    // Updates the picked date when a weekday radio button is selected.
    // The computation is done relative to today's baseline.
    private void updatePickedDateForDay(DayOfWeek day) {
        LocalDate baseline = getBaseline();
        if (baseline.getDayOfWeek().getValue() <= day.getValue()) {
            pickedDate = baseline.with(TemporalAdjusters.nextOrSame(day));
        } else {
            pickedDate = baseline.with(TemporalAdjusters.previous(day));
        }
        updateDateLabelAndWeekdaySelection();
    }

    // Helper method: returns a baseline date based on today.
    // If today is Saturday or Sunday, returns the previous Friday; otherwise, returns today.
    private LocalDate getBaseline() {
        LocalDate today = LocalDate.now();
        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return today.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        } else {
            return today;
        }
    }

    // Opens a dialog with a JCalendar for the user to pick a date.
    private void pickDate() {
        JDialog dialog = new JDialog(this, "Select Date", true);
        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JCalendar calendar = new JCalendar();
        dialog.add(calendar, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        JButton okButton = new JButton("OK");
        panel.add(okButton);
        dialog.add(panel, BorderLayout.SOUTH);

        okButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);

        Date selectedUtilDate = calendar.getDate();
        pickedDate = selectedUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        adjustPickedDateIfWeekend();
        updateDateLabelAndWeekdaySelection();
    }

    // Adjusts pickedDate if it falls on a weekend.
    private void adjustPickedDateIfWeekend() {
        DayOfWeek dow = pickedDate.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) {
            pickedDate = pickedDate.minusDays(1); // Set to Friday.
        } else if (dow == DayOfWeek.SUNDAY) {
            pickedDate = pickedDate.plusDays(1); // Set to Monday.
        }
    }

    // Updates the date label, radio button selection, and synchronizes App.LATEST_DATE.
    private void updateDateLabelAndWeekdaySelection() {
        adjustPickedDateIfWeekend();
        selectedDateLabel.setText("Selected Date: " + formatDate(pickedDate));
        // Synchronize the static App.LATEST_DATE with the UI-picked date.
        App.LATEST_DATE = pickedDate;
        DayOfWeek dow = pickedDate.getDayOfWeek();
        switch (dow) {
            case MONDAY:
                mondayButton.setSelected(true);
                break;
            case TUESDAY:
                tuesdayButton.setSelected(true);
                break;
            case WEDNESDAY:
                wednesdayButton.setSelected(true);
                break;
            case THURSDAY:
                thursdayButton.setSelected(true);
                break;
            case FRIDAY:
                fridayButton.setSelected(true);
                break;
            default:
                break;
        }
    }

    // Formats a LocalDate as "MM-dd-yyyy EEEE".
    private String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy EEEE");
        return formatter.format(date);
    }

    // Processes the Daily Latest screenshot using the picked date (synchronized with App.LATEST_DATE) and selected currency.
    private void processDailyLatest() {
        App.forexChartType = ForexChartType.DAILY_LATEST;
        String selectedCurrency = getSelectedCurrency();
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH, App.TARGET_DIRECTORY_PATH, selectedCurrency);
    }

    // Processes the Five Minute Latest screenshot using the selected currency.
    private void processFiveMinuteLatest() {
        App.forexChartType = ForexChartType.FIVE_MIN_LATEST;
        String selectedCurrency = getSelectedCurrency();
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH, App.TARGET_DIRECTORY_PATH, selectedCurrency);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserInterface ui = new UserInterface();
            // Determine the leftmost monitor.
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            GraphicsDevice leftMost = screens[0];
            for (GraphicsDevice screen : screens) {
                if (screen.getDefaultConfiguration().getBounds().x < leftMost.getDefaultConfiguration().getBounds().x) {
                    leftMost = screen;
                }
            }
            // Get the leftmost monitor's bounds.
            Rectangle bounds = leftMost.getDefaultConfiguration().getBounds();
            int x = bounds.x + (bounds.width - ui.getWidth()) / 2;
            int y = bounds.y + (bounds.height - ui.getHeight()) / 2;
            ui.setLocation(x, y);
            ui.setVisible(true);
        });
    }
}
