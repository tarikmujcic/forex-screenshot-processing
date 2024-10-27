package org.example.enums;

public enum ForexChartType {
    FIVE_MIN,
    HOURLY_1,
    HOURLY_23,
    FOUR_HOUR,
    DAILY,
    WEEKLY,
    DAILY_LATEST,
    HOURLY_23_LATEST,
    FIVE_MIN_LATEST,
    /**
     * User is manually moving Coinexx M5 view in a way that it shows the whole day. We are just incrementing the date for the label.
     */
    FIVE_MIN_WHOLE_DAY
}
