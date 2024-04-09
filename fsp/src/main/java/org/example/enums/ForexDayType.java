package org.example.enums;

import org.example.service.DateFileService;

import java.time.LocalDate;

public enum ForexDayType {
    /**
     * 23 Candles
     */
    GOOD_DAY,
    /**
     * Less than 23 candles (but not 0)
     */
    BAD_DAY,
    /**
     * 0 Candles
     */
    OFF_DAY;

    public static ForexDayType determineDayTypeForLocalDate(final LocalDate localDate) {
        if (DateFileService.forexOffDays.isEmpty()) {
            DateFileService.initializeForexOffDays();
        }
        if (DateFileService.non23hdaysMap.isEmpty()) {
            DateFileService.initializeNon23hDaysMap();
        }
        if (DateFileService.forexOffDays.contains(localDate)) {
            return OFF_DAY;
        }
        if (DateFileService.non23hdaysMap.containsKey(localDate)) {
            return BAD_DAY;
        }
        return GOOD_DAY;
    }
}
