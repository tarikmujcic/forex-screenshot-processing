package org.example.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class InstanceCounterService {

    private static final String INSTANCE_COUNTER_HOURLY_1_FILE_PATH = "counters" + File.separator + "hourly-1-instance-counter.txt";

    private static int HOURLY_1_INSTANCE_COUNT = 0;

    public static void initializeInstanceCounters() {
        HOURLY_1_INSTANCE_COUNT = readHourly1Count();
    }

    public static int getAndIncrementHOURLY_1_INSTANCE_COUNT() {
        int currentInstanceCount = HOURLY_1_INSTANCE_COUNT;
        incrementHourly1Count();
        return currentInstanceCount;
    }

    private static int readHourly1Count() {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(INSTANCE_COUNTER_HOURLY_1_FILE_PATH))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                count = Integer.parseInt(line);
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Unable to read instance count for HOURLY_1");
        }
        return count;
    }


    /**
     * Method increments the HOURLY_1 count both in the file and the HOURLY_1_INSTANCE_COUNT variable.
     */
    public static void incrementHourly1Count() {
        if (HOURLY_1_INSTANCE_COUNT == 0) {
            HOURLY_1_INSTANCE_COUNT = readHourly1Count();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INSTANCE_COUNTER_HOURLY_1_FILE_PATH))) {
            writer.write(String.valueOf(++HOURLY_1_INSTANCE_COUNT));
        } catch (IOException e) {
            throw new RuntimeException("Unable to increment instance count for HOURLY_1");
        }
    }
}
