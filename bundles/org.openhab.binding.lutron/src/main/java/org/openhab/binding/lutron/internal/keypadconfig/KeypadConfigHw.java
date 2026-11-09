/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lutron.internal.keypadconfig;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;

/**
 * Keypad configuration definition for legacy HomeWorks keypads, which are controlled with the L232 command set.
 *
 * L232 numbers the buttons of every keypad 1 to 24, and gives each button the LED with the same number. Models are
 * added here only with button numbers that were verified on the processor.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public final class KeypadConfigHw extends KeypadConfig {
    /** A keypad with all 24 buttons and LEDs that L232 can address. */
    public static final String GENERIC = "GENERIC";

    public static final int MAX_BUTTONS = 24;

    private static final int LED_ID_OFFSET = 100;

    private record Component(int id, String channel, String description,
            ComponentType type) implements KeypadComponent {
    }

    public static String buttonChannel(int button) {
        return "button" + button;
    }

    public static String ledChannel(int led) {
        return "led" + led;
    }

    /** Returns the component id of a button, which is its L232 button number. */
    public static int buttonId(int button) {
        return button;
    }

    /** Returns the component id of the LED belonging to a button. */
    public static int ledId(int button) {
        return LED_ID_OFFSET + button;
    }

    public KeypadConfigHw() {
        modelData.put(GENERIC, components(MAX_BUTTONS));
    }

    private static List<KeypadComponent> components(int buttons) {
        List<KeypadComponent> buttonList = new ArrayList<>();
        List<KeypadComponent> ledList = new ArrayList<>();
        for (int n = 1; n <= buttons; n++) {
            buttonList.add(new Component(buttonId(n), buttonChannel(n), "Button " + n, ComponentType.BUTTON));
            ledList.add(new Component(ledId(n), ledChannel(n), "LED " + n, ComponentType.LED));
        }
        return combinedList(buttonList, ledList);
    }

    @Override
    public boolean isButton(int id) {
        return id >= 1 && id <= MAX_BUTTONS;
    }

    @Override
    public boolean isLed(int id) {
        return id > LED_ID_OFFSET && id <= LED_ID_OFFSET + MAX_BUTTONS;
    }

    @Override
    public boolean isCCI(int id) {
        return false;
    }
}
