/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.handler;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with GRAFIK Eye QS devices in
 * a RadioRA 2 or HomeWorks QS System.
 *
 * Does not communicate with the scene controller, timeclock controller, or wireless
 * and EcoSystem occupancy sensors.
 *
 * @author Bob Adair - Initial contribution
 */
public class GrafikEyeKeypadHandler extends BaseKeypadHandler {

    private static enum Component implements KeypadComponent {
        BUTTON1(70, "button1", "Button 1"), // Scene button 1
        BUTTON2(71, "button2", "Button 2"), // Scene button 2
        BUTTON3(76, "button3", "Button 3"), // Scene button 3
        BUTTON4(77, "button4", "Button 4"), // Scene button 4
        BUTTON5(83, "button5", "Button 5"), // Scene button 5/Off

        BUTTON10(38, "button10", "Button 10"), // Col 1
        BUTTON11(39, "button11", "Button 11"), // Col 1
        BUTTON12(40, "button12", "Button 12"), // Col 1
        LOWER1(41, "buttonlower1", "Lower button col 1"), // Col 1 lower
        RAISE1(47, "buttonraise1", "Raise button col 1"), // Col 1 raise

        BUTTON20(44, "button20", "Button 20"), // Col 2
        BUTTON21(45, "button21", "Button 21"), // Col 2
        BUTTON22(46, "button22", "Button 22"), // Col 2
        LOWER2(52, "buttonlower2", "Lower button col 2"), // Col 2 lower
        RAISE2(53, "buttonraise2", "Raise button col 2"), // Col 2 raise

        BUTTON30(50, "button30", "Button 30"), // Col 3
        BUTTON31(51, "button31", "Button 31"), // Col 3
        BUTTON32(56, "button32", "Button 32"), // Col 3
        LOWER3(57, "buttonlower3", "Lower button col 3"), // Col 3 lower
        RAISE3(58, "buttonraise3", "Raise button col 3"), // Col 3 raise

        CCI1(163, "cci1", "CCI 1"),

        LED1(201, "led1", "LED 1"), // Scene button LEDs
        LED2(210, "led2", "LED 2"),
        LED3(219, "led3", "LED 3"),
        LED4(228, "led4", "LED 4"),
        LED5(237, "led5", "LED 5"),

        LED10(174, "led10", "LED 10"), // Col 1 LEDs
        LED11(175, "led11", "LED 11"),
        LED12(211, "led12", "LED 12"),

        LED20(183, "led20", "LED 20"), // Col 2 LEDs
        LED21(184, "led21", "LED 21"),
        LED22(220, "led22", "LED 22"),

        LED30(192, "led30", "LED 30"), // Col 3 LEDs
        LED31(193, "led31", "LED 31"),
        LED32(229, "led32", "LED 32");

        private final int id;
        private final String channel;
        private final String description;

        Component(int id, String channel, String description) {
            this.id = id;
            this.channel = channel;
            this.description = description;
        }

        @Override
        public int id() {
            return this.id;
        }

        @Override
        public String channel() {
            return this.channel;
        }

        @Override
        public String description() {
            return this.description;
        }

    }

    private static final List<Component> SCENE_BUTTON_GROUP = Arrays.asList(Component.BUTTON1, Component.BUTTON2,
            Component.BUTTON3, Component.BUTTON4, Component.BUTTON5);
    private static final List<Component> SCENE_LED_GROUP = Arrays.asList(Component.LED1, Component.LED2, Component.LED3,
            Component.LED4, Component.LED5);

    private static final List<Component> CCI_GROUP = Arrays.asList(Component.CCI1);

    private static final List<Component> COL1_BUTTON_GROUP = Arrays.asList(Component.BUTTON10, Component.BUTTON11,
            Component.BUTTON12, Component.LOWER1, Component.RAISE1);
    private static final List<Component> COL2_BUTTON_GROUP = Arrays.asList(Component.BUTTON20, Component.BUTTON21,
            Component.BUTTON22, Component.LOWER2, Component.RAISE2);
    private static final List<Component> COL3_BUTTON_GROUP = Arrays.asList(Component.BUTTON30, Component.BUTTON31,
            Component.BUTTON32, Component.LOWER3, Component.RAISE3);

    private static final List<Component> COL1_LED_GROUP = Arrays.asList(Component.LED10, Component.LED11,
            Component.LED12);
    private static final List<Component> COL2_LED_GROUP = Arrays.asList(Component.LED20, Component.LED21,
            Component.LED22);
    private static final List<Component> COL3_LED_GROUP = Arrays.asList(Component.LED30, Component.LED31,
            Component.LED32);

    private final Logger logger = LoggerFactory.getLogger(GrafikEyeKeypadHandler.class);

    @Override
    protected boolean isLed(int id) {
        return (id >= 174 && id <= 237);
    }

    @Override
    protected boolean isButton(int id) {
        return (id >= 38 && id <= 83);
    }

    @Override
    protected boolean isCCI(int id) {
        return (id == 163);
    }

    @Override
    protected void configureComponents(String model) {
        String mod = model == null ? "3COL" : model;
        logger.debug("Configuring components for GRAFIK Eye QS");

        buttonList.addAll(SCENE_BUTTON_GROUP);
        ledList.addAll(SCENE_LED_GROUP);
        cciList.addAll(CCI_GROUP);

        switch (mod) {
            default:
                logger.warn("No valid keypad model defined ({}). Assuming model 3COL.", mod);
            case "3COL":
                buttonList.addAll(COL1_BUTTON_GROUP);
                buttonList.addAll(COL2_BUTTON_GROUP);
                buttonList.addAll(COL3_BUTTON_GROUP);
                ledList.addAll(COL1_LED_GROUP);
                ledList.addAll(COL2_LED_GROUP);
                ledList.addAll(COL3_LED_GROUP);
                break;
            case "2COL":
                buttonList.addAll(COL1_BUTTON_GROUP);
                buttonList.addAll(COL2_BUTTON_GROUP);
                ledList.addAll(COL1_LED_GROUP);
                ledList.addAll(COL2_LED_GROUP);
                break;
            case "1COL":
                buttonList.addAll(COL1_BUTTON_GROUP);
                ledList.addAll(COL1_LED_GROUP);
                break;
            case "0COL":
                break;
        }
    }

    public GrafikEyeKeypadHandler(Thing thing) {
        super(thing);
    }

}
