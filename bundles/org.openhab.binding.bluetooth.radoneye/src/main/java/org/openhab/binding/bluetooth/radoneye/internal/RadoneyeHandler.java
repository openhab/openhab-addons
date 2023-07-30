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
package org.openhab.binding.bluetooth.radoneye.internal;

import static org.openhab.binding.bluetooth.radoneye.internal.RadoneyeBindingConstants.*;

import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RadoneyeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Peter Obel - Initial contribution
 */
@NonNullByDefault
public class RadoneyeHandler extends AbstractRadoneyeHandler {

    private static final UUID SERVICE_UUID_V1 = UUID.fromString("00001523-1212-efde-1523-785feabcd123");
    private static final UUID SERVICE_UUID_V2 = UUID.fromString("00001524-0000-1000-8000-00805f9b34fb");
    private static final UUID TRIGGER_UID_V1 = UUID.fromString("00001524-1212-efde-1523-785feabcd123");
    private static final UUID TRIGGER_UID_V2 = UUID.fromString("00001524-0000-1000-8000-00805f9b34fb");
    private static final UUID DATA_UUID_V1 = UUID.fromString("00001525-1212-efde-1523-785feabcd123");
    private static final UUID DATA_UUID_V2 = UUID.fromString("00001525-0000-1000-8000-00805f9b34fb");
    private static final byte[] DATA_TRIGGER_V1 = new byte[] { 0x50 };
    private static final byte[] DATA_TRIGGER_V2 = new byte[] { 0x50 };

    public RadoneyeHandler(Thing thing) {
        super(thing);
    }

    private final Logger logger = LoggerFactory.getLogger(RadoneyeHandler.class);

    @Override
    protected void updateChannels(int[] is) {
        Map<String, Number> data;
        try {
            data = RadoneyeDataParser.parseRd200Data(getFwVersion(), is);
            logger.debug("Parsed data: {}", data);
            Number radon = data.get(RadoneyeDataParser.RADON);
            logger.debug("Parsed data radon number: {}", radon);
            if (radon != null) {
                updateState(CHANNEL_ID_RADON, new QuantityType<Density>(radon, BECQUEREL_PER_CUBIC_METRE));
            }
        } catch (RadoneyeParserException e) {
            logger.error("Failed to parse data received from Radoneye sensor: {}", e.getMessage());
        }
    }

    @Override
    protected UUID getDataUUID() {
        int fwVersion = getFwVersion();
        switch (fwVersion) {
            case 1:
                return DATA_UUID_V1;
            case 2:
                return DATA_UUID_V2;
            default:
                throw new UnsupportedOperationException("fwVersion: " + fwVersion + " is not implemented");
        }
    }

    @Override
    protected UUID getTriggerUUID() {
        int fwVersion = getFwVersion();
        switch (fwVersion) {
            case 1:
                return TRIGGER_UID_V1;
            case 2:
                return TRIGGER_UID_V2;
            default:
                throw new UnsupportedOperationException("fwVersion: " + fwVersion + " is not implemented");
        }
    }

    @Override
    protected byte[] getTriggerData() {
        int fwVersion = getFwVersion();
        switch (fwVersion) {
            case 1:
                return DATA_TRIGGER_V1;
            case 2:
                return DATA_TRIGGER_V2;
            default:
                throw new UnsupportedOperationException("fwVersion: " + fwVersion + " is not implemented");
        }
    }
}
