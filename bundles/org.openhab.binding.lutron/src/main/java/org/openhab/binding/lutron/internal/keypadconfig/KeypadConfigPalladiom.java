/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;

/**
 * Keypad configuration definition for Palladiom keypad line
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public final class KeypadConfigPalladiom extends KeypadConfig {

    private enum Component implements KeypadComponent {
        BUTTON1(1, "button1", "Button 1", ComponentType.BUTTON),
        BUTTON2(2, "button2", "Button 2", ComponentType.BUTTON),
        BUTTON3(3, "button3", "Button 3", ComponentType.BUTTON),
        BUTTON4(4, "button4", "Button 4", ComponentType.BUTTON),
        BUTTON5(5, "button5", "Button 5", ComponentType.BUTTON),
        BUTTON6(6, "button6", "Button 6", ComponentType.BUTTON),
        BUTTON7(7, "button7", "Button 7", ComponentType.BUTTON),
        BUTTON8(8, "button8", "Button 8", ComponentType.BUTTON),

        LOWER1(16, "buttonlower1", "Lower button 1", ComponentType.BUTTON),
        RAISE1(17, "buttonraise1", "Raise button 2", ComponentType.BUTTON),
        LOWER2(18, "buttonlower2", "Lower button 3", ComponentType.BUTTON),
        RAISE2(19, "buttonraise2", "Raise button 4", ComponentType.BUTTON),

        LED1(81, "led1", "LED 1", ComponentType.LED),
        LED2(82, "led2", "LED 2", ComponentType.LED),
        LED3(83, "led3", "LED 3", ComponentType.LED),
        LED4(84, "led4", "LED 4", ComponentType.LED),
        LED5(85, "led5", "LED 5", ComponentType.LED),
        LED6(86, "led6", "LED 6", ComponentType.LED),
        LED7(87, "led7", "LED 7", ComponentType.LED),
        LED8(88, "led8", "LED 8", ComponentType.LED);

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
        return (id >= 81 && id <= 88);
    }

    @Override
    public boolean isButton(int id) {
        return ((id >= 1 && id <= 8) || (id >= 16 && id <= 19));
    }

    @Override
    public boolean isCCI(int id) {
        return false;
    }

    public KeypadConfigPalladiom() {
        modelData.put("2W", Arrays.asList(Component.BUTTON1, Component.BUTTON4, Component.LED1, Component.LED4));

        modelData.put("3W", Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON4, Component.LED1,
                Component.LED2, Component.LED4));

        modelData.put("4W", Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.BUTTON4,
                Component.LED1, Component.LED2, Component.LED3, Component.LED4));

        modelData.put("RW", Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.LOWER1,
                Component.RAISE1, Component.LED1, Component.LED2, Component.LED3));

        modelData.put("22W", Arrays.asList(Component.BUTTON1, Component.BUTTON4, Component.BUTTON5, Component.BUTTON8,
                Component.LED1, Component.LED4, Component.LED5, Component.LED8));

        modelData.put("24W",
                Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.BUTTON4,
                        Component.BUTTON5, Component.BUTTON8, Component.LED1, Component.LED2, Component.LED3,
                        Component.LED4, Component.LED5, Component.LED8));

        modelData.put("42W",
                Arrays.asList(Component.BUTTON1, Component.BUTTON4, Component.BUTTON5, Component.BUTTON6,
                        Component.BUTTON7, Component.BUTTON8, Component.LED1, Component.LED4, Component.LED5,
                        Component.LED6, Component.LED7, Component.LED8));

        modelData.put("44W",
                Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.BUTTON4,
                        Component.BUTTON5, Component.BUTTON6, Component.BUTTON7, Component.BUTTON8, Component.LED1,
                        Component.LED2, Component.LED3, Component.LED4, Component.LED5, Component.LED6, Component.LED7,
                        Component.LED8));

        modelData.put("2RW",
                Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.BUTTON5,
                        Component.BUTTON8, Component.LOWER1, Component.RAISE1, Component.LED1, Component.LED2,
                        Component.LED3, Component.LED5, Component.LED8));

        modelData.put("4RW",
                Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.BUTTON5,
                        Component.BUTTON6, Component.BUTTON7, Component.BUTTON8, Component.LOWER1, Component.RAISE1,
                        Component.LED1, Component.LED2, Component.LED3, Component.LED5, Component.LED6, Component.LED7,
                        Component.LED8));

        modelData.put("RRW",
                Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.BUTTON5,
                        Component.BUTTON6, Component.BUTTON7, Component.LOWER1, Component.RAISE1, Component.LOWER2,
                        Component.RAISE2, Component.LED1, Component.LED2, Component.LED3, Component.LED5,
                        Component.LED6, Component.LED7));

        // Superset of all models
        modelData.put("Generic", Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3,
                Component.BUTTON4, Component.BUTTON5, Component.BUTTON6, Component.BUTTON7, Component.BUTTON8,
                Component.LOWER1, Component.RAISE1, Component.LOWER2, Component.RAISE2, Component.LED1, Component.LED2,
                Component.LED3, Component.LED4, Component.LED5, Component.LED6, Component.LED7, Component.LED8));
    }
}
