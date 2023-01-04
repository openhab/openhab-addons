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

    private static final String SERVICE_UUID = "00001523-1212-efde-1523-785feabcd123";
    private static final String TRIGGER_UID = "00001524-1212-efde-1523-785feabcd123";
    private static final String DATA_UUID = "00001525-1212-efde-1523-785feabcd123";

    public RadoneyeHandler(Thing thing) {
        super(thing);
    }

    private final Logger logger = LoggerFactory.getLogger(RadoneyeHandler.class);

    private final UUID dataUuid = UUID.fromString(DATA_UUID);
    private final UUID triggerUuid = UUID.fromString(TRIGGER_UID);
    private final byte[] triggerData = new byte[] { 0x50 };

    @Override
    protected void updateChannels(int[] is) {
        Map<String, Number> data;
        try {
            data = RadoneyeDataParser.parseRd200Data(is);
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
        return dataUuid;
    }

    @Override
    protected UUID getTriggerUUID() {
        return triggerUuid;
    }

    @Override
    protected byte[] getTriggerData() {
        return triggerData;
    }
}
