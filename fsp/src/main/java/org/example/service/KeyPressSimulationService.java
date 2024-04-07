package org.example.service;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class KeyPressSimulationService {

    /**
     * Simulates pressing F12 key 'numberOfPresses' times and before simulating makes a delay equal to the passed in parameter.
     * @param numberOfPresses How many times does the F12 key be simulated
     * @param delay How much of a delay is needed before it starts simulating key presses
     */
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
