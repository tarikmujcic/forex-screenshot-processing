package org.example.service;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.example.App;

public class KeyListenerService {

    private static final int CONTINUE_KEY = NativeKeyEvent.VC_B;
    private static final int TOGGLE_AUTOMATION_KEY = NativeKeyEvent.VC_T;

    public static void initializeGlobalKeyListener() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("Failed to register native hook: " + ex.getMessage());
        }

        // Add a key listener to listen for keyboard events
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                // Not needed for this implementation (we do it on release of the key)
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                if (e.getKeyCode() == CONTINUE_KEY) {
                    App.IS_TRIGGER_KEY_PRESSED = true;
                } else if (e.getKeyCode() == TOGGLE_AUTOMATION_KEY) {
                    App.IS_FULLY_AUTOMATED = !App.IS_FULLY_AUTOMATED;
                    System.out.println("Full Automation turned " + (App.IS_FULLY_AUTOMATED ? "ON" : "OFF"));
                }
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
                // Not needed for this implementation
            }
        });
    }

}
