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
package org.openhab.binding.lutron.internal.handler;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron QS IO Interfaces
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class QSIOHandler extends BaseKeypadHandler {

    private enum Component implements KeypadComponent {
        CCI1(1, "cci1", "CCI 1", ComponentType.CCI),
        CCI2(2, "cci2", "CCI 2", ComponentType.CCI),
        CCI3(3, "cci3", "CCI 3", ComponentType.CCI),
        CCI4(4, "cci4", "CCI 4", ComponentType.CCI),
        CCI5(5, "cci5", "CCI 5", ComponentType.CCI);

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

    private final Logger logger = LoggerFactory.getLogger(QSIOHandler.class);

    @Override
    protected boolean isLed(int id) {
        return false;
    }

    @Override
    protected boolean isButton(int id) {
        return false;
    }

    @Override
    protected boolean isCCI(int id) {
        return (id >= 1 && id <= 5);
    }

    @Override
    protected void configureComponents(@Nullable String model) {
        logger.debug("Configuring components for VCRX");

        cciList.addAll(Arrays.asList(Component.CCI1, Component.CCI2, Component.CCI3, Component.CCI4, Component.CCI5));
    }

    public QSIOHandler(Thing thing) {
        super(thing);
    }
}
