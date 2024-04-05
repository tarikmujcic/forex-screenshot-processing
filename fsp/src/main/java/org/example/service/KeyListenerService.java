package org.example.service;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.example.App;

public class KeyListenerService {
    public static void initializeGlobalKeyListener(int nativeKeyEvent) {
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
                if (e.getKeyCode() == nativeKeyEvent) {
                    App.IS_TRIGGER_KEY_PRESSED = true;
                }
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
                // Not needed for this implementation
            }
        });
    }

}
