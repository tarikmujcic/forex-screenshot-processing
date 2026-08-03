package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.io.IOException;
import java.text.DateFormatSymbols;
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

    // ────────────────────────────────────────────────
    // NEW: combos for Month / Day / Year
    // ────────────────────────────────────────────────
    private final JComboBox<String> monthCombo;
    private final JComboBox<Integer> dayCombo;
    private final JComboBox<Integer> yearCombo;

    // Currency list items.
    private static final String[] CURRENCY_CODES = {
            "NASUSD", "OIL", "U30USD", "SPXUSD", "GOLD",
            "EURUSD", "USDCAD", "GBPUSD", "AUDUSD", "USDJPY", "SILVER", "EURNZD", "AUDJPY","GBPAUD", "USDCHF",
            "EURJPY", "GBPJPY", "EURCAD", "BTCUSD"
    };

    private final JRadioButton[] currencyRadioButtons;
    private final ButtonGroup   currencyButtonGroup;

    private final JRadioButton mondayButton;
    private final JRadioButton tuesdayButton;
    private final JRadioButton wednesdayButton;
    private final JRadioButton thursdayButton;
    private final JRadioButton fridayButton;
    private final JButton addArrowButton = new JButton("Add Arrow");

    public UserInterface() {
        setTitle("Forex Screenshot UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        /*──────────────────────────────
         * Currency selection panel
         *──────────────────────────────*/
        JPanel currencyPanel  = new JPanel();
        JLabel currencyLabel  = new JLabel("Select Currency: ");
        currencyPanel.add(currencyLabel);

        currencyRadioButtons  = new JRadioButton[CURRENCY_CODES.length];
        currencyButtonGroup   = new ButtonGroup();
        JPanel currencyRadioPanel = new JPanel(new GridLayout(0, 3));

        for (int i = 0; i < CURRENCY_CODES.length; i++) {
            String code = CURRENCY_CODES[i];
            JRadioButton rb = new JRadioButton(code);
            currencyRadioButtons[i] = rb;
            currencyButtonGroup.add(rb);
            currencyRadioPanel.add(rb);
            if (code.equals(App.FOREX_CURRENCY_CODE)) {
                rb.setSelected(true);
            }
        }
        currencyPanel.add(currencyRadioPanel);

        JButton copyCurrencyButton = new JButton("Copy Currency");
        copyCurrencyButton.addActionListener(e -> {
            String selectedCurrency = getSelectedCurrency();
            if (selectedCurrency != null) {
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(selectedCurrency), null);
            }
        });
        currencyPanel.add(copyCurrencyButton);

        /*──────────────────────────────
         * Screenshot / date action buttons
         *──────────────────────────────*/
        JButton dailyLatestButton        = new JButton("Daily Latest");
        JButton fiveMinuteLatestButton   = new JButton("Five Minute Latest");
        JButton fifteenMinuteLatestButton= new JButton("Fifteen Minute Latest");
        JButton hourlyLatestButton       = new JButton("H1");
        JButton h4LatestButton           = new JButton("H4");
        JButton oneMinuteLatestButton    = new JButton("One Minute Latest");
        JButton pickDateButton           = new JButton("Pick Date");
        JButton yesterdayButton          = new JButton("Yesterday");
        JButton oneDayForwardButton      = new JButton("One Day Forward");
        JButton goBackOneDayButton       = new JButton("Go Back One Day");
        JButton weeklyLatest             = new JButton("Weekly (Mon-Fri)");


        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.add(dailyLatestButton);
        actionButtonPanel.add(fiveMinuteLatestButton);
        actionButtonPanel.add(hourlyLatestButton);
        actionButtonPanel.add(h4LatestButton);
        actionButtonPanel.add(fifteenMinuteLatestButton);
        actionButtonPanel.add(oneMinuteLatestButton);
        actionButtonPanel.add(pickDateButton);
        actionButtonPanel.add(yesterdayButton);
        actionButtonPanel.add(oneDayForwardButton);
        actionButtonPanel.add(goBackOneDayButton);
        actionButtonPanel.add(weeklyLatest);
        actionButtonPanel.add(addArrowButton);

        /*──────────────────────────────
         * Selected‑date label + copy
         *──────────────────────────────*/
        selectedDateLabel = new JLabel("Selected Date: " + formatDate(pickedDate));

        JButton copyDateButton = new JButton("Copy Date");
        copyDateButton.addActionListener(e -> {
            String dateText = selectedDateLabel.getText().replace("Selected Date: ", "");
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(dateText), null);
        });

        JPanel datePanel = new JPanel();
        datePanel.add(selectedDateLabel);
        datePanel.add(copyDateButton);

        /*──────────────────────────────
         * NEW: Month / Day / Year combos
         *──────────────────────────────*/
        JPanel mdYPanel = new JPanel(new FlowLayout());

        String[] months = new DateFormatSymbols().getMonths();
        monthCombo = new JComboBox<>();
        for (int m = 0; m < 12; m++) {
            monthCombo.addItem("(" + (m + 1) + ") " + months[m]);
        }


        dayCombo = new JComboBox<>();
        for (int d = 1; d <= 31; d++) dayCombo.addItem(d);

        yearCombo = new JComboBox<>();
        int thisYear = LocalDate.now().getYear();
        for (int y = thisYear - 10; y <= thisYear + 10; y++) yearCombo.addItem(y);

        // initialise combos with the current pickedDate
        monthCombo.setSelectedIndex(pickedDate.getMonthValue() - 1);
        dayCombo.setSelectedItem(pickedDate.getDayOfMonth());
        yearCombo.setSelectedItem(pickedDate.getYear());

        JButton selectDateButton = new JButton("Select");
        selectDateButton.addActionListener(e -> {
            int year  = (Integer) yearCombo.getSelectedItem();
            int month = monthCombo.getSelectedIndex() + 1;
            int day   = (Integer) dayCombo.getSelectedItem();

            // Guard against invalid (e.g., 31 Feb) – fallback to last valid day
            try {
                pickedDate = LocalDate.of(year, month, day);
            } catch (Exception ex) {
                pickedDate = LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());
            }

            adjustPickedDateIfWeekend();
            updateDateLabelAndWeekdaySelection();
        });

        mdYPanel.add(new JLabel("Month:"));
        mdYPanel.add(monthCombo);
        mdYPanel.add(new JLabel("Day:"));
        mdYPanel.add(dayCombo);
        mdYPanel.add(new JLabel("Year:"));
        mdYPanel.add(yearCombo);
        mdYPanel.add(selectDateButton);

        /*──────────────────────────────
         * Weekday selector
         *──────────────────────────────*/
        JPanel weekdayPanel = new JPanel(new FlowLayout());
        mondayButton    = new JRadioButton("Monday");
        tuesdayButton   = new JRadioButton("Tuesday");
        wednesdayButton = new JRadioButton("Wednesday");
        thursdayButton  = new JRadioButton("Thursday");
        fridayButton    = new JRadioButton("Friday");

        ButtonGroup weekdayButtonGroup = new ButtonGroup();
        weekdayButtonGroup.add(mondayButton);
        weekdayButtonGroup.add(tuesdayButton);
        weekdayButtonGroup.add(wednesdayButton);
        weekdayButtonGroup.add(thursdayButton);
        weekdayButtonGroup.add(fridayButton);

        JButton backButton    = new JButton("Back");
        JButton forwardButton = new JButton("Forward");

        weekdayPanel.add(mondayButton);
        weekdayPanel.add(tuesdayButton);
        weekdayPanel.add(wednesdayButton);
        weekdayPanel.add(thursdayButton);
        weekdayPanel.add(fridayButton);
        weekdayPanel.add(backButton);
        weekdayPanel.add(forwardButton);

        /*──────────────────────────────
         * Bottom layout
         *──────────────────────────────*/
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(weekdayPanel);
        bottomPanel.add(mdYPanel);      // ← NEW combos live here
        bottomPanel.add(datePanel);

        /*──────────────────────────────
         * Frame layout
         *──────────────────────────────*/
        setLayout(new BorderLayout());
        add(currencyPanel,    BorderLayout.NORTH);
        add(actionButtonPanel,BorderLayout.CENTER);
        add(bottomPanel,      BorderLayout.SOUTH);

        /*──────────────────────────────
         * Button actions
         *──────────────────────────────*/
        pickDateButton.addActionListener(e -> pickDate());
        dailyLatestButton.addActionListener(e -> processDailyLatest());
        weeklyLatest.addActionListener(e -> processWeeklyLatest());
        fiveMinuteLatestButton.addActionListener(e -> processFiveMinuteLatest());
        hourlyLatestButton.addActionListener(e -> processOneHourLatest());
        h4LatestButton.addActionListener(e -> processFourHourLatest());
        fifteenMinuteLatestButton.addActionListener(e -> processFifteenMinuteLatest());
        oneMinuteLatestButton.addActionListener(e -> processOneMinuteLatest());

        yesterdayButton.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            switch (today.getDayOfWeek()) {
                case MONDAY  -> pickedDate = today.minusDays(3);
                case SATURDAY-> pickedDate = today.minusDays(1);
                case SUNDAY  -> pickedDate = today.minusDays(2);
                default      -> pickedDate = today.minusDays(1);
            }
            updateDateLabelAndWeekdaySelection();
        });

        oneDayForwardButton.addActionListener(e -> {
            pickedDate = (pickedDate.getDayOfWeek() == DayOfWeek.FRIDAY)
                    ? pickedDate.plusDays(3)
                    : pickedDate.plusDays(1);
            updateDateLabelAndWeekdaySelection();
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(formatDate(pickedDate)), null);
        });

        goBackOneDayButton.addActionListener(e -> {
            pickedDate = (pickedDate.getDayOfWeek() == DayOfWeek.MONDAY)
                    ? pickedDate.minusDays(3)
                    : pickedDate.minusDays(1);
            updateDateLabelAndWeekdaySelection();
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(formatDate(pickedDate)), null);
        });

        mondayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.MONDAY));
        tuesdayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.TUESDAY));
        wednesdayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.WEDNESDAY));
        thursdayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.THURSDAY));
        fridayButton.addActionListener(e -> updatePickedDateForDay(DayOfWeek.FRIDAY));

        backButton.addActionListener   (e -> { pickedDate = pickedDate.minusWeeks(1); updateDateLabelAndWeekdaySelection(); });
        forwardButton.addActionListener(e -> { pickedDate = pickedDate.plusWeeks(1);  updateDateLabelAndWeekdaySelection(); });


        /*──────────────────────────────
         * Initial sync
         *──────────────────────────────*/
        updateDateLabelAndWeekdaySelection();
    }


    /*──────────────────────────────
     * Helper methods
     *──────────────────────────────*/
    private String getSelectedCurrency() {
        for (JRadioButton rb : currencyRadioButtons) if (rb.isSelected()) return rb.getText();
        return null;
    }

    private void updatePickedDateForDay(DayOfWeek day) {
        LocalDate baseline = getBaseline();
        pickedDate = (baseline.getDayOfWeek().getValue() <= day.getValue())
                ? baseline.with(TemporalAdjusters.nextOrSame(day))
                : baseline.with(TemporalAdjusters.previous(day));
        updateDateLabelAndWeekdaySelection();
    }

    private LocalDate getBaseline() {
        LocalDate today = LocalDate.now();
        return switch (today.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> today.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
            default               -> today;
        };
    }

    private void pickDate() {
        JDialog dialog = new JDialog(this, "Select Date", true);
        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JCalendar calendar = new JCalendar();
        dialog.add(calendar, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        dialog.add(okButton, BorderLayout.SOUTH);

        dialog.setVisible(true);

        Date selectedUtilDate = calendar.getDate();
        pickedDate = selectedUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        adjustPickedDateIfWeekend();
        updateDateLabelAndWeekdaySelection();
    }

    private void adjustPickedDateIfWeekend() {
        pickedDate = switch (pickedDate.getDayOfWeek()) {
            case SATURDAY -> pickedDate.minusDays(1);
            case SUNDAY   -> pickedDate.plusDays(1);
            default       -> pickedDate;
        };
    }

    private void updateDateLabelAndWeekdaySelection() {
        adjustPickedDateIfWeekend();
        selectedDateLabel.setText("Selected Date: " + formatDate(pickedDate));
        App.LATEST_DATE = pickedDate;

        monthCombo.setSelectedIndex(pickedDate.getMonthValue() - 1);
        dayCombo.setSelectedItem(pickedDate.getDayOfMonth());
        yearCombo.setSelectedItem(pickedDate.getYear());

        mondayButton.setSelected(pickedDate.getDayOfWeek() == DayOfWeek.MONDAY);
        tuesdayButton.setSelected(pickedDate.getDayOfWeek() == DayOfWeek.TUESDAY);
        wednesdayButton.setSelected(pickedDate.getDayOfWeek() == DayOfWeek.WEDNESDAY);
        thursdayButton.setSelected(pickedDate.getDayOfWeek() == DayOfWeek.THURSDAY);
        fridayButton.setSelected(pickedDate.getDayOfWeek() == DayOfWeek.FRIDAY);
    }

    private String formatDate(LocalDate date) {
        return DateTimeFormatter.ofPattern("MM-dd-yyyy EEEE").format(date);
    }

    private void processDailyLatest() {
        App.forexChartType = ForexChartType.DAILY_LATEST;
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH,
                App.TARGET_DIRECTORY_PATH, getSelectedCurrency());
    }

    private void processFourHourLatest() {
        App.forexChartType = ForexChartType.FOUR_HOUR;
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH,
                App.TARGET_DIRECTORY_PATH, getSelectedCurrency());
    }

    private void processFiveMinuteLatest() {
        App.forexChartType = ForexChartType.FIVE_MIN_LATEST;
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH,
                App.TARGET_DIRECTORY_PATH, getSelectedCurrency());
    }

    private void processOneHourLatest() {
        App.forexChartType = ForexChartType.HOURLY_1;
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH,
                App.TARGET_DIRECTORY_PATH, getSelectedCurrency());
    }

    private void processOneMinuteLatest() {
        App.forexChartType = ForexChartType.ONE_MIN_LATEST;
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH,
                App.TARGET_DIRECTORY_PATH, getSelectedCurrency());
    }

    private void processFifteenMinuteLatest() {
        App.forexChartType = ForexChartType.FIFTEEN_MIN_LATEST;
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH,
                App.TARGET_DIRECTORY_PATH, getSelectedCurrency());
    }

    private void processWeeklyLatest() {
        App.forexChartType = ForexChartType.WEEKLY;
        ScreenshotService.takeScreenshot(App.SOURCE_DIRECTORY_PATH, ScreenshotService.SCREENSHOT_FILE_NAME);
        ScreenshotService.processScreenshot(App.forexChartType, App.SOURCE_DIRECTORY_PATH,
                App.TARGET_DIRECTORY_PATH, getSelectedCurrency());
    }

    /**
     * Returns the Monday and Friday for the week that contains pickedDate.
     * If pickedDate is on a weekend:
     *   - Saturday -> shifted to Friday
     *   - Sunday   -> shifted to Monday
     */
    private LocalDate[] getCurrentWeekMondayAndFriday() {
        LocalDate base = pickedDate;

        // Normalize weekends
        switch (base.getDayOfWeek()) {
            case SATURDAY -> base = base.minusDays(1); // Saturday -> Friday
            case SUNDAY   -> base = base.plusDays(1);  // Sunday   -> Monday
            default -> {
                // leave as is
            }
        }

        LocalDate monday = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate friday = monday.plusDays(4); // Monday + 4 days = Friday

        return new LocalDate[]{ monday, friday };
    }

    /**
     * Copies only the Monday and Friday of the current week to clipboard.
     * Format: "MM-dd-yyyy EEEE" on separate lines.
     */
    private void copyCurrentWeekMondayAndFridayToClipboard() {
        LocalDate[] range = getCurrentWeekMondayAndFriday();
        LocalDate monday = range[0];
        LocalDate friday = range[1];

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd-yyyy EEEE");
        StringBuilder sb = new StringBuilder();
        sb.append(fmt.format(monday))
                .append(System.lineSeparator())
                .append(fmt.format(friday));

        StringSelection selection = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(selection, null);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
//          TelegramBotListener.start();
            UserInterface ui = new UserInterface();

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            GraphicsDevice leftMost = screens[0];
            for (GraphicsDevice screen : screens) {
                if (screen.getDefaultConfiguration().getBounds().x <
                        leftMost.getDefaultConfiguration().getBounds().x) {
                    leftMost = screen;
                }
            }
            Rectangle bounds = leftMost.getDefaultConfiguration().getBounds();
            int x = bounds.x + (bounds.width  - ui.getWidth())  / 2;
            int y = bounds.y + (bounds.height - ui.getHeight()) / 2;
            ui.setLocation(x, y);
            ui.setVisible(true);
        });
    }
}
