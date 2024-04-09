package org.example.service;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.time.LocalDate;

public class DateFileServiceTest extends TestCase {

    public void testInitializeForexOffDays() {
        DateFileService.initializeForexOffDays();
        Assert.assertNotNull(DateFileService.forexOffDays);
        Assert.assertTrue(DateFileService.forexOffDays.size() > 1);
        System.out.println(DateFileService.forexOffDays);
    }

    public void testInitializeNon23hDaysMap() {
        DateFileService.initializeNon23hDaysMap();
        Assert.assertNotNull(DateFileService.non23hdaysMap);
        Assert.assertTrue(DateFileService.non23hdaysMap.size() > 1);
        System.out.println(DateFileService.non23hdaysMap);
    }

    public void testGetForexHoursForDate() {
        DateFileService.initializeNon23hDaysMap();
        LocalDate dateFromCsv = DateFileService.non23hdaysMap.keySet().iterator().next();
        int expectedHours = DateFileService.non23hdaysMap.get(dateFromCsv);

        int actualHours = DateFileService.getForexHoursForDate(dateFromCsv);

        Assert.assertEquals(expectedHours, actualHours);
    }
}