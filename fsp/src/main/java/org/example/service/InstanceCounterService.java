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

    private static final String INSTANCE_COUNTER_FIVE_MIN_FILE_PATH = "counters" + File.separator + "five-min-instance-counter.txt";
    private static int FIVE_MIN_INSTANCE_COUNT = 0;

    public static void initializeInstanceCounters() {
        HOURLY_1_INSTANCE_COUNT = readCountFromCounterFile(INSTANCE_COUNTER_HOURLY_1_FILE_PATH);
        FIVE_MIN_INSTANCE_COUNT = readCountFromCounterFile(INSTANCE_COUNTER_FIVE_MIN_FILE_PATH);
    }

    public static int getAndIncrementHOURLY_1_INSTANCE_COUNT() {
        int currentInstanceCount = HOURLY_1_INSTANCE_COUNT;
        incrementHourly1Count();
        return currentInstanceCount;
    }

    public static int getAndIncrementFIVE_MIN_INSTANCE_COUNT() {
        int currentInstanceCount = FIVE_MIN_INSTANCE_COUNT;
        incrementFiveMinCount();
        return currentInstanceCount;
    }

    private static int readCountFromCounterFile(String path) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
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
            HOURLY_1_INSTANCE_COUNT = readCountFromCounterFile(INSTANCE_COUNTER_HOURLY_1_FILE_PATH);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INSTANCE_COUNTER_HOURLY_1_FILE_PATH))) {
            writer.write(String.valueOf(++HOURLY_1_INSTANCE_COUNT));
        } catch (IOException e) {
            throw new RuntimeException("Unable to increment instance count for HOURLY_1");
        }
    }

    /**
     * Method increments the FIVE_MIN count both in the file and the FIVE_MIN_INSTANCE_COUNT variable.
     */
    public static void incrementFiveMinCount() {
        if (FIVE_MIN_INSTANCE_COUNT == 0) {
            FIVE_MIN_INSTANCE_COUNT = readCountFromCounterFile(INSTANCE_COUNTER_FIVE_MIN_FILE_PATH);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INSTANCE_COUNTER_FIVE_MIN_FILE_PATH))) {
            writer.write(String.valueOf(++FIVE_MIN_INSTANCE_COUNT));
        } catch (IOException e) {
            throw new RuntimeException("Unable to increment instance count for HOURLY_1");
        }
    }
}
