/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal.handler;

import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Smart power socket handler with nightlight
 *
 * @author Cato Sognen - Initial contribution
 * @author John Marshall - Nightlight support
 */
@NonNullByDefault
public class BroadlinkSocketModel3Handler extends BroadlinkSocketModel2Handler {

    public BroadlinkSocketModel3Handler(Thing thing) {
        super(thing, false);
    }

    OnOffType deriveNightLightStateFromStatusBytes(byte[] statusPayload) {
        return deriveOnOffBitFromStatusPayload(statusPayload, (byte) 0x02);
    }

    static int mergeOnOffBits(Command powerOnOff, Command nightLightOnOff) {
        int powerBit = powerOnOff == OnOffType.ON ? 0x01 : 0x00;
        int nightLightBit = nightLightOnOff == OnOffType.ON ? 0x02 : 0x00;
        return powerBit | nightLightBit;
    }

    @Override
    protected void getStatusFromDevice() throws IOException {
        logger.debug("SP3 getting status...");
        byte[] statusBytes = getStatusBytesFromDevice();
        updateState(COMMAND_POWER_ON, derivePowerStateFromStatusBytes(statusBytes));
        updateState(COMMAND_NIGHTLIGHT, deriveNightLightStateFromStatusBytes(statusBytes));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            // Always pull back the latest device status and merge it:
            byte[] statusBytes = getStatusBytesFromDevice();
            OnOffType powerStatus = derivePowerStateFromStatusBytes(statusBytes);
            OnOffType nightLightStatus = deriveNightLightStateFromStatusBytes(statusBytes);

            if (channelUID.getId().equals(COMMAND_POWER_ON)) {
                setStatusOnDevice(mergeOnOffBits(command, nightLightStatus));
            }

            if (channelUID.getId().equals(COMMAND_NIGHTLIGHT)) {
                setStatusOnDevice(mergeOnOffBits(powerStatus, command));
            }
        } catch (IOException e) {
            logger.warn("Could not send command to SP3 device", e);
        }
    }
}
