package org.example.service;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class KeyPressSimulationService {

    /**
     * Simulates pressing F12 key 'numberOfPresses' times and before simulating makes a delay equal to the passed in parameter.
     * @param numberOfPresses How many times does the F12 key be simulated
     */
    public static void simulateKeyPressF12(int numberOfPresses) {
        try {
            System.out.println("Simulating key press " + numberOfPresses + " times...");
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

    public static void sendKeyPressToSpecificWindow(String windowContainsText, int keyCode, int numberOfPresses) {
        try {
            User32 user32 = User32.INSTANCE;

            // Define a callback to process each window
            WinUser.WNDENUMPROC callback = new WinUser.WNDENUMPROC() {
                @Override
                public boolean callback(WinDef.HWND hwnd, Pointer pointer) {
                    char[] windowText = new char[512];
                    user32.GetWindowText(hwnd, windowText, 512);
                    String title = Native.toString(windowText);
                    if (title.toLowerCase().contains(windowContainsText)) {
                        System.out.println("TIUTLE: " + title);
                        // Found the window, bring it to the foreground
                        user32.SetForegroundWindow(hwnd); // MANDATORY !
                        // Found the window, send key events
                        for (int i = 0; i < numberOfPresses; i++) {
                            user32.PostMessage(hwnd, WinUser.WM_KEYDOWN, new WinDef.WPARAM(keyCode), new WinDef.LPARAM(0));
                            try {
                                Thread.sleep(50); // Adjust this delay as needed
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return false; // Stop enumerating windows
                    }
                    return true; // Continue enumerating windows
                }
            };

            // Enumerate all top-level windows
            user32.EnumWindows(callback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
