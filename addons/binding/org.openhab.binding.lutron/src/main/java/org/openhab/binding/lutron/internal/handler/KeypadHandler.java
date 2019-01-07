/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.handler;

import java.util.Arrays;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron seeTouch and Hybrid seeTouch keypads used in
 * RadioRA2 and Homeworks QS systems
 *
 * @author Bob Adair - Initial contribution
 */
public class KeypadHandler extends BaseKeypadHandler {

    private static enum Component implements KeypadComponent {
        BUTTON1(1, "button1", "Button 1"),
        BUTTON2(2, "button2", "Button 2"),
        BUTTON3(3, "button3", "Button 3"),
        BUTTON4(4, "button4", "Button 4"),
        BUTTON5(5, "button5", "Button 5"),
        BUTTON6(6, "button6", "Button 6"),
        BUTTON7(7, "button7", "Button 7"),

        LOWER1(16, "buttontoplower", "Top lower button"),
        RAISE1(17, "buttontopraise", "Top raise button"),
        LOWER2(18, "buttonbottomlower", "Bottom lower button"),
        RAISE2(19, "buttonbottomraise", "Bottom raise button"),

        // CCI1(25, "cci1", ""), // listed in spec but currently unused in binding
        // CCI2(26, "cci2", ""), // listed in spec but currently unused in binding

        LED1(81, "led1", "LED 1"),
        LED2(82, "led2", "LED 2"),
        LED3(83, "led3", "LED 3"),
        LED4(84, "led4", "LED 4"),
        LED5(85, "led5", "LED 5"),
        LED6(86, "led6", "LED 6"),
        LED7(87, "led7", "LED 7");

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

    private final Logger logger = LoggerFactory.getLogger(KeypadHandler.class);

    @Override
    protected boolean isLed(int id) {
        return (id >= 81 && id <= 87);
    }

    @Override
    protected boolean isButton(int id) {
        return ((id >= 1 && id <= 7) || (id >= 16 && id <= 19));
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
            case "W1RLD":
            case "H1RLD":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3,
                        Component.BUTTON5, Component.BUTTON6));
                buttonList.addAll(Arrays.asList(Component.LOWER2, Component.RAISE2));
                ledList.addAll(
                        Arrays.asList(Component.LED1, Component.LED2, Component.LED3, Component.LED5, Component.LED6));
                break;
            case "W2RLD":
            case "H2RLD":
                buttonList.addAll(
                        Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON5, Component.BUTTON6));
                buttonList
                        .addAll(Arrays.asList(Component.LOWER1, Component.RAISE1, Component.LOWER2, Component.RAISE2));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED2, Component.LED5, Component.LED6));
                break;
            case "W3S":
            case "H3S":
                buttonList.addAll(
                        Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3, Component.BUTTON6));
                buttonList.addAll(Arrays.asList(Component.LOWER2, Component.RAISE2));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED2, Component.LED3, Component.LED6));
                break;
            case "W3BD":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3,
                        Component.BUTTON5, Component.BUTTON6, Component.BUTTON7));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED2, Component.LED3, Component.LED5,
                        Component.LED6, Component.LED7));
                break;
            case "W3BRL":
                buttonList.addAll(Arrays.asList(Component.BUTTON2, Component.BUTTON3, Component.BUTTON4));
                buttonList.addAll(Arrays.asList(Component.LOWER2, Component.RAISE2));
                ledList.addAll(Arrays.asList(Component.LED2, Component.LED3, Component.LED4));
                break;
            case "W3BSRL":
            case "H3BSRL":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON3, Component.BUTTON5));
                buttonList.addAll(Arrays.asList(Component.LOWER2, Component.RAISE2));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED3, Component.LED5));
                break;
            case "W4S":
            case "H4S":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3,
                        Component.BUTTON4, Component.BUTTON6));
                buttonList.addAll(Arrays.asList(Component.LOWER2, Component.RAISE2));
                ledList.addAll(
                        Arrays.asList(Component.LED1, Component.LED2, Component.LED3, Component.LED4, Component.LED6));
                break;
            case "W5BRL":
            case "H5BRL":
            case "W5BRLIR":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3,
                        Component.BUTTON4, Component.BUTTON5));
                buttonList.addAll(Arrays.asList(Component.LOWER2, Component.RAISE2));
                ledList.addAll(
                        Arrays.asList(Component.LED1, Component.LED2, Component.LED3, Component.LED4, Component.LED5));
                break;
            case "W6BRL":
            case "H6BRL":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3,
                        Component.BUTTON4, Component.BUTTON5, Component.BUTTON6));
                buttonList.addAll(Arrays.asList(Component.LOWER2, Component.RAISE2));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED2, Component.LED3, Component.LED4,
                        Component.LED5, Component.LED6));
                break;
            case "W7B":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3,
                        Component.BUTTON4, Component.BUTTON5, Component.BUTTON6, Component.BUTTON7));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED2, Component.LED3, Component.LED4,
                        Component.LED5, Component.LED6, Component.LED7));
                break;
            default:
                logger.warn("No valid keypad model defined ({}). Assuming Generic model.", mod);
                // fall through
            case "Generic":
                buttonList.addAll(Arrays.asList(Component.BUTTON1, Component.BUTTON2, Component.BUTTON3,
                        Component.BUTTON4, Component.BUTTON5, Component.BUTTON6, Component.BUTTON7));
                buttonList
                        .addAll(Arrays.asList(Component.LOWER1, Component.RAISE1, Component.LOWER2, Component.RAISE2));
                ledList.addAll(Arrays.asList(Component.LED1, Component.LED2, Component.LED3, Component.LED4,
                        Component.LED5, Component.LED6, Component.LED7));
                break;
        }
    }

    public KeypadHandler(Thing thing) {
        super(thing);
    }

}
