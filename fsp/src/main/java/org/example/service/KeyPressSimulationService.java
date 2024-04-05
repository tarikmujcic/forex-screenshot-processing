package org.example.service;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class KeyPressSimulationService {

    public static void simulateKeyPressF12(int numberOfPresses, int delay) {
        try {
            if (delay != 0) {
                Thread.sleep(delay);
            }
            Robot robot = new Robot();
            for (int i = 0; i < numberOfPresses; i++) {
                robot.keyPress(KeyEvent.VK_F12);
                robot.keyRelease(KeyEvent.VK_F12);
                Thread.sleep(50);
            }
        } catch (AWTException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
