package org.example.service;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class FocusedAppCheckerService {

    private static final String APPLICATION_NAME = "coinexx";

    public static boolean isTraderAppFocused() {
        User32 user32 = User32.INSTANCE;
        WinDef.HWND foregroundWindow = user32.GetForegroundWindow();
        char[] windowTitle = new char[512];
        user32.GetWindowText(foregroundWindow, windowTitle, 512);
        String title = Native.toString(windowTitle);
        return (title.toLowerCase().contains(APPLICATION_NAME));
    }
}
