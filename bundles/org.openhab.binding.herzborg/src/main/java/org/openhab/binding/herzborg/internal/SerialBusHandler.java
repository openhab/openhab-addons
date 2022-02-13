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
package org.openhab.binding.herzborg.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link SerialBusHandler} implements specific handling for Herzborg serial bus,
 * connected directly via a serial port.
 *
 * @author Pavel Fedin - Initial contribution
 */
@NonNullByDefault
public class SerialBusHandler extends BusHandler {
    private SerialBusConfiguration config = new SerialBusConfiguration();

    public SerialBusHandler(Bridge bridge, SerialPortManager portManager) {
        super(bridge, new SerialBus(portManager));
    }

    @Override
    public void initialize() {
        config = getConfigAs(SerialBusConfiguration.class);

        Bus.Result result = ((SerialBus) bus).initialize(config.port);

        if (result.code == ThingStatusDetail.NONE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, result.code, result.message);
        }
    }

    @Override
    public void dispose() {
        bus.dispose();
    }
}
