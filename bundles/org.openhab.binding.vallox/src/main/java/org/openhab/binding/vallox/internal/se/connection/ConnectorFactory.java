/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vallox.internal.se.connection;

import static org.openhab.binding.vallox.internal.se.constants.ValloxSEConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;

/**
 * The {@link ConnectorFactory} implements factory class to create connections to Vallox.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class ConnectorFactory {

    /**
     * Return connector class
     *
     * @param thingTypeUID ThingTypeUID to get connector
     * @param portManager SerialPortManager to use in serial connection
     * @return the connector
     * @throws IOException if ThingType is unknown
     */
    public static ValloxConnector getConnector(ThingTypeUID thingTypeUID, SerialPortManager portManager,
            ScheduledExecutorService scheduler) throws IOException {
        if (THING_TYPE_VALLOX_SE_IP.equals(thingTypeUID)) {
            return new ValloxIpConnector(scheduler);
        } else if (THING_TYPE_VALLOX_SE_SERIAL.equals(thingTypeUID)) {
            return new ValloxSerialConnector(portManager, scheduler);
        } else {
            throw new IOException(String.format("Unknown connection type for thing %s", thingTypeUID));
        }
    }
}
