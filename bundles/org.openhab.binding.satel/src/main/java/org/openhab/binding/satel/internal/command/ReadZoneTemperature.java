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
package org.openhab.binding.satel.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.event.ZoneTemperatureEvent;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command class for command that reads temperature in a zone.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class ReadZoneTemperature extends SatelCommandBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final byte COMMAND_CODE = (byte) 0x7d;

    /**
     * Creates new command class instance to read temperature in given zone.
     *
     * @param zoneNbr zone number, 1 ... 256
     */
    public ReadZoneTemperature(int zoneNbr) {
        super(COMMAND_CODE, new byte[] { (byte) (zoneNbr == 256 ? 0 : zoneNbr) });
    }

    /**
     * Returns zone temperature (Celsius degrees).
     *
     * @return zone temperature
     */
    public float getTemperature() {
        final byte[] payload = getResponse().getPayload();
        int temp = ((payload[1] & 0xff) << 8) + (payload[2] & 0xff);
        return (temp - 110) / 2.0f;
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        // validate response
        if (response.getPayload().length != 3) {
            logger.debug("Invalid payload length: {}", response.getPayload().length);
            return false;
        }
        return true;
    }

    @Override
    protected void handleResponseInternal(final EventDispatcher eventDispatcher) {
        // dispatch temperature event
        int zoneNbr = getResponse().getPayload()[0];
        eventDispatcher.dispatchEvent(new ZoneTemperatureEvent(zoneNbr == 0 ? 256 : zoneNbr, getTemperature()));
    }
}
