/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;

/**
 * Keypad configuration definition for Pico models
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public final class KeypadConfigPico extends KeypadConfig {

    // Button mappings for LEAP protocol
    public static final Map<Integer, Integer> LEAPBUTTONS_2B = Map.of(2, 1, 4, 2);
    public static final Map<Integer, Integer> LEAPBUTTONS_2BRL = Map.of(2, 1, 4, 2, 5, 3, 6, 4);
    public static final Map<Integer, Integer> LEAPBUTTONS_3B = Map.of(2, 1, 3, 2, 4, 3);
    public static final Map<Integer, Integer> LEAPBUTTONS_4B = Map.of(8, 1, 9, 2, 10, 3, 11, 4);
    public static final Map<Integer, Integer> LEAPBUTTONS_3BRL = Map.of(2, 1, 3, 2, 4, 3, 5, 4, 6, 5);

    private static enum Component implements KeypadComponent {
        // Buttons for 2B, 2BRL, 3B, and 3BRL models
        BUTTON1(2, "button1", "Button 1", ComponentType.BUTTON),
        BUTTON2(3, "button2", "Button 2", ComponentType.BUTTON),
        BUTTON3(4, "button3", "Button 3", ComponentType.BUTTON),

        RAISE(5, "buttonraise", "Raise Button", ComponentType.BUTTON),
        LOWER(6, "buttonlower", "Lower Button", ComponentType.BUTTON),

        // Buttons for PJ2-4B model
        BUTTON1_4B(8, "button01", "Button 1", ComponentType.BUTTON),
        BUTTON2_4B(9, "button02", "Button 2", ComponentType.BUTTON),
        BUTTON3_4B(10, "button03", "Button 3", ComponentType.BUTTON),
        BUTTON4_4B(11, "button04", "Button 4", ComponentType.BUTTON);

        private final int id;
        private final String channel;
        private final String description;
        private final ComponentType type;

        Component(int id, String channel, String description, ComponentType type) {
            this.id = id;
            this.channel = channel;
            this.description = description;
            this.type = type;
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public String channel() {
            return channel;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public ComponentType type() {
            return type;
        }
    }

    @Override
    public boolean isLed(int id) {
        return false;
    }

    @Override
    public boolean isButton(int id) {
        return (id >= 2 && id <= 11);
    }

    @Override
    public boolean isCCI(int id) {
        return false;
    }

    public KeypadConfigPico() {
        modelData.put("2B", Arrays.asList(Component.BUTTON1, Component.BUTTON3));
        modelData.put("2BRL", Arrays.asList(Component.BUTTON1, Component.BUTTON3, Component.RAISE, Component.LOWER));
        modelData.put("3B", Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3));
        modelData.put("3BRL", Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.RAISE,
                Component.LOWER));
        modelData.put("4B",
                Arrays.asList(Component.BUTTON1_4B, Component.BUTTON2_4B, Component.BUTTON3_4B, Component.BUTTON4_4B));
    }
}
