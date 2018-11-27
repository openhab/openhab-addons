/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.handler;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron Tabletop seeTouch keypads used in RadioRA2 and Homeworks QS systems
 * (e.g. RR-T5RL, RR-T10RL, RR-T15RL, etc.)
 *
 * @author Bob Adair - Initial contribution
 */
public class TabletopKeypadHandler extends BaseKeypadHandler {

    private static enum Component implements KeypadComponent {
        BUTTON1(1, "button1", "Button 1"),
        BUTTON2(2, "button2", "Button 2"),
        BUTTON3(3, "button3", "Button 3"),
        BUTTON4(4, "button4", "Button 4"),
        BUTTON5(5, "button5", "Button 5"),
        BUTTON6(6, "button6", "Button 6"),
        BUTTON7(7, "button7", "Button 7"),
        BUTTON8(8, "button8", "Button 8"),
        BUTTON9(9, "button9", "Button 9"),
        BUTTON10(10, "button10", "Button 10"),
        BUTTON11(11, "button11", "Button 11"),
        BUTTON12(12, "button12", "Button 12"),
        BUTTON13(13, "button13", "Button 13"),
        BUTTON14(14, "button14", "Button 14"),
        BUTTON15(15, "button15", "Button 15"),

        BUTTON16(16, "button16", "Button 16"),
        BUTTON17(17, "button17", "Button 17"),

        LOWER1(20, "buttonlower1", "Lower button 1"),
        RAISE1(21, "buttonraise1", "Raise button 1"),
        LOWER2(22, "buttonlower2", "Lower button 2"),
        RAISE2(23, "buttonraise2", "Raise button 2"),
        LOWER3(24, "buttonlower3", "Lower button 3"),
        RAISE3(25, "buttonraise3", "Raise button 3"),

        LED1(81, "led1", "LED 1"),
        LED2(82, "led2", "LED 2"),
        LED3(83, "led3", "LED 3"),
        LED4(84, "led4", "LED 4"),
        LED5(85, "led5", "LED 5"),
        LED6(86, "led6", "LED 6"),
        LED7(87, "led7", "LED 7"),
        LED8(88, "led8", "LED 8"),
        LED9(89, "led9", "LED 9"),
        LED10(90, "led10", "LED 10"),
        LED11(91, "led11", "LED 11"),
        LED12(92, "led12", "LED 12"),
        LED13(93, "led13", "LED 13"),
        LED14(94, "led14", "LED 14"),
        LED15(95, "led15", "LED 15"),

        LED16(96, "led16", "LED 16"),
        LED17(97, "led17", "LED 17");

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

    }

    private static final List<Component> buttonGroup1 = Arrays.asList(Component.BUTTON1, Component.BUTTON2,
            Component.BUTTON3, Component.BUTTON4, Component.BUTTON5);
    private static final List<Component> buttonGroup2 = Arrays.asList(Component.BUTTON6, Component.BUTTON7,
            Component.BUTTON8, Component.BUTTON9, Component.BUTTON10);
    private static final List<Component> buttonGroup3 = Arrays.asList(Component.BUTTON11, Component.BUTTON12,
            Component.BUTTON13, Component.BUTTON14, Component.BUTTON15);

    private static final List<Component> buttonsBottomRL = Arrays.asList(Component.BUTTON16, Component.BUTTON17,
            Component.LOWER3, Component.RAISE3);
    private static final List<Component> buttonsBottomCRL = Arrays.asList(Component.LOWER1, Component.RAISE1,
            Component.LOWER2, Component.RAISE2, Component.LOWER3, Component.RAISE3);

    private static final List<Component> ledGroup1 = Arrays.asList(Component.LED1, Component.LED2, Component.LED3,
            Component.LED4, Component.LED5);
    private static final List<Component> ledGroup2 = Arrays.asList(Component.LED6, Component.LED7, Component.LED8,
            Component.LED9, Component.LED10);
    private static final List<Component> ledGroup3 = Arrays.asList(Component.LED11, Component.LED12, Component.LED13,
            Component.LED14, Component.LED15);

    private static final List<Component> LedsBottomRL = Arrays.asList(Component.LED16, Component.LED17);

    private final Logger logger = LoggerFactory.getLogger(TabletopKeypadHandler.class);

    @Override
    protected boolean isLed(int id) {
        return (id >= 81 && id <= 95);
    }

    @Override
    protected boolean isButton(int id) {
        return (id >= 1 && id <= 25);
    }

    @Override
    protected boolean isCCI(int id) {
        return false;
    }

    @Override
    protected void configureComponents(String model) {
        String mod = model == null ? "Generic" : model;
        logger.debug("Configuring components for keypad model {}", model);

        switch (mod) {
            default:
                logger.warn("No valid keypad model defined ({}). Assuming model T15RL.", mod);
                // fall through
            case "Generic":
            case "T15RL":
                buttonList.addAll(buttonGroup3);
                ledList.addAll(ledGroup3);
                // fall through
            case "T10RL":
                buttonList.addAll(buttonGroup2);
                ledList.addAll(ledGroup2);
                // fall through
            case "T5RL":
                buttonList.addAll(buttonGroup1);
                buttonList.addAll(buttonsBottomRL);
                ledList.addAll(ledGroup1);
                ledList.addAll(LedsBottomRL);
                break;

            case "T15CRL":
                buttonList.addAll(buttonGroup3);
                ledList.addAll(ledGroup3);
                // fall through
            case "T10CRL":
                buttonList.addAll(buttonGroup2);
                ledList.addAll(ledGroup2);
                // fall through
            case "T5CRL":
                buttonList.addAll(buttonGroup1);
                buttonList.addAll(buttonsBottomCRL);
                ledList.addAll(ledGroup1);
                break;
        }
    }

    public TabletopKeypadHandler(Thing thing) {
        super(thing);
    }

}
