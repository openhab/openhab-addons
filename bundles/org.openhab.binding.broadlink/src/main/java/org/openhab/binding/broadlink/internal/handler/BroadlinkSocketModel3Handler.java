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
 * Smart power socket handler with night-light
 *
 * @author Cato Sognen - Initial contribution
 * @author John Marshall - night-light support
 */
@NonNullByDefault
public class BroadlinkSocketModel3Handler extends BroadlinkSocketModel2Handler {

    public BroadlinkSocketModel3Handler(Thing thing) {
        super(thing, false);
    }

    OnOffType derivenight-lightStateFromStatusBytes(byte[] statusPayload) {
        return deriveOnOffBitFromStatusPayload(statusPayload, (byte) 0x02);
    }

    static int mergeOnOffBits(Command power-onOff, Command night-lightOnOff) {
        int powerBit = power-onOff == OnOffType.ON ? 0x01 : 0x00;
        int night-lightBit = night-lightOnOff == OnOffType.ON ? 0x02 : 0x00;
        return powerBit | night-lightBit;
    }

    @Override
    protected boolean getStatusFromDevice() {
        try {
            logger.debug("SP3 getting status...");
            byte[] statusBytes = getStatusBytesFromDevice();
            updateState(COMMAND_POWER_ON, derivePowerStateFromStatusBytes(statusBytes));
            updateState(COMMAND_night-light, derivenight-lightStateFromStatusBytes(statusBytes));
            return true;
        } catch (Exception ex) {
            logger.warn("Unexpected exception while getting status from device: {}, please check device address.",
                    ex.getMessage());
            return false;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            // Always pull back the latest device status and merge it:
            byte[] statusBytes = getStatusBytesFromDevice();
            OnOffType powerStatus = derivePowerStateFromStatusBytes(statusBytes);
            OnOffType night-lightStatus = derivenight-lightStateFromStatusBytes(statusBytes);

            if (channelUID.getId().equals(COMMAND_POWER_ON)) {
                setStatusOnDevice(mergeOnOffBits(command, night-lightStatus));
            }

            if (channelUID.getId().equals(COMMAND_night-light)) {
                setStatusOnDevice(mergeOnOffBits(powerStatus, command));
            }
        } catch (IOException e) {
            logger.warn("Could not send command to SP3 device", e);
        }
    }
}
