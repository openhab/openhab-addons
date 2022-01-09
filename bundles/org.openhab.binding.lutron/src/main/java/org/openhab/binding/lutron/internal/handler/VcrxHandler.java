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
 * Handler responsible for communicating with Lutron VCRX visor control receiver
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class VcrxHandler extends BaseKeypadHandler {

    private static enum Component implements KeypadComponent {
        BUTTON1(1, "button1", "Button 1", ComponentType.BUTTON),
        BUTTON2(2, "button2", "Button 2", ComponentType.BUTTON),
        BUTTON3(3, "button3", "Button 3", ComponentType.BUTTON),
        BUTTON4(4, "button4", "Button 4", ComponentType.BUTTON),
        BUTTON5(5, "button5", "Button 5", ComponentType.BUTTON),
        BUTTON6(6, "button6", "Button 6", ComponentType.BUTTON),

        CCI1(30, "cci1", "CCI 1", ComponentType.CCI),
        CCI2(31, "cci2", "CCI 2", ComponentType.CCI),
        CCI3(32, "cci3", "CCI 3", ComponentType.CCI),
        CCI4(33, "cci4", "CCI 4", ComponentType.CCI),

        LED1(81, "led1", "LED 1", ComponentType.LED),
        LED2(82, "led2", "LED 2", ComponentType.LED),
        LED3(83, "led3", "LED 3", ComponentType.LED),
        LED4(84, "led4", "LED 4", ComponentType.LED),
        LED5(85, "led5", "LED 5", ComponentType.LED),
        LED6(86, "led6", "LED 6", ComponentType.LED);

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

    private static final List<Component> BUTTON_GROUP = Arrays.asList(Component.BUTTON1, Component.BUTTON2,
            Component.BUTTON3, Component.BUTTON4, Component.BUTTON5, Component.BUTTON6);

    private static final List<Component> LED_GROUP = Arrays.asList(Component.LED1, Component.LED2, Component.LED3,
            Component.LED4, Component.LED5, Component.LED6);

    private static final List<Component> CCI_GROUP = Arrays.asList(Component.CCI1, Component.CCI2, Component.CCI3,
            Component.CCI4);

    private final Logger logger = LoggerFactory.getLogger(VcrxHandler.class);

    @Override
    protected boolean isLed(int id) {
        return (id >= 81 && id <= 86);
    }

    @Override
    protected boolean isButton(int id) {
        return (id >= 1 && id <= 6);
    }

    @Override
    protected boolean isCCI(int id) {
        return (id >= 30 && id <= 33);
    }

    @Override
    protected void configureComponents(@Nullable String model) {
        logger.debug("Configuring components for VCRX");

        buttonList.addAll(BUTTON_GROUP);
        ledList.addAll(LED_GROUP);
        cciList.addAll(CCI_GROUP);
    }

    public VcrxHandler(Thing thing) {
        super(thing);
    }
}
