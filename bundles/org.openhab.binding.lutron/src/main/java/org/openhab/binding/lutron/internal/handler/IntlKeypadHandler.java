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

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron International seeTouch keypads used in
 * Homeworks QS systems
 *
 * @author Bob Adair - Initial contribution
 */
public class IntlKeypadHandler extends BaseKeypadHandler {

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

        LOWER1(18, "buttonlower", "Lower button"),
        RAISE1(19, "buttonraise", "Raise button"),

        CCI1(25, "cci1", ""),
        CCI2(26, "cci2", ""),

        LED1(81, "led1", "LED 1"),
        LED2(82, "led2", "LED 2"),
        LED3(83, "led3", "LED 3"),
        LED4(84, "led4", "LED 4"),
        LED5(85, "led5", "LED 5"),
        LED6(86, "led6", "LED 6"),
        LED7(87, "led7", "LED 7"),
        LED8(88, "led8", "LED 8"),
        LED9(89, "led9", "LED 9"),
        LED10(90, "led10", "LED 10");

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

    private final Logger logger = LoggerFactory.getLogger(IntlKeypadHandler.class);

    @Override
    protected boolean isLed(int id) {
        return (id >= 81 && id <= 90);
    }

    @Override
    protected boolean isButton(int id) {
        return ((id >= 1 && id <= 10) || (id >= 18 && id <= 19));
    }

    @Override
    protected boolean isCCI(int id) {
        return (id >= 25 && id <= 26);
    }

    @Override
    protected void configureComponents(String model) {
        String mod = model == null ? "Generic" : model;
        logger.debug("Configuring components for keypad model {}", model);

        cciList.addAll(Arrays.asList(Component.CCI1, Component.CCI2));
        
        switch (mod) {
            case "2B":
                buttonList.addAll(Arrays.asList(Component.BUTTON7, Component.BUTTON9));
                ledList.addAll(Arrays.asList(Component.LED7, Component.LED9));
                break;
            case "3B":
                buttonList.addAll(Arrays.asList(Component.BUTTON6, Component.BUTTON8, Component.BUTTON10));
                ledList.addAll(Arrays.asList(Component.LED6, Component.LED8, Component.LED10));
                break;
            case "4B":
                buttonList.addAll(Arrays.asList(Component.BUTTON2, Component.BUTTON4, Component.BUTTON7, Component.BUTTON9));
                ledList.addAll(Arrays.asList(Component.LED2, Component.LED4, Component.LED7, Component.LED9));
                break;
            case "5BRL":
                buttonList.addAll(Arrays.asList(Component.BUTTON6, Component.BUTTON7, Component.BUTTON8, Component.BUTTON9, Component.BUTTON10));
                buttonList.addAll(Arrays.asList(Component.LOWER1, Component.RAISE1));
                ledList.addAll(Arrays.asList(Component.LED6, Component.LED7, Component.LED8, Component.LED9, Component.LED10));
                break;
            case "6BRL":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON3, Component.BUTTON5, Component.BUTTON6, Component.BUTTON8, Component.BUTTON10));
                buttonList.addAll(Arrays.asList(Component.LOWER1, Component.RAISE1));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED3, Component.LED5, Component.LED6, Component.LED8, Component.LED10));
                break;
            case "7BRL":
                buttonList.addAll(Arrays.asList(Component.BUTTON2, Component.BUTTON4, Component.BUTTON6, Component.BUTTON7, Component.BUTTON8, Component.BUTTON9, Component.BUTTON10));
                buttonList.addAll(Arrays.asList(Component.LOWER1, Component.RAISE1));
                ledList.addAll(Arrays.asList(Component.LED2, Component.LED4, Component.LED6, Component.LED7, Component.LED8, Component.LED9, Component.LED10));
                break;
            case "8BRL":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON3, Component.BUTTON5, Component.BUTTON6, Component.BUTTON7, Component.BUTTON8, Component.BUTTON9, Component.BUTTON10));
                buttonList.addAll(Arrays.asList(Component.LOWER1, Component.RAISE1));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED3, Component.LED5, Component.LED6, Component.LED7, Component.LED8, Component.LED9, Component.LED10));
                break;
            default:
                logger.warn("No valid keypad model defined ({}). Assuming 10BRL model.", mod);
                // fall through
            case "Generic":
            case "10BRL":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.BUTTON4, Component.BUTTON5, Component.BUTTON6, Component.BUTTON7, Component.BUTTON8, Component.BUTTON9, Component.BUTTON10));
                buttonList.addAll(Arrays.asList(Component.LOWER1, Component.RAISE1));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED2, Component.LED3, Component.LED4, Component.LED5, Component.LED6, Component.LED7, Component.LED8, Component.LED9, Component.LED10));
                break;
        }
    }

    public IntlKeypadHandler(Thing thing) {
        super(thing);
    }

}
