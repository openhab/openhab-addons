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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;

/**
 * Keypad configuration definition for Tabletop seeTouch line
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public final class KeypadConfigGrafikEye extends KeypadConfig {

    private static enum Component implements KeypadComponent {
        BUTTON1(70, "button1", "Button 1", ComponentType.BUTTON), // Scene button 1
        BUTTON2(71, "button2", "Button 2", ComponentType.BUTTON), // Scene button 2
        BUTTON3(76, "button3", "Button 3", ComponentType.BUTTON), // Scene button 3
        BUTTON4(77, "button4", "Button 4", ComponentType.BUTTON), // Scene button 4
        BUTTON5(83, "button5", "Button 5", ComponentType.BUTTON), // Scene button 5/Off

        BUTTON10(38, "button10", "Button 10", ComponentType.BUTTON), // Col 1
        BUTTON11(39, "button11", "Button 11", ComponentType.BUTTON), // Col 1
        BUTTON12(40, "button12", "Button 12", ComponentType.BUTTON), // Col 1
        LOWER1(41, "buttonlower1", "Lower button col 1", ComponentType.BUTTON), // Col 1 lower
        RAISE1(47, "buttonraise1", "Raise button col 1", ComponentType.BUTTON), // Col 1 raise

        BUTTON20(44, "button20", "Button 20", ComponentType.BUTTON), // Col 2
        BUTTON21(45, "button21", "Button 21", ComponentType.BUTTON), // Col 2
        BUTTON22(46, "button22", "Button 22", ComponentType.BUTTON), // Col 2
        LOWER2(52, "buttonlower2", "Lower button col 2", ComponentType.BUTTON), // Col 2 lower
        RAISE2(53, "buttonraise2", "Raise button col 2", ComponentType.BUTTON), // Col 2 raise

        BUTTON30(50, "button30", "Button 30", ComponentType.BUTTON), // Col 3
        BUTTON31(51, "button31", "Button 31", ComponentType.BUTTON), // Col 3
        BUTTON32(56, "button32", "Button 32", ComponentType.BUTTON), // Col 3
        LOWER3(57, "buttonlower3", "Lower button col 3", ComponentType.BUTTON), // Col 3 lower
        RAISE3(58, "buttonraise3", "Raise button col 3", ComponentType.BUTTON), // Col 3 raise

        CCI1(163, "cci1", "CCI 1", ComponentType.CCI),

        LED1(201, "led1", "LED 1", ComponentType.LED), // Scene button LEDs
        LED2(210, "led2", "LED 2", ComponentType.LED),
        LED3(219, "led3", "LED 3", ComponentType.LED),
        LED4(228, "led4", "LED 4", ComponentType.LED),
        LED5(237, "led5", "LED 5", ComponentType.LED),

        LED10(174, "led10", "LED 10", ComponentType.LED), // Col 1 LEDs
        LED11(175, "led11", "LED 11", ComponentType.LED),
        LED12(211, "led12", "LED 12", ComponentType.LED),

        LED20(183, "led20", "LED 20", ComponentType.LED), // Col 2 LEDs
        LED21(184, "led21", "LED 21", ComponentType.LED),
        LED22(220, "led22", "LED 22", ComponentType.LED),

        LED30(192, "led30", "LED 30", ComponentType.LED), // Col 3 LEDs
        LED31(193, "led31", "LED 31", ComponentType.LED),
        LED32(229, "led32", "LED 32", ComponentType.LED);

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

    private static final List<KeypadComponent> SCENE_BUTTON_GROUP = Arrays.asList(Component.BUTTON1, Component.BUTTON2,
            Component.BUTTON3, Component.BUTTON4, Component.BUTTON5);
    private static final List<KeypadComponent> SCENE_LED_GROUP = Arrays.asList(Component.LED1, Component.LED2,
            Component.LED3, Component.LED4, Component.LED5);

    private static final List<KeypadComponent> CCI_GROUP = Arrays.asList(Component.CCI1);

    private static final List<KeypadComponent> COL1_BUTTON_GROUP = Arrays.asList(Component.BUTTON10, Component.BUTTON11,
            Component.BUTTON12, Component.LOWER1, Component.RAISE1);
    private static final List<KeypadComponent> COL2_BUTTON_GROUP = Arrays.asList(Component.BUTTON20, Component.BUTTON21,
            Component.BUTTON22, Component.LOWER2, Component.RAISE2);
    private static final List<KeypadComponent> COL3_BUTTON_GROUP = Arrays.asList(Component.BUTTON30, Component.BUTTON31,
            Component.BUTTON32, Component.LOWER3, Component.RAISE3);

    private static final List<KeypadComponent> COL1_LED_GROUP = Arrays.asList(Component.LED10, Component.LED11,
            Component.LED12);
    private static final List<KeypadComponent> COL2_LED_GROUP = Arrays.asList(Component.LED20, Component.LED21,
            Component.LED22);
    private static final List<KeypadComponent> COL3_LED_GROUP = Arrays.asList(Component.LED30, Component.LED31,
            Component.LED32);

    @Override
    public boolean isLed(int id) {
        return (id >= 174 && id <= 237);
    }

    @Override
    public boolean isButton(int id) {
        return (id >= 38 && id <= 83);
    }

    @Override
    public boolean isCCI(int id) {
        return (id == 163);
    }

    public KeypadConfigGrafikEye() {
        modelData.put("0COL", combinedList(SCENE_BUTTON_GROUP, CCI_GROUP, SCENE_LED_GROUP));

        modelData.put("1COL",
                combinedList(SCENE_BUTTON_GROUP, COL1_BUTTON_GROUP, CCI_GROUP, SCENE_LED_GROUP, COL1_LED_GROUP));

        modelData.put("2COL", combinedList(SCENE_BUTTON_GROUP, COL1_BUTTON_GROUP, COL2_BUTTON_GROUP, CCI_GROUP,
                SCENE_LED_GROUP, COL1_LED_GROUP, COL2_LED_GROUP));

        modelData.put("3COL", combinedList(SCENE_BUTTON_GROUP, COL1_BUTTON_GROUP, COL2_BUTTON_GROUP, COL3_BUTTON_GROUP,
                CCI_GROUP, SCENE_LED_GROUP, COL1_LED_GROUP, COL2_LED_GROUP, COL3_LED_GROUP));
    }
}
