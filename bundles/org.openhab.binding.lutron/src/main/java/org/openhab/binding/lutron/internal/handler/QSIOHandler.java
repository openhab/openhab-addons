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
 * Handler responsible for communicating with Lutron QS IO Interfaces
 *
 * @author Bob Adair - Initial contribution
 */
public class QSIOHandler extends BaseKeypadHandler {

    private static enum Component implements KeypadComponent {
        CCI1(1, "cci1", "CCI 1"),
        CCI2(2, "cci2", "CCI 2"),
        CCI3(3, "cci3", "CCI 3"),
        CCI4(4, "cci4", "CCI 4"),
        CCI5(5, "cci5", "CCI 5");

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
    protected void configureComponents(String model) {
        logger.debug("Configuring components for VCRX");

        cciList.addAll(Arrays.asList(Component.CCI1, Component.CCI2, Component.CCI3, Component.CCI4, Component.CCI5));
    }

    public QSIOHandler(Thing thing) {
        super(thing);
    }

}
