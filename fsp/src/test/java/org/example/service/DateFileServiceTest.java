package org.example.service;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DateFileServiceTest extends TestCase {

    public void testInitializeForexOffDays() {
        DateFileService.initializeForexOffDays();
        Assert.assertNotNull(DateFileService.forexOffDays);
        Assert.assertTrue(DateFileService.forexOffDays.size() > 1);
        System.out.println(DateFileService.forexOffDays);
    }
}