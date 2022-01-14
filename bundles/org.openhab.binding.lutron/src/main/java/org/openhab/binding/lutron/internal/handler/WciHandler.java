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
package org.openhab.binding.lutron.internal.handler;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with the Lutron Wallbox Input Closure Interface (WCI)
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class WciHandler extends BaseKeypadHandler {

    private static enum Component implements KeypadComponent {
        BUTTON1(1, "button1", "Button 1", ComponentType.BUTTON),
        BUTTON2(2, "button2", "Button 2", ComponentType.BUTTON),
        BUTTON3(3, "button3", "Button 3", ComponentType.BUTTON),
        BUTTON4(4, "button4", "Button 4", ComponentType.BUTTON),
        BUTTON5(5, "button5", "Button 5", ComponentType.BUTTON),
        BUTTON6(6, "button6", "Button 6", ComponentType.BUTTON),
        BUTTON7(7, "button7", "Button 7", ComponentType.BUTTON),
        BUTTON8(8, "button8", "Button 8", ComponentType.BUTTON),

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

        @Override
        public ComponentType type() {
            return type;
        }
    }

    private static final List<KeypadComponent> BUTTON_LIST = Arrays.asList(Component.BUTTON1, Component.BUTTON2,
            Component.BUTTON3, Component.BUTTON4, Component.BUTTON5, Component.BUTTON6, Component.BUTTON7,
            Component.BUTTON8);

    private static final List<KeypadComponent> LED_LIST = Arrays.asList(Component.LED1, Component.LED2, Component.LED3,
            Component.LED4, Component.LED5, Component.LED6, Component.LED7, Component.LED8);

    private final Logger logger = LoggerFactory.getLogger(WciHandler.class);

    @Override
    protected boolean isLed(int id) {
        return (id >= 81 && id <= 88);
    }

    @Override
    protected boolean isButton(int id) {
        return (id >= 1 && id <= 8);
    }

    @Override
    protected boolean isCCI(int id) {
        return false;
    }

    @Override
    protected void configureComponents(@Nullable String model) {
        logger.trace("Configuring components for WCI");

        buttonList.addAll(BUTTON_LIST);
        ledList.addAll(LED_LIST);
    }

    public WciHandler(Thing thing) {
        super(thing);
    }
}
