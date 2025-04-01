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

    // Holds the date picked by the user; defaults to the current LATEST_DATE from App.
    private LocalDate pickedDate = App.LATEST_DATE;
    private final JLabel selectedDateLabel;
    private final JComboBox<String> currencyComboBox;

    // Currency list items.
    private static final String[] CURRENCY_CODES = {
            "NASUSD", "OIL", "U30USD", "SPXUSD", "GOLD", "EURUSD", "USDCAD", "GBPUSD", "AUDUSD", "USDJPY", "SILVER"
    };

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
        currencyComboBox = new JComboBox<>(CURRENCY_CODES);
        currencyComboBox.setSelectedItem(App.FOREX_CURRENCY_CODE); // default "GOLD"
        currencyPanel.add(currencyLabel);
        currencyPanel.add(currencyComboBox);

        // Copy Currency button.
        JButton copyCurrencyButton = new JButton("Copy Currency");
        copyCurrencyButton.addActionListener(e -> {
            String currencyText = (String) currencyComboBox.getSelectedItem();
            StringSelection selection = new StringSelection(currencyText);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        });
        currencyPanel.add(copyCurrencyButton);

        // Create buttons for Daily Latest, Five Minute Latest, and Pick Date.
        JButton dailyLatestButton = new JButton("Daily Latest");
        JButton fiveMinuteLatestButton = new JButton("Five Minute Latest");
        JButton pickDateButton = new JButton("Pick Date");

        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.add(dailyLatestButton);
        actionButtonPanel.add(fiveMinuteLatestButton);
        actionButtonPanel.add(pickDateButton);

        // Label to display the selected date.
        selectedDateLabel = new JLabel("Selected Date: " + formatDate(pickedDate));

        // Copy button to copy the date to the clipboard.
        JButton copyDateButton = new JButton("Copy Date");
        copyDateButton.addActionListener(e -> {
            // Remove the "Selected Date: " prefix to copy only the date string.
            String dateText = selectedDateLabel.getText().replace("Selected Date: ", "");
            StringSelection selection = new StringSelection(dateText);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        });

        // Panel to hold the date label and copy date button.
        JPanel datePanel = new JPanel();
        datePanel.add(selectedDateLabel);
        datePanel.add(copyDateButton);

        // Weekday selector panel (Monday to Friday) with Back and Forward buttons.
        JPanel weekdayPanel = new JPanel(new FlowLayout());

        mondayButton = new JRadioButton("Monday");
        tuesdayButton = new JRadioButton("Tuesday");
        wednesdayButton = new JRadioButton("Wednesday");
        thursdayButton = new JRadioButton("Thursday");
        fridayButton = new JRadioButton("Friday");

        // Weekday selector components.
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

        // Set the initial weekday selection based on pickedDate.
        updateDateLabelAndWeekdaySelection();

        // Panel to combine weekday selectors and date panel.
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

        // Action listeners for weekday radio buttons.
        mondayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.MONDAY));
        tuesdayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.TUESDAY));
        wednesdayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.WEDNESDAY));
        thursdayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.THURSDAY));
        fridayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.FRIDAY));

        // Action for Back button: go one week back.
        backButton.addActionListener(e -> {
            pickedDate = pickedDate.minusWeeks(1);
            updateDateLabelAndWeekdaySelection();
        });

        // Action for Forward button: go one week forward.
        forwardButton.addActionListener(e -> {
            pickedDate = pickedDate.plusWeeks(1);
            updateDateLabelAndWeekdaySelection();
        });
    }

    // Opens a dialog with a JCalendar to let the user pick a date.
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

        // Convert the selected Date to LocalDate.
        Date selectedUtilDate = calendar.getDate();
        pickedDate = selectedUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // Ensure pickedDate is a weekday.
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

    // Updates pickedDate based on the selected weekday from the radio buttons.
    private void updatePickedDateForDay(DayOfWeek day) {
        // Adjust pickedDate to the next or same occurrence of the selected day.
        pickedDate = pickedDate.with(TemporalAdjusters.nextOrSame(day));
        updateDateLabelAndWeekdaySelection();
    }

    // Updates the date label and radio button selection based on pickedDate.
    private void updateDateLabelAndWeekdaySelection() {
        adjustPickedDateIfWeekend();
        selectedDateLabel.setText("Selected Date: " + formatDate(pickedDate));
        DayOfWeek dow = pickedDate.getDayOfWeek();
        switch(dow) {
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

    // Processes the Daily Latest screenshot using the picked date and selected currency.
    private void processDailyLatest() {
        App.forexChartType = ForexChartType.DAILY_LATEST;
        if (pickedDate != null) {
            App.LATEST_DATE = pickedDate;
        }
        String selectedCurrency = (String) currencyComboBox.getSelectedItem();
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH, App.TARGET_DIRECTORY_PATH, selectedCurrency);
    }

    // Processes the Five Minute Latest screenshot using the selected currency.
    private void processFiveMinuteLatest() {
        App.forexChartType = ForexChartType.FIVE_MIN_LATEST;
        String selectedCurrency = (String) currencyComboBox.getSelectedItem();
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
                if (screen.getDefaultConfiguration().getBounds().x <
                        leftMost.getDefaultConfiguration().getBounds().x) {
                    leftMost = screen;
                }
            }
            // Get the leftmost monitor's bounds.
            Rectangle bounds = leftMost.getDefaultConfiguration().getBounds();
            // Calculate the center position of the leftmost monitor.
            int x = bounds.x + (bounds.width - ui.getWidth()) / 2;
            int y = bounds.y + (bounds.height - ui.getHeight()) / 2;
            ui.setLocation(x, y);
            ui.setVisible(true);
        });
    }
}
