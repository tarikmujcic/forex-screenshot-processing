package org.example.service;

import junit.framework.TestCase;

import java.awt.event.KeyEvent;

public class KeyPressSimulationServiceTest extends TestCase {

    public void testSendKeyPressToSpecificWindow() {
        KeyPressSimulationService.sendKeyPressToSpecificWindow("fsp", KeyEvent.VK_A, 1);
    }
}